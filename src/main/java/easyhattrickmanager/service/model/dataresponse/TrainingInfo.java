package easyhattrickmanager.service.model.dataresponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingInfo {

    int trainingType;
    int trainingLevel;
    int staminaTrainingPart;
}
