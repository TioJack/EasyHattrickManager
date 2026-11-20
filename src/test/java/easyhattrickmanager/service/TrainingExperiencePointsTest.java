package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import easyhattrickmanager.client.hattrick.model.matchdetail.Booking;
import easyhattrickmanager.client.hattrick.model.matchdetail.Injury;
import easyhattrickmanager.client.hattrick.model.matchdetail.MatchDetail;
import easyhattrickmanager.client.hattrick.model.matchesarchive.Match;
import easyhattrickmanager.client.hattrick.model.matchesarchive.MatchesArchive;
import easyhattrickmanager.client.hattrick.model.matchlineup.MatchLineup;
import easyhattrickmanager.client.hattrick.model.matchlineup.Player;
import easyhattrickmanager.client.hattrick.model.matchlineup.Substitution;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.Training;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrainingExperiencePointsTest {

    private final TeamDAO teamDAO = mock(TeamDAO.class);
    private final LeagueDAO leagueDAO = mock(LeagueDAO.class);
    private final HattrickService hattrick = mock(HattrickService.class);
    private final TrainingPercentageService service = new TrainingPercentageService(teamDAO, leagueDAO, hattrick);
    private final Training training = Training.builder().trainingType(4).trainingLevel(40).staminaTrainingPart(50).build();
    private final ZonedDateTime date = ZonedDateTime.parse("2026-11-15T15:00:00Z");

    @BeforeEach
    void setUp() {
        when(teamDAO.get(42)).thenReturn(Team.builder().id(42).leagueId(1).build());
        when(leagueDAO.get(1)).thenReturn(Optional.of(League.builder().trainingDate(date).build()));
    }

    @Test
    void capsMinutesPerMatchAndStillCountsOtherMatchesThatWeek() {
        Match league = match(100, 1, 120);
        Match friendly = match(200, 8, 90);
        archive(List.of(league, friendly, league));

        var result = service.calculateWeeklyTraining("S091W03", 42, training, 5, 0);
        assertEquals(4.2, result.experiencePoints().get(1), 1e-12);
        verify(hattrick, times(1)).getMatchDetail(42, 100);
    }

    @Test
    void calculatesWeeklyExperienceWithoutTrainingSettings() {
        Match league = match(100, 1, 120);
        Match friendly = match(200, 8, 90);
        archive(List.of(league, friendly, league));

        var result = service.calculateWeeklyExperiencePoints("S091W03", 42);

        assertEquals(4.2, result.get(1), 1e-12);
        verify(hattrick, times(1)).getMatchDetail(42, 100);
    }

    @Test
    void calculatesWeeklyExperienceWithoutMatches() {
        archive(null);
        assertTrue(service.calculateWeeklyExperiencePoints("S091W03", 42).isEmpty());
    }

    @Test
    void sumsPositionIntervalsBeforeApplyingTheMatchCap() {
        Match match = match(100, 1, 90);
        MatchLineup lineup = hattrick.getMatchLineup(42, 100);
        lineup.getTeam().setStartingLineup(List.of(player(1, 100), player(2, 101)));
        lineup.getTeam().setSubstitutions(List.of(substitution(1, 2, 3, 30), substitution(1, 3, 1, 60)));
        archive(List.of(match));

        var result = service.calculateWeeklyTraining("S091W03", 42, training, 5, 0).experiencePoints();
        assertEquals(3.5 * 60 / 90, result.get(1), 1e-12);
        assertEquals(3.5, result.get(2), 1e-12);
        assertEquals(3.5 * 30 / 90, result.get(3), 1e-12);
    }

    @Test
    void stopsCountingOnRedCardsAndDeparturesWithoutReplacement() {
        Match match = match(100, 1, 90);
        MatchLineup lineup = hattrick.getMatchLineup(42, 100);
        lineup.getTeam().setStartingLineup(List.of(player(1, 100), player(2, 101)));
        lineup.getTeam().setSubstitutions(List.of(substitution(2, 0, 1, 45)));
        Booking red = new Booking();
        red.setPlayerId(1); red.setType(2); red.setMinute(30);
        Booking yellow = new Booking();
        yellow.setPlayerId(2); yellow.setType(1); yellow.setMinute(10);
        hattrick.getMatchDetail(42, 100).getMatch().setBookings(List.of(red, yellow));
        archive(List.of(match));

        var result = service.calculateWeeklyTraining("S091W03", 42, training, 5, 0).experiencePoints();
        assertEquals(3.5 / 3, result.get(1), 1e-12);
        assertEquals(1.75, result.get(2), 1e-12);
    }

    @Test
    void supportsWeeksWithoutMatches() {
        archive(null);
        var result = service.calculateWeeklyTraining("S091W03", 42, training, 5, 0);
        assertTrue(result.experiencePoints().isEmpty());
        assertTrue(result.players().isEmpty());
    }

    @Test
    void stopsCountingOnInjuriesButNotBruises() {
        Match match = match(100, 1, 90);
        var lineup = hattrick.getMatchLineup(42, 100);
        lineup.getTeam().setStartingLineup(List.of(player(1, 100), player(2, 101)));
        Injury injured = new Injury(); injured.setPlayerId(1); injured.setType(2); injured.setMinute(45);
        Injury bruised = new Injury(); bruised.setPlayerId(2); bruised.setType(1); bruised.setMinute(20);
        hattrick.getMatchDetail(42, 100).getMatch().setInjuries(List.of(injured, bruised));
        archive(List.of(match));
        var result = service.calculateWeeklyTraining("S091W03", 42, training, 5, 0).experiencePoints();
        assertEquals(1.75, result.get(1), 1e-12);
        assertEquals(3.5, result.get(2), 1e-12);
    }

    @Test
    void readsBookingMinutesFromTheMatchDetailsXml() throws Exception {
        XmlMapper xml = XmlMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
        MatchDetail match = xml.readValue("""
            <HattrickData><Match><Bookings Count="1"><Booking Index="0">
            <BookingPlayerID>123</BookingPlayerID><BookingType>2</BookingType><BookingMinute>45</BookingMinute>
            </Booking></Bookings></Match></HattrickData>
            """, MatchDetail.class);
        assertEquals(123, match.getMatch().getBookings().get(0).getPlayerId());
        assertEquals(45, match.getMatch().getBookings().get(0).getMinute());
        MatchDetail empty = xml.readValue("<HattrickData><Match><Bookings/></Match></HattrickData>", MatchDetail.class);
        assertTrue(empty.getMatch().getBookings() == null || empty.getMatch().getBookings().isEmpty());
    }

    @Test
    void readsTournamentFixturesIncludingTheKnockoutRound() throws Exception {
        XmlMapper xml = XmlMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
        var fixtures = xml.readValue("""
            <HattrickData><Matches><Match><MatchId>123</MatchId><HomeTeamId>3001</HomeTeamId>
            <AwayTeamId>3002</AwayTeamId><MatchRound>4</MatchRound><Group>0</Group><MatchType>51</MatchType>
            </Match></Matches></HattrickData>
            """, easyhattrickmanager.client.hattrick.model.tournamentfixtures.TournamentFixtures.class);
        assertEquals(123, fixtures.getMatches().get(0).getMatchId());
        assertEquals(4, fixtures.getMatches().get(0).getMatchRound());
        assertEquals(0, fixtures.getMatches().get(0).getGroup());
    }

    @Test
    void readsNationalMatchesWithTheirCupContext() throws Exception {
        XmlMapper xml = XmlMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
        var matches = xml.readValue("""
            <HattrickData><LeagueOfficeTypeID>4</LeagueOfficeTypeID><Matches><Match>
            <MatchID>123</MatchID><HomeTeamId>3001</HomeTeamId><AwayTeamId>3002</AwayTeamId>
            <MatchType>11</MatchType><MatchContextId>149</MatchContextId>
            </Match></Matches></HattrickData>
            """, easyhattrickmanager.client.hattrick.model.nationalteammatches.NationalTeamMatches.class);
        var match = matches.getMatches().get(0);
        assertEquals(123, match.getMatchId());
        assertEquals(3001, match.getHomeTeamId());
        assertEquals(3002, match.getAwayTeamId());
        assertEquals(149, match.getMatchContextId());
        assertEquals(11, match.getMatchType());
    }

    private void archive(List<Match> matches) {
        MatchesArchive archive = new MatchesArchive();
        var team = new easyhattrickmanager.client.hattrick.model.matchesarchive.Team();
        team.setMatches(matches); archive.setTeam(team);
        when(hattrick.getMatchesArchive(eq(42), any(), any())).thenReturn(archive);
    }

    private Match match(int id, int type, int minutes) {
        Match match = new Match(); match.setMatchId(id); match.setMatchType(type);
        MatchDetail detail = new MatchDetail();
        var data = new easyhattrickmanager.client.hattrick.model.matchdetail.Match();
        data.setMatchDate(date); data.setFinishedDate(date.plusMinutes(minutes + 15));
        detail.setMatch(data);
        MatchLineup lineup = new MatchLineup();
        var team = new easyhattrickmanager.client.hattrick.model.matchlineup.Team();
        team.setStartingLineup(List.of(player(1, 100))); team.setLineup(List.of(player(1, 100)));
        lineup.setTeam(team);
        when(hattrick.getMatchDetail(42, id)).thenReturn(detail);
        when(hattrick.getMatchLineup(42, id)).thenReturn(lineup);
        return match;
    }

    private Player player(int id, int role) {
        Player player = new Player(); player.setPlayerId(id); player.setRoleId(role); return player;
    }

    private Substitution substitution(int subject, int object, int type, int minute) {
        Substitution substitution = new Substitution();
        substitution.setSubjectPlayerId(subject); substitution.setObjectPlayerId(object);
        substitution.setOrderType(type); substitution.setMatchMinute(minute); substitution.setNewPositionId(101);
        return substitution;
    }
}
