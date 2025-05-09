package easyhattrickmanager.repository.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Training {

    String seasonWeek;
    LocalDateTime date;
    int teamId;
    int trainingType;
    int trainingLevel;
    int staminaTrainingPart;
}
