package easyhattrickmanager.repository.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerExp {

    int id;
    String seasonWeek;
    int teamId;
    double exp;
}
