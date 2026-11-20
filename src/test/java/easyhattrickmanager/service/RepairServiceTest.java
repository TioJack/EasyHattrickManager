package easyhattrickmanager.service;

import static org.mockito.Mockito.*;

import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.PlayerExpDAO;
import easyhattrickmanager.repository.StaffMemberDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.TrainerDAO;
import easyhattrickmanager.repository.TrainingDAO;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.PlayerExp;
import easyhattrickmanager.repository.model.Team;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    @Mock private TeamDAO teamDAO;
    @Mock private PlayerDataDAO playerDataDAO;
    @Mock private PlayerExpDAO playerExpDAO;
    @Mock private TrainingDAO trainingDAO;
    @Mock private TrainerDAO trainerDAO;
    @Mock private StaffMemberDAO staffMemberDAO;
    @Mock private TrainingPercentageService trainingPercentageService;
    @Mock private PlayerExperienceService playerExperienceService;
    @InjectMocks private RepairService service;

    @Test
    void repairsMissingExperienceWithoutLoadingTrainingOrStaff() {
        int teamId = 3227775;
        String week = "S095W01";
        List<PlayerData> players = List.of(
            PlayerData.builder().id(1).seasonWeek(week).teamId(teamId).build(),
            PlayerData.builder().id(2).seasonWeek(week).teamId(teamId).build());
        PlayerExp existing = PlayerExp.builder().id(1).seasonWeek(week).teamId(teamId).exp(5.1).build();
        PlayerExp repaired = PlayerExp.builder().id(2).seasonWeek(week).teamId(teamId).exp(6.2).build();
        Map<Integer, Double> points = Map.of(1, 3.5, 2, 3.5);
        when(teamDAO.getActiveTeams()).thenReturn(List.of(Team.builder().id(teamId).name("epilepticos").build()));
        when(playerDataDAO.get(teamId)).thenReturn(players);
        when(playerExpDAO.get(teamId)).thenReturn(List.of(existing));
        when(trainingPercentageService.calculateWeeklyExperiencePoints(week, teamId)).thenReturn(points);
        when(playerExperienceService.calculatePlayerExp(week, teamId, players, points)).thenReturn(List.of(existing, repaired));

        service.getPlayerExp();

        verify(playerExpDAO).insert(repaired);
        verify(playerExpDAO, never()).insert(existing);
        verifyNoInteractions(trainingDAO, trainerDAO, staffMemberDAO);
        verifyNoMoreInteractions(trainingPercentageService);
    }
}
