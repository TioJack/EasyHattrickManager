package easyhattrickmanager.service.model;

import easyhattrickmanager.repository.model.Trainer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainerGap {

    int teamId;
    String seasonWeekStart;
    String seasonWeekEnd;
    int missingWeeks;
    Trainer trainerStart;
    Trainer trainerEnd;

}
