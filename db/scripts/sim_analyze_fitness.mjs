#!/usr/bin/env node
/* Export the complete observed form/stamina time curves. No API calls. */
import {execFileSync} from 'node:child_process';

const db = {
  host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1', port: process.env.SIM_MYSQL_PORT ?? '2000',
  user: process.env.SIM_MYSQL_USER ?? 'ehm', password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm', database: process.env.SIM_MYSQL_DATABASE ?? 'sim'
};
const mysql = sql => execFileSync('mysql', [
  `--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`, `--database=${db.database}`,
  '--batch', '--skip-column-names', '--raw', '--execute', sql
], {encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, env: {...process.env, MYSQL_PWD: db.password}}).trim();
const rows = sql => {
  const value = mysql(sql);
  return value ? value.split('\n').map(line => line.split('\t')) : [];
};

const probes = new Map(rows(`
  SELECT p.id, REPLACE(TO_BASE64(CAST(p.metadata_json AS CHAR)), CHAR(10), '')
    FROM calibration_probe p
   WHERE p.status = 'COMPLETED'
     AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign')) = 'fitness-v1'
`).map(([id, encoded]) => [Number(id), JSON.parse(Buffer.from(encoded, 'base64').toString('utf8'))]));
if (!probes.size) throw new Error('No completed fitness-v1 probes found.');

const cases = new Map([...probes].map(([id, metadata]) => [id, {...metadata, sectors: {}, player: {}}]));
for (const [probeId, minute, sector, rating] of rows(`
  SELECT pr.probe_id, pr.minute, sr.sector_id, sr.rating
    FROM calibration_prediction pr
    JOIN calibration_sector_rating sr ON sr.prediction_id=pr.id
    JOIN calibration_probe p ON p.id=pr.probe_id
   WHERE p.status='COMPLETED'
     AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign'))='fitness-v1'
   ORDER BY pr.probe_id, pr.minute, sr.sector_id
`)) {
  const item = cases.get(Number(probeId));
  item.sectors[minute] ??= Array(7).fill(null);
  item.sectors[minute][Number(sector)] = Number(rating);
}
for (const [probeId, minute, stars, stamina] of rows(`
  SELECT pr.probe_id, pr.minute, pl.stars, pl.stamina
    FROM calibration_prediction pr
    JOIN calibration_player_rating pl ON pl.prediction_id=pr.id
    JOIN calibration_probe p ON p.id=pr.probe_id
   WHERE p.status='COMPLETED'
     AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign'))='fitness-v1'
   ORDER BY pr.probe_id, pr.minute
`)) {
  const item = cases.get(Number(probeId));
  item.player[minute] = {stars: Number(stars), stamina: Number(stamina)};
}
const sum = values => values.reduce((total, value) => total + value, 0);
const summary = [...cases.values()].map(item => ({
  role: item.role, skill: item.skill, skillLevel: item.skillLevel, form: item.form, stamina: item.stamina,
  kickoffTotal: sum(item.sectors[0]), minute90Total: sum(item.sectors[90]), minute120Total: sum(item.sectors[120]),
  kickoffStars: item.player[0]?.stars, minute90Stars: item.player[90]?.stars, minute120Stars: item.player[120]?.stars
}));
console.log(JSON.stringify({
  generatedAt: new Date().toISOString(),
  scope: 'fitness-v1; all sector and player observations from minute 0 through 120.',
  cases: [...cases.values()],
  summary,
  interpretation: 'Sector totals are only a compact diagnostic. V2 must use sector-specific curves and discrete observed ratings.'
}, null, 2));
