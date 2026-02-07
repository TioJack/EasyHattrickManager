package easyhattrickmanager.service;

import static java.util.stream.DoubleStream.of;

import org.springframework.stereotype.Service;

@Service
public class TSIFormService {

    private static final double KEEPER_FACTOR = 3.359;
    private static final double DEFENSE_FACTOR = 1.034;
    private static final double PLAYMAKER_FACTOR = 1.031;
    private static final double SCORER_FACTOR = 1.038;
    private static final double PASSING_FACTOR = 1.035;
    private static final double WINGER_FACTOR = 0.826;
    private static final double STAMINA_FACTOR = 0.5;
    private static final double FORM_FACTOR = 0.5;
    private static final int FIELD_AGE_FULL_TSI_MAX = 27;
    private static final double FIELD_AGE_MIN_TSI_FACTOR = 0.125;
    private static final int KEEPER_AGE_FULL_TSI_MAX = 30;
    private static final double KEEPER_AGE_MIN_TSI_FACTOR = 0.2;
    private static final double KEEPER_LOW_SKILL_BASELINE = 8.0;
    private static final double KEEPER_LOW_SKILL_STEP = 0.3;

    public double calculateTSI(
        int age,
        double form,
        double stamina,
        double keeper,
        double defender,
        double playmaker,
        double winger,
        double passing,
        double scorer
    ) {
        double maxSkill = of(defender, playmaker, winger, passing, scorer).max().orElseThrow();

        if (keeper > maxSkill) {
            double gk = Math.max(0.0, keeper - 1.0);
            double fm = Math.max(0.0, form - 1.0);
            double keeperLowSkillAdjustment = getKeeperLowSkillAdjustment(keeper);
            return 3.0 * Math.pow(gk, KEEPER_FACTOR) * Math.pow(fm, 0.5) * getKeeperAgeTSIFactor(age) * keeperLowSkillAdjustment;
        }

        double d = Math.max(0.0, defender - 1.0);
        double pm = Math.max(0.0, playmaker - 1.0);
        double wg = Math.max(0.0, winger - 1.0);
        double ps = Math.max(0.0, passing - 1.0);
        double sc = Math.max(0.0, scorer - 1.0);
        double st = Math.max(0.0, stamina - 1.0);
        double fm = Math.max(0.0, form - 1.0);

        double core = DEFENSE_FACTOR * Math.pow(d, 3) + PLAYMAKER_FACTOR * Math.pow(pm, 3) + SCORER_FACTOR * Math.pow(sc, 3) + PASSING_FACTOR * Math.pow(ps, 3) + WINGER_FACTOR * Math.pow(wg, 3);
        double base = Math.pow(core, 2);
        double staminaFactor = Math.pow(st, STAMINA_FACTOR);
        double formFactor = Math.pow(fm, FORM_FACTOR);
        return base * staminaFactor * formFactor * getFieldAgeTSIFactor(age) / 1000.0;
    }

    public double calculateForm(
        int age,
        double TSI,
        double stamina,
        double keeper,
        double defender,
        double playmaker,
        double winger,
        double passing,
        double scorer
    ) {
        if (TSI <= 0.0) {
            return 1.0;
        }

        double maxSkill = of(defender, playmaker, winger, passing, scorer).max().orElseThrow();

        if (keeper > maxSkill) {
            double gk = Math.max(0.0, keeper - 1.0);
            if (gk <= 0.0) {
                return 1.0;
            }

            double keeperLowSkillAdjustment = getKeeperLowSkillAdjustment(keeper);
            double ratio = TSI / (3.0 * Math.pow(gk, KEEPER_FACTOR) * getKeeperAgeTSIFactor(age) * keeperLowSkillAdjustment);
            if (ratio <= 0.0) {
                return 1.0;
            }

            double fm = Math.pow(ratio, 1.0 / FORM_FACTOR);
            return fm + 1.0;
        }

        double d = Math.max(0.0, defender - 1.0);
        double pm = Math.max(0.0, playmaker - 1.0);
        double wg = Math.max(0.0, winger - 1.0);
        double ps = Math.max(0.0, passing - 1.0);
        double sc = Math.max(0.0, scorer - 1.0);
        double st = Math.max(0.0, stamina - 1.0);

        double core = DEFENSE_FACTOR * Math.pow(d, 3) + PLAYMAKER_FACTOR * Math.pow(pm, 3) + SCORER_FACTOR * Math.pow(sc, 3) + PASSING_FACTOR * Math.pow(ps, 3) + WINGER_FACTOR * Math.pow(wg, 3);
        double base = Math.pow(core, 2);
        double staminaFactor = Math.pow(st, STAMINA_FACTOR);
        double denominator = base * staminaFactor * getFieldAgeTSIFactor(age);

        if (denominator <= 0.0) {
            return 1.0;
        }

        double ratio = TSI * 1000.0 / denominator;
        if (ratio <= 0.0) {
            return 1.0;
        }

        double fm = Math.pow(ratio, 1.0 / FORM_FACTOR);
        return fm + 1.0;
    }

    private double getFieldAgeTSIFactor(int age) {
        if (age <= FIELD_AGE_FULL_TSI_MAX) {
            return 1.0;
        }
        return Math.max(FIELD_AGE_MIN_TSI_FACTOR, 1.0 - (age - FIELD_AGE_FULL_TSI_MAX) * 0.125);
    }

    private double getKeeperAgeTSIFactor(int age) {
        if (age <= KEEPER_AGE_FULL_TSI_MAX) {
            return 1.0;
        }
        return Math.max(KEEPER_AGE_MIN_TSI_FACTOR, 1.0 - (age - KEEPER_AGE_FULL_TSI_MAX) * 0.1);
    }

    private double getKeeperLowSkillAdjustment(double keeper) {
        if (keeper >= KEEPER_LOW_SKILL_BASELINE) {
            return 1.0;
        }
        return 1.0 + (KEEPER_LOW_SKILL_BASELINE - keeper) * KEEPER_LOW_SKILL_STEP;
    }

    public double calculateHiddenForm(double form, double formPreviousWeek) {
        return 3 * (form - formPreviousWeek) + formPreviousWeek;
    }

    public double calculateExpectedForm(double form, double hiddenForm) {
        return (hiddenForm - form) / 3 + form;
    }
}
