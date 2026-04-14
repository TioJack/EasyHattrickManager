package easyhattrickmanager.service.model.teamtrainingfollowup;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.teamtraining.WeekInfo;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamTrainingFollowUpResponse {

    WeekInfo iniWeek;
    WeekInfo actualWeek;
    WeekInfo endWeek;
    List<PlayerInfo> initialPlayers;

    Map<Integer, WeekInfo> weekInfoPlanned;
    Map<Integer, WeekInfo> weekInfo;
    Map<Integer, WeekTraining> weekTrainingPlanned;
    Map<Integer, WeekTraining> weekTraining;

    Map<Integer, List<PlayerInfo>> weekPlayersPlanned;
    Map<Integer, List<PlayerInfo>> weekPlayers;
    Map<Integer, List<PlayerInfo>> weekPlayersPlannedFromActual;
    Map<Integer, Map<Integer, Integer>> weekParticipationPlanned;
    Map<Integer, Map<Integer, Double>> weekParticipationExpected;
    Map<Integer, Map<Integer, String>> weekPlayerIndicators;
}
