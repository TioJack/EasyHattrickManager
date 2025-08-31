package easyhattrickmanager.repository.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Training {

    String seasonWeek;
    int teamId;
    int trainingType;
    int trainingLevel;
    int staminaTrainingPart;
}
