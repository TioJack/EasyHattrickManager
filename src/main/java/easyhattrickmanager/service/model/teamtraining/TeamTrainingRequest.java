package easyhattrickmanager.service.model.teamtraining;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamTrainingRequest {

    WeekInfo iniWeek;
    List<TeamTrainingPlayer> players;
    List<TrainingStage> stages;
    List<StagePlayerParticipation> participations;

}
