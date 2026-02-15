package easyhattrickmanager.service.model.teamtraining;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FormationRating {

    String formation;
    List<PlayerRating> players;
    Ratings rating;

}
