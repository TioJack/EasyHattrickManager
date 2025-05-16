package easyhattrickmanager.service.model.dataresponse;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamExtendedInfo {

    TeamInfo team;
    LeagueInfo league;
    List<WeeklyInfo> weeklyData;
}
