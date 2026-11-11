#!/usr/bin/env node
/* Queue campaign documents in calibration_probe. Use --enqueue to write. */
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {readFileSync} from 'node:fs';

const args = process.argv.slice(2);
const file = args[args.indexOf('--file') + 1];
const enqueue = args.includes('--enqueue');
if (!file) throw new Error('Usage: --file <campaign.json> [--enqueue]');
const document = JSON.parse(readFileSync(file, 'utf8'));
if (document.format !== 'sim-campaign-v1' || !Array.isArray(document.campaigns)) throw new Error('Invalid campaign document.');
for (const campaign of document.campaigns) {
  if (!campaign.name || !Array.isArray(campaign.probes)) throw new Error('Every campaign needs name and probes.');
  for (const probe of campaign.probes) {
    if (!Array.isArray(probe?.request?.lineup?.positions) || probe.request.lineup.positions.length !== 14 || !Array.isArray(probe.request.extraPlayers)) throw new Error(`Invalid request in ${campaign.name}.`);
  }
}
const total = document.campaigns.reduce((sum, campaign) => sum + campaign.probes.length, 0);
if (!enqueue) {
  console.log(JSON.stringify({valid: true, mode: 'dry-run', campaigns: document.campaigns.map(campaign => ({name: campaign.name, probes: campaign.probes.length})), totalProbes: total}, null, 2));
  process.exit(0);
}
const db = {host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1', port: process.env.SIM_MYSQL_PORT ?? '2000', user: process.env.SIM_MYSQL_USER ?? 'ehm', password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm', database: process.env.SIM_MYSQL_DATABASE ?? 'sim'};
// SQL can include full 11-player JSON templates. Send it through stdin rather
// than an argv --execute value so it cannot exceed the operating-system argv
// size limit.
const mysql = sql => execFileSync('mysql', [`--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`, `--database=${db.database}`, '--batch', '--skip-column-names', '--raw'], {input: sql, encoding: 'utf8', env: {...process.env, MYSQL_PWD: db.password}, stdio: ['pipe', 'pipe', 'pipe']}).trim();
for (const campaign of document.campaigns) {
  const safeName = `campaign:${campaign.name}`.replace(/'/g, "''");
  const batchId = Number(mysql(`INSERT INTO calibration_batch (name,metadata_json) VALUES ('${safeName}',JSON_OBJECT('campaign','${safeName}')) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id); SELECT LAST_INSERT_ID();`).split('\n').pop());
  const values = campaign.probes.map(probe => {
    const request = JSON.stringify(probe.request), metadata = JSON.stringify(probe.metadata ?? {});
    return `(${batchId},CONVERT(FROM_BASE64('${Buffer.from(request).toString('base64')}') USING utf8mb4),'${createHash('sha256').update(request).digest('hex')}',CONVERT(FROM_BASE64('${Buffer.from(metadata).toString('base64')}') USING utf8mb4))`;
  });
  // Campaign generation is repeatable. Never turn an already collected probe
  // back into PENDING merely because its campaign document was regenerated.
  for (let offset = 0; offset < values.length; offset += 20) mysql(`INSERT INTO calibration_probe (batch_id,request_template_json,request_template_sha256,metadata_json) VALUES ${values.slice(offset, offset + 20).join(',')} ON DUPLICATE KEY UPDATE metadata_json=VALUES(metadata_json);`);
}
console.log(JSON.stringify({queued: total, campaigns: document.campaigns.map(campaign => campaign.name)}, null, 2));
