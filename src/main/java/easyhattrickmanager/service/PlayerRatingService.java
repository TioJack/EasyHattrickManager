package easyhattrickmanager.service;

import static easyhattrickmanager.service.RatingService.getSkill;
import static easyhattrickmanager.service.RatingService.getTeamSpiritNormalFactor;
import static easyhattrickmanager.service.model.teamtraining.ZoneMatch.CENTRAL_ATTACK;
import static easyhattrickmanager.service.model.teamtraining.ZoneMatch.CENTRAL_DEFENSE;
import static easyhattrickmanager.service.model.teamtraining.ZoneMatch.LEFT_ATTACK;
import static easyhattrickmanager.service.model.teamtraining.ZoneMatch.LEFT_DEFENSE;
import static easyhattrickmanager.service.model.teamtraining.ZoneMatch.MIDFIELD;
import static easyhattrickmanager.service.model.teamtraining.ZoneMatch.RIGHT_ATTACK;
import static easyhattrickmanager.service.model.teamtraining.ZoneMatch.RIGHT_DEFENSE;
import static java.util.Map.entry;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.playertraining.Skill;
import easyhattrickmanager.service.model.teamtraining.LineMatch;
import easyhattrickmanager.service.model.teamtraining.MatchDetail;
import easyhattrickmanager.service.model.teamtraining.PlayerRating;
import easyhattrickmanager.service.model.teamtraining.Position;
import easyhattrickmanager.service.model.teamtraining.RatingConfig;
import easyhattrickmanager.service.model.teamtraining.RatingConfigGroup;
import easyhattrickmanager.service.model.teamtraining.RatingConfigProp;
import easyhattrickmanager.service.model.teamtraining.RatingConfigSection;
import easyhattrickmanager.service.model.teamtraining.RatingZone;
import easyhattrickmanager.service.model.teamtraining.Ratings;
import easyhattrickmanager.service.model.teamtraining.Specialty;
import easyhattrickmanager.service.model.teamtraining.ZoneMatch;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerRatingService {

    private static final Position[] POSITIONS = Position.values();
    private static final Skill[] SKILLS = Skill.values();

    private static final Map<ZoneMatch, RatingConfigGroup> ZONE_GROUP = Map.ofEntries(
        entry(RIGHT_DEFENSE, RatingConfigGroup.SIDEDEFENSE),
        entry(CENTRAL_DEFENSE, RatingConfigGroup.CENTRALDEFENSE),
        entry(LEFT_DEFENSE, RatingConfigGroup.SIDEDEFENSE),
        entry(MIDFIELD, RatingConfigGroup.MIDFIELD),
        entry(RIGHT_ATTACK, RatingConfigGroup.SIDEATTACK),
        entry(CENTRAL_ATTACK, RatingConfigGroup.CENTRALATTACK),
        entry(LEFT_ATTACK, RatingConfigGroup.SIDEATTACK)
    );

    private static final Map<ZoneMatch, RatingZone> ZONE_SIDE = Map.ofEntries(
        entry(RIGHT_DEFENSE, RatingZone.RIGHT),
        entry(CENTRAL_DEFENSE, RatingZone.ALLSIDES),
        entry(LEFT_DEFENSE, RatingZone.LEFT),
        entry(MIDFIELD, RatingZone.ALLSIDES),
        entry(RIGHT_ATTACK, RatingZone.RIGHT),
        entry(CENTRAL_ATTACK, RatingZone.ALLSIDES),
        entry(LEFT_ATTACK, RatingZone.LEFT)
    );

    public List<PlayerRating> getRatings(final PlayerInfo player, final MatchDetail matchDetail) {
        final Map<Skill, Double> effectiveSkills = this.getEffectiveSkills(player);
        final Map<Position, RatingConfigProp> positionProps = new EnumMap<>(Position.class);
        for (Position position : POSITIONS) {
            positionProps.put(position, this.getRatingConfigProp(position, player));
        }

        final List<PlayerRating> ratings = new java.util.ArrayList<>(POSITIONS.length);
        for (Position position : POSITIONS) {
            ratings.add(PlayerRating.builder()
                .playerId(player.getId())
                .position(position)
                .rating(Ratings.builder()
                    .rightDefense(this.getCoefficientByPositionAndZone(position, RIGHT_DEFENSE, matchDetail, positionProps.get(position), effectiveSkills))
                    .centralDefense(this.getCoefficientByPositionAndZone(position, CENTRAL_DEFENSE, matchDetail, positionProps.get(position), effectiveSkills))
                    .leftDefense(this.getCoefficientByPositionAndZone(position, LEFT_DEFENSE, matchDetail, positionProps.get(position), effectiveSkills))
                    .midfield(this.getCoefficientByPositionAndZone(position, MIDFIELD, matchDetail, positionProps.get(position), effectiveSkills))
                    .rightAttack(this.getCoefficientByPositionAndZone(position, RIGHT_ATTACK, matchDetail, positionProps.get(position), effectiveSkills))
                    .centralAttack(this.getCoefficientByPositionAndZone(position, CENTRAL_ATTACK, matchDetail, positionProps.get(position), effectiveSkills))
                    .leftAttack(this.getCoefficientByPositionAndZone(position, LEFT_ATTACK, matchDetail, positionProps.get(position), effectiveSkills))
                    .build())
                .build());
        }
        return ratings;
    }

    private double getCoefficientByPositionAndZone(final Position position,
                                                   final ZoneMatch zone,
                                                   final MatchDetail matchDetail,
                                                   final RatingConfigProp positionProp,
                                                   final Map<Skill, Double> effectiveSkills) {
        final RatingConfigGroup group = ZONE_GROUP.get(zone);
        final RatingZone zoneSide = ZONE_SIDE.get(zone);

        double c = 0.0;
        for (Map.Entry<RatingConfigSection, Map<RatingConfigProp, Double>> section : RatingConfig.config.get(group).entrySet()) {
            if (section.getKey() == RatingConfigSection.general || section.getKey().getSkill() == null) {
                continue;
            }
            c += this.getSectionContribution(position, positionProp, zoneSide, section.getKey(), section.getValue(), effectiveSkills);
        }

        return this.applyCommonProps(c, zone, matchDetail);
    }

    private double getSectionContribution(final Position position,
                                          final RatingConfigProp positionProp,
                                          final RatingZone zoneSide,
                                          final RatingConfigSection section,
                                          final Map<RatingConfigProp, Double> sectionProps,
                                          final Map<Skill, Double> effectiveSkills) {
        final Skill skill = section.getSkill();
        final double sideWeight = this.getSideWeight(position, skill, section.getZone(), zoneSide);
        if (sideWeight == 0.0) {
            return 0.0;
        }
        final double coefficient = sectionProps.getOrDefault(positionProp, 0.0);
        if (coefficient == 0.0) {
            return 0.0;
        }
        return sideWeight * effectiveSkills.getOrDefault(skill, 0.0) * coefficient;
    }

    private double getSideWeight(final Position position, final Skill skill, final RatingZone sectionZone, final RatingZone zoneSide) {
        final boolean useLeft = (sectionZone == RatingZone.THISSIDE && zoneSide == RatingZone.LEFT)
            || (sectionZone == RatingZone.OTHERSIDE && zoneSide == RatingZone.RIGHT)
            || sectionZone == RatingZone.ALLSIDES;
        final boolean useMiddle = sectionZone == RatingZone.MIDDLE || sectionZone == RatingZone.ALLSIDES;
        final boolean useRight = (sectionZone == RatingZone.THISSIDE && zoneSide == RatingZone.RIGHT)
            || (sectionZone == RatingZone.OTHERSIDE && zoneSide == RatingZone.LEFT)
            || sectionZone == RatingZone.ALLSIDES;

        final boolean isLeft = position.getRole().isLeft();
        final boolean isMiddle = position.getRole().isMiddle();
        final boolean isRight = position.getRole().isRight();
        final boolean isSideZone = (useLeft || useRight) && !useMiddle;

        if ((isLeft && useLeft) || (isRight && useRight) || (isMiddle && useMiddle)) {
            return 1.0;
        }
        if (isMiddle && isSideZone && skill == Skill.DEFENDING) {
            return 0.5;
        }
        return 0.0;
    }

    private RatingConfigProp getRatingConfigProp(final Position position, final PlayerInfo player) {
        final String base = position.getRole().getRoleGroup().getAbr() + "_" + position.getBehaviour().getAbr();
        if (player.getSpecialty() == Specialty.TECHNICAL.getValue() && "FW_def".equals(base)) {
            return RatingConfigProp.FW_def_tech;
        }
        return RatingConfigProp.valueOf(base);
    }

    private Map<Skill, Double> getEffectiveSkills(final PlayerInfo player) {
        final Map<Skill, Double> effectiveSkills = new EnumMap<>(Skill.class);
        for (Skill skill : SKILLS) {
            effectiveSkills.put(skill, this.getEffectiveSkill(player, skill));
        }
        return effectiveSkills;
    }

    private double applyCommonProps(final double inVal, final ZoneMatch zone, final MatchDetail matchDetail) {
        double retVal = inVal;
        if (zone == MIDFIELD) {
            final Map<RatingConfigProp, Double> props = RatingConfig.config.get(RatingConfigGroup.MIDFIELD).get(RatingConfigSection.general);
            final double teamSpirit = matchDetail.getTeamSpirit().getValue() + matchDetail.getTeamSubSpirit();
            retVal *= getTeamSpiritNormalFactor(teamSpirit);
            retVal *= props.getOrDefault(matchDetail.getSideMatch().getProp(), 1.0);
            retVal *= props.getOrDefault(matchDetail.getTeamAttitude().getProp(), 1.0);
        }
        if (zone.getLine() == LineMatch.ATTACK) {
            final Map<RatingConfigProp, Double> props = RatingConfig.config.get(ZONE_GROUP.get(zone)).get(RatingConfigSection.general);
            final double teamConfidence = matchDetail.getTeamConfidence().getValue() + matchDetail.getTeamSubConfidence();
            // Confidence scale is centered at 4.5 when using sublevel 0.5 for "Decent" baseline.
            retVal *= (1.0 + props.getOrDefault(RatingConfigProp.confidence, 0.0) * (teamConfidence - 4.5));
        }

        final double offensive = zone.getLine().getTrainerOffensiveFactor();
        final double defensive = zone.getLine().getTrainerDefensiveFactor();
        final double outlier = matchDetail.getStyleOfPlay() >= 0 ? offensive : defensive;
        retVal *= 1.0 + (Math.abs(matchDetail.getStyleOfPlay()) * 0.01) * (outlier - 1.0);

        retVal = Math.pow(retVal, 1.165);
        retVal += 0.75;
        if (!Double.isFinite(retVal)) {
            return 0.0;
        }
        return retVal;
    }

    private double getEffectiveSkill(final PlayerInfo player, final Skill skill) {
        double form = player.getPlayerForm();
        final double rawSkill = getSkill(player, skill);
        // Hattrick formulas use calculation scale: visible skill reduced by 1.
        final double calcSkill = rawSkill < 1.0 ? 0.0 : rawSkill - 1.0;
        double value = Math.max(0.0, calcSkill + this.getLoyaltyHomegrownBonus(player));
        final double xp = player.getExperience() <= 0 ? 0.0 : (4.0 / 3.0) * Math.log10(player.getExperience());
        value += xp;
        form = Math.max(form, 1.0);
        form = Math.min(form, 8.0);
        form *= 0.125;
        form = Math.pow(form, 2.0 / 3.0);
        return value * form;
    }

    private double getLoyaltyHomegrownBonus(final PlayerInfo player) {
        return RatingConfig.getLoyaltyBonus(player.getLoyalty(), player.isMotherClubBonus());
    }
}
