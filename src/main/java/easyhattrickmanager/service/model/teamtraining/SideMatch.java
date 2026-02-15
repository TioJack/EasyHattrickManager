package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SideMatch {
    HOME(RatingConfigProp.home, 1.20),
    AWAY_DERBY(RatingConfigProp.awayDerby, 1.10),
    AWAY(RatingConfigProp.away, 1.00);

    private final RatingConfigProp prop;
    private final double factor;
}
