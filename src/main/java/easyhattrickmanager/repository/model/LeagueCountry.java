package easyhattrickmanager.repository.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeagueCountry {

    int leagueId;
    int countryId;
}