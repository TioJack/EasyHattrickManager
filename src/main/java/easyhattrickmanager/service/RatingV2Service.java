package easyhattrickmanager.service;

import easyhattrickmanager.service.model.teamtraining.v2.LineupV2Request;
import easyhattrickmanager.service.model.teamtraining.v2.RatingsV2;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Isolated home of the formula inferred from the calibration data in sim.
 * It intentionally does not call RatingService or RatingConfig. V2 models a
 * fixed starting eleven only: substitutions and positional swaps are excluded.
 */
@Service
@RequiredArgsConstructor
public class RatingV2Service {

    private final EmpiricalRatingV2Model empiricalRatingV2Model;

    public RatingsV2 getRatingsV2(final LineupV2Request lineup) {
        final EmpiricalRatingV2Model.Estimate estimate = this.empiricalRatingV2Model.estimateKickoff(
            lineup == null ? List.of() : lineup.getFieldPlayers()
        );
        return RatingsV2.empirical(estimate.ratings());
    }
}
