package easyhattrickmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.teamtraining.Behaviour;
import easyhattrickmanager.service.model.teamtraining.LineupPlayer;
import easyhattrickmanager.service.model.teamtraining.Ratings;
import easyhattrickmanager.service.model.teamtraining.Role;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Data-driven kickoff model built exclusively from the stored simulator matrix.
 * It has no dependency on the legacy RatingService or RatingConfig.
 */
@Component
public class EmpiricalRatingV2Model {

    private static final List<String> SKILLS = List.of("goalkeeping", "defending", "playmaking", "winger", "passing", "scoring");
    private static final int[] MIRROR_SECTOR = {2, 1, 0, 3, 6, 5, 4};
    private static final double[] AGGREGATION_INTERCEPT = {
        -4.8849166562, -4.8111491279, -4.0990856001, -8.4118552733,
        -7.4247544441, -11.4014177382, -6.4499479492
    };
    private static final double[] AGGREGATION_SLOPE = {1.3324978067, 1.3440707669, 1.3118180421, 1.3048498845, 1.4221035564, 1.4132665678, 1.39837601499};
    private static final double[] AGGREGATION_352 = {-0.5824978067, -2.0779646165, -1.2581834947, -3.6014241724, -0.7164886680, -1.9030662710, -1.6747865917};
    private static final double[] AGGREGATION_532 = {-1.7468667753, -2.6207961278, -1.6693176579, -5.8575827560, -0.8573058757, -2.1598664688, -1.2072871122};

    private final ObjectMapper objectMapper;
    private JsonNode curves;

    public EmpiricalRatingV2Model(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadCalibration() throws IOException {
        try (var input = new ClassPathResource("rating-v2/marginal-observed-1.json").getInputStream()) {
            this.curves = this.objectMapper.readTree(input).path("curves");
        }
    }

    public Estimate estimateKickoff(final List<LineupPlayer> fieldPlayers) {
        final Set<String> warnings = new LinkedHashSet<>();
        final int[] composition = new int[7];
        final List<LineupPlayer> players = fieldPlayers == null ? List.of() : fieldPlayers;
        int defenders = 0;
        int midfielders = 0;
        int forwards = 0;
        for (final LineupPlayer lineupPlayer : players) {
            final Integer slot = this.slot(lineupPlayer, warnings);
            if (slot == null || lineupPlayer.getPlayer() == null) {
                continue;
            }
            if (slot >= 1 && slot <= 5) defenders++;
            if (slot >= 6 && slot <= 10) midfielders++;
            if (slot >= 11 && slot <= 13) forwards++;
            for (final String skill : SKILLS) {
                final int level = this.skill(lineupPlayer.getPlayer(), skill);
                this.addSkillContribution(composition, skill, slot, this.behaviour(lineupPlayer), level, warnings);
            }
            this.warnOutsideIndividualCalibration(lineupPlayer.getPlayer(), warnings);
        }

        if (players.size() == 11) {
            this.applyFullLineupAggregation(composition, defenders, midfielders, forwards, warnings);
        } else {
            warnings.add("La corrección de agregación está validada para XI completos; esta estimación parcial suma las contribuciones observadas.");
        }
        return new Estimate(this.toRatings(composition), List.copyOf(warnings));
    }

    private void addSkillContribution(final int[] target, final String skill, final int requestedSlot, final int behaviour, final int requestedLevel, final Set<String> warnings) {
        final int level = Math.max(0, Math.min(20, requestedLevel));
        if (level != requestedLevel) {
            warnings.add("Las habilidades fuera de 0-20 se recortan al rango calibrado.");
        }
        final int sourceSlot = mirrorSource(requestedSlot);
        final boolean mirrored = sourceSlot != requestedSlot;
        final JsonNode deltas = this.curves.path(skill).path(sourceSlot + ":" + behaviour).path("0").path("deltasByLevel");
        if (!deltas.isArray() || deltas.size() <= level) {
            warnings.add("No hay curva empírica para " + skill + " en posición/comportamiento " + requestedSlot + "/" + behaviour + ".");
            return;
        }
        final JsonNode values = deltas.path(level);
        for (int sector = 0; sector < 7; sector++) {
            target[sector] += values.path(mirrored ? MIRROR_SECTOR[sector] : sector).asInt();
        }
    }

    private void applyFullLineupAggregation(final int[] values, final int defenders, final int midfielders, final int forwards, final Set<String> warnings) {
        final boolean threeFiveTwo = defenders == 3 && midfielders == 5 && forwards == 2;
        final boolean fiveThreeTwo = defenders == 5 && midfielders == 3 && forwards == 2;
        final boolean fourFourTwo = defenders == 4 && midfielders == 4 && forwards == 2;
        if (!fourFourTwo && !threeFiveTwo && !fiveThreeTwo) {
            warnings.add("La corrección de XI está validada para 4-4-2, 3-5-2 y 5-3-2; se usa la base 4-4-2 fuera de esas formaciones.");
        }
        for (int sector = 0; sector < 7; sector++) {
            double corrected = AGGREGATION_INTERCEPT[sector] + AGGREGATION_SLOPE[sector] * values[sector];
            if (threeFiveTwo) corrected += AGGREGATION_352[sector];
            if (fiveThreeTwo) corrected += AGGREGATION_532[sector];
            values[sector] = Math.max(0, (int) Math.round(corrected));
        }
    }

    private Integer slot(final LineupPlayer player, final Set<String> warnings) {
        if (player.getRole() == null || player.getRole().getValue() < Role.KEEPER.getValue() || player.getRole().getValue() > Role.LEFT_FORWARD.getValue()) {
            warnings.add("Sólo se calculan jugadores del once inicial; suplentes y órdenes se ignoran.");
            return null;
        }
        return player.getRole().getValue() - Role.KEEPER.getValue();
    }

    private int behaviour(final LineupPlayer player) {
        if (player.getBehaviour() == null || player.getBehaviour() == Behaviour.NO_CHANGE) {
            return Behaviour.NORMAL.getValue();
        }
        return player.getBehaviour().getValue();
    }

    private int skill(final PlayerInfo player, final String skill) {
        return switch (skill) {
            case "goalkeeping" -> player.getKeeperSkill();
            case "defending" -> player.getDefenderSkill();
            case "playmaking" -> player.getPlaymakerSkill();
            case "winger" -> player.getWingerSkill();
            case "passing" -> player.getPassingSkill();
            case "scoring" -> player.getScorerSkill();
            default -> 0;
        };
    }

    private void warnOutsideIndividualCalibration(final PlayerInfo player, final Set<String> warnings) {
        if (player.getPlayerForm() != 8 || player.getStaminaSkill() != 9) {
            warnings.add("Las curvas individuales base se calibraron con forma 8 y resistencia 9; el ajuste de forma/resistencia aún no está promovido a producción.");
        }
        if (player.getExperience() != 0 || player.getLoyalty() != 0) {
            warnings.add("Experiencia y fidelidad se han medido, pero su ajuste por rol aún no está promovido a producción.");
        }
    }

    private Ratings toRatings(final int[] raw) {
        return Ratings.builder()
            .rightDefense(this.toHattrickRating(raw[0]))
            .centralDefense(this.toHattrickRating(raw[1]))
            .leftDefense(this.toHattrickRating(raw[2]))
            .midfield(this.toHattrickRating(raw[3]))
            .rightAttack(this.toHattrickRating(raw[4]))
            .centralAttack(this.toHattrickRating(raw[5]))
            .leftAttack(this.toHattrickRating(raw[6]))
            .build();
    }

    private double toHattrickRating(final int raw) {
        return (Math.max(0, raw) + 3.0d) / 4.0d;
    }

    private static int mirrorSource(final int slot) {
        return switch (slot) {
            case 4 -> 2;
            case 5 -> 1;
            case 9 -> 7;
            case 10 -> 6;
            case 13 -> 11;
            default -> slot;
        };
    }

    public record Estimate(Ratings ratings, List<String> warnings) {
    }
}
