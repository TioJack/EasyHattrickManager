package easyhattrickmanager.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerRole {

    int playerId;
    int roleId;
    int start;
    int end;
    PercentageBySkill perTrainingByRole;
}
