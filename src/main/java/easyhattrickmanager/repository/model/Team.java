package easyhattrickmanager.repository.model;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Team {

    int userId;
    int id;
    String name;
    boolean primaryClub;
    ZonedDateTime foundedDate;
    ZonedDateTime closureDate;
    int leagueId;
    int countryId;
    boolean bot;
}
