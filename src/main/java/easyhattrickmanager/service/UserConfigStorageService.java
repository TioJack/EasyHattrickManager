package easyhattrickmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import easyhattrickmanager.repository.UserConfigDAO;
import easyhattrickmanager.repository.UserPlannerDAO;
import easyhattrickmanager.service.model.dataresponse.ProjectInfo;
import easyhattrickmanager.service.model.dataresponse.ProjectPlannerInfo;
import easyhattrickmanager.service.model.dataresponse.UserConfig;
import easyhattrickmanager.service.model.dataresponse.UserPlanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserConfigStorageService {

    private final UserConfigDAO userConfigDAO;
    private final UserPlannerDAO userPlannerDAO;
    private final ObjectMapper objectMapper;

    public UserConfig getUserConfig(int userId) {
        try {
            String rawConfig = userConfigDAO.get(userId);
            if (Objects.isNull(rawConfig)) {
                return null;
            }
            UserConfig userConfig = objectMapper.readValue(rawConfig, UserConfig.class);
            String rawPlanner = userPlannerDAO.get(userId);
            if (Objects.nonNull(rawPlanner)) {
                UserPlanner userPlanner = objectMapper.readValue(rawPlanner, UserPlanner.class);
                mergePlanner(userConfig, userPlanner);
            } else if (hasPlannerData(userConfig)) {
                // Lazy migration for legacy rows that still keep planner data inside user_config.
                saveUserConfig(userId, userConfig);
            }
            return userConfig;
        } catch (Exception e) {
            System.err.printf("Error getUserConfig %s. %s%n", userId, e.getMessage());
            return null;
        }
    }

    @Transactional
    public void saveUserConfig(int userId, UserConfig userConfig) {
        try {
            String sanitizedConfig = objectMapper.writeValueAsString(removePlanner(userConfig));
            String plannerConfig = objectMapper.writeValueAsString(extractPlanner(userConfig));
            upsertUserConfig(userId, sanitizedConfig);
            upsertUserPlanner(userId, plannerConfig);
        } catch (Exception e) {
            System.err.printf("Error saveUserConfig %s %s. %s%n", userId, userConfig, e.getMessage());
        }
    }

    private void upsertUserConfig(int userId, String config) {
        if (Objects.isNull(userConfigDAO.get(userId))) {
            userConfigDAO.insert(userId, config);
            return;
        }
        userConfigDAO.update(userId, config);
    }

    private void upsertUserPlanner(int userId, String plannerConfig) {
        if (Objects.isNull(userPlannerDAO.get(userId))) {
            userPlannerDAO.insert(userId, plannerConfig);
            return;
        }
        userPlannerDAO.update(userId, plannerConfig);
    }

    private UserConfig removePlanner(UserConfig userConfig) {
        if (Objects.isNull(userConfig)) {
            return null;
        }
        List<ProjectInfo> projects = userConfig.getProjects();
        List<ProjectInfo> sanitizedProjects = Objects.isNull(projects)
            ? null
            : projects.stream()
                .map(project -> ProjectInfo.builder()
                    .name(project.getName())
                    .teamId(project.getTeamId())
                    .iniSeason(project.getIniSeason())
                    .iniWeek(project.getIniWeek())
                    .endSeason(project.getEndSeason())
                    .endWeek(project.getEndWeek())
                    .filter(project.getFilter())
                    .sort(project.getSort())
                    .planner(null)
                    .build())
                .toList();
        return UserConfig.builder()
            .languageId(userConfig.getLanguageId())
            .currency(userConfig.getCurrency())
            .dateFormat(userConfig.getDateFormat())
            .showTrainingInfo(userConfig.isShowTrainingInfo())
            .showSubSkills(userConfig.isShowSubSkills())
            .projects(sanitizedProjects)
            .build();
    }

    private UserPlanner extractPlanner(UserConfig userConfig) {
        if (Objects.isNull(userConfig) || Objects.isNull(userConfig.getProjects())) {
            return UserPlanner.builder().projects(null).build();
        }
        return UserPlanner.builder()
            .projects(userConfig.getProjects().stream()
                .map(project -> ProjectPlannerInfo.builder()
                    .name(project.getName())
                    .teamId(project.getTeamId())
                    .iniSeason(project.getIniSeason())
                    .iniWeek(project.getIniWeek())
                    .endSeason(project.getEndSeason())
                    .endWeek(project.getEndWeek())
                    .planner(project.getPlanner())
                    .build())
                .toList())
            .build();
    }

    private void mergePlanner(UserConfig userConfig, UserPlanner userPlanner) {
        if (Objects.isNull(userConfig) || Objects.isNull(userConfig.getProjects()) || Objects.isNull(userPlanner) || Objects.isNull(userPlanner.getProjects())) {
            return;
        }
        List<ProjectPlannerInfo> plannerProjects = userPlanner.getProjects();
        List<ProjectInfo> mergedProjects = new ArrayList<>();
        for (ProjectInfo project : userConfig.getProjects()) {
            ProjectPlannerInfo plannerProject = findPlannerProject(project, plannerProjects);
            if (Objects.nonNull(plannerProject)) {
                project.setPlanner(plannerProject.getPlanner());
            }
            mergedProjects.add(project);
        }
        userConfig.setProjects(mergedProjects);
    }

    private ProjectPlannerInfo findPlannerProject(ProjectInfo project, List<ProjectPlannerInfo> plannerProjects) {
        return plannerProjects.stream()
            .filter(plannerProject -> plannerProject.getTeamId() == project.getTeamId())
            .filter(plannerProject -> Objects.equals(plannerProject.getName(), project.getName()))
            .filter(plannerProject -> plannerProject.getIniSeason() == project.getIniSeason())
            .filter(plannerProject -> plannerProject.getIniWeek() == project.getIniWeek())
            .filter(plannerProject -> Objects.equals(plannerProject.getEndSeason(), project.getEndSeason()))
            .filter(plannerProject -> Objects.equals(plannerProject.getEndWeek(), project.getEndWeek()))
            .findFirst()
            .orElse(null);
    }

    private boolean hasPlannerData(UserConfig userConfig) {
        return Objects.nonNull(userConfig)
            && Objects.nonNull(userConfig.getProjects())
            && userConfig.getProjects().stream().anyMatch(project -> Objects.nonNull(project.getPlanner()));
    }
}
