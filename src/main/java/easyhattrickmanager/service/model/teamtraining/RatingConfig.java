package easyhattrickmanager.service.model.teamtraining;

import java.util.LinkedHashMap;
import java.util.Map;

public class RatingConfig {

    private static final double[] LOYALTY_BONUS_BY_LEVEL = {
        0.0,        // 0
        0.0,        // 1
        1.0 / 19.0, // 2
        2.0 / 19.0, // 3
        3.0 / 19.0, // 4
        4.0 / 19.0, // 5
        5.0 / 19.0, // 6
        6.0 / 19.0, // 7
        7.0 / 19.0, // 8
        8.0 / 19.0, // 9
        9.0 / 19.0, // 10
        10.0 / 19.0,// 11
        11.0 / 19.0,// 12
        12.0 / 19.0,// 13
        13.0 / 19.0,// 14
        14.0 / 19.0,// 15
        15.0 / 19.0,// 16
        16.0 / 19.0,// 17
        17.0 / 19.0,// 18
        18.0 / 19.0,// 19
        1.0         // 20
    };

    public static double getLoyaltyBonus(int loyalty, boolean motherClubBonus) {
        if (motherClubBonus) {
            return 1.5;
        }
        int clampedLevel = Math.max(0, Math.min(20, loyalty));
        return LOYALTY_BONUS_BY_LEVEL[clampedLevel];
    }

    private static final double GK100 = 0.16;
    private static final double SIDEGK100 = 0.26;
    private static final double DEF100 = 0.16402001064369948;
    private static final double SIDEDEF100 = 0.2695;
    private static final double MID100 = 0.11;
    private static final double FW100 = 0.16175;
    private static final double SIDEFW100 = 0.191;
    private static final double WI100 = 0.191;
    private static final double PS100 = 0.16175;
    private static final double SIDEPS100 = 0.192;

    public static final Map<RatingConfigGroup, Map<RatingConfigSection, Map<RatingConfigProp, Double>>> config =
        new LinkedHashMap<RatingConfigGroup, Map<RatingConfigSection, Map<RatingConfigProp, Double>>>() {{
            put(RatingConfigGroup.SIDEDEFENSE, new LinkedHashMap<RatingConfigSection, Map<RatingConfigProp, Double>>() {{
                put(RatingConfigSection.general, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.delta, 0.75);
                    put(RatingConfigProp.power, 1.196);
                    put(RatingConfigProp.multiplier, 0.972);
                    put(RatingConfigProp.squareMod, 0.0);
                    put(RatingConfigProp.cubeMod, 0.0);
                    put(RatingConfigProp.extraMulti, 1.000);
                    put(RatingConfigProp.trainerOff, 0.88);
                    put(RatingConfigProp.trainerDef, 1.128);
                    put(RatingConfigProp.trainerNeutral, 1.0);
                    put(RatingConfigProp.tacticAIM, 0.85);
                    put(RatingConfigProp.tacticCreative, 0.93);
                    put(RatingConfigProp.defleadPercentDef, 0.0);
                    put(RatingConfigProp.pullback, 0.125);
                }});
                put(RatingConfigSection.goalkeeping_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.GK_norm, SIDEGK100 * 0.61);
                }});
                put(RatingConfigSection.defending_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.GK_norm, SIDEGK100 * 0.25);
                }});
                put(RatingConfigSection.defending_thisside, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.CD_norm, SIDEDEF100 * 0.52);
                    put(RatingConfigProp.CD_off, SIDEDEF100 * 0.40);
                    put(RatingConfigProp.CD_tw, SIDEDEF100 * 0.81);
                    put(RatingConfigProp.WB_norm, SIDEDEF100 * 0.92);
                    put(RatingConfigProp.WB_off, SIDEDEF100 * 0.74);
                    put(RatingConfigProp.WB_tm, SIDEDEF100 * 0.75);
                    put(RatingConfigProp.WB_def, SIDEDEF100 * 1.00);
                    put(RatingConfigProp.IM_norm, SIDEDEF100 * 0.19);
                    put(RatingConfigProp.IM_off, SIDEDEF100 * 0.09);
                    put(RatingConfigProp.IM_def, SIDEDEF100 * 0.27);
                    put(RatingConfigProp.IM_tw, SIDEDEF100 * 0.24);
                    put(RatingConfigProp.WI_norm, SIDEDEF100 * 0.35);
                    put(RatingConfigProp.WI_off, SIDEDEF100 * 0.22);
                    put(RatingConfigProp.WI_def, SIDEDEF100 * 0.61);
                    put(RatingConfigProp.WI_tm, SIDEDEF100 * 0.29);
                }});
            }});
            put(RatingConfigGroup.CENTRALDEFENSE, new LinkedHashMap<RatingConfigSection, Map<RatingConfigProp, Double>>() {{
                put(RatingConfigSection.general, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.delta, 0.75);
                    put(RatingConfigProp.power, 1.196);
                    put(RatingConfigProp.multiplier, 0.972);
                    put(RatingConfigProp.squareMod, 0.0);
                    put(RatingConfigProp.cubeMod, 0.0);
                    put(RatingConfigProp.extraMulti, 1.000);
                    put(RatingConfigProp.trainerOff, 0.88);
                    put(RatingConfigProp.trainerDef, 1.128);
                    put(RatingConfigProp.trainerNeutral, 1.0);
                    put(RatingConfigProp.tacticAOW, 0.85);
                    put(RatingConfigProp.tacticCreative, 0.93);
                    put(RatingConfigProp.defleadPercentDef, 0.0);
                    put(RatingConfigProp.pullback, 0.125);
                }});
                put(RatingConfigSection.goalkeeping_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.GK_norm, GK100 * 0.87);
                }});
                put(RatingConfigSection.defending_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.GK_norm, GK100 * 0.35);
                    put(RatingConfigProp.CD_norm, DEF100 * 1.00);
                    put(RatingConfigProp.CD_off, DEF100 * 0.73);
                    put(RatingConfigProp.CD_tw, DEF100 * 0.67);
                    put(RatingConfigProp.WB_norm, DEF100 * 0.38);
                    put(RatingConfigProp.WB_off, DEF100 * 0.35);
                    put(RatingConfigProp.WB_tm, DEF100 * 0.70);
                    put(RatingConfigProp.WB_def, DEF100 * 0.43);
                    put(RatingConfigProp.IM_norm, DEF100 * 0.40);
                    put(RatingConfigProp.IM_off, DEF100 * 0.16);
                    put(RatingConfigProp.IM_def, DEF100 * 0.58);
                    put(RatingConfigProp.IM_tw, DEF100 * 0.33);
                    put(RatingConfigProp.WI_norm, DEF100 * 0.20);
                    put(RatingConfigProp.WI_off, DEF100 * 0.13);
                    put(RatingConfigProp.WI_def, DEF100 * 0.25);
                    put(RatingConfigProp.WI_tm, DEF100 * 0.25);
                }});
            }});
            put(RatingConfigGroup.MIDFIELD, new LinkedHashMap<RatingConfigSection, Map<RatingConfigProp, Double>>() {{
                put(RatingConfigSection.general, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.delta, 0.75);
                    put(RatingConfigProp.power, 1.196);
                    put(RatingConfigProp.squareMod, 0.0);
                    put(RatingConfigProp.cubeMod, 0.0);
                    put(RatingConfigProp.home, 1.20);
                    put(RatingConfigProp.awayDerby, 1.10);
                    put(RatingConfigProp.away, 1.00);
                    put(RatingConfigProp.pic, 0.84);
                    put(RatingConfigProp.mots, 83.0 / 75.0);
                    put(RatingConfigProp.teamSpiritPreMulti, 0.147832);
                    put(RatingConfigProp.teamSpiritPower, 0.417779);
                    put(RatingConfigProp.extraMulti, 1.000);
                    put(RatingConfigProp.tacticCounter, 0.930000);
                    put(RatingConfigProp.tacticLongShots, 0.96);
                }});
                put(RatingConfigSection.playmaking_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.CD_norm, MID100 * 0.25);
                    put(RatingConfigProp.CD_off, MID100 * 0.40);
                    put(RatingConfigProp.CD_tw, MID100 * 0.15);
                    put(RatingConfigProp.WB_norm, MID100 * 0.15);
                    put(RatingConfigProp.WB_off, MID100 * 0.2);
                    put(RatingConfigProp.WB_def, MID100 * 0.1);
                    put(RatingConfigProp.WB_tm, MID100 * 0.2);
                    put(RatingConfigProp.IM_norm, MID100 * 1.0);
                    put(RatingConfigProp.IM_off, MID100 * 0.95);
                    put(RatingConfigProp.IM_def, MID100 * 0.95);
                    put(RatingConfigProp.IM_tw, MID100 * 0.9);
                    put(RatingConfigProp.WI_norm, MID100 * 0.45);
                    put(RatingConfigProp.WI_off, MID100 * 0.3);
                    put(RatingConfigProp.WI_def, MID100 * 0.3);
                    put(RatingConfigProp.WI_tm, MID100 * 0.55);
                    put(RatingConfigProp.FW_norm, MID100 * 0.25);
                    put(RatingConfigProp.FW_def, MID100 * 0.35);
                    put(RatingConfigProp.FW_tw, MID100 * 0.15);
                }});
            }});
            put(RatingConfigGroup.SIDEATTACK, new LinkedHashMap<RatingConfigSection, Map<RatingConfigProp, Double>>() {{
                put(RatingConfigSection.general, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.delta, 0.75);
                    put(RatingConfigProp.power, 1.196);
                    put(RatingConfigProp.trainerOff, 1.074);
                    put(RatingConfigProp.trainerDef, 0.882);
                    put(RatingConfigProp.trainerNeutral, 1.0);
                    put(RatingConfigProp.confidence, 0.05);
                    put(RatingConfigProp.tacticLongShots, 0.96);
                    put(RatingConfigProp.pullback, -0.25);
                }});
                put(RatingConfigSection.passing_middle, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.IM_norm, SIDEPS100 * 0.13);
                    put(RatingConfigProp.IM_off, SIDEPS100 * 0.18);
                    put(RatingConfigProp.IM_def, SIDEPS100 * 0.07);
                }});
                put(RatingConfigSection.passing_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.FW_norm, SIDEPS100 * 0.14);
                    put(RatingConfigProp.FW_def, SIDEPS100 * 0.31);
                    put(RatingConfigProp.FW_def_tech, SIDEPS100 * 0.41);
                }});
                put(RatingConfigSection.passing_thisside, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.IM_norm, SIDEPS100 * 0.26);
                    put(RatingConfigProp.IM_off, SIDEPS100 * 0.36);
                    put(RatingConfigProp.IM_def, SIDEPS100 * 0.14);
                    put(RatingConfigProp.IM_tw, SIDEPS100 * 0.31);
                    put(RatingConfigProp.WI_norm, SIDEPS100 * 0.26);
                    put(RatingConfigProp.WI_off, SIDEPS100 * 0.29);
                    put(RatingConfigProp.WI_def, SIDEPS100 * 0.21);
                    put(RatingConfigProp.WI_tm, SIDEPS100 * 0.15);
                    put(RatingConfigProp.FW_tw, SIDEPS100 * 0.21);
                }});
                put(RatingConfigSection.passing_otherside, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.FW_tw, SIDEPS100 * 0.06);
                }});
                put(RatingConfigSection.winger_thisside, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.CD_tw, WI100 * 0.26);
                    put(RatingConfigProp.WB_norm, WI100 * 0.59);
                    put(RatingConfigProp.WB_off, WI100 * 0.69);
                    put(RatingConfigProp.WB_tm, WI100 * 0.35);
                    put(RatingConfigProp.WB_def, WI100 * 0.45);
                    put(RatingConfigProp.IM_tw, WI100 * 0.59);
                    put(RatingConfigProp.WI_norm, WI100 * 0.86);
                    put(RatingConfigProp.WI_off, WI100 * 1.0);
                    put(RatingConfigProp.WI_def, WI100 * 0.69);
                    put(RatingConfigProp.WI_tm, WI100 * 0.74);
                    put(RatingConfigProp.FW_tw, WI100 * 0.64);
                }});
                put(RatingConfigSection.winger_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.FW_norm, WI100 * 0.24);
                    put(RatingConfigProp.FW_def, WI100 * 0.13);
                    put(RatingConfigProp.FW_def_tech, WI100 * 0.13);
                }});
                put(RatingConfigSection.winger_otherside, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.FW_tw, WI100 * 0.21);
                }});
                put(RatingConfigSection.scoring_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.FW_norm, SIDEFW100 * 0.27);
                    put(RatingConfigProp.FW_def, SIDEFW100 * 0.13);
                    put(RatingConfigProp.FW_def_tech, SIDEFW100 * 0.13);
                }});
                put(RatingConfigSection.scoring_otherside, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.FW_tw, SIDEFW100 * 0.19);
                }});
                put(RatingConfigSection.scoring_thisside, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.FW_tw, SIDEFW100 * 0.51);
                }});
            }});
            put(RatingConfigGroup.CENTRALATTACK, new LinkedHashMap<RatingConfigSection, Map<RatingConfigProp, Double>>() {{
                put(RatingConfigSection.general, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.delta, 0.75);
                    put(RatingConfigProp.power, 1.196);
                    put(RatingConfigProp.trainerOff, 1.074);
                    put(RatingConfigProp.trainerDef, 0.882);
                    put(RatingConfigProp.confidence, 0.05);
                    put(RatingConfigProp.trainerNeutral, 1.0);
                    put(RatingConfigProp.tacticLongShots, 0.96);
                    put(RatingConfigProp.pullback, -0.25);
                }});
                put(RatingConfigSection.passing_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.IM_norm, PS100 * 0.33);
                    put(RatingConfigProp.IM_off, PS100 * 0.49);
                    put(RatingConfigProp.IM_def, PS100 * 0.18);
                    put(RatingConfigProp.IM_tw, PS100 * 0.23);
                    put(RatingConfigProp.WI_norm, PS100 * 0.11);
                    put(RatingConfigProp.WI_off, PS100 * 0.13);
                    put(RatingConfigProp.WI_def, PS100 * 0.05);
                    put(RatingConfigProp.WI_tm, PS100 * 0.16);
                    put(RatingConfigProp.FW_norm, PS100 * 0.33);
                    put(RatingConfigProp.FW_def, PS100 * 0.53);
                    put(RatingConfigProp.FW_def_tech, PS100 * 0.53);
                    put(RatingConfigProp.FW_tw, PS100 * 0.23);
                }});
                put(RatingConfigSection.scoring_allsides, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.IM_norm, FW100 * 0.22);
                    put(RatingConfigProp.IM_off, FW100 * 0.31);
                    put(RatingConfigProp.IM_def, FW100 * 0.13);
                    put(RatingConfigProp.FW_norm, FW100 * 1.0);
                    put(RatingConfigProp.FW_def, FW100 * 0.56);
                    put(RatingConfigProp.FW_def_tech, FW100 * 0.56);
                    put(RatingConfigProp.FW_tw, FW100 * 0.66);
                }});
            }});
            put(RatingConfigGroup.PLAYERSTRENGTH, new LinkedHashMap<RatingConfigSection, Map<RatingConfigProp, Double>>() {{
                put(RatingConfigSection.general, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.formMin, 1.0);
                    put(RatingConfigProp.formMax, 8.0);
                    put(RatingConfigProp.formDelta, 0.0);
                    put(RatingConfigProp.formMultiplier, 0.125);
                    put(RatingConfigProp.formPower, 0.6666666666666666);
                    put(RatingConfigProp.resultMultiForm, 1.0);
                    put(RatingConfigProp.multiXpLog10, 1.33333333333333333333333333333333333333333333333333333333333);
                    put(RatingConfigProp.resultAddXp, 1.0);
                    put(RatingConfigProp.skillDelta, 0.0);
                    put(RatingConfigProp.skillMin, 0.0);
                    put(RatingConfigProp.weatherBonus, 0.05);
                    put(RatingConfigProp.twoCdMulti, 0.95);
                    put(RatingConfigProp.threeCdMulti, 0.90);
                    put(RatingConfigProp.twoMfMulti, 0.9);
                    put(RatingConfigProp.threeMfMulti, 0.8);
                    put(RatingConfigProp.twoFwMulti, 0.94);
                    put(RatingConfigProp.threeFwMulti, 0.865);
                }});
            }});
            put(RatingConfigGroup.TACTICS, new LinkedHashMap<RatingConfigSection, Map<RatingConfigProp, Double>>() {{
                put(RatingConfigSection.general, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.delta, 0.000000);
                    put(RatingConfigProp.extraMulti, 1.000000);
                    put(RatingConfigProp.squareMod, 0.000000);
                    put(RatingConfigProp.cubeMod, 0.000000);
                }});
                put(RatingConfigSection.aim_aow, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.postMulti, 0.194912);
                    put(RatingConfigProp.squareMod, 0.009067);
                    put(RatingConfigProp.cubeMod, -0.000351);
                }});
                put(RatingConfigSection.counter, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.multiPs, 0.23);
                    put(RatingConfigProp.multiDe, 0.115);
                }});
                put(RatingConfigSection.pressing, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.multiDe, 0.085);
                    put(RatingConfigProp.postDelta, 0.075);
                }});
                put(RatingConfigSection.longshots, new LinkedHashMap<RatingConfigProp, Double>() {{
                    put(RatingConfigProp.multiSc, 0.166666666666667);
                    put(RatingConfigProp.multiSp, 0.0555555555555556);
                    put(RatingConfigProp.postDelta, -7.6);
                }});
            }});
        }};

}
