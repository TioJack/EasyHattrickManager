package easyhattrickmanager.service.model.dataresponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerTrainingInfo {

    double keeper;
    double defender;
    double playmaker;
    double winger;
    double passing;
    double scorer;
    double setPieces;
}
