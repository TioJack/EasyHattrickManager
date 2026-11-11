#!/usr/bin/env node
/* Compare completed context probes to the fixed away/base XI. No API calls. */
import {execFileSync} from 'node:child_process';

const db = {host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1', port: process.env.SIM_MYSQL_PORT ?? '2000', user: process.env.SIM_MYSQL_USER ?? 'ehm', password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm', database: process.env.SIM_MYSQL_DATABASE ?? 'sim'};
const mysql = sql => execFileSync('mysql', [`--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`, `--database=${db.database}`, '--batch', '--skip-column-names', '--raw', '--execute', sql], {encoding: 'utf8', maxBuffer: 16 * 1024 * 1024, env: {...process.env, MYSQL_PWD: db.password}}).trim();
const rows = sql => { const value = mysql(sql); return value ? value.split('\n').map(line => line.split('\t')) : []; };
const probes = new Map(rows(`SELECT p.id, REPLACE(TO_BASE64(CAST(p.metadata_json AS CHAR)), CHAR(10), '') FROM calibration_probe p WHERE p.status='COMPLETED' AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign'))='contexts-v1'`).map(([id, encoded]) => [Number(id), JSON.parse(Buffer.from(encoded, 'base64').toString('utf8'))]));
const cases = new Map([...probes].map(([id, metadata]) => [id, {...metadata, sectors: Array(7).fill(null), tacticRating: null, htRating: null}]));
for (const [probeId, tacticRating, htRating, sector, rating] of rows(`SELECT pr.probe_id, pr.tactic_rating, pr.ht_rating, sr.sector_id, sr.rating FROM calibration_prediction pr JOIN calibration_sector_rating sr ON sr.prediction_id=pr.id JOIN calibration_probe p ON p.id=pr.probe_id WHERE p.status='COMPLETED' AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign'))='contexts-v1' AND pr.minute=0 ORDER BY pr.probe_id,sr.sector_id`)) {
  const item = cases.get(Number(probeId)); item.tacticRating = Number(tacticRating); item.htRating = Number(htRating); item.sectors[Number(sector)] = Number(rating);
}
const baseline = [...cases.values()].find(item => item.dimension === 'venue' && item.venue === 'away');
if (!baseline) throw new Error('Missing away/base context probe.');
const comparison = [...cases.values()].map(item => ({...item, sectorDelta: item.sectors.map((value, sector) => value - baseline.sectors[sector]), tacticRatingDelta: item.tacticRating - baseline.tacticRating, htRatingDelta: item.htRating - baseline.htRating}));
console.log(JSON.stringify({generatedAt: new Date().toISOString(), baseline, comparison, interpretation: 'All deltas use the same complete XI. The duplicate baseline request is intentionally stored once.'}, null, 2));
