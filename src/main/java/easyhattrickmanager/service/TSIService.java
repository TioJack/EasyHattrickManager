package easyhattrickmanager.service;

import static java.util.stream.DoubleStream.of;

import org.springframework.stereotype.Service;

@Service
public class TSIService {

    public double calculateTSI(
        double form,
        double stamina,
        double keeper,
        double defender,
        double playmaker,
        double winger,
        double passing,
        double scorer
    ) {
        double maxOutfield = of(defender, playmaker, winger, passing, scorer).max().orElseThrow();

        if (keeper > maxOutfield) {
            double gk = Math.max(0.0, keeper - 1.0);
            double fm = Math.max(0.0, form - 1.0);
            return 3.0 * Math.pow(gk, 3.359) * Math.pow(fm, 0.5);
        }

        double d = Math.max(0.0, defender - 1.0);
        double pm = Math.max(0.0, playmaker - 1.0);
        double wg = Math.max(0.0, winger - 1.0);
        double ps = Math.max(0.0, passing - 1.0);
        double sc = Math.max(0.0, scorer - 1.0);
        double st = Math.max(0.0, stamina - 1.0);
        double fm = Math.max(0.0, form - 1.0);

        double core = 1.03 * Math.pow(d, 3) + 1.03 * Math.pow(pm, 3) + 1.03 * Math.pow(sc, 3) + 1.0 * Math.pow(ps, 3) + 0.84 * Math.pow(wg, 3);
        double base = Math.pow(core, 2);
        double staminaFactor = Math.pow(st, 0.5);
        double formFactor = Math.pow(fm, 0.5);
        return base * staminaFactor * formFactor / 1000.0;
    }
}
