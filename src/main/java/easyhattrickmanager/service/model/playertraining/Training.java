package easyhattrickmanager.service.model.playertraining;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Training {
    SET_PIECES(2, List.of(SkillCoefficient.builder().skill(Skill.SET_PIECES).coefficient(0.1470).build())),
    SET_PIECES_PLUS(2, List.of(SkillCoefficient.builder().skill(Skill.SET_PIECES).coefficient(0.18375).build())),
    DEFENDING(3, List.of(SkillCoefficient.builder().skill(Skill.DEFENDING).coefficient(0.0288).build())),
    SCORING(4, List.of(SkillCoefficient.builder().skill(Skill.SCORING).coefficient(0.0324).build())),
    WINGER(5, List.of(SkillCoefficient.builder().skill(Skill.WINGER).coefficient(0.0480).build())),
    SCORING_SET_PIECES(7, List.of(SkillCoefficient.builder().skill(Skill.SCORING).coefficient(0.0150).build(), SkillCoefficient.builder().skill(Skill.SET_PIECES).coefficient(0.0150).build())),
    PASSING(7, List.of(SkillCoefficient.builder().skill(Skill.PASSING).coefficient(0.0360).build())),
    PLAY_MAKING(8, List.of(SkillCoefficient.builder().skill(Skill.PLAY_MAKING).coefficient(0.0336).build())),
    GOALKEEPING(9, List.of(SkillCoefficient.builder().skill(Skill.GOALKEEPING).coefficient(0.0510).build())),
    PASSING_EXTENSIVE(10, List.of(SkillCoefficient.builder().skill(Skill.PASSING).coefficient(0.0315).build())),
    DEFENDING_EXTENSIVE(11, List.of(SkillCoefficient.builder().skill(Skill.DEFENDING).coefficient(0.0138).build())),
    WINGER_EXTENSIVE(12, List.of(SkillCoefficient.builder().skill(Skill.WINGER).coefficient(0.0312).build()));

    private final int value;
    private final List<SkillCoefficient> skillCoefficients;
}
