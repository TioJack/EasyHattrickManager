package easyhattrickmanager.service.model.teamtraining;

import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.tacticAIM;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.tacticAOW;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.tacticCounter;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.tacticCreative;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.tacticLongShots;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.tacticNormal;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.tacticPressing;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Tactic {
    NORMAL(0, tacticNormal),
    PRESSING(1, tacticPressing),
    COUNTER_ATTACKS(2, tacticCounter),
    ATTACK_IN_THE_MIDDLE(3, tacticAIM),
    ATTACK_IN_WINGS(4, tacticAOW),
    PLAY_CREATIVELY(7, tacticCreative),
    LONG_SHOTS(8, tacticLongShots);

    private final int value;
    private final RatingConfigProp prop;
}
