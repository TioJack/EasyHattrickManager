package easyhattrickmanager.service.model.teamtraining.v2;

import easyhattrickmanager.service.model.teamtraining.Ratings;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** V2 response for an independently calculated lineup rating. */
@Value
@Builder
@Jacksonized
public class RatingsV2 {

    Ratings ratings;

    public static RatingsV2 empirical(final Ratings ratings) {
        return RatingsV2.builder()
            .ratings(ratings)
            .build();
    }
}
