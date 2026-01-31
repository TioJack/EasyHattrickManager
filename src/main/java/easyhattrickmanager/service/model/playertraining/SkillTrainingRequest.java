package easyhattrickmanager.service.model.playertraining;

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
    double coefficientSkill;
    int minutes;
}
