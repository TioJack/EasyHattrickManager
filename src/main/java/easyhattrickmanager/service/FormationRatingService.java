package easyhattrickmanager.service;

import static java.util.stream.Collectors.toList;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.teamtraining.BestFormationCriteria;
import easyhattrickmanager.service.model.teamtraining.Formation;
import easyhattrickmanager.service.model.teamtraining.FormationRating;
import easyhattrickmanager.service.model.teamtraining.Lineup;
import easyhattrickmanager.service.model.teamtraining.LineupPlayer;
import easyhattrickmanager.service.model.teamtraining.MatchDetail;
import easyhattrickmanager.service.model.teamtraining.PlayerRating;
import easyhattrickmanager.service.model.teamtraining.Position;
import easyhattrickmanager.service.model.teamtraining.Role;
import easyhattrickmanager.service.model.teamtraining.RoleGroup;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FormationRatingService {

    private static final Formation[] FORMATIONS = Formation.values();

    private final PlayerRatingService playerRatingService;
    private final RatingService ratingService;

    public FormationRating getRatings(final List<PlayerInfo> players, final MatchDetail matchDetail, final BestFormationCriteria bestFormationCriteria,
                                      final Formation fixedFormation) {
        if (players == null || players.isEmpty()) {
            return this.emptyFormationRating(fixedFormation, matchDetail);
        }
        final Comparator<FormationRating> formationComparator = bestFormationCriteria.getFormationRatingComparator();
        final List<PlayerRating> sortedRatings = new ArrayList<>(players.size() * Position.values().length);
        final Map<Integer, PlayerInfo> playersById = new java.util.HashMap<>(players.size() * 2);
        for (PlayerInfo player : players) {
            playersById.put(player.getId(), player);
            sortedRatings.addAll(this.playerRatingService.getRatings(player, matchDetail));
        }
        sortedRatings.sort(bestFormationCriteria.getPlayerRatingComparator());

        FormationRating bestFormationRating = null;
        for (Formation formation : FORMATIONS) {
            if (fixedFormation != null && formation != fixedFormation) {
                continue;
            }

            final List<PlayerRating> best11 = this.pickBest11(sortedRatings, formation);
            if (best11 == null) {
                continue;
            }
            final FormationRating formationRating = this.buildFormationRating(formation.getName(), best11, playersById, matchDetail);

            if (bestFormationRating == null || formationComparator.compare(formationRating, bestFormationRating) < 0) {
                bestFormationRating = formationRating;
            }
        }

        if (bestFormationRating == null) {
            return this.buildFallbackFormationRating(sortedRatings, playersById, matchDetail, fixedFormation, formationComparator);
        }

        return bestFormationRating;
    }

    public FormationRating getRatingsForPreviousLineup(final List<PlayerInfo> players,
                                                       final FormationRating previousWeekFormationRating,
                                                       final MatchDetail matchDetail) {
        if (players == null || players.isEmpty() || previousWeekFormationRating == null || previousWeekFormationRating.getPlayers() == null
            || previousWeekFormationRating.getPlayers().isEmpty()) {
            return null;
        }

        final Map<Integer, PlayerInfo> playersById = new java.util.HashMap<>(players.size() * 2);
        for (PlayerInfo player : players) {
            playersById.put(player.getId(), player);
        }

        final List<LineupPlayer> fieldPlayers = new ArrayList<>(previousWeekFormationRating.getPlayers().size());
        for (PlayerRating previousPlayerRating : previousWeekFormationRating.getPlayers()) {
            final PlayerInfo player = playersById.get(previousPlayerRating.getPlayerId());
            final Position position = previousPlayerRating.getPosition();
            if (player == null || position == null) {
                return null;
            }
            fieldPlayers.add(LineupPlayer.builder()
                .player(player)
                .role(position.getRole())
                .behaviour(position.getBehaviour())
                .build());
        }

        final Lineup lineup = Lineup.builder()
            .matchDetail(matchDetail)
            .fieldPlayers(fieldPlayers)
            .build();

        final List<PlayerRating> repeatedPlayers = new ArrayList<>(previousWeekFormationRating.getPlayers().size());
        for (PlayerRating previousPlayerRating : previousWeekFormationRating.getPlayers()) {
            repeatedPlayers.add(PlayerRating.builder()
                .playerId(previousPlayerRating.getPlayerId())
                .position(previousPlayerRating.getPosition())
                .rating(previousPlayerRating.getRating())
                .build());
        }

        return FormationRating.builder()
            .formation(previousWeekFormationRating.getFormation())
            .players(repeatedPlayers)
            .rating(this.ratingService.getRatings(lineup))
            .build();
    }

    private FormationRating buildFallbackFormationRating(final List<PlayerRating> sortedRatings,
                                                         final Map<Integer, PlayerInfo> playersById,
                                                         final MatchDetail matchDetail,
                                                         final Formation fixedFormation,
                                                         final Comparator<FormationRating> formationComparator) {
        final int maxPlayers = Math.min(11, playersById.size());
        if (maxPlayers <= 0 || sortedRatings.isEmpty()) {
            return this.emptyFormationRating(fixedFormation, matchDetail);
        }

        FormationRating bestFallbackFormationRating = null;
        for (Formation formation : FORMATIONS) {
            if (fixedFormation != null && formation != fixedFormation) {
                continue;
            }
            final List<PlayerRating> fallbackPlayers = this.pickBestAvailable(sortedRatings, formation, maxPlayers);
            if (fallbackPlayers.isEmpty()) {
                continue;
            }
            final FormationRating fallbackFormationRating = this.buildFormationRating(formation.getName(), fallbackPlayers, playersById, matchDetail);
            if (bestFallbackFormationRating == null || formationComparator.compare(fallbackFormationRating, bestFallbackFormationRating) < 0) {
                bestFallbackFormationRating = fallbackFormationRating;
            }
        }

        return bestFallbackFormationRating != null ? bestFallbackFormationRating : this.emptyFormationRating(fixedFormation, matchDetail);
    }

    private FormationRating emptyFormationRating(final Formation fixedFormation, final MatchDetail matchDetail) {
        final String formationName = this.getFallbackFormationName(fixedFormation);
        return FormationRating.builder()
            .formation(formationName)
            .players(List.of())
            .rating(this.ratingService.getRatings(Lineup.builder()
                .matchDetail(matchDetail)
                .fieldPlayers(List.of())
                .build()))
            .build();
    }

    private FormationRating buildFormationRating(final String formationName,
                                                 final List<PlayerRating> players,
                                                 final Map<Integer, PlayerInfo> playersById,
                                                 final MatchDetail matchDetail) {
        final Lineup lineup = Lineup.builder()
            .matchDetail(matchDetail)
            .fieldPlayers(players.stream().map(fr -> {
                final PlayerInfo player = playersById.get(fr.getPlayerId());
                if (player == null) {
                    throw new IllegalStateException("No player found for id " + fr.getPlayerId());
                }
                return LineupPlayer.builder()
                    .player(player)
                    .role(fr.getPosition().getRole())
                    .behaviour(fr.getPosition().getBehaviour())
                    .build();
            }).collect(toList()))
            .build();

        return FormationRating.builder()
            .formation(formationName)
            .players(players)
            .rating(this.ratingService.getRatings(lineup))
            .build();
    }

    private List<PlayerRating> pickBestAvailable(final List<PlayerRating> sortedRatings, final Formation formation, final int maxPlayers) {
        final Map<RoleGroup, Integer> availableRoleGroups = this.countRoleGroups(formation.getRoleGroups());
        final Set<Integer> usedPlayers = new HashSet<>(maxPlayers);
        final Set<Role> usedRoles = new HashSet<>(14);
        final List<PlayerRating> players = new ArrayList<>(maxPlayers);

        for (PlayerRating candidate : sortedRatings) {
            if (players.size() == maxPlayers) {
                break;
            }
            if (!usedPlayers.add(candidate.getPlayerId())) {
                continue;
            }
            final Role role = candidate.getPosition().getRole();
            if (!usedRoles.add(role)) {
                usedPlayers.remove(candidate.getPlayerId());
                continue;
            }
            if (!this.consumeRoleGroup(availableRoleGroups, role.getRoleGroup())) {
                usedPlayers.remove(candidate.getPlayerId());
                usedRoles.remove(role);
                continue;
            }
            players.add(candidate);
        }
        return players;
    }

    private List<PlayerRating> pickBest11(final List<PlayerRating> sortedRatings, final Formation formation) {
        final Map<RoleGroup, Integer> availableRoleGroups = this.countRoleGroups(formation.getRoleGroups());
        final Set<Integer> usedPlayers = new HashSet<>(11);
        final Set<Role> usedRoles = new HashSet<>(11);
        final List<PlayerRating> best11 = new ArrayList<>(11);

        for (PlayerRating candidate : sortedRatings) {
            if (best11.size() == 11) {
                break;
            }
            if (usedPlayers.contains(candidate.getPlayerId())) {
                continue;
            }
            final Role role = candidate.getPosition().getRole();
            if (usedRoles.contains(role)) {
                continue;
            }
            if (!this.consumeRoleGroup(availableRoleGroups, role.getRoleGroup())) {
                continue;
            }
            usedPlayers.add(candidate.getPlayerId());
            usedRoles.add(role);
            best11.add(candidate);
        }

        return best11.size() == 11 ? best11 : null;
    }

    private boolean consumeRoleGroup(final Map<RoleGroup, Integer> availableRoleGroups, final RoleGroup roleGroup) {
        final int available = availableRoleGroups.getOrDefault(roleGroup, 0);
        if (available <= 0) {
            return false;
        }
        availableRoleGroups.put(roleGroup, available - 1);
        return true;
    }

    private Map<RoleGroup, Integer> countRoleGroups(final List<RoleGroup> roleGroups) {
        final Map<RoleGroup, Integer> available = new EnumMap<>(RoleGroup.class);
        roleGroups.forEach(roleGroup -> available.merge(roleGroup, 1, Integer::sum));
        return available;
    }

    private String getFallbackFormationName(final Formation fixedFormation) {
        if (fixedFormation != null) {
            return fixedFormation.getName();
        }
        return Formation.F_4_4_2_A.getName();
    }
}
