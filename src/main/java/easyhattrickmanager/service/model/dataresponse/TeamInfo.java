package easyhattrickmanager.service.model.dataresponse;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamInfo {

    int id;
    String name;
    ZonedDateTime foundedDate;
}
