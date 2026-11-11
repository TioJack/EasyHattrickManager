#!/usr/bin/env node
/* Analyze observed experience/loyalty deltas. No external calls. */
import {execFileSync} from 'node:child_process';

const db = {host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1', port: process.env.SIM_MYSQL_PORT ?? '2000', user: process.env.SIM_MYSQL_USER ?? 'ehm', password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm', database: process.env.SIM_MYSQL_DATABASE ?? 'sim'};
const mysql = sql => execFileSync('mysql', [`--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`, `--database=${db.database}`, '--batch', '--skip-column-names', '--raw', '--execute', sql], {encoding: 'utf8', maxBuffer: 32 * 1024 * 1024, env: {...process.env, MYSQL_PWD: db.password}}).trim();
const rows = sql => { const value = mysql(sql); return value ? value.split('\n').map(line => line.split('\t')) : []; };
const selectedMinutes = [0, 90];
const probes = new Map(rows(`SELECT p.id, REPLACE(TO_BASE64(CAST(p.metadata_json AS CHAR)), CHAR(10), '') FROM calibration_probe p WHERE p.status='COMPLETED' AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign'))='experience-loyalty-v1'`).map(([id, encoded]) => [Number(id), JSON.parse(Buffer.from(encoded, 'base64').toString('utf8'))]));
if (!probes.size) throw new Error('No completed experience-loyalty-v1 probes found.');
const cases = new Map([...probes].map(([id, metadata]) => [id, {...metadata, sectors: {}, player: {}}]));
for (const [probeId, minute, sector, rating] of rows(`SELECT pr.probe_id, pr.minute, sr.sector_id, sr.rating FROM calibration_prediction pr JOIN calibration_sector_rating sr ON sr.prediction_id=pr.id JOIN calibration_probe p ON p.id=pr.probe_id WHERE p.status='COMPLETED' AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign'))='experience-loyalty-v1' AND pr.minute IN (0,90) ORDER BY pr.probe_id,pr.minute,sr.sector_id`)) {
  const item = cases.get(Number(probeId)); item.sectors[minute] ??= Array(7).fill(null); item.sectors[minute][Number(sector)] = Number(rating);
}
for (const [probeId, minute, stars, stamina] of rows(`SELECT pr.probe_id, pr.minute, pl.stars, pl.stamina FROM calibration_prediction pr JOIN calibration_player_rating pl ON pl.prediction_id=pr.id JOIN calibration_probe p ON p.id=pr.probe_id WHERE p.status='COMPLETED' AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign'))='experience-loyalty-v1' AND pr.minute IN (0,90) ORDER BY pr.probe_id,pr.minute`)) {
  cases.get(Number(probeId)).player[minute] = {stars: Number(stars), stamina: Number(stamina)};
}
const baselineKey = item => `${item.role}|${item.skillLevel}`;
const baselines = new Map([...cases.values()].filter(item => item.experience === 0 && item.loyalty === 0).map(item => [baselineKey(item), item]));
const deltas = [...cases.values()].map(item => {
  const base = baselines.get(baselineKey(item));
  const sectorDeltas = Object.fromEntries(selectedMinutes.map(minute => [minute, item.sectors[minute].map((rating, sector) => rating - base.sectors[minute][sector]) ]));
  const starDeltas = Object.fromEntries(selectedMinutes.map(minute => [minute, item.player[minute].stars - base.player[minute].stars]));
  return {...item, sectorDeltas, starDeltas};
});
const nonZero = deltas.filter(item => Object.values(item.sectorDeltas).flat().some(value => value !== 0) || Object.values(item.starDeltas).some(value => value !== 0));
console.log(JSON.stringify({
  generatedAt: new Date().toISOString(),
  scope: 'experience-loyalty-v1 at minutes 0 and 90; deltas use exp=0, loyalty=0 in the same role and primary skill level.',
  cases: deltas.length,
  nonZeroCases: nonZero.length,
  deltas,
  interpretation: 'A zero observed delta means no effect at the measured discrete output resolution, not proof that an underlying latent contribution is exactly zero.'
}, null, 2));
