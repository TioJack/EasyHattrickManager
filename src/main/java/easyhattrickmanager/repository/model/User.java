package easyhattrickmanager.repository.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    int id;
    String name;
    int languageId;
    LocalDateTime activationDate;
    String token;
    String tokenSecret;
    boolean active;
}
