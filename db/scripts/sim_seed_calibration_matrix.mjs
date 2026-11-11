#!/usr/bin/env node
/* Generate 3,276 self-contained request templates: 6 skills × 21 × 26 orders. */
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';

const db = {host: process.env.SIM_MYSQL_HOST ?? '127.0.0.1', port: process.env.SIM_MYSQL_PORT ?? '2000', user: process.env.SIM_MYSQL_USER ?? 'ehm', password: process.env.SIM_MYSQL_PASSWORD ?? 'ehm', database: process.env.SIM_MYSQL_DATABASE ?? 'sim'};
const skills = {goalkeeping: 'keeperSkill', defending: 'defenderSkill', playmaking: 'playmakerSkill', winger: 'wingerSkill', passing: 'passerSkill', scoring: 'scorerSkill'};
const orders = [[0,0],[1,0],[1,1],[1,2],[1,3],[2,0],[2,1],[2,4],[3,0],[3,1],[6,0],[6,1],[6,2],[6,3],[7,0],[7,1],[7,2],[7,4],[8,0],[8,1],[8,2],[11,0],[11,2],[11,4],[12,0],[12,2]];
const mysql = sql => execFileSync('mysql', [`--host=${db.host}`, `--port=${db.port}`, `--user=${db.user}`, `--database=${db.database}`, '--batch', '--skip-column-names', '--raw', '--execute', sql], {encoding: 'utf8', env: {...process.env, MYSQL_PWD: db.password}, stdio: ['ignore', 'pipe', 'pipe']}).trim();
function fictionalPlayer(skill, level) {
  const p = {aggressiveness:3,arrivalDate:'2016-05-06T20:16:00+02:00',avatar:1,bestRatingMatchId:0,cards:0,days:0,defenderSkill:0,experience:0,firstName:'',formNumber:8,fullPlayerName:'SIM',gentleness:3,hasSalaryBonus:false,trainingThisWeekStatus:0,health:-1,honesty:3,isTransferlisted:false,keeperSkill:0,kickerSkill:0,lastClubMatchPlayedMinutes:0,lastClubMatchPositionId:0,lastMatchDate:null,lastMatchId:0,lastMatchPositionsWithMinutes:[],lastMatchRating:0,lastMatchStaminaEndOfGame:0,lastName:'SIM',leadership:3,leagueId:36,loyalty:0,manMarkingName:'SIM',nickName:'',nrOfTransfers:1,passerSkill:0,playerCategoryId:0,playerId:1,playerNumber:100,playmakerSkill:0,positionIdForFutureMatch:0,salary:0,scorerSkill:0,shortPlayerName:'SIM',specialty:0,staminaSkill:9,totalDayAge:2128,tsi:0,wingerSkill:0,years:19,isExtraPlayer:true,isCustomPlayer:true,lastMatchPositionId:0,hasTrainedThisWeek:false,salaryWithoutBonus:0};
  p[skills[skill]] = level; return p;
}
function requestTemplate(skill, level, position, behaviour) {
  const empty = length => Array.from({length}, () => ({id:0,behaviour:0,salaryWithoutBonus:0}));
  return {sourceSystem:'Hattrick',matchType:5,isHomeTeam:false,isHomeArena:false,specialTournamentType:0,lineup:{positions:Array.from({length:14},(_, index) => index === position ? {id:1,behaviour,salary:0} : {id:0,behaviour:0,salaryWithoutBonus:0}),substitutions:[],settings:{speechLevel:0,tactic:0,coachModifier:0,playAsSoonAsPossible:false,playAsSoonAsPossibleOnLoad:false,newLineup:''},bench:empty(14),kickers:empty(11),captain:0,setPieces:0,tournamentId:0},teamId:0,weather:2,extraPlayers:[fictionalPlayer(skill,level)],simulatorSettings:{venue:2,selfConfidence:-1,morale:-1}};
}
for (const skill of Object.keys(skills)) {
  const batchName = `${skill}-form8-stamina9-exp0-loyalty0`;
  const batchId = Number(mysql(`INSERT INTO calibration_batch (name,metadata_json) VALUES ('${batchName}',JSON_OBJECT('kind','single_player_skill_sweep','skill','${skill}')) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id); SELECT LAST_INSERT_ID();`).split('\n').pop());
  const values = [];
  for (let level = 0; level <= 20; level++) for (const [position, behaviour] of orders) {
    const template = requestTemplate(skill,level,position,behaviour), json = JSON.stringify(template);
    const metadata = JSON.stringify({kind:'single_player_skill_sweep',testedSkill:skill,testedSkillLevel:level,position,behaviour,form:8,stamina:9,experience:0,loyalty:0});
    values.push(`(${batchId},CONVERT(FROM_BASE64('${Buffer.from(json).toString('base64')}') USING utf8mb4),'${createHash('sha256').update(json).digest('hex')}',CONVERT(FROM_BASE64('${Buffer.from(metadata).toString('base64')}') USING utf8mb4))`);
  }
  for (let offset = 0; offset < values.length; offset += 20) {
    mysql(`INSERT INTO calibration_probe (batch_id,request_template_json,request_template_sha256,metadata_json) VALUES ${values.slice(offset, offset + 20).join(',')} ON DUPLICATE KEY UPDATE request_template_json=VALUES(request_template_json),metadata_json=VALUES(metadata_json),status='PENDING',attempt_count=0,error_message=NULL,started_at=NULL,completed_at=NULL;`);
  }
}
console.log('Queued 3276 complete request templates as PENDING.');
