package easyhattrickmanager.service.model;

import easyhattrickmanager.repository.model.Training;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingGap {

    int teamId;
    String seasonWeekStart;
    String seasonWeekEnd;
    int missingWeeks;
    Training trainingStart;
    Training trainingEnd;

}
