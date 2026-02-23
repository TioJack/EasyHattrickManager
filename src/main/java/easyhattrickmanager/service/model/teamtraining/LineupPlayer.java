package easyhattrickmanager.service.model.teamtraining;

import com.fasterxml.jackson.annotation.JsonIgnore;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
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
        final String base = this.role.getRoleGroup().getAbr() + "_" + this.behaviour.getAbr();
        if (this.player.getSpecialty() == Specialty.TECHNICAL.getValue() && "FW_def".equals(base)) {
            return RatingConfigProp.FW_def_tech;
        }
        return RatingConfigProp.valueOf(base);
    }

}
