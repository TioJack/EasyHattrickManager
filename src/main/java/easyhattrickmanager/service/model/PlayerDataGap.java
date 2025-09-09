package easyhattrickmanager.service.model;

import easyhattrickmanager.repository.model.PlayerData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerDataGap {

    int teamId;
    int playerId;
    String seasonWeekStart;
    String seasonWeekEnd;
    int missingWeeks;
    PlayerData playerDataStart;
    PlayerData playerDataEnd;

}
