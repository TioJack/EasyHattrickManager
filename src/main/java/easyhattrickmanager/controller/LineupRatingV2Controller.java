package easyhattrickmanager.controller;

import easyhattrickmanager.service.RatingV2Service;
import easyhattrickmanager.service.model.teamtraining.v2.LineupV2Request;
import easyhattrickmanager.service.model.teamtraining.v2.RatingsV2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Independent endpoint for the data-driven rating model.
 * The legacy /data/lineup/rating endpoint remains untouched.
 */
@RestController
@RequestMapping("lineup/rating/v2")
@RequiredArgsConstructor
public class LineupRatingV2Controller {

    private final RatingV2Service ratingV2Service;

    @PostMapping
    public RatingsV2 getRatingsV2(@RequestBody final LineupV2Request lineup) {
        return this.ratingV2Service.getRatingsV2(lineup);
    }
}
