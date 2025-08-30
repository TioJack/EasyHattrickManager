package easyhattrickmanager.repository.model;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffMember {

    String seasonWeek;
    int teamId;

    int id;
    String name;
    int type;
    int level;
    int hofPlayerId;
    ZonedDateTime startDate;
    Integer cost;
}