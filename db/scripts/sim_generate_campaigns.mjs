#!/usr/bin/env node
/* Build complete API-request campaigns. This script never touches MySQL. */
import {writeFileSync} from 'node:fs';

const args = process.argv.slice(2);
const option = name => args[args.indexOf(name) + 1];
const campaign = option('--campaign') ?? 'all';
const output = option('--output');
if (!output || !['mirrors', 'crowding', 'interactions', 'fitness', 'experience-loyalty', 'full-lineup-holdout', 'full-lineup-aggregation', 'tactics', 'contexts', 'all'].includes(campaign)) {
  throw new Error('Usage: --campaign mirrors|crowding|interactions|fitness|experience-loyalty|full-lineup-holdout|full-lineup-aggregation|tactics|contexts|all --output <campaign.json>');
}

const skillProperty = {goalkeeping: 'keeperSkill', defending: 'defenderSkill', playmaking: 'playmakerSkill', winger: 'wingerSkill', passing: 'passerSkill', scoring: 'scorerSkill'};
const allSkills = Object.keys(skillProperty);
const emptyEntries = length => Array.from({length}, () => ({id: 0, behaviour: 0, salaryWithoutBonus: 0}));

function player(id, skills = {}, overrides = {}) {
  const result = {
    aggressiveness: 3, arrivalDate: '2016-05-06T20:16:00+02:00', avatar: 1, bestRatingMatchId: 0, cards: 0, days: 0,
    defenderSkill: 0, experience: 0, firstName: '', formNumber: 8, fullPlayerName: `SIM-${id}`, gentleness: 3,
    hasSalaryBonus: false, trainingThisWeekStatus: 0, health: -1, honesty: 3, isTransferlisted: false, keeperSkill: 0,
    kickerSkill: 0, lastClubMatchPlayedMinutes: 0, lastClubMatchPositionId: 0, lastMatchDate: null, lastMatchId: 0,
    lastMatchPositionsWithMinutes: [], lastMatchRating: 0, lastMatchStaminaEndOfGame: 0, lastName: `SIM-${id}`,
    leadership: 3, leagueId: 36, loyalty: 0, manMarkingName: `SIM-${id}`, nickName: '', nrOfTransfers: 1,
    passerSkill: 0, playerCategoryId: 0, playerId: id, playerNumber: 100 + id, playmakerSkill: 0,
    positionIdForFutureMatch: 0, salary: 0, scorerSkill: 0, shortPlayerName: `SIM-${id}`, specialty: 0,
    staminaSkill: 9, totalDayAge: 2128, tsi: 0, wingerSkill: 0, years: 19, isExtraPlayer: true,
    isCustomPlayer: true, lastMatchPositionId: 0, hasTrainedThisWeek: false, salaryWithoutBonus: 0,
    ...overrides
  };
  for (const [skill, value] of Object.entries(skills)) result[skillProperty[skill] ?? skill] = value;
  return result;
}

function request(placements) {
  const positions = emptyEntries(14);
  const players = [];
  for (const [index, placement] of placements.entries()) {
    const id = index + 1;
    positions[placement.slot] = {id, behaviour: placement.behaviour ?? 0, salary: 0};
    players.push(player(id, placement.skills, placement.playerOverrides));
  }
  return {
    sourceSystem: 'Hattrick', matchType: 5, isHomeTeam: false, isHomeArena: false, specialTournamentType: 0,
    lineup: {positions, substitutions: [], settings: {speechLevel: 0, tactic: 0, coachModifier: 0, playAsSoonAsPossible: false, playAsSoonAsPossibleOnLoad: false, newLineup: ''}, bench: emptyEntries(14), kickers: emptyEntries(11), captain: 0, setPieces: 0, tournamentId: 0},
    teamId: 0, weather: 2, extraPlayers: players, simulatorSettings: {venue: 2, selfConfidence: -1, morale: -1}
  };
}

function mirrors() {
  // right/left slot pairs: fullback, central defender, winger, inner midfield, forward
  const pairs = [[1, 5], [2, 4], [6, 10], [7, 9], [11, 13]];
  const probes = [];
  for (const [right, left] of pairs) for (const skill of allSkills) for (const level of [6, 12, 18]) {
    for (const [side, slot] of [['right', right], ['left', left]]) {
      probes.push({metadata: {campaign: 'mirror-symmetry-v1', side, slot, skill, level}, request: request([{slot, skills: {[skill]: level}}])});
    }
  }
  return {name: 'mirror-symmetry-v1', description: 'Verify sector symmetry of every open-play skill on mirrored roles.', probes};
}

function crowding() {
  const groups = [
    {name: 'central_defenders', slots: [3, 2, 4], skill: 'defending'},
    {name: 'inner_midfielders', slots: [8, 7, 9], skill: 'playmaking'},
    {name: 'forwards', slots: [12, 11, 13], skill: 'scoring'}
  ];
  const probes = [];
  for (const group of groups) for (const level of [4, 8, 12, 16, 20]) for (let count = 1; count <= 3; count++) {
    const placements = group.slots.slice(0, count).map(slot => ({slot, skills: {[group.skill]: level}}));
    probes.push({metadata: {campaign: 'crowding-v1', group: group.name, count, skill: group.skill, level}, request: request(placements)});
  }
  return {name: 'crowding-v1', description: 'Measure 1/2/3-player aggregation in central-defense, inner-midfield and forward groups.', probes};
}

function interactions() {
  // Target pairs are selected where both skills have a measured effect in the
  // role.  Their individual curves already exist, so each probe identifies a
  // true within-player interaction instead of duplicating the base matrix.
  const roles = [
    {role: 'goalkeeper', slot: 0, pairs: [['goalkeeping', 'defending'], ['goalkeeping', 'passing']]},
    {role: 'central_defender', slot: 3, pairs: [['defending', 'passing'], ['defending', 'playmaking']]},
    {role: 'wing_back', slot: 1, pairs: [['defending', 'winger'], ['defending', 'passing'], ['winger', 'passing']]},
    {role: 'winger', slot: 6, pairs: [['winger', 'playmaking'], ['winger', 'passing'], ['winger', 'scoring']]},
    {role: 'inner_midfielder', slot: 8, pairs: [['playmaking', 'defending'], ['playmaking', 'winger'], ['playmaking', 'passing'], ['playmaking', 'scoring']]},
    {role: 'forward', slot: 12, pairs: [['scoring', 'passing'], ['scoring', 'winger'], ['scoring', 'playmaking']]}
  ];
  const levels = [6, 12, 18];
  const probes = [];
  for (const {role, slot, pairs} of roles) for (const [firstSkill, secondSkill] of pairs) {
    for (const firstLevel of levels) for (const secondLevel of levels) {
      probes.push({
        metadata: {campaign: 'skill-interactions-v1', role, slot, behaviour: 0, firstSkill, firstLevel, secondSkill, secondLevel},
        request: request([{slot, skills: {[firstSkill]: firstLevel, [secondSkill]: secondLevel}}])
      });
    }
  }
  return {name: 'skill-interactions-v1', description: 'Measure within-player cross-skill interactions in roles where both skills have observed effects.', probes};
}

function fitness() {
  // The base matrix fixes form=8 and stamina=9.  These probes vary both on
  // representative primary roles, preserving all other player fields.
  const roles = [
    {role: 'goalkeeper', slot: 0, skill: 'goalkeeping'},
    {role: 'central_defender', slot: 3, skill: 'defending'},
    {role: 'wing_back', slot: 1, skill: 'defending'},
    {role: 'winger', slot: 6, skill: 'winger'},
    {role: 'inner_midfielder', slot: 8, skill: 'playmaking'},
    {role: 'forward', slot: 12, skill: 'scoring'}
  ];
  const forms = [1, 4, 8];
  const staminaLevels = [0, 3, 6, 9];
  const probes = [];
  for (const {role, slot, skill} of roles) for (const skillLevel of [8, 16]) {
    for (const form of forms) for (const stamina of staminaLevels) {
      probes.push({
        metadata: {campaign: 'fitness-v1', role, slot, behaviour: 0, skill, skillLevel, form, stamina},
        request: request([{slot, skills: {[skill]: skillLevel}, playerOverrides: {formNumber: form, staminaSkill: stamina}}])
      });
    }
  }
  return {name: 'fitness-v1', description: 'Measure form and stamina effects over time in representative primary roles.', probes};
}

function experienceLoyalty() {
  const roles = [
    {role: 'goalkeeper', slot: 0, skill: 'goalkeeping'},
    {role: 'central_defender', slot: 3, skill: 'defending'},
    {role: 'wing_back', slot: 1, skill: 'defending'},
    {role: 'winger', slot: 6, skill: 'winger'},
    {role: 'inner_midfielder', slot: 8, skill: 'playmaking'},
    {role: 'forward', slot: 12, skill: 'scoring'}
  ];
  const experiences = [0, 4, 8, 12, 16, 20];
  const loyalties = [0, 10, 20];
  const probes = [];
  for (const {role, slot, skill} of roles) for (const skillLevel of [8, 16]) {
    for (const experience of experiences) for (const loyalty of loyalties) {
      probes.push({
        metadata: {campaign: 'experience-loyalty-v1', role, slot, behaviour: 0, skill, skillLevel, experience, loyalty},
        request: request([{slot, skills: {[skill]: skillLevel}, playerOverrides: {experience, loyalty}}])
      });
    }
  }
  return {name: 'experience-loyalty-v1', description: 'Measure experience and loyalty contributions with form=8 and stamina=9.', probes};
}

function fullLineupHoldout() {
  // These are deliberately not used to fit individual coefficients. They are
  // unseen complete XIs for validating the aggregation step of V2.
  const formations = {
    '4-4-2': [0, 1, 2, 4, 5, 6, 7, 9, 10, 11, 13],
    '3-5-2': [0, 1, 2, 4, 6, 7, 8, 9, 10, 11, 13],
    '5-3-2': [0, 1, 2, 3, 4, 5, 7, 8, 9, 11, 13]
  };
  const profiles = {
    goalkeeper: {goalkeeping: 15, defending: 5, passing: 7},
    wingBack: {defending: 12, winger: 11, passing: 8, playmaking: 6},
    centralDefender: {defending: 15, passing: 8, playmaking: 6, winger: 3},
    winger: {winger: 15, playmaking: 10, passing: 10, scoring: 7, defending: 5},
    innerMidfielder: {playmaking: 16, defending: 9, passing: 11, winger: 7, scoring: 5},
    forward: {scoring: 17, passing: 10, winger: 7, playmaking: 5}
  };
  const roleForSlot = slot => slot === 0 ? 'goalkeeper' : [1, 5].includes(slot) ? 'wingBack' : [2, 3, 4].includes(slot) ? 'centralDefender' : [6, 10].includes(slot) ? 'winger' : [7, 8, 9].includes(slot) ? 'innerMidfielder' : 'forward';
  const emphases = {
    balanced: {},
    middle: {playmaking: 1.25, scoring: 1.15, winger: 0.8},
    wings: {winger: 1.3, passing: 1.15, playmaking: 0.85}
  };
  const skillsFor = (role, strength, emphasis) => Object.fromEntries(Object.entries(profiles[role]).map(([skill, value]) => [skill, Math.min(20, Math.max(0, Math.round(value * strength * (emphasis[skill] ?? 1))))]));
  const probes = [];
  for (const [formation, slots] of Object.entries(formations)) for (const strength of [0.7, 1, 1.2]) for (const [emphasisName, emphasis] of Object.entries(emphases)) {
    const placements = slots.map(slot => ({slot, skills: skillsFor(roleForSlot(slot), strength, emphasis)}));
    probes.push({
      metadata: {campaign: 'full-lineup-holdout-v1', formation, strength, emphasis: emphasisName, players: placements.length},
      request: request(placements)
    });
  }
  return {name: 'full-lineup-holdout-v1', description: 'Twenty-seven synthetic complete XIs held out for aggregation validation.', probes};
}

function fullLineupAggregation() {
  // Distinct strength points from the holdout campaign. These observations
  // train aggregation only; full-lineup-holdout-v1 remains untouched.
  const formations = {
    '4-4-2': [0, 1, 2, 4, 5, 6, 7, 9, 10, 11, 13],
    '3-5-2': [0, 1, 2, 4, 6, 7, 8, 9, 10, 11, 13],
    '5-3-2': [0, 1, 2, 3, 4, 5, 7, 8, 9, 11, 13]
  };
  const profiles = {
    goalkeeper: {goalkeeping: 15, defending: 5, passing: 7}, wingBack: {defending: 12, winger: 11, passing: 8, playmaking: 6},
    centralDefender: {defending: 15, passing: 8, playmaking: 6, winger: 3}, winger: {winger: 15, playmaking: 10, passing: 10, scoring: 7, defending: 5},
    innerMidfielder: {playmaking: 16, defending: 9, passing: 11, winger: 7, scoring: 5}, forward: {scoring: 17, passing: 10, winger: 7, playmaking: 5}
  };
  const roleForSlot = slot => slot === 0 ? 'goalkeeper' : [1, 5].includes(slot) ? 'wingBack' : [2, 3, 4].includes(slot) ? 'centralDefender' : [6, 10].includes(slot) ? 'winger' : [7, 8, 9].includes(slot) ? 'innerMidfielder' : 'forward';
  const emphases = {balanced: {}, middle: {playmaking: 1.25, scoring: 1.15, winger: 0.8}, wings: {winger: 1.3, passing: 1.15, playmaking: 0.85}};
  const skillsFor = (role, strength, emphasis) => Object.fromEntries(Object.entries(profiles[role]).map(([skill, value]) => [skill, Math.min(20, Math.max(0, Math.round(value * strength * (emphasis[skill] ?? 1))))]));
  const probes = [];
  for (const [formation, slots] of Object.entries(formations)) for (const strength of [0.6, 0.85, 1.1, 1.35]) for (const [emphasisName, emphasis] of Object.entries(emphases)) {
    probes.push({metadata: {campaign: 'full-lineup-aggregation-v1', formation, strength, emphasis: emphasisName, players: slots.length}, request: request(slots.map(slot => ({slot, skills: skillsFor(roleForSlot(slot), strength, emphasis)})))});
  }
  return {name: 'full-lineup-aggregation-v1', description: 'Thirty-six complete XIs used only to learn sector aggregation; held-out lineups remain separate.', probes};
}

function tactics() {
  // Tactic identifiers are API values. All player and match inputs remain
  // fixed, so any change can be attributed to lineup.settings.tactic.
  const slots = [0, 1, 2, 4, 5, 6, 7, 9, 10, 11, 13];
  const profiles = {
    goalkeeper: {goalkeeping: 15, defending: 5, passing: 7}, wingBack: {defending: 12, winger: 11, passing: 8, playmaking: 6},
    centralDefender: {defending: 15, passing: 8, playmaking: 6, winger: 3}, winger: {winger: 15, playmaking: 10, passing: 10, scoring: 7, defending: 5},
    innerMidfielder: {playmaking: 16, defending: 9, passing: 11, winger: 7, scoring: 5}, forward: {scoring: 17, passing: 10, winger: 7, playmaking: 5}
  };
  const roleForSlot = slot => slot === 0 ? 'goalkeeper' : [1, 5].includes(slot) ? 'wingBack' : [2, 3, 4].includes(slot) ? 'centralDefender' : [6, 10].includes(slot) ? 'winger' : [7, 8, 9].includes(slot) ? 'innerMidfielder' : 'forward';
  const tactics = [{id: 0, name: 'normal'}, {id: 1, name: 'pressing'}, {id: 2, name: 'counter_attacks'}, {id: 3, name: 'attack_in_the_middle'}, {id: 4, name: 'attack_on_wings'}, {id: 7, name: 'play_creatively'}, {id: 8, name: 'long_shots'}];
  const probes = tactics.map(tactic => {
    const item = request(slots.map(slot => ({slot, skills: profiles[roleForSlot(slot)]})));
    item.lineup.settings.tactic = tactic.id;
    return {metadata: {campaign: 'tactics-v1', formation: '4-4-2', tacticId: tactic.id, tactic: tactic.name}, request: item};
  });
  return {name: 'tactics-v1', description: 'Measure all supported tactics on one fixed complete XI.', probes};
}

function contexts() {
  const slots = [0, 1, 2, 4, 5, 6, 7, 9, 10, 11, 13];
  const profiles = {
    goalkeeper: {goalkeeping: 15, defending: 5, passing: 7}, wingBack: {defending: 12, winger: 11, passing: 8, playmaking: 6},
    centralDefender: {defending: 15, passing: 8, playmaking: 6, winger: 3}, winger: {winger: 15, playmaking: 10, passing: 10, scoring: 7, defending: 5},
    innerMidfielder: {playmaking: 16, defending: 9, passing: 11, winger: 7, scoring: 5}, forward: {scoring: 17, passing: 10, winger: 7, playmaking: 5}
  };
  const roleForSlot = slot => slot === 0 ? 'goalkeeper' : [1, 5].includes(slot) ? 'wingBack' : [2, 3, 4].includes(slot) ? 'centralDefender' : [6, 10].includes(slot) ? 'winger' : [7, 8, 9].includes(slot) ? 'innerMidfielder' : 'forward';
  const base = () => request(slots.map(slot => ({slot, skills: profiles[roleForSlot(slot)]})));
  const probes = [];
  for (const weather of [0, 1, 2, 3, 4, 5]) {
    const item = base(); item.weather = weather;
    probes.push({metadata: {campaign: 'contexts-v1', dimension: 'weather', weather}, request: item});
  }
  for (const [name, values] of [['neutral', {isHomeTeam: false, isHomeArena: false, venue: 0}], ['home', {isHomeTeam: true, isHomeArena: true, venue: 1}], ['away', {isHomeTeam: false, isHomeArena: false, venue: 2}]]) {
    const item = base(); Object.assign(item, {isHomeTeam: values.isHomeTeam, isHomeArena: values.isHomeArena}); item.simulatorSettings.venue = values.venue;
    probes.push({metadata: {campaign: 'contexts-v1', dimension: 'venue', venue: name}, request: item});
  }
  for (const selfConfidence of [0, 4, 8]) for (const morale of [0, 4, 8]) {
    const item = base(); item.simulatorSettings.selfConfidence = selfConfidence; item.simulatorSettings.morale = morale;
    probes.push({metadata: {campaign: 'contexts-v1', dimension: 'confidence_morale', selfConfidence, morale}, request: item});
  }
  return {name: 'contexts-v1', description: 'Measure weather, venue, confidence and morale independently on a fixed complete XI.', probes};
}

const campaigns = [];
if (campaign === 'mirrors' || campaign === 'all') campaigns.push(mirrors());
if (campaign === 'crowding' || campaign === 'all') campaigns.push(crowding());
if (campaign === 'interactions' || campaign === 'all') campaigns.push(interactions());
if (campaign === 'fitness' || campaign === 'all') campaigns.push(fitness());
if (campaign === 'experience-loyalty' || campaign === 'all') campaigns.push(experienceLoyalty());
if (campaign === 'full-lineup-holdout' || campaign === 'all') campaigns.push(fullLineupHoldout());
if (campaign === 'full-lineup-aggregation' || campaign === 'all') campaigns.push(fullLineupAggregation());
if (campaign === 'tactics' || campaign === 'all') campaigns.push(tactics());
if (campaign === 'contexts' || campaign === 'all') campaigns.push(contexts());
const document = {format: 'sim-campaign-v1', generatedAt: new Date().toISOString(), campaigns};
writeFileSync(output, `${JSON.stringify(document, null, 2)}\n`, 'utf8');
console.log(JSON.stringify({output, campaigns: campaigns.map(item => ({name: item.name, probes: item.probes.length})), totalProbes: campaigns.reduce((sum, item) => sum + item.probes.length, 0)}, null, 2));
