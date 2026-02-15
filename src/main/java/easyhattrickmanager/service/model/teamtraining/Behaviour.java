package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Behaviour {
    NO_CHANGE(-1, ""),
    NORMAL(0, "norm"),
    OFFENSIVE(1, "off"),
    DEFENSIVE(2, "def"),
    TOWARDS_MIDDLE(3, "tm"),
    TOWARDS_WING(4, "tw");

    private final int value;
    private final String abr;
}
