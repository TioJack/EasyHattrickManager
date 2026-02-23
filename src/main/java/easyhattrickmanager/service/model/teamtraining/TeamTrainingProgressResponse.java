package easyhattrickmanager.service.model.teamtraining;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamTrainingProgressResponse {

    int totalWeeks;
    int calculatedWeeks;
    double percent;
    boolean inFlight;
    boolean done;
}
