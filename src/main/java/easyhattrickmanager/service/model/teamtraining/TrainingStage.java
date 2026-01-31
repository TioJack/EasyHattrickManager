package easyhattrickmanager.service.model.teamtraining;

import easyhattrickmanager.service.model.playertraining.Training;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingStage {

    int id;
    int duration;
    int coach;
    int assistants;
    int intensity;
    int stamina;
    Training training;

}
