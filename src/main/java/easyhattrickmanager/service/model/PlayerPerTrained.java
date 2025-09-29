package easyhattrickmanager.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerPerTrained {

    int playerId;
    PercentageBySkill perTrained;
}
