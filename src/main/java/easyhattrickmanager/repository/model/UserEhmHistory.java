package easyhattrickmanager.repository.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEhmHistory {

    int id;
    int userEhmId;
    LocalDateTime connectionTime;
    String ipAddress;
    String userAgent;

}
