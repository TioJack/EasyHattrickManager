package easyhattrickmanager.repository.model;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Training {

    String seasonWeek;
    ZonedDateTime date;
    int teamId;
    int trainingType;
    int trainingLevel;
    int staminaTrainingPart;
}
