#!/usr/bin/env node
/* Restore per-probe raw JSON links from equivalent archived JSON. No network calls. */
import {execFileSync} from 'node:child_process';

const apply = process.argv.includes('--apply');
const db = {host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1', port: process.env.SIM_MYSQL_PORT ?? '2000', user: process.env.SIM_MYSQL_USER ?? 'ehm', password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm', database: process.env.SIM_MYSQL_DATABASE ?? 'sim'};
const mysql = sql => execFileSync('mysql', [`--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`, `--database=${db.database}`, '--batch', '--skip-column-names', '--raw', '--execute', sql], {encoding: 'utf8', maxBuffer: 128 * 1024 * 1024, env: {...process.env, MYSQL_PWD: db.password}}).trim();
const mysqlInput = sql => execFileSync('mysql', [`--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`, `--database=${db.database}`], {input: sql, encoding: 'utf8', maxBuffer: 128 * 1024 * 1024, env: {...process.env, MYSQL_PWD: db.password}});
const rows = sql => { const text = mysql(sql); return text ? text.split('\n').map(line => line.split('\t')) : []; };
const decode = encoded => JSON.parse(Buffer.from(encoded, 'base64').toString('utf8'));
const encode = value => Buffer.from(JSON.stringify(value)).toString('base64');
const stable = value => Array.isArray(value) ? `[${value.map(stable).join(',')}]` : value && typeof value === 'object' ? `{${Object.keys(value).sort().map(key => `${JSON.stringify(key)}:${stable(value[key])}`).join(',')}}` : JSON.stringify(value);

function responseSignatures() {
  const sectorRows = rows(`
    SELECT p.id,pr.minute,pr.second,pr.tactic_rating,pr.ht_rating,sr.sector_id,sr.rating
      FROM calibration_probe p JOIN calibration_prediction pr ON pr.probe_id=p.id
      JOIN calibration_sector_rating sr ON sr.prediction_id=pr.id
     WHERE p.status='COMPLETED'
     ORDER BY p.id,pr.minute,pr.second,sr.sector_id;`);
  const playerRows = rows(`
    SELECT p.id,pr.minute,pr.second,r.player_slot,r.player_id,r.position_id,r.stars,r.stamina
      FROM calibration_probe p JOIN calibration_prediction pr ON pr.probe_id=p.id
      JOIN calibration_player_rating r ON r.prediction_id=pr.id
     WHERE p.status='COMPLETED'
     ORDER BY p.id,pr.minute,pr.second,r.player_slot;`);
  const points = new Map();
  for (const [probeId, minute, second, tactic, ht, sectorId, rating] of sectorRows) {
    const key = `${probeId}/${minute}/${second}`;
    const point = points.get(key) ?? {probeId, minute, second, tactic, ht, sectors: [], players: []};
    point.sectors.push(`${sectorId}:${rating}`); points.set(key, point);
  }
  for (const [probeId, minute, second, slot, playerId, positionId, stars, stamina] of playerRows) {
    const key = `${probeId}/${minute}/${second}`;
    const point = points.get(key);
    if (point) point.players[Number(slot)] = `${playerId}:${positionId}:${Number(stars)}:${Number(stamina).toFixed(15)}`;
  }
  const byProbe = new Map();
  for (const point of points.values()) {
    const values = byProbe.get(point.probeId) ?? [];
    values.push(point); byProbe.set(point.probeId, values);
  }
  return new Map([...byProbe.entries()].map(([probeId, values]) => [Number(probeId), values.sort((a,b) => Number(a.minute) - Number(b.minute) || Number(a.second) - Number(b.second)).map(point => `${Number(point.minute)}:${Number(point.second)}:${Number(point.tactic)}:${Number(point.ht)}:${point.sectors.map(value => value.split(':').map(Number).join(':')).sort().join(',')}:${point.players.join(',')}`).join('|')]));
}

const existingRequests = new Map();
for (const [, raw, hash] of rows("SELECT probe_id,REPLACE(TO_BASE64(CAST(request_json AS CHAR)),CHAR(10),''),request_sha256 FROM calibration_request;")) {
  const request = decode(raw); request.teamId = 0;
  existingRequests.set(stable(request), {raw, hash});
}
const missingRequests = rows("SELECT p.id,REPLACE(TO_BASE64(CAST(p.request_template_json AS CHAR)),CHAR(10),'') FROM calibration_probe p LEFT JOIN calibration_request r ON r.probe_id=p.id WHERE p.status='COMPLETED' AND r.probe_id IS NULL;")
  .map(([id, raw]) => ({id: Number(id), source: existingRequests.get(stable(decode(raw)))}));

const rawResponsesByProbe = new Map(rows("SELECT probe_id,REPLACE(TO_BASE64(CAST(response_json AS CHAR)),CHAR(10),''),response_sha256 FROM calibration_response;")
  .map(([id, raw, hash]) => [Number(id), {raw, hash}]));
const allResponseSignatures = responseSignatures();
const responseSourceBySignature = new Map();
for (const [id, signature] of allResponseSignatures) {
  const source = rawResponsesByProbe.get(id);
  if (source) responseSourceBySignature.set(signature, source);
}
const missingResponses = [...allResponseSignatures.entries()]
  .filter(([id]) => !rawResponsesByProbe.has(id))
  .map(([id, signature]) => ({id, source: responseSourceBySignature.get(signature)}));

const summary = {
  mode: apply ? 'apply' : 'dry-run',
  requests: {missing: missingRequests.length, recoverable: missingRequests.filter(item => item.source).length},
  responses: {missing: missingResponses.length, recoverable: missingResponses.filter(item => item.source).length}
};
if (process.argv.includes('--debug')) {
  const firstExisting = responseSourceBySignature.entries().next().value;
  const firstMissing = missingResponses[0];
  console.log(JSON.stringify({
    debug: {
      existingSignatureStart: firstExisting?.[0]?.slice(0, 500),
      missingSignatureStart: firstMissing ? allResponseSignatures.get(firstMissing.id)?.slice(0, 500) : null
    }
  }, null, 2));
}
if (!apply || summary.requests.missing !== summary.requests.recoverable || summary.responses.missing !== summary.responses.recoverable) {
  console.log(JSON.stringify(summary, null, 2));
  if (apply && (summary.requests.missing !== summary.requests.recoverable || summary.responses.missing !== summary.responses.recoverable)) process.exitCode = 1;
  process.exit();
}
function insert(table, jsonField, hashField, entries) {
  const chunkSize = 20;
  for (let offset = 0; offset < entries.length; offset += chunkSize) {
    const values = entries.slice(offset, offset + chunkSize).map(item => `(${item.id},CONVERT(FROM_BASE64('${item.source.raw}') USING utf8mb4),'${item.source.hash}')`);
    mysqlInput(`INSERT INTO ${table} (probe_id,${jsonField},${hashField}) VALUES ${values.join(',')};`);
  }
}
insert('calibration_request', 'request_json', 'request_sha256', missingRequests);
insert('calibration_response', 'response_json', 'response_sha256', missingResponses);
console.log(JSON.stringify(summary, null, 2));
