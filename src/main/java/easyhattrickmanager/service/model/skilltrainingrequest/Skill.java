package easyhattrickmanager.service.model.skilltrainingrequest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Skill {
    GOALKEEPING(0, 29),
    DEFENDING(1, 28),
    WINGER(2, 27),
    PLAY_MAKING(3, 27),
    SCORING(4, 26),
    PASSING(5, 27),
    SET_PIECES(8, 30);

    private final int value;
    private final int ageNoDrop;
}
