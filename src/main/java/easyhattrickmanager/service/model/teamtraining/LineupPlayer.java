package easyhattrickmanager.service.model.teamtraining;

import com.fasterxml.jackson.annotation.JsonIgnore;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LineupPlayer {

    PlayerInfo player;
    Role role;
    Behaviour behaviour;

    @JsonIgnore
    public RatingConfigProp getRatingConfigProp() {
        return RatingConfigProp.valueOf(Stream.of(
                this.role.getRoleGroup().getAbr(),
                this.behaviour.getAbr(),
                this.role.getRoleGroup() == RoleGroup.FORWARD && this.player.getSpecialty() == Specialty.TECHNICAL.getValue() ? "tech" : null)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("_")));
    }

}
