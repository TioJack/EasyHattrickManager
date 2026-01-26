package easyhattrickmanager.repository.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerSubSkill {

    int id;
    String seasonWeek;
    int teamId;
    double stamina;
    double keeper;
    double defender;
    double playmaker;
    double winger;
    double passing;
    double scorer;
    double setPieces;
    int htms;
    int htms28;
}
