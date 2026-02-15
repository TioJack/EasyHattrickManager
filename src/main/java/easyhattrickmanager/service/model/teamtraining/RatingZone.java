package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RatingZone {
    THISSIDE(0),
    OTHERSIDE(1),
    ALLSIDES(2),
    MIDDLE(3),
    LEFT(4),
    RIGHT(5);

    private final int value;
}
