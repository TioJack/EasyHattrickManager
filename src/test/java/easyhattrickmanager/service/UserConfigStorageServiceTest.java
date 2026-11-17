package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import easyhattrickmanager.repository.UserConfigDAO;
import easyhattrickmanager.repository.UserPlannerDAO;
import easyhattrickmanager.service.model.dataresponse.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class UserConfigStorageServiceTest {

    @Test
    void persistsAndRestoresCustomPlayersAndParticipationInThePlannerStorage() throws Exception {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        UserConfigDAO configDAO = mock(UserConfigDAO.class);
        UserPlannerDAO plannerDAO = mock(UserPlannerDAO.class);
        AtomicReference<String> configJson = new AtomicReference<>();
        AtomicReference<String> plannerJson = new AtomicReference<>();
        when(configDAO.get(42)).thenAnswer(call -> configJson.get());
        when(plannerDAO.get(42)).thenAnswer(call -> plannerJson.get());
        doAnswer(call -> { configJson.set(call.getArgument(1)); return null; }).when(configDAO).insert(eq(42), anyString());
        doAnswer(call -> { plannerJson.set(call.getArgument(1)); return null; }).when(plannerDAO).insert(eq(42), anyString());
        UserConfigStorageService service = new UserConfigStorageService(configDAO, plannerDAO, mapper);
        PlayerInfo custom = PlayerInfo.builder().id(-1).firstName("Prospect").age(17).ageDays(111)
            .arrivalDate(ZonedDateTime.parse("2026-11-17T12:00:00Z")).scorerSkill(12)
            .playerSubSkill(PlayerSubSkillInfo.builder().scorer(0.75).build()).build();
        ProjectTrainingPlannerInfo planner = ProjectTrainingPlannerInfo.builder()
            .customPlayers(List.of(custom)).trainingPlanPercents(Map.of(-1, List.of(50, 100))).build();
        UserConfig config = UserConfig.builder().projects(List.of(ProjectInfo.builder()
            .name("Plan").teamId(123).iniSeason(90).iniWeek(3).planner(planner).build())).build();

        service.saveUserConfig(42, config);
        assertNotNull(plannerJson.get());
        assertFalse(configJson.get().contains("Prospect"));
        assertTrue(plannerJson.get().contains("Prospect"));
        assertTrue(plannerJson.get().contains("\"customPlayers\""));
        UserConfig restored = service.getUserConfig(42);
        assertNotNull(restored);
        ProjectTrainingPlannerInfo restoredPlanner = restored.getProjects().get(0).getPlanner();
        assertEquals(custom, restoredPlanner.getCustomPlayers().get(0));
        assertEquals(List.of(50, 100), restoredPlanner.getTrainingPlanPercents().get(-1));
    }

    @Test
    void stillLoadsPlannersCreatedBeforeCustomPlayers() throws Exception {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        ProjectTrainingPlannerInfo planner = mapper.readValue("{\"trainingPlans\":[],\"trainingPlanPercents\":{}}", ProjectTrainingPlannerInfo.class);
        assertNull(planner.getCustomPlayers());
        assertTrue(planner.getTrainingPlans().isEmpty());
    }

}
