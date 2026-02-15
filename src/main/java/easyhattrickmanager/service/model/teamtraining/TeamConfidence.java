package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TeamConfidence {
    NON_EXISTENT(0),
    DISASTROUS(1),
    WRETCHED(2),
    POOR(3),
    DECENT(4),
    STRONG(5),
    WONDERFUL(6),
    SLIGHTLY_EXAGGERATED(7),
    EXAGGERATED(8),
    COMPLETELY_EXAGGERATED(9);

    private final int value;
}
