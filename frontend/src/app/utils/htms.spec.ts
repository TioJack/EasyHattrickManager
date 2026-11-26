import {calculateSubskillHtms} from './htms';
import {PlayerInfo, PlayerSubSkillInfo} from '../services/model/data-response';

// Expected results generated with the server's HTMSUtils double-skill overload.
const SERVER_CASES: [number, number, number[], number, number][] = [
[17,0,[6.12567, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0],652,2293],
[17,0,[0.29, 1.57, 7.99, 12.35, 4.25, 20.99, 22.99],2562,4204],
[17,0,[20.99, 20.99, 20.99, 20.99, 20.99, 20.99, 20.99],9057,10699],
[20,42,[6.12567, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0],652,1760],
[20,42,[0.29, 1.57, 7.99, 12.35, 4.25, 20.99, 22.99],2562,3670],
[20,42,[20.99, 20.99, 20.99, 20.99, 20.99, 20.99, 20.99],9057,10165],
[27,111,[6.12567, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0],652,653],
[27,111,[0.29, 1.57, 7.99, 12.35, 4.25, 20.99, 22.99],2562,2564],
[27,111,[20.99, 20.99, 20.99, 20.99, 20.99, 20.99, 20.99],9057,9058],
[28,0,[6.12567, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0],652,652],
[28,0,[0.29, 1.57, 7.99, 12.35, 4.25, 20.99, 22.99],2562,2562],
[28,0,[20.99, 20.99, 20.99, 20.99, 20.99, 20.99, 20.99],9057,9057],
[28,111,[6.12567, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0],652,521],
[28,111,[0.29, 1.57, 7.99, 12.35, 4.25, 20.99, 22.99],2562,2431],
[28,111,[20.99, 20.99, 20.99, 20.99, 20.99, 20.99, 20.99],9057,8926],
[29,42,[6.12567, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0],652,474],
[29,42,[0.29, 1.57, 7.99, 12.35, 4.25, 20.99, 22.99],2562,2385],
[29,42,[20.99, 20.99, 20.99, 20.99, 20.99, 20.99, 20.99],9057,8880],
[42,42,[6.12567, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0],652,-918],
[42,42,[0.29, 1.57, 7.99, 12.35, 4.25, 20.99, 22.99],2562,992],
[42,42,[20.99, 20.99, 20.99, 20.99, 20.99, 20.99, 20.99],9057,7487],
[45,111,[6.12567, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0],652,-1262],
[45,111,[0.29, 1.57, 7.99, 12.35, 4.25, 20.99, 22.99],2562,649],
[45,111,[20.99, 20.99, 20.99, 20.99, 20.99, 20.99, 20.99],9057,7144],
[46,0,[6.12567, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0],652,0],
[46,0,[0.29, 1.57, 7.99, 12.35, 4.25, 20.99, 22.99],2562,0],
[46,0,[20.99, 20.99, 20.99, 20.99, 20.99, 20.99, 20.99],9057,0],
];

describe('Live HTMS preview', () => {
  it('matches server interpolation, rounding and age adjustments across age and skill boundaries', () => {
    const levels = ['keeperSkill', 'defenderSkill', 'playmakerSkill', 'wingerSkill', 'passingSkill', 'scorerSkill', 'setPiecesSkill'] as const;
    const fractions = ['keeper', 'defender', 'playmaker', 'winger', 'passing', 'scorer', 'setPieces'] as const;
    for (const [age, ageDays, skills, htms, htms28] of SERVER_CASES) {
      const player = {age, ageDays} as PlayerInfo;
      const subskills: Partial<PlayerSubSkillInfo> = {};
      skills.forEach((skill, i) => {
        player[levels[i]] = Math.floor(skill);
        subskills[fractions[i]] = skill - Math.floor(skill);
      });
      expect(calculateSubskillHtms(player, subskills)).toEqual({htms, htms28});
    }
  });
});
