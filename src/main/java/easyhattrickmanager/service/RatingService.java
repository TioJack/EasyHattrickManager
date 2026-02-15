package easyhattrickmanager.service;


import static easyhattrickmanager.service.model.teamtraining.RatingConfigGroup.PLAYERSTRENGTH;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.confidence;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.cubeMod;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.delta;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.formDelta;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.formMax;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.formMin;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.multiXpLog10;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.multiplier;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.power;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.skillDelta;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.squareMod;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.teamSpiritPower;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.teamSpiritPreMulti;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.threeCdMulti;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.threeFwMulti;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.threeMfMulti;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.trainerDef;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.trainerNeutral;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.trainerOff;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.twoCdMulti;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.twoFwMulti;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigProp.twoMfMulti;
import static easyhattrickmanager.service.model.teamtraining.RatingConfigSection.general;
import static easyhattrickmanager.service.model.teamtraining.RatingZone.ALLSIDES;
import static easyhattrickmanager.service.model.teamtraining.RatingZone.LEFT;
import static easyhattrickmanager.service.model.teamtraining.RatingZone.MIDDLE;
import static easyhattrickmanager.service.model.teamtraining.RatingZone.OTHERSIDE;
import static easyhattrickmanager.service.model.teamtraining.RatingZone.RIGHT;
import static easyhattrickmanager.service.model.teamtraining.RatingZone.THISSIDE;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.playertraining.Skill;
import easyhattrickmanager.service.model.teamtraining.Lineup;
import easyhattrickmanager.service.model.teamtraining.LineupPlayer;
import easyhattrickmanager.service.model.teamtraining.RatingConfig;
import easyhattrickmanager.service.model.teamtraining.RatingConfigGroup;
import easyhattrickmanager.service.model.teamtraining.RatingConfigProp;
import easyhattrickmanager.service.model.teamtraining.RatingConfigSection;
import easyhattrickmanager.service.model.teamtraining.RatingZone;
import easyhattrickmanager.service.model.teamtraining.Ratings;
import easyhattrickmanager.service.model.teamtraining.RoleGroup;
import easyhattrickmanager.service.model.teamtraining.Tactic;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService {

    // Forum table baseline (Normal attitude) for team spirit at x.5 levels:
    // 0.5..10.5 -> [0.447, 0.628, ..., 1.481]
    private static final double[] TEAM_SPIRIT_NORMAL_FACTORS = {
        0.447, 0.628, 0.773, 0.895, 1.0, 1.094, 1.180, 1.263, 1.340, 1.415, 1.481
    };

    public static double getTeamSpiritNormalFactor(final double teamSpiritValue) {
        final double clamped = Math.max(0.5, Math.min(10.5, teamSpiritValue));
        final double position = clamped - 0.5; // 0..10
        final int lowerIndex = (int) Math.floor(position);
        final int upperIndex = (int) Math.ceil(position);
        if (lowerIndex == upperIndex) {
            return TEAM_SPIRIT_NORMAL_FACTORS[lowerIndex];
        }
        final double t = position - lowerIndex;
        final double lower = TEAM_SPIRIT_NORMAL_FACTORS[lowerIndex];
        final double upper = TEAM_SPIRIT_NORMAL_FACTORS[upperIndex];
        return lower + (upper - lower) * t;
    }

    public Ratings getRatings(final Lineup startingLineup) {
        final Map<Integer, Lineup> lineupEvolution = this.getLineupEvolution(startingLineup);
        return Ratings.builder()
            .leftDefense(this.getRatings(lineupEvolution, RatingConfigGroup.SIDEDEFENSE, LEFT))
            .centralDefense(this.getRatings(lineupEvolution, RatingConfigGroup.CENTRALDEFENSE, ALLSIDES))
            .rightDefense(this.getRatings(lineupEvolution, RatingConfigGroup.SIDEDEFENSE, RIGHT))
            .midfield(this.getRatings(lineupEvolution, RatingConfigGroup.MIDFIELD, ALLSIDES))
            .leftAttack(this.getRatings(lineupEvolution, RatingConfigGroup.SIDEATTACK, LEFT))
            .centralAttack(this.getRatings(lineupEvolution, RatingConfigGroup.CENTRALATTACK, ALLSIDES))
            .rightAttack(this.getRatings(lineupEvolution, RatingConfigGroup.SIDEATTACK, RIGHT))
            .build();
    }

    private double getAverageRatings(final Map<Integer, Double> ratings) {
        final List<Integer> minutes = ratings.keySet().stream().sorted().collect(Collectors.toList());
        return IntStream.range(0, minutes.size() - 1)
            .mapToDouble(m -> (ratings.get(minutes.get(m)) + ratings.get(minutes.get(m + 1))) / 2.0 * (minutes.get(m + 1) - minutes.get(m)))
            .sum() / 90.0;
    }

    private double getRatings(final Map<Integer, Lineup> lineupEvolution, final RatingConfigGroup ratingConfigGroup, final RatingZone zone) {
        return roundToQuarter(this.getAverageRatings(lineupEvolution.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entrySet -> this.getRating(entrySet.getKey(), entrySet.getValue(), ratingConfigGroup, zone)))));
    }

    private static double roundToQuarter(final double input) {
        final int value = (int) input;
        final double dec = input - value;
        if (0.0 < dec && dec <= 0.25) {
            return value + 0.25;
        }
        if (0.25 < dec && dec <= 0.5) {
            return value + 0.50;
        }
        if (0.5 < dec && dec <= 0.75) {
            return value + 0.75;
        }
        if (0.75 < dec) {
            return value + 1.0;
        }
        return value;
    }

    private Map<Integer, Lineup> getLineupEvolution(final Lineup startingLineup) {
        return IntStream.range(0, 19).mapToObj(i -> i * 5).collect(Collectors.toMap(k -> k, v -> startingLineup));
    }

    private double getRating(final int minute, final Lineup lineup, final RatingConfigGroup ratingConfigGroup, final RatingZone zone) {
        final double retVal = RatingConfig.config.get(ratingConfigGroup).entrySet().stream()
            .filter(section -> section.getKey() != general)
            .mapToDouble(section -> this.getPartialRating(minute, lineup, section.getKey(), section.getValue(), zone))
            .sum();
        return this.applyCommonProps(retVal, lineup, RatingConfig.config.get(ratingConfigGroup).get(general));
    }

    private double getPartialRating(final int minute, final Lineup lineup, final RatingConfigSection section, final Map<RatingConfigProp, Double> props, final RatingZone zone) {
        final RatingZone sectionZone = section.getZone();
        final boolean useLeft = (sectionZone == THISSIDE && zone == LEFT) || (sectionZone == OTHERSIDE && zone == RIGHT) || (sectionZone == ALLSIDES);
        final boolean useMiddle = (sectionZone == MIDDLE) || (sectionZone == ALLSIDES);
        final boolean useRight = (sectionZone == THISSIDE && zone == RIGHT) || (sectionZone == OTHERSIDE && zone == LEFT) || (sectionZone == ALLSIDES);
        return this.getPlayersStrength(minute, lineup, section.getSkill(), props, useLeft, useMiddle, useRight);
    }

    private double getPlayersStrength(final int minute, final Lineup lineup, final Skill skill, final Map<RatingConfigProp, Double> props, final boolean useLeft, final boolean useMiddle, final boolean useRight) {
        return lineup.getFieldPlayers().stream()
            .filter(lineupPlayer -> props.containsKey(lineupPlayer.getRatingConfigProp()))
            .mapToDouble(lineupPlayer -> {
                final boolean isLeft = lineupPlayer.getRole().isLeft();
                final boolean isMiddle = lineupPlayer.getRole().isMiddle();
                final boolean isRight = lineupPlayer.getRole().isRight();
                final boolean isSideZone = (useLeft || useRight) && !useMiddle;
                double sideWeight = 0.0;
                if ((isLeft && useLeft) || (isRight && useRight) || (isMiddle && useMiddle)) {
                    sideWeight = 1.0;
                } else if (isMiddle && isSideZone && skill == Skill.DEFENDING) {
                    // Split central defender impact 50/50 on side defenses.
                    // Do not apply this generically: attack has explicit "middle" sections and this would double count.
                    sideWeight = 0.5;
                }
                if (sideWeight == 0.0) {
                    return 0.0;
                }
                return sideWeight * this.getPlayerStrength(minute, lineup, lineupPlayer, skill, props);
            })
            .sum();
    }

    private double getPlayerStrength(final int minute, final Lineup lineup, final LineupPlayer lineupPlayer, final Skill skill, final Map<RatingConfigProp, Double> props) {
        double retVal;
        retVal = this.getPlayerSkillStrength(lineupPlayer.getPlayer(), skill);
        retVal *= props.get(lineupPlayer.getRatingConfigProp());
        // Simulator mode: evaluate stamina at kickoff only (minute 0).
        retVal *= this.getStaminaEffect(lineupPlayer.getPlayer().getStaminaSkill(), 0, 0, lineup.getMatchDetail().getTactic() == Tactic.PRESSING);
        retVal *= this.adjustForCrowding(lineup, lineupPlayer.getRole().getRoleGroup());
        return retVal;
    }

    private double getPlayerSkillStrength(final PlayerInfo player, final Skill skillType) {
        final Map<RatingConfigProp, Double> props = RatingConfig.config.get(PLAYERSTRENGTH).get(general);
        double form = player.getPlayerForm() + props.getOrDefault(formDelta, 0.0);
        final double rawSkill = getSkill(player, skillType);
        // Hattrick formulas use calculation scale: visible skill reduced by 1.
        // Loyalty/homegrown bonus applies to the effective skill and can lift very low skills.
        final double calcSkill = rawSkill < 1.0 ? 0.0 : rawSkill - 1.0;
        double skill = Math.max(0.0, calcSkill + this.getLoyaltyHomegrownBonus(player));
        final double xp = player.getExperience() <= 0 ? 0.0 : props.getOrDefault(multiXpLog10, 0.0) * Math.log10(player.getExperience());
        skill += props.getOrDefault(skillDelta, 0.0) + xp;
        // Excel fidelity: Ef(F) = SQRT(IF(F<=1;6;F-1)/7)
        // Keep configured min/max clamps to avoid invalid out-of-range inputs.
        form = Math.max(form, props.getOrDefault(formMin, 0.0));
        form = Math.min(form, props.getOrDefault(formMax, 99999.0));
        form = Math.sqrt((form <= 1.0 ? 6.0 : form - 1.0) / 7.0);
        return skill * form;
    }

    public static double getSkill(final PlayerInfo player, final Skill skillType) {
        final double skill = switch (skillType) {
            case GOALKEEPING -> player.getKeeperSkill();
            case DEFENDING -> player.getDefenderSkill();
            case PLAY_MAKING -> player.getPlaymakerSkill();
            case WINGER -> player.getWingerSkill();
            case PASSING -> player.getPassingSkill();
            case SCORING -> player.getScorerSkill();
            case SET_PIECES -> player.getSetPiecesSkill();
        };
        if (player.getPlayerSubSkill() == null) {
            return skill;
        }
        return skill + switch (skillType) {
            case GOALKEEPING -> player.getPlayerSubSkill().getKeeper();
            case DEFENDING -> player.getPlayerSubSkill().getDefender();
            case PLAY_MAKING -> player.getPlayerSubSkill().getPlaymaker();
            case WINGER -> player.getPlayerSubSkill().getWinger();
            case PASSING -> player.getPlayerSubSkill().getPassing();
            case SCORING -> player.getPlayerSubSkill().getScorer();
            case SET_PIECES -> player.getPlayerSubSkill().getSetPieces();
        };
    }

    private double getLoyaltyHomegrownBonus(final PlayerInfo player) {
        return RatingConfig.getLoyaltyBonus(player.getLoyalty(), player.isMotherClubBonus());
    }

    private double getStaminaEffect(double stamina, final double t, final double tEnter, final boolean isTacticPressing) {
        stamina -= 1;
        final double P = isTacticPressing ? 1.1 : 1.0;
        final double energyLossPerMinute;
        double energy;
        if (stamina >= 7) {
            energyLossPerMinute = -3.25 * P / 5;
            energy = 125 + (stamina - 7) * 100 / 7.0 - energyLossPerMinute;  //energy when entering the field for player whose stamina >= 8
        } else {
            energyLossPerMinute = -P * (5.95 - 27 * stamina / 70.0) / 5;
            energy = 102 + 23 / 7.0 * stamina - energyLossPerMinute; //energy when entering the field for player whose stamina < 8
        }
        if (t > 45d && tEnter < 45d) {
            energy += 18.75;  // Energy recovery during half-time
        }
        energy += energyLossPerMinute * (t - tEnter);
        return Math.max(10, Math.min(100, energy)) / 100.0;
    }

    private double adjustForCrowding(final Lineup lineup, final RoleGroup roleGroup) {
        final Map<RatingConfigProp, Double> props = RatingConfig.config.get(PLAYERSTRENGTH).get(general);
        double weight = 1;
        switch (roleGroup) {
            case CENTRAL_DEFENDER:
                if (lineup.getNumberCentralDefender() == 2) {
                    weight = props.get(twoCdMulti);
                } else if (lineup.getNumberCentralDefender() == 3) {
                    weight = props.get(threeCdMulti);
                }
                break;
            case INNER_MIDFIELD:
                if (lineup.getNumberInnerMidfield() == 2) {
                    weight = props.get(twoMfMulti);
                } else if (lineup.getNumberInnerMidfield() == 3) {
                    weight = props.get(threeMfMulti);
                }
                break;
            case FORWARD:
                if (lineup.getNumberForward() == 2) {
                    weight = props.get(twoFwMulti);
                } else if (lineup.getNumberForward() == 3) {
                    weight = props.get(threeFwMulti);
                }
                break;
        }
        return weight;
    }

    private double applyCommonProps(final double inVal, final Lineup lineup, final Map<RatingConfigProp, Double> props) {
        double retVal = inVal;
        retVal += props.getOrDefault(squareMod, 0.0) * Math.pow(retVal, 2);
        retVal += props.getOrDefault(cubeMod, 0.0) * Math.pow(retVal, 3);

        // Apply area exponent first, then context multipliers to better match spreadsheet flow.
        retVal *= props.getOrDefault(multiplier, 1.0);
        retVal = Math.pow(retVal, props.getOrDefault(power, 1.0));

        retVal *= props.getOrDefault(lineup.getMatchDetail().getTactic().getProp(), 1.0);
        final double teamSpirit = lineup.getMatchDetail().getTeamSpirit().getValue() + lineup.getMatchDetail().getTeamSubSpirit();
        if (props.containsKey(teamSpiritPreMulti) || props.containsKey(teamSpiritPower)) {
            retVal *= getTeamSpiritNormalFactor(teamSpirit);
        }
        retVal *= props.getOrDefault(lineup.getMatchDetail().getSideMatch().getProp(), 1.0);
        retVal *= props.getOrDefault(lineup.getMatchDetail().getTeamAttitude().getProp(), 1.0);
        final double teamConfidence = lineup.getMatchDetail().getTeamConfidence().getValue() + lineup.getMatchDetail().getTeamSubConfidence();
        // Confidence scale is centered at 4.5 when using sublevel 0.5 for "Decent" baseline.
        retVal *= (1.0 + props.getOrDefault(confidence, 0.0) * (teamConfidence - 4.5));
        final double offensive = props.getOrDefault(trainerOff, 1.0);
        final double defensive = props.getOrDefault(trainerDef, 1.0);
        final double neutral = props.getOrDefault(trainerNeutral, 1.0);
        final double outlier = lineup.getMatchDetail().getStyleOfPlay() >= 0 ? offensive : defensive;
        retVal *= neutral + (Math.abs(lineup.getMatchDetail().getStyleOfPlay()) * 0.01) * (outlier - neutral);

        retVal += props.getOrDefault(delta, 0.0);
        if (!Double.isFinite(retVal)) {
            return 0.0;
        }
        return retVal;
    }

}
