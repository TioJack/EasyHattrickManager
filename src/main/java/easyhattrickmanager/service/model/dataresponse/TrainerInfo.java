package easyhattrickmanager.service.model.dataresponse;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainerInfo {

    int id;
    String name;
    int type;
    int leadership;
    int skillLevel;
    int status;
    ZonedDateTime startDate;
    int cost;
}