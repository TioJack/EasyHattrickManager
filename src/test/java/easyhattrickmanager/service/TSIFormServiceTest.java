package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TSIFormServiceTest {

    private final TSIFormService tsiFormService = new TSIFormService();

    @Test
    void shouldKeepInverseForFieldPlayerWithoutAgePenalty() {
        int age = 19;
        double stamina = 6.738453968;
        double keeper = 2.0;
        double defender = 6.35;
        double playmaker = 6.099012065;
        double winger = 14.80265245;
        double passing = 5.62;
        double scorer = 6.25;
        double form = 6.999849703;

        double tsi = tsiFormService.calculateTSI(age, form, stamina, keeper, defender, playmaker, winger, passing, scorer);
        double calculatedForm = tsiFormService.calculateForm(age, tsi, stamina, keeper, defender, playmaker, winger, passing, scorer);

        assertEquals(form, calculatedForm, 1e-9);
    }

    @Test
    void shouldKeepInverseForFieldPlayerWithAgePenalty() {
        int age = 30;
        double stamina = 6.738453968;
        double keeper = 2.0;
        double defender = 6.35;
        double playmaker = 6.099012065;
        double winger = 14.80265245;
        double passing = 5.62;
        double scorer = 6.25;
        double form = 6.999849703;

        double tsi = tsiFormService.calculateTSI(age, form, stamina, keeper, defender, playmaker, winger, passing, scorer);
        double calculatedForm = tsiFormService.calculateForm(age, tsi, stamina, keeper, defender, playmaker, winger, passing, scorer);

        assertEquals(form, calculatedForm, 1e-9);
    }

    @Test
    void shouldApplyAgeFactorLikeSpreadsheet() {
        double form = 7.0;
        double stamina = 7.0;
        double keeper = 2.0;
        double defender = 7.0;
        double playmaker = 6.0;
        double winger = 5.0;
        double passing = 6.0;
        double scorer = 7.0;

        double tsiAge27 = tsiFormService.calculateTSI(27, form, stamina, keeper, defender, playmaker, winger, passing, scorer);
        double tsiAge30 = tsiFormService.calculateTSI(30, form, stamina, keeper, defender, playmaker, winger, passing, scorer);
        double tsiAge35 = tsiFormService.calculateTSI(35, form, stamina, keeper, defender, playmaker, winger, passing, scorer);

        assertEquals(tsiAge27 * 0.625, tsiAge30, 1e-9);
        assertEquals(tsiAge27 * 0.125, tsiAge35, 1e-9);
    }

    @Test
    void shouldApplyKeeperAgeFactorLikeSpreadsheet() {
        double form = 7.0;
        double stamina = 7.0;
        double keeper = 16.0;
        double defender = 9.0;
        double playmaker = 9.0;
        double winger = 8.0;
        double passing = 8.0;
        double scorer = 8.0;

        double tsiAge30 = tsiFormService.calculateTSI(30, form, stamina, keeper, defender, playmaker, winger, passing, scorer);
        double tsiAge31 = tsiFormService.calculateTSI(31, form, stamina, keeper, defender, playmaker, winger, passing, scorer);
        double tsiAge35 = tsiFormService.calculateTSI(35, form, stamina, keeper, defender, playmaker, winger, passing, scorer);
        double tsiAge38 = tsiFormService.calculateTSI(38, form, stamina, keeper, defender, playmaker, winger, passing, scorer);
        double tsiAge40 = tsiFormService.calculateTSI(40, form, stamina, keeper, defender, playmaker, winger, passing, scorer);

        assertEquals(tsiAge30 * 0.9, tsiAge31, 1e-9);
        assertEquals(tsiAge30 * 0.5, tsiAge35, 1e-9);
        assertEquals(tsiAge30 * 0.2, tsiAge38, 1e-9);
        assertEquals(tsiAge30 * 0.2, tsiAge40, 1e-9);
    }

    @Test
    void shouldReduceLowKeeperOverestimation() {
        int age = 18;
        double tsi = 2650.0;
        double stamina = 5.927399074372884;
        double keeper = 6.0;
        double defender = 3.0;
        double playmaker = 2.7014732569855346;
        double winger = 3.0;
        double passing = 1.2420970707104552;
        double scorer = 1.0;

        double form = tsiFormService.calculateForm(age, tsi, stamina, keeper, defender, playmaker, winger, passing, scorer);
        double tsiBack = tsiFormService.calculateTSI(age, form, stamina, keeper, defender, playmaker, winger, passing, scorer);

        assertEquals(7.142268315334355, form, 1e-9);
        assertEquals(tsi, tsiBack, 1e-9);
    }
}
