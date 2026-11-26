package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import easyhattrickmanager.configuration.EhmConfiguration;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.UpdateExecutionDAO;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.UpdateExecution;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

class UpdateExecutionServiceTest {

    private final TaskScheduler scheduler = mock(TaskScheduler.class);
    private final UpdateService updateService = mock(UpdateService.class);
    private final UpdateExecutionDAO executionDAO = mock(UpdateExecutionDAO.class);
    private final TeamDAO teamDAO = mock(TeamDAO.class);
    private final EhmConfiguration configuration = new EhmConfiguration();
    private final Random random = mock(Random.class);
    private final UpdateExecutionService service = new UpdateExecutionService(
        scheduler, updateService, executionDAO, teamDAO, mock(UserDAO.class), configuration);
    private final ZonedDateTime trainingDate = ZonedDateTime.now().plusDays(2)
        .withHour(4).withMinute(30).withSecond(0).withNano(0);

    @BeforeEach
    void setUp() {
        when(teamDAO.get(42)).thenReturn(Team.builder().id(42).leagueId(1).build());
        when(updateService.getLeague(1)).thenReturn(League.builder().trainingDate(trainingDate).build());
        ReflectionTestUtils.setField(service, "random", random);
    }

    @Test
    void schedulesPrivilegedTeamOneHourAfterTrainingWithoutRandomDelay() {
        configuration.setPrivilegedTeamIds(Set.of(42, 99));

        service.addUpdateExecution(42);

        assertScheduledAt(trainingDate.plusHours(1));
        verifyNoInteractions(random);
    }

    @ParameterizedTest
    @CsvSource({"false,0", "false,119", "true,37"})
    void keepsRandomDelayForTeamsOutsideThePrivilegedList(boolean hasPrivilegedTeams, int delayMinutes) {
        if (hasPrivilegedTeams) {
            configuration.setPrivilegedTeamIds(Set.of(99));
        }
        when(random.nextInt(120)).thenReturn(delayMinutes);

        service.addUpdateExecution(42);

        assertScheduledAt(trainingDate.plusHours(1).plusMinutes(delayMinutes));
        verify(random).nextInt(120);
    }

    private void assertScheduledAt(ZonedDateTime expectedTime) {
        ArgumentCaptor<UpdateExecution> execution = ArgumentCaptor.forClass(UpdateExecution.class);
        verify(executionDAO).insert(execution.capture());
        assertEquals(42, execution.getValue().getTeamId());
        assertEquals(expectedTime, execution.getValue().getExecutionTime());
        verify(scheduler).schedule(any(Runnable.class), eq(expectedTime.toInstant()));
    }
}
