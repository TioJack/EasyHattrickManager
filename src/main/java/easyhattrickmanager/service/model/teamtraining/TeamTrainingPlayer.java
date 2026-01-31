package easyhattrickmanager.service.model.teamtraining;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamTrainingPlayer {

    PlayerInfo player;
    int inclusionWeek;

}
