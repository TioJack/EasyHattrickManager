#!/usr/bin/env node
/* Read campaign outcomes from sim. It never calls the Hattrick API. */
import {execFileSync} from 'node:child_process';

const db = {
  host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1',
  port: process.env.SIM_MYSQL_PORT ?? '2000',
  user: process.env.SIM_MYSQL_USER ?? 'ehm',
  password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm',
  database: process.env.SIM_MYSQL_DATABASE ?? 'sim'
};
const mysql = sql => execFileSync('mysql', [
  `--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`,
  `--database=${db.database}`, '--batch', '--skip-column-names', '--raw', '--execute', sql
], {encoding: 'utf8', maxBuffer: 32 * 1024 * 1024, env: {...process.env, MYSQL_PWD: db.password}}).trim();
const rows = sql => {
  const result = mysql(sql);
  return result ? result.split('\n').map(line => line.split('\t')) : [];
};
const selectedMinutes = [0, 90];
const metadataByProbe = new Map(rows(`
  SELECT p.id, REPLACE(TO_BASE64(CAST(p.metadata_json AS CHAR)), CHAR(10), '')
    FROM calibration_probe p
   WHERE p.status = 'COMPLETED'
     AND (
       JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign')) IN ('mirror-symmetry-v1', 'crowding-v1', 'skill-interactions-v1')
       OR JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.kind')) = 'single_player_skill_sweep'
     )
`).map(([id, metadata]) => [Number(id), JSON.parse(Buffer.from(metadata, 'base64').toString('utf8'))]));
const ratingsByProbe = new Map();
for (const [id, minute, sector, rating] of rows(`
  SELECT pr.probe_id, pr.minute, sr.sector_id, sr.rating
    FROM calibration_prediction pr
    JOIN calibration_sector_rating sr ON sr.prediction_id = pr.id
    JOIN calibration_probe p ON p.id = pr.probe_id
   WHERE p.status = 'COMPLETED'
     AND (
       JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign')) IN ('mirror-symmetry-v1', 'crowding-v1', 'skill-interactions-v1')
       OR JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.kind')) = 'single_player_skill_sweep'
     )
     AND pr.minute IN (0, 90)
   ORDER BY pr.probe_id, pr.minute, sr.sector_id
`)) {
  const byMinute = ratingsByProbe.get(Number(id)) ?? new Map();
  const sectors = byMinute.get(Number(minute)) ?? Array(7).fill(null);
  sectors[Number(sector)] = Number(rating);
  byMinute.set(Number(minute), sectors);
  ratingsByProbe.set(Number(id), byMinute);
}

const mirrorSlots = new Map([[1, [1, 5]], [5, [1, 5]], [2, [2, 4]], [4, [2, 4]], [6, [6, 10]], [10, [6, 10]], [7, [7, 9]], [9, [7, 9]], [11, [11, 13]], [13, [11, 13]]]);
const sectorMirror = [2, 1, 0, 3, 6, 5, 4];
const mirrorCases = new Map();
const crowdingCases = new Map();
const singlePlayerCases = new Map();
const interactionCases = [];
for (const [probeId, metadata] of metadataByProbe) {
  const ratings = ratingsByProbe.get(probeId);
  if (!ratings) continue;
  if (metadata.campaign === 'mirror-symmetry-v1') {
    const [rightSlot, leftSlot] = mirrorSlots.get(Number(metadata.slot)) ?? [];
    const key = `${rightSlot}|${leftSlot}|${metadata.skill}|${metadata.level}`;
    const item = mirrorCases.get(key) ?? {rightSlot, leftSlot, skill: metadata.skill, level: metadata.level};
    item[metadata.side] = Object.fromEntries(selectedMinutes.map(minute => [minute, ratings.get(minute)]));
    mirrorCases.set(key, item);
  }
  if (metadata.campaign === 'crowding-v1') {
    const key = `${metadata.group}|${metadata.skill}|${metadata.level}`;
    const item = crowdingCases.get(key) ?? {group: metadata.group, skill: metadata.skill, level: metadata.level, counts: {}};
    item.counts[metadata.count] = Object.fromEntries(selectedMinutes.map(minute => [minute, ratings.get(minute)]));
    crowdingCases.set(key, item);
  }
  if (metadata.kind === 'single_player_skill_sweep') {
    singlePlayerCases.set(`${metadata.testedSkill}|${metadata.position}|${metadata.behaviour}|${metadata.testedSkillLevel}`, Object.fromEntries(selectedMinutes.map(minute => [minute, ratings.get(minute)])));
  }
  if (metadata.campaign === 'skill-interactions-v1') {
    interactionCases.push({metadata, ratings: Object.fromEntries(selectedMinutes.map(minute => [minute, ratings.get(minute)]))});
  }
}

const mirror = [];
let comparedSectorValues = 0;
let mismatchedSectorValues = 0;
for (const item of mirrorCases.values()) {
  const mismatches = [];
  for (const minute of selectedMinutes) {
    const right = item.right?.[minute];
    const left = item.left?.[minute];
    if (!right || !left) {
      mismatches.push({minute, reason: 'missing side'});
      continue;
    }
    for (let sector = 0; sector < 7; sector++) {
      comparedSectorValues++;
      const expected = right[sectorMirror[sector]];
      if (left[sector] !== expected) {
        mismatchedSectorValues++;
        mismatches.push({minute, sector, left: left[sector], mirroredRight: expected});
      }
    }
  }
  mirror.push({...item, symmetric: mismatches.length === 0, mismatches});
}

const subtraction = (current, previous) => current.map((value, sector) => value - previous[sector]);
const crowding = [...crowdingCases.values()].map(item => {
  const increments = {};
  for (const minute of selectedMinutes) {
    const count1 = item.counts[1]?.[minute];
    const count2 = item.counts[2]?.[minute];
    const count3 = item.counts[3]?.[minute];
    increments[minute] = count1 && count2 && count3 ? {one: count1, secondPlayer: subtraction(count2, count1), thirdPlayer: subtraction(count3, count2), totalTwo: count2, totalThree: count3} : null;
  }
  return {...item, increments};
});

const interactions = interactionCases.map(({metadata, ratings}) => {
  const lookup = (skill, level) => singlePlayerCases.get(`${skill}|${metadata.slot}|${metadata.behaviour}|${level}`);
  const zero = lookup(metadata.firstSkill, 0);
  const first = lookup(metadata.firstSkill, metadata.firstLevel);
  const second = lookup(metadata.secondSkill, metadata.secondLevel);
  const residuals = {};
  for (const minute of selectedMinutes) {
    const combined = ratings[minute];
    if (!zero?.[minute] || !first?.[minute] || !second?.[minute] || !combined) {
      residuals[minute] = null;
      continue;
    }
    residuals[minute] = combined.map((value, sector) => value - first[minute][sector] - second[minute][sector] + zero[minute][sector]);
  }
  const exactAdditive = Object.values(residuals).every(value => Array.isArray(value) && value.every(delta => delta === 0));
  return {...metadata, ratings, residuals, exactAdditive};
});

console.log(JSON.stringify({
  generatedAt: new Date().toISOString(),
  scope: 'Completed symmetry, crowding and within-player interaction observations at kickoff and minute 90.',
  mirror: {
    expectedSectorMap: sectorMirror,
    cases: mirror.length,
    comparedSectorValues,
    mismatchedSectorValues,
    exactCases: mirror.filter(item => item.symmetric).length,
    details: mirror
  },
  crowding: {cases: crowding.length, details: crowding},
  interactions: {
    cases: interactions.length,
    exactAdditiveCases: interactions.filter(item => item.exactAdditive).length,
    nonAdditiveCases: interactions.filter(item => !item.exactAdditive).length,
    details: interactions
  },
  interpretation: 'Sector ratings are discrete. Differences are constraints for the eventual aggregation model, not raw player coefficients.'
}, null, 2));
