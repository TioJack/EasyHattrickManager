package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import easyhattrickmanager.controller.model.ManualSubSkillRequest;
import easyhattrickmanager.controller.model.ManualSubSkillRequest.Change;
import easyhattrickmanager.repository.*;
import easyhattrickmanager.repository.model.*;
import easyhattrickmanager.service.model.dataresponse.mapper.PlayerInfoMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

class ManualSubSkillServiceTest {
    UserDAO users = mock(UserDAO.class);
    TeamDAO teams = mock(TeamDAO.class);
    LeagueDAO leagues = mock(LeagueDAO.class);
    PlayerDAO players = mock(PlayerDAO.class);
    PlayerDataDAO data = mock(PlayerDataDAO.class);
    PlayerSubSkillDAO subs = mock(PlayerSubSkillDAO.class);
    PlayerFormDAO forms = mock(PlayerFormDAO.class);
    PlayerExpDAO exps = mock(PlayerExpDAO.class);
    ManualSubSkillDAO writes = mock(ManualSubSkillDAO.class);
    PlayerInfoMapper mapper = mock(PlayerInfoMapper.class);
    ManualSubSkillService service = new ManualSubSkillService(users, teams, leagues, players, data, subs, forms, exps, writes, mapper);
    PlayerData player;

    @BeforeEach
    void setup() {
        when(users.get("manager")).thenReturn(User.builder().id(1).build());
        when(teams.getByUserId(1)).thenReturn(List.of(Team.builder().id(2).leagueId(3).build()));
        when(leagues.get(3)).thenReturn(Optional.of(League.builder().seasonOffset(-7).build()));
        when(writes.lockPlayer(2, "S097W01", 4)).thenReturn(true);
        player = PlayerData.builder().id(4).teamId(2).seasonWeek("S097W01").age(20).ageDays(42)
            .keeperSkill(5).defenderSkill(6).playmakerSkill(7).wingerSkill(5).passingSkill(6).scorerSkill(5)
            .setPiecesSkill(4).staminaSkill(7).playerForm(7).experience(6).build();
        when(data.getPlayer(2, 4)).thenReturn(List.of(player));
        when(subs.getPlayer(2, 4)).thenReturn(List.of(PlayerSubSkill.builder().id(4).teamId(2)
            .seasonWeek("S097W01").keeper(0.123456).defender(0.5).build()));
        when(players.getPlayer(4)).thenReturn(Player.builder().id(4).build());
    }

    ManualSubSkillRequest request(Change... changes) { return new ManualSubSkillRequest(2, 90, 1, List.of(changes)); }

    @Test
    void savesOnlyRequestedFractionsAndRecordsExactPreviousValue() {
        service.save("manager", request(new Change(4, "keeper", 0.123456, 0.75)));
        var saved = ArgumentCaptor.forClass(PlayerSubSkill.class);
        verify(writes).saveSkills(saved.capture());
        assertEquals(0.75, saved.getValue().getKeeper());
        assertEquals(0.5, saved.getValue().getDefender());
        assertTrue(saved.getValue().getHtms() > 0);
        verify(writes).recordChange(1, 2, "S097W01", 4, "keeper", 0.123456, 0.75);
        assertEquals(5, player.getKeeperSkill());
    }

    @Test
    void rejectsOtherUsersTeamsBeforeReadingOrWritingPlayerData() {
        when(teams.getByUserId(1)).thenReturn(List.of());
        assertEquals(403, assertThrows(ResponseStatusException.class,
            () -> service.save("manager", request(new Change(4, "keeper", 0.1, 0.2)))).getStatusCode().value());
        verifyNoInteractions(writes, data);
    }

    @Test
    void rejectsStaleDraftWithoutOverwritingCurrentSublevel() {
        assertEquals(409, assertThrows(ResponseStatusException.class,
            () -> service.save("manager", request(new Change(4, "keeper", 0.1, 0.2)))).getStatusCode().value());
        verify(writes, never()).saveSkills(any());
        verify(writes, never()).recordChange(anyInt(), anyInt(), anyString(), anyInt(), anyString(), anyDouble(), anyDouble());
    }

    @Test
    void rejectsInvalidUnknownAndDuplicateFieldsBeforeAnyDatabaseAccess() {
        for (var change : List.of(new Change(4, "keeper", 0.1, -0.01), new Change(4, "keeper", 0.1, 1.0),
            new Change(4, "keeper", 0.1, Double.NaN), new Change(4, "keeper", 0.1, 0.123),
            new Change(4, "htms", 0.1, 0.2), new Change(null, "keeper", 0.1, 0.2))) {
            assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.save("manager", request(change))).getStatusCode().value());
        }
        var change = new Change(4, "keeper", 0.1, 0.2);
        assertThrows(ResponseStatusException.class, () -> service.save("manager", request(change, change)));
        verifyNoInteractions(users, writes);
    }

    @Test
    void rejectsPlayersOutsideTheSelectedWeek() {
        when(writes.lockPlayer(2, "S097W01", 4)).thenReturn(false);
        assertEquals(400, assertThrows(ResponseStatusException.class,
            () -> service.save("manager", request(new Change(4, "keeper", 0.1, 0.2)))).getStatusCode().value());
        verifyNoInteractions(data);
    }

    @Test
    void savesFormAndExperienceWithoutChangingTheirIntegerLevelsOrHiddenForm() {
        when(forms.getPlayer(2, 4)).thenReturn(List.of(PlayerForm.builder().seasonWeek("S097W01")
            .form(8.4).hiddenForm(6.3).expectedForm(7.2).build()));
        when(exps.getPlayer(2, 4)).thenReturn(List.of(PlayerExp.builder().seasonWeek("S097W01").exp(6.25).build()));
        service.save("manager", request(new Change(4, "form", 0.99, 0.35), new Change(4, "experience", 0.25, 0.5)));
        verify(writes).saveForm(2, "S097W01", 4, 7.35);
        verify(writes).saveExperience(2, "S097W01", 4, 6.5);
        verify(writes).recordChange(1, 2, "S097W01", 4, "form", 0.99, 0.35);
        verify(writes, never()).saveSkills(any());
    }

    @Test
    void doesNotCreateHistoryForUnchangedValues() {
        service.save("manager", request(new Change(4, "defender", 0.5, 0.5)));
        verify(writes, never()).saveSkills(any());
        verify(writes, never()).recordChange(anyInt(), anyInt(), anyString(), anyInt(), anyString(), anyDouble(), anyDouble());
    }

    @Test
    void refusesSublevelsAboveMaximumStaminaAndForm() {
        player.setStaminaSkill(9);
        player.setPlayerForm(8);
        for (String skill : List.of("stamina", "form")) {
            assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.save("manager", request(new Change(4, skill, 0.0, 0.1)))).getStatusCode().value());
        }
    }

    @Test
    void createsMissingSubskillRowsAndLocksPlayersInStableOrder() {
        when(subs.getPlayer(2, 4)).thenReturn(List.of());
        service.save("manager", request(new Change(4, "keeper", 0.0, 0.25)));
        var ordered = inOrder(writes, subs);
        ordered.verify(writes).lockPlayer(2, "S097W01", 4);
        ordered.verify(subs).getPlayer(2, 4);
        ordered.verify(writes).recordChange(1, 2, "S097W01", 4, "keeper", 0.0, 0.25);
        ordered.verify(writes).saveSkills(any());
    }
}
