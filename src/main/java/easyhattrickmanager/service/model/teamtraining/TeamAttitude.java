package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TeamAttitude {
    PIC(-1, RatingConfigProp.pic, 0.84),
    PIN(0, RatingConfigProp.pin, 1.0),
    MOTS(1, RatingConfigProp.mots, 83.0 / 75.0);

    private final int value;
    private final RatingConfigProp prop;
    private final double factor;
}
