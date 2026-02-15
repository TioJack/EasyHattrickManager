package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TeamSpirit {
    LIKE_THE_COLD_WAR(0),
    MURDEROUS(1),
    FURIOUS(2),
    IRRITATED(3),
    COMPOSED(4),
    CALM(5),
    CONTENT(6),
    SATISFIED(7),
    DELIRIOUS(8),
    WALKING_ON_CLOUDS(9),
    PARADISE_ON_EARTH(10);

    private final int value;
}
