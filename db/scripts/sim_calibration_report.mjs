#!/usr/bin/env node
/* Read-only calibration report. It never calls Hattrick. */
import {execFileSync} from 'node:child_process';

const db = {
  host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1',
  port: process.env.SIM_MYSQL_PORT ?? '2000',
  user: process.env.SIM_MYSQL_USER ?? 'ehm',
  password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm',
  database: process.env.SIM_MYSQL_DATABASE ?? 'sim'
};
const query = sql => execFileSync('mysql', [
  `--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`,
  `--database=${db.database}`, '--batch', '--skip-column-names', '--raw', '--execute', sql
], {encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, env: {...process.env, MYSQL_PWD: db.password}}).trim();
const rows = sql => {
  const text = query(sql);
  return text ? text.split('\n').map(line => line.split('\t')) : [];
};

const statusRows = rows('SELECT status, COUNT(*) FROM calibration_probe GROUP BY status;');
const completed = Number(statusRows.find(([status]) => status === 'COMPLETED')?.[1] ?? 0);
const total = statusRows.reduce((sum, [, count]) => sum + Number(count), 0);
const samples = rows(`
  SELECT REPLACE(TO_BASE64(CAST(p.metadata_json AS CHAR)), CHAR(10), ''), pr.minute, sr.sector_id, sr.rating
    FROM calibration_probe p
    JOIN calibration_prediction pr ON pr.probe_id = p.id
    JOIN calibration_sector_rating sr ON sr.prediction_id = pr.id
   WHERE p.status = 'COMPLETED' AND pr.minute IN (0, 90)
   ORDER BY p.id, pr.minute, sr.sector_id;
`);

const coverage = {};
const curves = {};
for (const [encodedMetadata, minute, sector, rating] of samples) {
  const metadata = JSON.parse(Buffer.from(encodedMetadata, 'base64').toString('utf8'));
  const skill = metadata.testedSkill ?? 'unlabelled';
  const position = metadata.position ?? 'n/a';
  const behaviour = metadata.behaviour ?? 'n/a';
  const level = metadata.testedSkillLevel ?? 'n/a';
  coverage[skill] ??= new Set();
  coverage[skill].add(`${position}/${behaviour}/${level}`);
  if (Number(minute) !== 0) continue;
  const key = `${skill}|${position}|${behaviour}|${sector}`;
  curves[key] ??= [];
  curves[key].push({level: Number(level), rating: Number(rating)});
}
const kickoffCurves = Object.entries(curves).map(([key, values]) => {
  values.sort((a, b) => a.level - b.level);
  const baseline = values.find(value => value.level === 0)?.rating ?? null;
  return {key, baseline, points: values.map(value => ({...value, delta: baseline === null ? null : value.rating - baseline}))};
});
console.log(JSON.stringify({
  generatedAt: new Date().toISOString(),
  progress: {total, completed, pending: total - completed, percent: total ? Number((completed * 100 / total).toFixed(2)) : 0},
  completedInputCombinations: Object.fromEntries(Object.entries(coverage).map(([skill, keys]) => [skill, keys.size])),
  kickoffCurves,
  interpretation: {
    target: 'Observed sector ratings are discrete outputs; fit them as interval/ordinal constraints, not as falsely precise continuous measurements.',
    scope: 'These curves isolate one player and one skill. They identify marginal positional effects, not multi-player or cross-skill interactions.'
  }
}, null, 2));
