package easyhattrickmanager.service.model.teamtraining;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamTrainingResponse {

    WeekInfo endWeek;
    // <week,players>
    Map<Integer, List<PlayerInfo>> weekPlayers;
    // <week,FormationRating>
    Map<Integer, FormationRating> weekFormationRatings;

    FormationRating bestFormationRating;
    int bestWeek;
}
