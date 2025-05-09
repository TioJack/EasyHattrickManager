package easyhattrickmanager.repository.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Player {

    int id;
    String firstName;
    String lastName;
    int agreeability;
    int aggressiveness;
    int honesty;
    int specialty;
    int countryId;
}
