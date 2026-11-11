#!/usr/bin/env node
/*
 * Build the first, strictly empirical V2 calibration artefact.
 *
 * It reads only the already captured simulator observations.  The artefact is
 * intentionally scoped to isolated, single-player sweeps: it records the
 * observed discrete sector-rating curves, rather than pretending that they
 * are already an additive XI formula.  Multi-player campaigns validate that
 * latter step separately.
 *
 * Usage:
 *   node db/scripts/sim_fit_marginal_model.mjs --output /tmp/marginal-v1.json
 */
import {execFileSync} from 'node:child_process';
import {mkdirSync, writeFileSync} from 'node:fs';
import {dirname, resolve} from 'node:path';

const outputIndex = process.argv.indexOf('--output');
const compact = process.argv.includes('--compact');
if (outputIndex === -1 || !process.argv[outputIndex + 1]) {
  console.error('Usage: node db/scripts/sim_fit_marginal_model.mjs --output <file.json>');
  process.exit(2);
}

const db = {
  host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1',
  port: process.env.SIM_MYSQL_PORT ?? '2000',
  user: process.env.SIM_MYSQL_USER ?? 'ehm',
  password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm',
  database: process.env.SIM_MYSQL_DATABASE ?? 'sim'
};
const expectedSkills = ['goalkeeping', 'defending', 'playmaking', 'winger', 'passing', 'scoring'];
const expectedLevels = Array.from({length: 21}, (_, level) => level);
const expectedMinutes = Array.from({length: 25}, (_, index) => index * 5);
const expectedSectors = Array.from({length: 7}, (_, sector) => sector);

function mysql(sql) {
  return execFileSync('mysql', [
    `--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`,
    `--database=${db.database}`, '--batch', '--skip-column-names', '--raw', '--execute', sql
  ], {
    encoding: 'utf8',
    maxBuffer: 256 * 1024 * 1024,
    env: {...process.env, MYSQL_PWD: db.password}
  }).trim();
}

function queryRows(sql) {
  const result = mysql(sql);
  return result ? result.split('\n').map(line => line.split('\t')) : [];
}

const probes = new Map(queryRows(`
  SELECT p.id, REPLACE(TO_BASE64(CAST(p.metadata_json AS CHAR)), CHAR(10), '')
    FROM calibration_probe p
   WHERE p.status = 'COMPLETED'
     AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.kind')) = 'single_player_skill_sweep'
`).map(([id, encodedMetadata]) => [Number(id), JSON.parse(Buffer.from(encodedMetadata, 'base64').toString('utf8'))]));

if (!probes.size) {
  throw new Error('No completed single-player calibration probes were found.');
}

const observations = queryRows(`
  SELECT pr.probe_id, pr.minute, sr.sector_id, sr.rating
    FROM calibration_prediction pr
    JOIN calibration_sector_rating sr ON sr.prediction_id = pr.id
    JOIN calibration_probe p ON p.id = pr.probe_id
   WHERE p.status = 'COMPLETED'
     AND JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.kind')) = 'single_player_skill_sweep'
   ORDER BY pr.probe_id, pr.minute, sr.sector_id
`);

const samples = new Map();
for (const [probeIdText, minuteText, sectorText, ratingText] of observations) {
  const probeId = Number(probeIdText);
  const metadata = probes.get(probeId);
  if (!metadata) continue;
  const key = `${metadata.testedSkill}|${metadata.position}|${metadata.behaviour}|${metadata.testedSkillLevel}`;
  const minute = Number(minuteText);
  const sector = Number(sectorText);
  const byMinute = samples.get(key) ?? new Map();
  const sectors = byMinute.get(minute) ?? new Map();
  sectors.set(sector, Number(ratingText));
  byMinute.set(minute, sectors);
  samples.set(key, byMinute);
}

const configurations = new Map();
for (const metadata of probes.values()) {
  const skill = metadata.testedSkill;
  if (!expectedSkills.includes(skill)) continue;
  const key = `${skill}|${metadata.position}|${metadata.behaviour}`;
  configurations.set(key, {skill, position: Number(metadata.position), behaviour: Number(metadata.behaviour)});
}

const model = {};
const diagnostics = {missingPoints: [], nonMonotonicDeltas: [], configurations: 0, probes: probes.size, observations: observations.length};
for (const {skill, position, behaviour} of configurations.values()) {
  const configKey = `${skill}|${position}|${behaviour}`;
  const minuteCurves = {};
  for (const minute of expectedMinutes) {
    const baseline = samples.get(`${configKey}|0`)?.get(minute);
    const ratingsByLevel = expectedLevels.map(level => samples.get(`${configKey}|${level}`)?.get(minute));
    if (!baseline || ratingsByLevel.some(sectors => !sectors || expectedSectors.some(sector => !sectors.has(sector)))) {
      diagnostics.missingPoints.push({skill, position, behaviour, minute});
      continue;
    }
    const base = expectedSectors.map(sector => baseline.get(sector));
    const deltasByLevel = ratingsByLevel.map(sectors => expectedSectors.map(sector => sectors.get(sector) - baseline.get(sector)));
    for (const sector of expectedSectors) {
      for (let level = 1; level < deltasByLevel.length; level++) {
        if (deltasByLevel[level][sector] < deltasByLevel[level - 1][sector]) {
          diagnostics.nonMonotonicDeltas.push({skill, position, behaviour, minute, sector, fromLevel: level - 1, toLevel: level, from: deltasByLevel[level - 1][sector], to: deltasByLevel[level][sector]});
        }
      }
    }
    minuteCurves[minute] = {baselineSectors: base, deltasByLevel};
  }
  model[skill] ??= {};
  model[skill][`${position}:${behaviour}`] = minuteCurves;
  diagnostics.configurations++;
}

const artefact = {
  schemaVersion: 1,
  modelVersion: 'v2-marginal-observed-1',
  generatedAt: new Date().toISOString(),
  scope: {
    source: 'Hattrick simulator observations captured in sim',
    kind: 'single_player_skill_sweep',
    fixedInputs: {form: 8, stamina: 9, experience: 0, loyalty: 0, specialty: 0, weather: 2, venue: 2, morale: -1, selfConfidence: -1},
    caveat: 'The values are discrete observed ratings. This is not yet an additive full-lineup formula; crowding, cross-skill and context campaigns are required before V2 can return production ratings.'
  },
  dimensions: {skills: expectedSkills, levels: expectedLevels, minutes: expectedMinutes, sectors: expectedSectors},
  curves: model,
  diagnostics: {
    ...diagnostics,
    complete: diagnostics.missingPoints.length === 0,
    note: 'A non-monotonic discrete delta is a diagnostic event, not an automatic error: sector ratings are rounded discrete outputs.'
  }
};

const output = resolve(process.argv[outputIndex + 1]);
mkdirSync(dirname(output), {recursive: true});
writeFileSync(output, `${JSON.stringify(artefact, null, compact ? 0 : 2)}\n`);
console.log(JSON.stringify({
  output,
  modelVersion: artefact.modelVersion,
  configurations: diagnostics.configurations,
  complete: artefact.diagnostics.complete,
  missingPoints: diagnostics.missingPoints.length,
  nonMonotonicDeltas: diagnostics.nonMonotonicDeltas.length
}, null, 2));
