package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Specialty {
    NONE(0),
    TECHNICAL(1),
    QUICK(2),
    POWERFUL(3),
    UNPREDICTABLE(4),
    HEAD_SPECIALIST(5),
    RESILIENT(6),
    SUPPORT(8);

    private final int value;
}
