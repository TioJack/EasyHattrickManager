package easyhattrickmanager.service.model.teamtraining;

import easyhattrickmanager.service.model.playertraining.Skill;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RatingConfigSection {
    general(null, null),
    goalkeeping_allsides(Skill.GOALKEEPING, RatingZone.ALLSIDES),
    defending_allsides(Skill.DEFENDING, RatingZone.ALLSIDES),
    defending_thisside(Skill.DEFENDING, RatingZone.THISSIDE),
    defending_middle(Skill.DEFENDING, RatingZone.MIDDLE),
    playmaking_allsides(Skill.PLAY_MAKING, RatingZone.ALLSIDES),
    passing_middle(Skill.PASSING, RatingZone.MIDDLE),
    passing_allsides(Skill.PASSING, RatingZone.ALLSIDES),
    passing_thisside(Skill.PASSING, RatingZone.THISSIDE),
    passing_otherside(Skill.PASSING, RatingZone.OTHERSIDE),
    winger_thisside(Skill.WINGER, RatingZone.THISSIDE),
    winger_allsides(Skill.WINGER, RatingZone.ALLSIDES),
    winger_otherside(Skill.WINGER, RatingZone.OTHERSIDE),
    scoring_allsides(Skill.SCORING, RatingZone.ALLSIDES),
    scoring_otherside(Skill.SCORING, RatingZone.OTHERSIDE),
    scoring_thisside(Skill.SCORING, RatingZone.THISSIDE),
    aim_aow(null, null),
    counter(null, null),
    pressing(null, null),
    longshots(null, null);

    private final Skill skill;
    private final RatingZone zone;
}
