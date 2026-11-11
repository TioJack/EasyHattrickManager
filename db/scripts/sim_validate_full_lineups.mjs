#!/usr/bin/env node
/*
 * Strict holdout validation: compose no-fit single-player observations and
 * compare them with completed full XIs.  It never fits against holdout data
 * and never calls Hattrick.
 */
import {execFileSync} from 'node:child_process';

const campaignOption = process.argv.indexOf('--campaign');
const targetCampaign = campaignOption === -1 ? 'full-lineup-holdout-v1' : process.argv[campaignOption + 1];
if (!['full-lineup-holdout-v1', 'full-lineup-aggregation-v1'].includes(targetCampaign)) {
  throw new Error('Usage: node db/scripts/sim_validate_full_lineups.mjs [--campaign full-lineup-holdout-v1|full-lineup-aggregation-v1]');
}
const db = {host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1', port: process.env.SIM_MYSQL_PORT ?? '2000', user: process.env.SIM_MYSQL_USER ?? 'ehm', password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm', database: process.env.SIM_MYSQL_DATABASE ?? 'sim'};
const mysql = sql => execFileSync('mysql', [`--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`, `--database=${db.database}`, '--batch', '--skip-column-names', '--raw', '--execute', sql], {encoding: 'utf8', maxBuffer: 128 * 1024 * 1024, env: {...process.env, MYSQL_PWD: db.password}}).trim();
const rows = sql => { const value = mysql(sql); return value ? value.split('\n').map(line => line.split('\t')) : []; };
const minutes = [0, 90];
const skillFields = {goalkeeping: 'keeperSkill', defending: 'defenderSkill', playmaking: 'playmakerSkill', winger: 'wingerSkill', passing: 'passerSkill', scoring: 'scorerSkill'};
// Proven by mirror-symmetry-v1: the unmeasured left slots are exact mirrors
// of these measured right slots, including the sector transformation.
const mirroredSlotSource = new Map([[4, 2], [5, 1], [9, 7], [10, 6], [13, 11]]);
const sectorMirror = [2, 1, 0, 3, 6, 5, 4];

const metadata = new Map();
const templates = new Map();
for (const [id, encodedMetadata, encodedTemplate] of rows(`
  SELECT p.id, REPLACE(TO_BASE64(CAST(p.metadata_json AS CHAR)), CHAR(10), ''), REPLACE(TO_BASE64(CAST(p.request_template_json AS CHAR)), CHAR(10), '')
    FROM calibration_probe p
   WHERE p.status='COMPLETED'
     AND (JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.kind'))='single_player_skill_sweep'
       OR JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign'))='${targetCampaign}')
`)) {
  metadata.set(Number(id), JSON.parse(Buffer.from(encodedMetadata, 'base64').toString('utf8')));
  templates.set(Number(id), JSON.parse(Buffer.from(encodedTemplate, 'base64').toString('utf8')));
}
const ratings = new Map();
for (const [probeId, minute, sector, rating] of rows(`
  SELECT pr.probe_id, pr.minute, sr.sector_id, sr.rating
    FROM calibration_prediction pr JOIN calibration_sector_rating sr ON sr.prediction_id=pr.id
    JOIN calibration_probe p ON p.id=pr.probe_id
   WHERE p.status='COMPLETED' AND pr.minute IN (0,90)
     AND (JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.kind'))='single_player_skill_sweep'
       OR JSON_UNQUOTE(JSON_EXTRACT(p.metadata_json, '$.campaign'))='${targetCampaign}')
   ORDER BY pr.probe_id,pr.minute,sr.sector_id
`)) {
  const byMinute = ratings.get(Number(probeId)) ?? new Map();
  const sectors = byMinute.get(Number(minute)) ?? Array(7).fill(null);
  sectors[Number(sector)] = Number(rating);
  byMinute.set(Number(minute), sectors);
  ratings.set(Number(probeId), byMinute);
}
const single = new Map();
const holdouts = [];
for (const [probeId, item] of metadata) {
  if (item.kind === 'single_player_skill_sweep') single.set(`${item.testedSkill}|${item.position}|${item.behaviour}|${item.testedSkillLevel}`, ratings.get(probeId));
  if (item.campaign === targetCampaign) holdouts.push({probeId, metadata: item, template: templates.get(probeId), actual: ratings.get(probeId)});
}
const predictionFor = (template, minute) => {
  const slotByPlayerId = new Map(template.lineup.positions.map((position, slot) => [position.id, {slot, behaviour: position.behaviour}]).filter(([id]) => id));
  const predicted = Array(7).fill(0);
  const missing = [];
  for (const player of template.extraPlayers) {
    const assignment = slotByPlayerId.get(player.playerId);
    if (!assignment) { missing.push({playerId: player.playerId, reason: 'not assigned'}); continue; }
    for (const [skill, field] of Object.entries(skillFields)) {
      const level = player[field];
      const sourceSlot = mirroredSlotSource.get(assignment.slot) ?? assignment.slot;
      const value = single.get(`${skill}|${sourceSlot}|${assignment.behaviour}|${level}`)?.get(minute);
      const baseline = single.get(`${skill}|${sourceSlot}|${assignment.behaviour}|0`)?.get(minute);
      if (!value || !baseline) { missing.push({playerId: player.playerId, skill, level, slot: assignment.slot}); continue; }
      for (let sector = 0; sector < 7; sector++) {
        const sourceSector = mirroredSlotSource.has(assignment.slot) ? sectorMirror[sector] : sector;
        predicted[sector] += value[sourceSector] - baseline[sourceSector];
      }
    }
  }
  return {predicted, missing};
};
const validations = holdouts.map(item => {
  const byMinute = {};
  for (const minute of minutes) {
    const composition = predictionFor(item.template, minute);
    const actual = item.actual?.get(minute);
    byMinute[minute] = {actual, predicted: composition.predicted, residual: actual?.map((value, sector) => value - composition.predicted[sector]) ?? null, missing: composition.missing};
  }
  return {probeId: item.probeId, ...item.metadata, byMinute};
});
const allResiduals = validations.flatMap(item => minutes.flatMap(minute => item.byMinute[minute].residual ?? []));
const summary = {
  cases: validations.length,
  comparedValues: allResiduals.length,
  mae: Number((allResiduals.reduce((sum, value) => sum + Math.abs(value), 0) / allResiduals.length).toFixed(3)),
  maxAbsoluteError: Math.max(...allResiduals.map(Math.abs)),
  missingComponents: validations.reduce((sum, item) => sum + minutes.reduce((minuteSum, minute) => minuteSum + item.byMinute[minute].missing.length, 0), 0)
};
console.log(JSON.stringify({generatedAt: new Date().toISOString(), campaign: targetCampaign, scope: 'No-fit additive composition from base single-player sweeps versus complete XIs.', summary, validations}, null, 2));
