package easyhattrickmanager.repository.model;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEhmHistory {

    int id;
    int userEhmId;
    ZonedDateTime connectionTime;
    String ipAddress;
    String userAgent;

}
