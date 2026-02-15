package easyhattrickmanager.service.model.teamtraining;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ratings implements Serializable {

    double leftDefense;
    double centralDefense;
    double rightDefense;
    double midfield;
    double leftAttack;
    double centralAttack;
    double rightAttack;

    public int getHatStats() {
        return HTfloat2int(this.leftDefense) + HTfloat2int(this.centralDefense) + HTfloat2int(this.rightDefense) + HTfloat2int(this.midfield) * 3 + HTfloat2int(this.leftAttack) + HTfloat2int(this.centralAttack) + HTfloat2int(this.rightAttack);
    }

    public int getDefense() {
        return HTfloat2int(this.leftDefense) + HTfloat2int(this.centralDefense) + HTfloat2int(this.rightDefense);
    }

    public int getMidfield3() {
        return HTfloat2int(this.midfield) * 3;
    }

    public int getAttack() {
        return HTfloat2int(this.leftAttack) + HTfloat2int(this.centralAttack) + HTfloat2int(this.rightAttack);
    }

    private static int HTfloat2int(final double x) {
        if (!Double.isFinite(x)) {
            return 0;
        }
        return (int) (((x - 1.0f) * 4.0f) + 1.0f);
    }

    public double getPeasoStats() {
        return 0.46 * HTfloat2int(this.midfield) +
            0.32 * (0.3 * (HTfloat2int(this.leftAttack) + HTfloat2int(this.rightAttack)) + 0.4 * HTfloat2int(this.centralAttack)) +
            0.22 * (0.3 * (HTfloat2int(this.leftDefense) + HTfloat2int(this.rightDefense)) + 0.4 * HTfloat2int(this.centralDefense));
    }

}
