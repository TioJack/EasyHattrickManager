package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.PlayerExpDAO;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.PlayerExp;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerExperienceServiceTest {

    private final PlayerExpDAO expDAO = mock(PlayerExpDAO.class);
    private final PlayerDataDAO dataDAO = mock(PlayerDataDAO.class);
    private final NationalExperienceService national = mock(NationalExperienceService.class);
    private final PlayerExperienceService service = new PlayerExperienceService(expDAO, dataDAO, national);

    @BeforeEach
    void setUp() {
        when(expDAO.get(42)).thenReturn(List.of());
        when(dataDAO.get(42)).thenReturn(List.of());
    }

    @Test
    void startsAnUnknownSublevelAtZeroAndAddsTheWeeksPoints() {
        var result = service.calculatePlayerExp("S091W03", 42,
            List.of(player(1, 6), player(2, 0), player(3, 0)), Map.of(1, 3.85, 2, 0.35));
        assertEquals(6.0385, result.get(0).getExp(), 1e-12);
        assertEquals(0.0035, result.get(1).getExp(), 1e-12);
        assertEquals(0, result.get(2).getExp(), 1e-12);
        assertEquals(42, result.get(0).getTeamId());
        assertEquals("S091W03", result.get(0).getSeasonWeek());
    }

    @Test
    void carriesPreviousExperienceAndKeepsOverflowOnLevelUps() {
        when(expDAO.get(42)).thenReturn(List.of(previous(1, 6.98), previous(2, 6.20)));
        var result = service.calculatePlayerExp("S091W03", 42,
            List.of(player(1, 7), player(2, 6)), Map.of(1, 3.5, 2, 0.7));
        assertEquals(7.015, result.get(0).getExp(), 1e-12);
        assertEquals(6.207, result.get(1).getExp(), 1e-12);
    }

    @Test
    void addsNationalAndClubPointsWithoutChangingTheCallerMap() {
        when(national.calculatePoints(org.mockito.ArgumentMatchers.eq("S091W03"), org.mockito.ArgumentMatchers.eq(42),
            org.mockito.ArgumentMatchers.anyList())).thenReturn(Map.of(1, 14.0));
        Map<Integer, Double> clubPoints = Map.of(1, 3.5);
        var result = service.calculatePlayerExp("S091W03", 42, List.of(player(1, 6)), clubPoints);
        assertEquals(6.175, result.get(0).getExp(), 1e-12);
        assertEquals(Map.of(1, 3.5), clubPoints);
    }

    @Test
    void anchorsToTheObservedLevelWhenThePreviousSublevelWasUnderestimated() {
        when(dataDAO.get(42)).thenReturn(List.of(PlayerData.builder().id(1).seasonWeek("S091W02").experience(6).build()));
        var result = service.calculatePlayerExp("S091W03", 42, List.of(player(1, 7)), Map.of(1, 3.5));
        assertEquals(7.0, result.get(0).getExp(), 1e-12);
    }

    @Test
    void doesNotInventALevelUpAndDoesNotAddTheSameWeekTwice() {
        when(expDAO.get(42)).thenReturn(List.of(previous(1, 6.99),
            PlayerExp.builder().id(1).seasonWeek("S091W03").exp(6.5).build()));
        var result = service.calculatePlayerExp("S091W03", 42, List.of(player(1, 6)), Map.of(1, 3.5));
        assertTrue(result.get(0).getExp() < 7);
        assertTrue(result.get(0).getExp() >= 6.99);
        assertEquals(result, service.calculatePlayerExp("S091W03", 42, List.of(player(1, 6)), Map.of(1, 3.5)));
    }

    @Test
    void supportsASeasonBoundaryAndPlayersWhoDidNotPlay() {
        when(expDAO.get(42)).thenReturn(List.of(PlayerExp.builder().id(1).seasonWeek("S090W16").exp(3.1234).build()));
        var result = service.calculatePlayerExp("S091W01", 42, List.of(player(1, 3)), null);
        assertEquals(3.1234, result.get(0).getExp(), 1e-12);
        assertTrue(service.calculatePlayerExp("S091W01", 42, null, Map.of()).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"1,0,3.5", "2,0,7", "3,1,7", "3,2,1.75", "3,3,1.75", "4,0,0.35", "5,0,0.35",
        "7,0,17.5", "8,0,0.7", "9,0,0.7", "12,0,3.5", "50,0,0", "51,0,0", "61,0,0", "62,0,0",
        "80,0,0", "100,0,3.5", "101,0,0.35", "103,0,0.35", "105,0,0.35", "106,0,0.35", "6,0,0", "102,0,0"})
    void assignsPointsByMatchTypeAndCupLevel(int type, int cupLevel, double points) {
        assertEquals(points, PlayerExperienceService.getMatchPoints(type, cupLevel), 1e-12);
    }

    @ParameterizedTest
    @CsvSource({"3,183,17.5", "7,183,17.5", "10,137,28", "11,137,56", "10,149,28", "11,149,56",
        "10,0,0", "11,0,0", "50,137,0", "1,183,3.5"})
    void distinguishesCupIdsFromTournamentIds(int type, int cupId, double points) {
        assertEquals(points, PlayerExperienceService.getMatchPoints(type, 1, cupId), 1e-12);
    }

    private PlayerData player(int id, int experience) {
        return PlayerData.builder().id(id).experience(experience).build();
    }

    private PlayerExp previous(int id, double exp) {
        return PlayerExp.builder().id(id).seasonWeek("S091W02").exp(exp).build();
    }
}
