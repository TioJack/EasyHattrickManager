package easyhattrickmanager.service.model.teamtraining;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamTrainingRequest {

    WeekInfo iniWeek;
    List<TeamTrainingPlayer> players;
    List<TrainingStage> stages;
    List<StagePlayerParticipation> participations;

    BestFormationCriteria bestFormationCriteria;
    String fixedFormationCode;
    MatchDetail matchDetail;
    Boolean calculateBestFormation;
}
