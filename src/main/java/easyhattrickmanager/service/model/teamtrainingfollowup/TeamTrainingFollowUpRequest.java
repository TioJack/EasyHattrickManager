package easyhattrickmanager.service.model.teamtrainingfollowup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import easyhattrickmanager.controller.model.DataResponse;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingRequest;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamTrainingFollowUpRequest {

    Integer teamId;
    DataResponse dataResponse;
    TeamTrainingRequest teamTrainingRequest;
    TeamTrainingResponse teamTrainingResponse;
}
