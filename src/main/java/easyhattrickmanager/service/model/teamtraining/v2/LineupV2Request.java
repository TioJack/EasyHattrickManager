package easyhattrickmanager.service.model.teamtraining.v2;

import easyhattrickmanager.service.model.teamtraining.LineupPlayer;
import easyhattrickmanager.service.model.teamtraining.MatchDetail;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Separate V2 API contract for one fixed starting lineup.
 *
 * V2 deliberately has no bench, substitution, penalty order, or in-match
 * position-change model.
 */
@Value
@Builder
@Jacksonized
public class LineupV2Request {

    List<LineupPlayer> fieldPlayers;
    MatchDetail matchDetail;
}
