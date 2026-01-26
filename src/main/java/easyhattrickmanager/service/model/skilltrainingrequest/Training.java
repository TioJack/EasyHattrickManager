package easyhattrickmanager.service.model.skilltrainingrequest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Training {
    NO_TRAINING(0, 0.0, null),
    GOALKEEPING(9, 0.0510, Skill.GOALKEEPING),
    DEFENDING(3, 0.0288, Skill.DEFENDING),
    PLAY_MAKING(8, 0.0336, Skill.PLAY_MAKING),
    WINGER(5, 0.0480, Skill.WINGER),
    PASSING(7, 0.0360, Skill.PASSING),
    SCORING(4, 0.0324, Skill.SCORING),
    SET_PIECES(2, 0.1470, Skill.SET_PIECES),
    SET_PIECES_PLUS(2, 0.18375, Skill.SET_PIECES);

    private final int value;
    private final double coefficient;
    private final Skill skill;
}
