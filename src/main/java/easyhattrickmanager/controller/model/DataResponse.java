package easyhattrickmanager.controller.model;

import easyhattrickmanager.service.model.dataresponse.TeamExtendedInfo;
import easyhattrickmanager.service.model.dataresponse.UserInfo;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataResponse {

    String version;
    UserInfo user;
    List<TeamExtendedInfo> teams;
}
