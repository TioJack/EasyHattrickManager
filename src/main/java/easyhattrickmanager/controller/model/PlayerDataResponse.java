package easyhattrickmanager.controller.model;

import easyhattrickmanager.service.model.dataresponse.UserConfig;
import easyhattrickmanager.service.model.playerdataresponse.PlayerWeeklyInfo;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerDataResponse {

    List<PlayerWeeklyInfo> weeklyData;
    UserConfig userConfig;
}
