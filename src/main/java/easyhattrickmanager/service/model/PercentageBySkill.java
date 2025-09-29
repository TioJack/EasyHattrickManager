package easyhattrickmanager.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PercentageBySkill {

    double keeper;
    double defender;
    double playmaker;
    double winger;
    double passing;
    double scorer;
    double setPieces;
}
