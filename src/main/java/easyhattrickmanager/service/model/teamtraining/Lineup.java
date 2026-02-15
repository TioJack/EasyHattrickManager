package easyhattrickmanager.service.model.teamtraining;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Value
@Jacksonized
public class Lineup implements Serializable {

    private static final long serialVersionUID = 5618198214082859961L;

    List<LineupPlayer> fieldPlayers;
    List<LineupPlayer> benchPlayers;
    //List<Substitution> Substitutions;
    List<LineupPlayer> penaltyTakers;
    MatchDetail matchDetail;

    @JsonIgnore
    public int getNumberCentralDefender() {
        return (int) this.fieldPlayers.stream()
            .filter(lineupPlayer -> lineupPlayer.getRole().getRoleGroup() == RoleGroup.CENTRAL_DEFENDER)
            .count();
    }

    @JsonIgnore
    public int getNumberInnerMidfield() {
        return (int) this.fieldPlayers.stream()
            .filter(lineupPlayer -> lineupPlayer.getRole().getRoleGroup() == RoleGroup.INNER_MIDFIELD)
            .count();
    }

    @JsonIgnore
    public int getNumberForward() {
        return (int) this.fieldPlayers.stream()
            .filter(lineupPlayer -> lineupPlayer.getRole().getRoleGroup() == RoleGroup.FORWARD)
            .count();
    }

}
