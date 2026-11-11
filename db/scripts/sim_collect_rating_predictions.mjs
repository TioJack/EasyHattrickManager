#!/usr/bin/env node
/* Collect queued, complete simulator request templates. Credentials are runtime-only. */
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {readFileSync} from 'node:fs';

const args = new Set(process.argv.slice(2));
const dryRun = args.has('--dry-run'), runAll = args.has('--all'), retryFailed = args.has('--retry-failed');
const limitIndex = process.argv.indexOf('--limit');
const limit = runAll ? Number.MAX_SAFE_INTEGER : limitIndex >= 0 ? Number(process.argv[limitIndex + 1]) : 1;
if (!Number.isInteger(limit) || limit < 1) throw new Error('Use --limit with a positive integer, or --all.');
const db = {host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1', port: process.env.SIM_MYSQL_PORT ?? '2000', user: process.env.SIM_MYSQL_USER ?? 'ehm', password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm', database: process.env.SIM_MYSQL_DATABASE ?? 'sim'};
const delayMs = Number(process.env.SIM_DELAY_MS ?? '1500');
const mysql = sql => execFileSync('mysql', [`--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`, `--database=${db.database}`, '--batch', '--skip-column-names', '--raw', '--execute', sql], {encoding: 'utf8', env: {...process.env, MYSQL_PWD: db.password}, stdio: ['ignore', 'pipe', 'pipe']}).trim();
function next() {
  const row = mysql("SELECT id, TO_BASE64(CAST(request_template_json AS CHAR)), IFNULL(TO_BASE64(CAST(metadata_json AS CHAR)), '') FROM calibration_probe WHERE status='PENDING' ORDER BY id LIMIT 1;");
  if (!row) return null;
  const [id, template, metadata] = row.split('\t');
  return {id: Number(id), template: JSON.parse(Buffer.from(template, 'base64').toString('utf8')), metadata: metadata ? JSON.parse(Buffer.from(metadata, 'base64').toString('utf8')) : null};
}
const claim = id => mysql(`UPDATE calibration_probe SET status='RUNNING',attempt_count=attempt_count+1,started_at=NOW(),error_message=NULL WHERE id=${id} AND status='PENDING'; SELECT ROW_COUNT();`).endsWith('1');
function curlConfig() {
  if (!process.env.SIM_CURL_FILE) return {};
  const content = readFileSync(process.env.SIM_CURL_FILE, 'utf8');
  const header = name => content.match(new RegExp(`(?:-H|--header)\\s+'${name}:\\s*([^']*)'`, 'i'))?.[1];
  const data = content.match(/--data-raw\s+'([\s\S]*?)'\s*$/i)?.[1];
  return {cookie: content.match(/(?:-b|--cookie)\s+'([^']*)'/i)?.[1], token: header('hattrick-auth-token'), csrf: header('x-csrf-token'), teamId: data ? JSON.parse(data).teamId : undefined, endpoint: content.match(/curl\s+--url\s+'([^']+)'/i)?.[1]};
}
const runtime = curlConfig(), endpoint = process.env.SIM_ENDPOINT ?? runtime.endpoint ?? 'https://m82.hattrick.org/api/v40261/match/getDetailedRatingPredictions/0';
function payloadFor(probe) {
  const teamId = Number(process.env.SIM_TEAM_ID ?? runtime.teamId);
  if (!Number.isInteger(teamId) || teamId <= 0) throw new Error('SIM_TEAM_ID must be a positive integer for collection.');
  const payload = structuredClone(probe.template);
  if (!Array.isArray(payload?.lineup?.positions) || payload.lineup.positions.length !== 14 || !Array.isArray(payload.extraPlayers)) throw new Error(`Probe ${probe.id} has an invalid request template.`);
  payload.teamId = teamId;
  return payload;
}
function credentials() {
  const value = {cookie: process.env.SIM_COOKIE ?? runtime.cookie, token: process.env.SIM_AUTH_TOKEN ?? runtime.token, csrf: process.env.SIM_CSRF_TOKEN ?? runtime.csrf};
  if (Object.values(value).some(item => !item)) throw new Error('Live collection requires SIM_COOKIE, SIM_AUTH_TOKEN and SIM_CSRF_TOKEN.');
  return value;
}
async function fetchPrediction(payload, auth) {
  const response = await fetch(endpoint, {method: 'POST', headers: {accept: 'application/json, text/plain, */*', 'content-type': 'application/json', cookie: auth.cookie, 'hattrick-auth-token': auth.token, 'hattrick-current-teamid': String(payload.teamId), 'hattrick-override-language': '6', origin: 'https://www82.hattrick.org', referer: 'https://www82.hattrick.org/', 'x-csrf-token': auth.csrf}, body: JSON.stringify(payload)});
  if (!response.ok) throw new Error(`Prediction request failed with HTTP ${response.status}.`);
  const result = await response.json();
  if (!Array.isArray(result) || !result.length) throw new Error('Prediction response is not a non-empty array.');
  return result;
}
const b64 = value => Buffer.from(JSON.stringify(value)).toString('base64');
function save(probe, payload, response) {
  const request = JSON.stringify(payload), result = JSON.stringify(response), requestHash = createHash('sha256').update(request).digest('hex'), responseHash = createHash('sha256').update(result).digest('hex');
  const sql = ['START TRANSACTION;', `INSERT INTO calibration_request (probe_id,request_json,request_sha256) VALUES (${probe.id},CONVERT(FROM_BASE64('${b64(payload)}') USING utf8mb4),'${requestHash}') ON DUPLICATE KEY UPDATE request_json=VALUES(request_json),request_sha256=VALUES(request_sha256);`, `INSERT INTO calibration_response (probe_id,response_json,response_sha256) VALUES (${probe.id},CONVERT(FROM_BASE64('${b64(response)}') USING utf8mb4),'${responseHash}') ON DUPLICATE KEY UPDATE response_json=VALUES(response_json),response_sha256=VALUES(response_sha256);`, `DELETE FROM calibration_prediction WHERE probe_id=${probe.id};`];
  for (const point of response) {
    const ratings = point.ratings ?? {};
    sql.push(`INSERT INTO calibration_prediction (probe_id,minute,second,tactic_rating,ht_rating) VALUES (${probe.id},${Number(point.minute)},${Number(point.second)},${Number(ratings.tacticRating ?? 0)},${Number(ratings.htRating ?? 0)});`, 'SET @prediction_id=LAST_INSERT_ID();');
    for (const sector of ratings.sectors ?? []) sql.push(`INSERT INTO calibration_sector_rating (prediction_id,sector_id,rating) VALUES (@prediction_id,${Number(sector.sectorId)},${Number(sector.rating)});`);
    for (const [slot, player] of (ratings.players ?? []).entries()) sql.push(`INSERT INTO calibration_player_rating (prediction_id,player_slot,player_id,position_id,stars,stamina) VALUES (@prediction_id,${slot},${Number(player.playerId ?? 0)},${Number(player.positionId)},${Number(player.stars)},${Number(player.stamina)});`);
  }
  sql.push(`UPDATE calibration_probe SET status='COMPLETED',completed_at=NOW(),error_message=NULL WHERE id=${probe.id};`, 'COMMIT;');
  mysql(sql.join('\n'));
}
const fail = (probe, error) => mysql(`UPDATE calibration_probe SET status='FAILED',error_message='${String(error.message ?? error).replace(/'/g, "''").slice(0,1000)}' WHERE id=${probe.id};`);
if (retryFailed && !dryRun) mysql("UPDATE calibration_probe SET status='PENDING',error_message=NULL WHERE status='FAILED';");
for (let index = 0; index < limit; index += 1) {
  const probe = next(); if (!probe) break;
  const payload = payloadFor(probe);
  if (dryRun) { console.log(JSON.stringify({probe: {id: probe.id, metadata: probe.metadata}, payload}, null, 2)); break; }
  if (!claim(probe.id)) { index -= 1; continue; }
  try { save(probe, payload, await fetchPrediction(payload, credentials())); console.log(`Completed probe ${probe.id}.`); }
  catch (error) { fail(probe, error); console.error(`Probe ${probe.id} failed: ${error.message}`); process.exitCode = 1; break; }
  if (index + 1 < limit) await new Promise(resolve => setTimeout(resolve, delayMs));
}
