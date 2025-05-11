package easyhattrickmanager.repository.model;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    int id;
    String name;
    int languageId;
    ZonedDateTime activationDate;
    String token;
    String tokenSecret;
    boolean active;
}
