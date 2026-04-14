package easyhattrickmanager.service.model.teamtrainingfollowup;

import easyhattrickmanager.service.model.dataresponse.StaffInfo;
import easyhattrickmanager.service.model.dataresponse.TrainingInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeekTraining {

    TrainingInfo training;
    StaffInfo staff;
}
