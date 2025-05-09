package easyhattrickmanager.repository.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEhm {

    int id;
    String username;
    String password;
    LocalDateTime createdAt;
}
