package easyhattrickmanager.service.model.playertraining;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillCoefficient {

    Skill skill;
    double coefficient;
}
