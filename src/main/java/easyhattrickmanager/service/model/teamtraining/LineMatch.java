package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum LineMatch {
    DEFENSE(0.88, 1.128),
    MIDFIELD(1.0, 1.0),
    ATTACK(1.074, 0.882);

    private final double trainerOffensiveFactor;
    private final double trainerDefensiveFactor;
}
