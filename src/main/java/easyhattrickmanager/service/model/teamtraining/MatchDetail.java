package easyhattrickmanager.service.model.teamtraining;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchDetail implements Serializable {

    Tactic tactic;
    TeamAttitude teamAttitude;
    TeamSpirit teamSpirit;
    double teamSubSpirit;
    TeamConfidence teamConfidence;
    double teamSubConfidence;
    SideMatch sideMatch;
    int styleOfPlay;
}
