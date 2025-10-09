package easyhattrickmanager.service.model.playerdataresponse;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerWeeklyInfo {

    int season;
    int week;
    ZonedDateTime date;
    PlayerInfo player;
}
