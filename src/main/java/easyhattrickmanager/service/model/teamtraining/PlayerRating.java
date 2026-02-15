package easyhattrickmanager.service.model.teamtraining;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerRating {

    int playerId;
    Position position;
    Ratings rating;

}
