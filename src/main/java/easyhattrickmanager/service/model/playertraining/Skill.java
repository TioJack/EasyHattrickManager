package easyhattrickmanager.service.model.playertraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Skill {
    GOALKEEPING(29),
    DEFENDING(28),
    WINGER(27),
    PLAY_MAKING(27),
    SCORING(26),
    PASSING(27),
    SET_PIECES(30);

    private final int ageNoDrop;
}
