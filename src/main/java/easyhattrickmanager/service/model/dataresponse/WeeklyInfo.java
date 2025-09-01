package easyhattrickmanager.service.model.dataresponse;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeeklyInfo {

    int season;
    int week;
    ZonedDateTime date;
    TrainingInfo training;
    StaffInfo staff;
    List<PlayerInfo> players;
}
