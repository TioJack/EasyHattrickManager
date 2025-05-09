package easyhattrickmanager.repository.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Team {

    int userId;
    int id;
    String name;
    boolean primaryClub;
    LocalDateTime foundedDate;
    LocalDateTime closureDate;
    int leagueId;
    int countryId;
    boolean bot;
}
