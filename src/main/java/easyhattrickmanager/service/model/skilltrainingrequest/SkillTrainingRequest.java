package easyhattrickmanager.service.model.skilltrainingrequest;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillTrainingRequest {

    double skill;
    int age;
    int coach;
    int assistants;
    int intensity;
    int stamina;
    Training training;
    int minutes;
}
