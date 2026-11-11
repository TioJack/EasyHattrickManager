#!/usr/bin/env node
/* Queue an arbitrary complete lineup in the same calibration_probe table. */
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {readFileSync} from 'node:fs';
const args = process.argv.slice(2), at = flag => args[args.indexOf(flag) + 1], name = at('--name'), file = at('--file');
if (!name || !file || name.length > 160) throw new Error('Usage: --name <max-160-name> --file <request-body.json>');
const payload = JSON.parse(readFileSync(file,'utf8'));
if (!Array.isArray(payload?.lineup?.positions) || payload.lineup.positions.length !== 14 || !Array.isArray(payload.extraPlayers)) throw new Error('The JSON must contain 14 lineup positions and extraPlayers.');
const db = {host:process.env.SIM_MYSQL_HOST??'127.0.0.1',port:process.env.SIM_MYSQL_PORT??'2000',user:process.env.SIM_MYSQL_USER??'ehm',password:process.env.SIM_MYSQL_PASSWORD??'ehm',database:process.env.SIM_MYSQL_DATABASE??'sim'};
const safe = name.replace(/'/g,"''"), json = JSON.stringify(payload), hash = createHash('sha256').update(json).digest('hex'), encoded = Buffer.from(json).toString('base64');
const sql = `START TRANSACTION; INSERT INTO calibration_batch (name,metadata_json) VALUES ('lineup:${safe}',JSON_OBJECT('kind','complete_lineup')) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id); SET @batch=LAST_INSERT_ID(); INSERT INTO calibration_probe (batch_id,request_template_json,request_template_sha256,metadata_json) VALUES (@batch,CONVERT(FROM_BASE64('${encoded}') USING utf8mb4),'${hash}',JSON_OBJECT('kind','complete_lineup','name','${safe}')) ON DUPLICATE KEY UPDATE request_template_json=VALUES(request_template_json),metadata_json=VALUES(metadata_json),status='PENDING',attempt_count=0,error_message=NULL,started_at=NULL,completed_at=NULL; COMMIT;`;
execFileSync('mysql',[`--host=${db.host}`,`--port=${db.port}`,`--user=${db.user}`,`--database=${db.database}`,'--execute',sql],{encoding:'utf8',env:{...process.env,MYSQL_PWD:db.password},stdio:'inherit'});
console.log(`Lineup '${name}' queued as PENDING.`);
