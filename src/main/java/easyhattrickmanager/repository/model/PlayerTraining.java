package easyhattrickmanager.repository.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerTraining {

    int id;
    String seasonWeek;
    int teamId;
    double keeper;
    double defender;
    double playmaker;
    double winger;
    double passing;
    double scorer;
    double setPieces;
    int minutes;
}
