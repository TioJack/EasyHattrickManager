package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import easyhattrickmanager.client.hattrick.model.matchdetail.MatchDetail;
import easyhattrickmanager.client.hattrick.model.matchlineup.MatchLineup;
import easyhattrickmanager.client.hattrick.model.tournamentfixtures.TournamentFixtures;
import easyhattrickmanager.client.hattrick.model.tournamentfixtures.TournamentMatch;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.client.hattrick.model.nationalteammatches.NationalTeamMatches;
import easyhattrickmanager.client.hattrick.model.nationalteammatches.NationalTeamMatch;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.utils.SeasonWeekUtils;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NationalExperienceServiceTest {

    @ParameterizedTest
    @CsvSource({"4878483,false,8,14", "4878490,true,8,14", "4878492,true,4,21", "4878493,true,2,21",
        "4891573,false,0,14", "4892549,false,8,28", "4892549,true,4,28", "4892549,true,2,56", "4892549,true,1,56",
        "4892615,false,8,7", "4892615,true,4,14", "4894807,false,0,3.5", "123,true,1,0", "5001273,false,8,14", "5001277,true,4,21", "5001278,true,2,21", "5001279,true,1,21",
        "5001315,false,8,28", "5001315,true,2,56", "5001319,false,8,7", "5001319,true,4,14",
        "6244933,false,0,7", "6260697,false,0,7"})
    void assignsNationalCompetitionPointsByTournamentAndRound(int tournament, boolean knockout, long matches, double points) {
        assertEquals(points, NationalExperienceService.getTournamentPoints(tournament, knockout, matches), 1e-12);
    }

    @Test
    void retrievesRelevantNationalMatchesInTheWeeklyWindowAndOnlyCountsPlayersFromTheClub() {
        HattrickService hattrick = mock(HattrickService.class);
        TeamDAO teams = mock(TeamDAO.class);
        LeagueDAO leagues = mock(LeagueDAO.class);
        TrainingPercentageService minutes = mock(TrainingPercentageService.class);
        NationalExperienceService service = new NationalExperienceService(hattrick, teams, leagues, minutes);
        ZonedDateTime date = ZonedDateTime.parse("2026-11-15T15:00:00Z");
        var end = SeasonWeekUtils.seasonWeekTrainingDate("S091W03", date);
        when(teams.get(42)).thenReturn(Team.builder().leagueId(1).build());
        when(leagues.get(1)).thenReturn(Optional.of(League.builder().trainingDate(date).build()));
        when(hattrick.getNationalTeamMatches(eq(42), anyInt())).thenReturn(new NationalTeamMatches());
        TournamentFixtures empty = new TournamentFixtures(); empty.setMatches(List.of());
        when(hattrick.getTournamentFixtures(eq(42), anyInt(), eq(91))).thenReturn(empty);
        TournamentMatch match = new TournamentMatch();
        match.setMatchId(123); match.setMatchType(50); match.setGroup(1); match.setMatchRound(1);
        match.setHomeTeamId(3001); match.setAwayTeamId(3002); match.setMatchDate(end.minusDays(1));
        TournamentMatch outsideWindow = new TournamentMatch();
        outsideWindow.setMatchId(456); outsideWindow.setHomeTeamId(3001); outsideWindow.setMatchDate(end);
        TournamentFixtures worldCup = new TournamentFixtures(); worldCup.setMatches(List.of(match, outsideWindow, match));
        when(hattrick.getTournamentFixtures(42, 4892549, 91)).thenReturn(worldCup);
        MatchDetail detail = new MatchDetail();
        var data = new easyhattrickmanager.client.hattrick.model.matchdetail.Match();
        data.setFinishedDate(end.minusHours(20)); detail.setMatch(data);
        MatchLineup lineup = new MatchLineup();
        when(hattrick.getMatchDetail(42, 123, "HTOIntegrated")).thenReturn(detail);
        when(hattrick.getMatchLineup(42, 3001, 123, "HTOIntegrated")).thenReturn(lineup);
        when(minutes.calculateMatchExperiencePoints(detail, lineup, 28)).thenReturn(Map.of(1, 14.0, 999, 28.0));

        assertEquals(Map.of(1, 14.0), service.calculatePoints("S091W03", 42,
            List.of(PlayerData.builder().id(1).nationalTeamId(3001).build())));
        verify(hattrick, times(1)).getMatchDetail(42, 123, "HTOIntegrated");
        verify(hattrick, never()).getMatchDetail(eq(42), eq(456), anyString());
    }

    @ParameterizedTest
    @CsvSource({"12,0,3.5", "10,137,28", "11,137,56", "10,149,28", "11,149,56"})
    void checksNationalMatchesForAssignedPlayersAndUsesTheirActualMinutes(int type, int cupId, double matchPoints) {
        HattrickService hattrick = mock(HattrickService.class);
        TeamDAO teams = mock(TeamDAO.class);
        LeagueDAO leagues = mock(LeagueDAO.class);
        TrainingPercentageService minutes = new TrainingPercentageService(teams, leagues, hattrick);
        NationalExperienceService service = new NationalExperienceService(hattrick, teams, leagues, minutes);
        ZonedDateTime date = ZonedDateTime.parse("2026-11-15T15:00:00Z");
        var end = SeasonWeekUtils.seasonWeekTrainingDate("S091W03", date);
        when(teams.get(42)).thenReturn(Team.builder().leagueId(1).build());
        when(leagues.get(1)).thenReturn(Optional.of(League.builder().trainingDate(date).build()));
        TournamentFixtures empty = new TournamentFixtures();
        when(hattrick.getTournamentFixtures(eq(42), anyInt(), eq(91))).thenReturn(empty);
        NationalTeamMatch match = new NationalTeamMatch();
        match.setMatchId(123); match.setHomeTeamId(3001); match.setAwayTeamId(3002);
        match.setMatchType(type); match.setMatchContextId(cupId); match.setMatchDate(end.minusDays(1));
        NationalTeamMatch outside = new NationalTeamMatch();
        outside.setMatchId(456); outside.setHomeTeamId(3001); outside.setMatchType(12); outside.setMatchDate(end);
        NationalTeamMatches matches = new NationalTeamMatches(); matches.setMatches(List.of(match, outside, match));
        when(hattrick.getNationalTeamMatches(eq(42), anyInt())).thenReturn(matches);
        var detail = new MatchDetail();
        var data = new easyhattrickmanager.client.hattrick.model.matchdetail.Match();
        data.setMatchDate(end.minusDays(1)); data.setFinishedDate(end.minusDays(1).plusMinutes(105)); detail.setMatch(data);
        var lineup = new MatchLineup();
        var team = new easyhattrickmanager.client.hattrick.model.matchlineup.Team();
        var assigned = new easyhattrickmanager.client.hattrick.model.matchlineup.Player();
        assigned.setPlayerId(1); assigned.setRoleId(100);
        var unassigned = new easyhattrickmanager.client.hattrick.model.matchlineup.Player();
        unassigned.setPlayerId(2); unassigned.setRoleId(101);
        team.setStartingLineup(List.of(assigned, unassigned)); team.setLineup(List.of(unassigned));
        var substitution = new easyhattrickmanager.client.hattrick.model.matchlineup.Substitution();
        substitution.setSubjectPlayerId(1); substitution.setObjectPlayerId(0);
        substitution.setOrderType(1); substitution.setMatchMinute(45);
        team.setSubstitutions(List.of(substitution)); lineup.setTeam(team);
        when(hattrick.getMatchDetail(42, 123, "Hattrick")).thenReturn(detail);
        when(hattrick.getMatchLineup(42, 3001, 123, "Hattrick")).thenReturn(lineup);

        assertEquals(Map.of(1, matchPoints / 2), service.calculatePoints("S091W03", 42,
            List.of(PlayerData.builder().id(1).nationalTeamId(3001).build(), PlayerData.builder().id(2).build())));
        verify(hattrick).getMatchDetail(42, 123, "Hattrick");
        verify(hattrick, never()).getMatchDetail(eq(42), eq(456), anyString());
        verify(hattrick, never()).getTournamentFixtures(eq(42), eq(137), anyInt());
        verify(hattrick, never()).getTournamentFixtures(eq(42), eq(149), anyInt());
    }

    @Test
    void doesNotFetchTournamentsWhenThereAreNoNationalPlayers() {
        HattrickService hattrick = mock(HattrickService.class);
        var service = new NationalExperienceService(hattrick, mock(TeamDAO.class), mock(LeagueDAO.class), mock(TrainingPercentageService.class));
        assertTrue(service.calculatePoints("S091W03", 42, List.of(PlayerData.builder().id(1).caps(12).capsU21(20).build())).isEmpty());
        verifyNoInteractions(hattrick);
    }
}
