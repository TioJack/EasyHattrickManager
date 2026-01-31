package easyhattrickmanager.service.model.playertraining;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerTrainingRequest {

    PlayerInfo player;
    int coach;
    int assistants;
    int intensity;
    int stamina;
    Training training;
    int participation;
}
