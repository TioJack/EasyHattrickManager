package easyhattrickmanager.service;

import easyhattrickmanager.client.hattrick.model.tournamentfixtures.TournamentMatch;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.utils.SeasonWeekUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NationalExperienceService {

    // Competition IDs from the national cup pages; each ID is reused across seasons.
    private static final Set<Integer> CONTINENTAL_CUPS = Set.of(4878483, 4878490, 4878492, 4878493,
        5001273, 5001277, 5001278, 5001279);
    private static final List<Integer> NATIONAL_TOURNAMENTS = List.of(
        4878483, 4878490, 4878492, 4878493, 4891573, 4892549, 4892615, 4894807, 6260697,
        5001273, 5001277, 5001278, 5001279, 6244933, 5001315, 5001319);

    private final HattrickService hattrickService;
    private final TeamDAO teamDAO;
    private final LeagueDAO leagueDAO;
    private final TrainingPercentageService trainingPercentageService;

    public Map<Integer, Double> calculatePoints(String seasonWeek, int clubTeamId, List<PlayerData> players) {
        Set<Integer> nationalTeams = players.stream()
            .map(PlayerData::getNationalTeamId).filter(id -> id > 0).collect(Collectors.toSet());
        if (nationalTeams.isEmpty()) {
            return Map.of();
        }
        Set<Integer> playerIds = players.stream().filter(player -> player.getNationalTeamId() > 0)
            .map(PlayerData::getId).collect(Collectors.toSet());
        var club = teamDAO.get(clubTeamId);
        var league = leagueDAO.get(club.getLeagueId()).orElseThrow();
        var end = SeasonWeekUtils.seasonWeekTrainingDate(seasonWeek, league.getTrainingDate());
        var start = end.minusWeeks(1);
        int season = Integer.parseInt(seasonWeek.substring(1, 4));
        Map<Integer, Double> points = new HashMap<>();
        Set<String> processed = new HashSet<>();

        for (int tournamentId : NATIONAL_TOURNAMENTS) {
            var response = hattrickService.getTournamentFixtures(clubTeamId, tournamentId, season);
            List<TournamentMatch> fixtures = response.getMatches() == null ? List.of() : response.getMatches();
            Map<Integer, Long> knockoutRoundSizes = fixtures.stream().distinct().filter(match -> match.getGroup() == 0)
                .collect(Collectors.groupingBy(TournamentMatch::getMatchRound, Collectors.counting()));
            fixtures.stream().filter(match -> match.getMatchDate() != null
                    && !match.getMatchDate().isBefore(start) && match.getMatchDate().isBefore(end))
                .filter(match -> nationalTeams.contains(match.getHomeTeamId()) || nationalTeams.contains(match.getAwayTeamId()))
                .forEach(match -> addMatchPoints(clubTeamId, match.getMatchId(), match.getMatchType(),
                    match.getHomeTeamId(), match.getAwayTeamId(), getTournamentPoints(tournamentId, match.getGroup() == 0,
                        knockoutRoundSizes.getOrDefault(match.getMatchRound(), 0L)),
                    nationalTeams, playerIds, processed, points));
        }
        // The national match list also contains ordinary friendlies and World Cups using CupID 137/149.
        for (int officeType : List.of(2, 4)) {
            var response = hattrickService.getNationalTeamMatches(clubTeamId, officeType);
            if (response.getMatches() == null) continue;
            response.getMatches().stream()
                .filter(match -> match.getMatchDate() != null && !match.getMatchDate().isBefore(start)
                    && match.getMatchDate().isBefore(end))
                .filter(match -> nationalTeams.contains(match.getHomeTeamId()) || nationalTeams.contains(match.getAwayTeamId()))
                .forEach(match -> addMatchPoints(clubTeamId, match.getMatchId(), match.getMatchType(),
                    match.getHomeTeamId(), match.getAwayTeamId(),
                    PlayerExperienceService.getMatchPoints(match.getMatchType(), 0, match.getMatchContextId()),
                    nationalTeams, playerIds, processed, points));
        }
        return points;
    }

    private void addMatchPoints(int clubTeamId, int matchId, int matchType, int homeTeamId, int awayTeamId,
        double matchPoints, Set<Integer> nationalTeams, Set<Integer> playerIds, Set<String> processed,
        Map<Integer, Double> points) {
        if (matchPoints <= 0) return;
        String source = matchType >= 50 ? "HTOIntegrated" : "Hattrick";
        if (!processed.add(source + ":" + matchId)) return;
        var detail = hattrickService.getMatchDetail(clubTeamId, matchId, source);
        if (detail.getMatch().getFinishedDate() == null) {
            throw new IllegalStateException("National match has not finished: " + matchId);
        }
        for (int nationalTeam : Set.of(homeTeamId, awayTeamId)) {
            if (!nationalTeams.contains(nationalTeam)) continue;
            var lineup = hattrickService.getMatchLineup(clubTeamId, nationalTeam, matchId, source);
            trainingPercentageService.calculateMatchExperiencePoints(detail, lineup, matchPoints)
                .forEach((id, earned) -> {
                    if (playerIds.contains(id)) points.merge(id, earned, Double::sum);
                });
        }
    }

    static double getTournamentPoints(int tournamentId, boolean knockout, long matchesInRound) {
        if (CONTINENTAL_CUPS.contains(tournamentId)) {
            return knockout && matchesInRound > 0 && matchesInRound <= 4 ? 21 : 14;
        }
        return switch (tournamentId) {
            case 4891573 -> 14; // Wildcard rounds
            case 4892549, 5001315 -> knockout && matchesInRound > 0 && matchesInRound <= 2 ? 56 : 28;
            case 4892615, 5001319 -> knockout ? 14 : 7;
            case 4894807 -> 3.5;
            case 6244933, 6260697 -> 7; // Senior and U21 Contenders Leagues
            default -> 0;
        };
    }
}
