package easyhattrickmanager.service;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class WageService {

    private static final double BASE_SALARY = 2500.0;
    private static final double DISCOUNT_RATE = 0.5;
    private static final double DISCOUNT_RATE_HIGH = 0.4;
    private static final double PRIMARY_THRESHOLD = 20300.0;

    private static final double[] KEEPER_FORMULA = {250, 270, 350, 450, 610, 830, 1150, 1590, 2250, 3170, 4530, 6450, 9150, 12910, 18050, 24150, 31480, 40930, 52990, 68210, 87280};

    // a, b, d per skill
    private static final double[][] FORMULAE = {
        // Defender
        {0.0007145560, 6.4607813171, 0.7921},
        // Playmaker
        {0.0009418058, 6.4407950328, 0.7832},
        // Passing
        {0.0004476257, 6.5136791026, 0.7707},
        // Winger
        {0.0004437607, 6.4641257225, 0.7789},
        // Scorer
        {0.0009136982, 6.4090063683, 0.7984}
    };

    public double calculateWage(PlayerInfo playerInfo) {
        return calculateWage(
            playerInfo.getAge(),
            playerInfo.isAbroad(),
            playerInfo.getSpecialty(),
            playerInfo.getKeeperSkill() + playerInfo.getPlayerSubSkill().getKeeper(),
            playerInfo.getDefenderSkill() + playerInfo.getPlayerSubSkill().getDefender(),
            playerInfo.getPlaymakerSkill() + playerInfo.getPlayerSubSkill().getPlaymaker(),
            playerInfo.getPassingSkill() + playerInfo.getPlayerSubSkill().getPassing(),
            playerInfo.getWingerSkill() + playerInfo.getPlayerSubSkill().getWinger(),
            playerInfo.getScorerSkill() + playerInfo.getPlayerSubSkill().getScorer(),
            playerInfo.getSetPiecesSkill() + playerInfo.getPlayerSubSkill().getSetPieces()
        );
    }

    public double calculateWage(
        int age,
        boolean abroad,
        int specialty,
        double keeper,
        double defender,
        double playmaker,
        double winger,
        double passing,
        double scorer,
        double setPieces
    ) {
        double rate = getRate(age);
        double abroadMultiplier = abroad ? 1.2 : 1.0;
        double specialtyMultiplier = specialty > 0 ? 1.1 : 1.0;
        double totalMultiplier = abroadMultiplier * specialtyMultiplier;

        double spMultiplier = 1 + setPieces * 0.0025;

        double keeperValue = keeperComponent(keeper, rate, totalMultiplier);
        double defenderValue = skillComponent(defender, FORMULAE[0], rate, totalMultiplier);
        double playmakerValue = skillComponent(playmaker, FORMULAE[1], rate, totalMultiplier);
        double passingValue = skillComponent(passing, FORMULAE[2], rate, totalMultiplier);
        double wingerValue = skillComponent(winger, FORMULAE[3], rate, totalMultiplier);
        double scorerValue = skillComponent(scorer, FORMULAE[4], rate, totalMultiplier);

        double[] all = {keeperValue, defenderValue, playmakerValue, passingValue, wingerValue, scorerValue};
        int primary = 0;
        double maxValue = all[0];
        for (int i = 1; i < all.length; i++) {
            if (all[i] > maxValue) {
                maxValue = all[i];
                primary = i;
            }
        }

        double secondaryDiscount = maxValue > PRIMARY_THRESHOLD ? DISCOUNT_RATE_HIGH : DISCOUNT_RATE;
        for (int i = 0; i < all.length; i++) {
            if (i == primary) {
                continue;
            }
            all[i] *= secondaryDiscount;
        }

        double sum = 0;
        for (double value : all) {
            sum += value;
        }

        double baseSalary = BASE_SALARY * totalMultiplier;

        return roundUpTo10(baseSalary + sum * spMultiplier);
    }

    private double getRate(int age) {
        int cappedAge = Math.min(age, 37);
        if (cappedAge >= 29) {
            return 1.0 - (cappedAge - 28) / 10.0;
        }
        return 1.0;
    }

    private double keeperComponent(double level, double rate, double totalMultiplier) {
        if (level < 1) {
            return 0;
        }
        int idxLow = Math.max(0, (int) Math.floor(level) - 1);
        int idxHigh = Math.min(idxLow + 1, KEEPER_FORMULA.length - 1);
        double fraction = level - Math.floor(level);
        double base = KEEPER_FORMULA[idxLow] + (KEEPER_FORMULA[idxHigh] - KEEPER_FORMULA[idxLow]) * fraction;
        double value = base * 10.0 - 2500.0;
        return value * rate * totalMultiplier;
    }

    private double skillComponent(double level, double[] f, double rate, double totalMultiplier) {
        if (level < 1) {
            return 0;
        }
        double a = f[0];
        double b = f[1];
        double d = f[2];
        double value = a * Math.pow(level - 1, b);
        if (value > 20000) {
            value = 20000 + (value - 20000) * d;
        }
        value *= 10.0;
        return value * rate * totalMultiplier;
    }

    private double roundUpTo10(double value) {
        return new BigDecimal(value).setScale(-1, RoundingMode.CEILING).doubleValue();
    }
}
