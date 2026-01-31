package easyhattrickmanager.service.model.teamtraining;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeekInfo {

    int season;
    int week;
    ZonedDateTime date;
}
