package easyhattrickmanager.service.model.dataresponse;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffMemberInfo {

    int id;
    String name;
    int type;
    int level;
    int hofPlayerId;
    ZonedDateTime startDate;
    int cost;
}