package easyhattrickmanager.repository.model;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Trainer {

    String seasonWeek;
    int teamId;

    int id;
    String name;
    int type;
    int leadership;
    int skillLevel;
    int status;
    ZonedDateTime startDate;
    Integer cost;
}