package easyhattrickmanager.utils;

import easyhattrickmanager.client.hattrick.model.players.Player;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.service.model.HTMS;

public class HTMSUtils {

    public static HTMS calculateHTMS(Player playerHT) {
        return calculateHTMS(
            playerHT.getAge(),
            playerHT.getAgeDays(),
            playerHT.getKeeperSkill(),
            playerHT.getDefenderSkill(),
            playerHT.getPlaymakerSkill(),
            playerHT.getWingerSkill(),
            playerHT.getPassingSkill(),
            playerHT.getScorerSkill(),
            playerHT.getSetPiecesSkill()
        );
    }

    public static HTMS calculateHTMS(PlayerData playerData) {
        return calculateHTMS(
            playerData.getAge(),
            playerData.getAgeDays(),
            playerData.getKeeperSkill(),
            playerData.getDefenderSkill(),
            playerData.getPlaymakerSkill(),
            playerData.getWingerSkill(),
            playerData.getPassingSkill(),
            playerData.getScorerSkill(),
            playerData.getSetPiecesSkill()
        );
    }

    public static HTMS calculateHTMS(int years, int days, int keeper, int defending, int playmaking, int winger, int passing, int scoring, int setPieces) {
        double[] WEEK_PTS_PER_AGE = new double[46];
        WEEK_PTS_PER_AGE[17] = 10;
        WEEK_PTS_PER_AGE[18] = 9.92;
        WEEK_PTS_PER_AGE[19] = 9.81;
        WEEK_PTS_PER_AGE[20] = 9.69;
        WEEK_PTS_PER_AGE[21] = 9.54;
        WEEK_PTS_PER_AGE[22] = 9.39;
        WEEK_PTS_PER_AGE[23] = 9.22;
        WEEK_PTS_PER_AGE[24] = 9.04;
        WEEK_PTS_PER_AGE[25] = 8.85;
        WEEK_PTS_PER_AGE[26] = 8.66;
        WEEK_PTS_PER_AGE[27] = 8.47;
        WEEK_PTS_PER_AGE[28] = 8.27;
        WEEK_PTS_PER_AGE[29] = 8.07;
        WEEK_PTS_PER_AGE[30] = 7.87;
        WEEK_PTS_PER_AGE[31] = 7.67;
        WEEK_PTS_PER_AGE[32] = 7.47;
        WEEK_PTS_PER_AGE[33] = 7.27;
        WEEK_PTS_PER_AGE[34] = 7.07;
        WEEK_PTS_PER_AGE[35] = 6.87;
        WEEK_PTS_PER_AGE[36] = 6.67;
        WEEK_PTS_PER_AGE[37] = 6.47;
        WEEK_PTS_PER_AGE[38] = 6.26;
        WEEK_PTS_PER_AGE[39] = 6.06;
        WEEK_PTS_PER_AGE[40] = 5.86;
        WEEK_PTS_PER_AGE[41] = 5.65;
        WEEK_PTS_PER_AGE[42] = 6.45;
        WEEK_PTS_PER_AGE[43] = 6.24;
        WEEK_PTS_PER_AGE[44] = 6.04;
        WEEK_PTS_PER_AGE[45] = 5.83;

        int MAX_AGE = 45;

        int[][] SKILL_PTS_PER_LVL = {
            {0, 0, 0, 0, 0, 0, 0},    // Level 0
            {2, 4, 4, 2, 3, 4, 1},   // Level 1
            {12, 18, 17, 12, 14, 17, 2}, // Level 2
            {23, 39, 34, 25, 31, 36, 5}, // Level 3
            {39, 65, 57, 41, 51, 59, 9}, // Level 4
            {56, 98, 84, 60, 75, 88, 15}, // Level 5
            {76, 134, 114, 81, 104, 119, 21}, // Level 6
            {99, 175, 150, 105, 137, 156, 28}, // Level 7
            {123, 221, 190, 132, 173, 197, 37}, // Level 8
            {150, 271, 231, 161, 213, 240, 46}, // Level 9
            {183, 330, 281, 195, 259, 291, 56}, // Level 10
            {222, 401, 341, 238, 315, 354, 68}, // Level 11
            {268, 484, 412, 287, 381, 427, 81}, // Level 12
            {321, 580, 493, 344, 457, 511, 95}, // Level 13
            {380, 689, 584, 407, 540, 607, 112}, // Level 14
            {446, 809, 685, 478, 634, 713, 131}, // Level 15
            {519, 942, 798, 555, 738, 830, 153}, // Level 16
            {600, 1092, 924, 642, 854, 961, 179}, // Level 17
            {691, 1268, 1070, 741, 988, 1114, 210}, // Level 18
            {797, 1487, 1247, 855, 1148, 1300, 246}, // Level 19
            {924, 1791, 1480, 995, 1355, 1547, 287}, // Level 20
            {1074, 1791, 1791, 1172, 1355, 1547, 334}, // Level 21
            {1278, 1791, 1791, 1360, 1355, 1547, 388}, // Level 22
            {1278, 1791, 1791, 1360, 1355, 1547, 450}  // Level 23
        };

        int current = SKILL_PTS_PER_LVL[keeper][0]
            + SKILL_PTS_PER_LVL[defending][1]
            + SKILL_PTS_PER_LVL[playmaking][2]
            + SKILL_PTS_PER_LVL[winger][3]
            + SKILL_PTS_PER_LVL[passing][4]
            + SKILL_PTS_PER_LVL[scoring][5]
            + SKILL_PTS_PER_LVL[setPieces][6];

        int AGE_FACTOR = 28;
        int WEEKS_IN_SEASON = 16;
        int DAYS_IN_WEEK = 7;
        int DAYS_IN_SEASON = WEEKS_IN_SEASON * DAYS_IN_WEEK;

        double pointsDiff = 0;
        if (years < AGE_FACTOR) {
            double pointsPerWeek = WEEK_PTS_PER_AGE[years];
            pointsDiff += ((DAYS_IN_SEASON - days) / (double) DAYS_IN_WEEK) * pointsPerWeek;
            for (int i = years + 1; i < AGE_FACTOR; i++) {
                pointsDiff += WEEKS_IN_SEASON * WEEK_PTS_PER_AGE[i];
            }
        } else if (years <= MAX_AGE) {
            pointsDiff += (days / (double) DAYS_IN_WEEK) * WEEK_PTS_PER_AGE[years];
            for (int i = years; i > AGE_FACTOR; i--) {
                pointsDiff += WEEKS_IN_SEASON * WEEK_PTS_PER_AGE[i];
            }
            pointsDiff = -pointsDiff;
        } else {
            pointsDiff = -current;
        }

        return HTMS.builder()
            .htms(current)
            .htms28((int) Math.round(current + pointsDiff))
            .build();
    }

    public static HTMS calculateHTMS(int years, int days, double keeper, double defending, double playmaking, double winger, double passing, double scoring, double setPieces) {
        double[] WEEK_PTS_PER_AGE = new double[46];
        WEEK_PTS_PER_AGE[17] = 10;
        WEEK_PTS_PER_AGE[18] = 9.92;
        WEEK_PTS_PER_AGE[19] = 9.81;
        WEEK_PTS_PER_AGE[20] = 9.69;
        WEEK_PTS_PER_AGE[21] = 9.54;
        WEEK_PTS_PER_AGE[22] = 9.39;
        WEEK_PTS_PER_AGE[23] = 9.22;
        WEEK_PTS_PER_AGE[24] = 9.04;
        WEEK_PTS_PER_AGE[25] = 8.85;
        WEEK_PTS_PER_AGE[26] = 8.66;
        WEEK_PTS_PER_AGE[27] = 8.47;
        WEEK_PTS_PER_AGE[28] = 8.27;
        WEEK_PTS_PER_AGE[29] = 8.07;
        WEEK_PTS_PER_AGE[30] = 7.87;
        WEEK_PTS_PER_AGE[31] = 7.67;
        WEEK_PTS_PER_AGE[32] = 7.47;
        WEEK_PTS_PER_AGE[33] = 7.27;
        WEEK_PTS_PER_AGE[34] = 7.07;
        WEEK_PTS_PER_AGE[35] = 6.87;
        WEEK_PTS_PER_AGE[36] = 6.67;
        WEEK_PTS_PER_AGE[37] = 6.47;
        WEEK_PTS_PER_AGE[38] = 6.26;
        WEEK_PTS_PER_AGE[39] = 6.06;
        WEEK_PTS_PER_AGE[40] = 5.86;
        WEEK_PTS_PER_AGE[41] = 5.65;
        WEEK_PTS_PER_AGE[42] = 6.45;
        WEEK_PTS_PER_AGE[43] = 6.24;
        WEEK_PTS_PER_AGE[44] = 6.04;
        WEEK_PTS_PER_AGE[45] = 5.83;

        int MAX_AGE = 45;

        int[][] SKILL_PTS_PER_LVL = {
            {0, 0, 0, 0, 0, 0, 0},    // Level 0
            {2, 4, 4, 2, 3, 4, 1},   // Level 1
            {12, 18, 17, 12, 14, 17, 2}, // Level 2
            {23, 39, 34, 25, 31, 36, 5}, // Level 3
            {39, 65, 57, 41, 51, 59, 9}, // Level 4
            {56, 98, 84, 60, 75, 88, 15}, // Level 5
            {76, 134, 114, 81, 104, 119, 21}, // Level 6
            {99, 175, 150, 105, 137, 156, 28}, // Level 7
            {123, 221, 190, 132, 173, 197, 37}, // Level 8
            {150, 271, 231, 161, 213, 240, 46}, // Level 9
            {183, 330, 281, 195, 259, 291, 56}, // Level 10
            {222, 401, 341, 238, 315, 354, 68}, // Level 11
            {268, 484, 412, 287, 381, 427, 81}, // Level 12
            {321, 580, 493, 344, 457, 511, 95}, // Level 13
            {380, 689, 584, 407, 540, 607, 112}, // Level 14
            {446, 809, 685, 478, 634, 713, 131}, // Level 15
            {519, 942, 798, 555, 738, 830, 153}, // Level 16
            {600, 1092, 924, 642, 854, 961, 179}, // Level 17
            {691, 1268, 1070, 741, 988, 1114, 210}, // Level 18
            {797, 1487, 1247, 855, 1148, 1300, 246}, // Level 19
            {924, 1791, 1480, 995, 1355, 1547, 287}, // Level 20
            {1074, 1791, 1791, 1172, 1355, 1547, 334}, // Level 21
            {1278, 1791, 1791, 1360, 1355, 1547, 388}, // Level 22
            {1278, 1791, 1791, 1360, 1355, 1547, 450}  // Level 23
        };

        double current = interpolateSkillPoints(keeper, SKILL_PTS_PER_LVL, 0)
            + interpolateSkillPoints(defending, SKILL_PTS_PER_LVL, 1)
            + interpolateSkillPoints(playmaking, SKILL_PTS_PER_LVL, 2)
            + interpolateSkillPoints(winger, SKILL_PTS_PER_LVL, 3)
            + interpolateSkillPoints(passing, SKILL_PTS_PER_LVL, 4)
            + interpolateSkillPoints(scoring, SKILL_PTS_PER_LVL, 5)
            + interpolateSkillPoints(setPieces, SKILL_PTS_PER_LVL, 6);

        int AGE_FACTOR = 28;
        int WEEKS_IN_SEASON = 16;
        int DAYS_IN_WEEK = 7;
        int DAYS_IN_SEASON = WEEKS_IN_SEASON * DAYS_IN_WEEK;

        double pointsDiff = 0;
        if (years < AGE_FACTOR) {
            double pointsPerWeek = WEEK_PTS_PER_AGE[years];
            pointsDiff += ((DAYS_IN_SEASON - days) / (double) DAYS_IN_WEEK) * pointsPerWeek;
            for (int i = years + 1; i < AGE_FACTOR; i++) {
                pointsDiff += WEEKS_IN_SEASON * WEEK_PTS_PER_AGE[i];
            }
        } else if (years <= MAX_AGE) {
            pointsDiff += (days / (double) DAYS_IN_WEEK) * WEEK_PTS_PER_AGE[years];
            for (int i = years; i > AGE_FACTOR; i--) {
                pointsDiff += WEEKS_IN_SEASON * WEEK_PTS_PER_AGE[i];
            }
            pointsDiff = -pointsDiff;
        } else {
            pointsDiff = -current;
        }

        return HTMS.builder()
            .htms((int) Math.round(current))
            .htms28((int) Math.round(current + pointsDiff))
            .build();
    }

    private static double interpolateSkillPoints(double skill, int[][] skillPtsPerLvl, int column) {
        int minLevel = 0;
        int maxLevel = skillPtsPerLvl.length - 1;
        double clamped = Math.max(minLevel, Math.min(maxLevel, skill));
        int lower = (int) Math.floor(clamped);
        int upper = (int) Math.ceil(clamped);
        double fraction = clamped - lower;

        int lowerPts = skillPtsPerLvl[lower][column];
        if (upper == lower) {
            return lowerPts;
        }

        int upperPts = skillPtsPerLvl[upper][column];
        return lowerPts + (upperPts - lowerPts) * fraction;
    }

}
