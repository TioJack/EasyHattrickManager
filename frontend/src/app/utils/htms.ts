import {PlayerInfo, PlayerSubSkillInfo} from '../services/model/data-response';

export interface Htms { htms: number; htms28: number; }

// Keep the interpolation tables and age adjustment in sync with server HTMSUtils.
const SKILL_POINTS = [
  [0, 0, 0, 0, 0, 0, 0],
  [2, 4, 4, 2, 3, 4, 1],
  [12, 18, 17, 12, 14, 17, 2],
  [23, 39, 34, 25, 31, 36, 5],
  [39, 65, 57, 41, 51, 59, 9],
  [56, 98, 84, 60, 75, 88, 15],
  [76, 134, 114, 81, 104, 119, 21],
  [99, 175, 150, 105, 137, 156, 28],
  [123, 221, 190, 132, 173, 197, 37],
  [150, 271, 231, 161, 213, 240, 46],
  [183, 330, 281, 195, 259, 291, 56],
  [222, 401, 341, 238, 315, 354, 68],
  [268, 484, 412, 287, 381, 427, 81],
  [321, 580, 493, 344, 457, 511, 95],
  [380, 689, 584, 407, 540, 607, 112],
  [446, 809, 685, 478, 634, 713, 131],
  [519, 942, 798, 555, 738, 830, 153],
  [600, 1092, 924, 642, 854, 961, 179],
  [691, 1268, 1070, 741, 988, 1114, 210],
  [797, 1487, 1247, 855, 1148, 1300, 246],
  [924, 1791, 1480, 995, 1355, 1547, 287],
  [1074, 1791, 1791, 1172, 1355, 1547, 334],
  [1278, 1791, 1791, 1360, 1355, 1547, 388],
  [1278, 1791, 1791, 1360, 1355, 1547, 450],
];
const WEEK_POINTS = [10, 9.92, 9.81, 9.69, 9.54, 9.39, 9.22, 9.04, 8.85, 8.66, 8.47, 8.27, 8.07, 7.87, 7.67, 7.47, 7.27, 7.07, 6.87, 6.67, 6.47, 6.26, 6.06, 5.86, 5.65, 6.45, 6.24, 6.04, 5.83];
const SKILLS = [
  ['keeperSkill', 'keeper'], ['defenderSkill', 'defender'], ['playmakerSkill', 'playmaker'],
  ['wingerSkill', 'winger'], ['passingSkill', 'passing'], ['scorerSkill', 'scorer'], ['setPiecesSkill', 'setPieces']
] as const;

export function calculateSubskillHtms(player: PlayerInfo, subskills: Partial<PlayerSubSkillInfo>): Htms {
  const current = SKILLS.reduce((total, [level, fraction], column) => {
    const value = Math.max(0, Math.min(SKILL_POINTS.length - 1, (player[level] ?? 0) + (subskills[fraction] ?? 0)));
    const lower = Math.floor(value);
    const upper = Math.ceil(value);
    const points = SKILL_POINTS[lower][column]
      + (SKILL_POINTS[upper][column] - SKILL_POINTS[lower][column]) * (value - lower);
    return total + points;
  }, 0);
  let pointsDiff = 0;
  if (player.age < 28) {
    pointsDiff = ((112 - player.ageDays) / 7) * (WEEK_POINTS[player.age - 17] ?? 0);
    for (let age = player.age + 1; age < 28; age++) pointsDiff += 16 * (WEEK_POINTS[age - 17] ?? 0);
  } else if (player.age <= 45) {
    pointsDiff = (player.ageDays / 7) * WEEK_POINTS[player.age - 17];
    for (let age = player.age; age > 28; age--) pointsDiff += 16 * WEEK_POINTS[age - 17];
    pointsDiff = -pointsDiff;
  } else {
    pointsDiff = -current;
  }
  return {htms: Math.round(current), htms28: Math.round(current + pointsDiff) || 0};
}
