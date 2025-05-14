package easyhattrickmanager.service;

import easyhattrickmanager.configuration.EhmConfiguration;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.UpdateExecutionDAO;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.UpdateExecution;
import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UpdateExecutionService {

    private final TaskScheduler taskScheduler;
    private final UpdateService updateService;
    private final UpdateExecutionDAO updateExecutionDAO;
    private final TeamDAO teamDAO;
    private final EhmConfiguration ehmConfiguration;

    public UpdateExecutionService(
        @Qualifier("UpdateExecutionTaskScheduler")
        TaskScheduler taskScheduler,
        UpdateService updateService,
        UpdateExecutionDAO updateExecutionDAO,
        TeamDAO teamDAO,
        EhmConfiguration ehmConfiguration) {
        this.taskScheduler = taskScheduler;
        this.updateService = updateService;
        this.updateExecutionDAO = updateExecutionDAO;
        this.teamDAO = teamDAO;
        this.ehmConfiguration = ehmConfiguration;
    }

    @PostConstruct
    public void initializePendingTasks() {
        updateExecutionDAO.getPending().forEach(this::scheduleTask);
    }

    public void scheduleTask(UpdateExecution updateExecution) {
        Runnable task = () -> processExecution(updateExecution);
        taskScheduler.schedule(task, updateExecution.getExecutionTime().toInstant());
    }

    private void processExecution(UpdateExecution updateExecution) {
        try {
            updateService.update(updateExecution.getTeamId());
            updateExecution.setStatus("OK");
            updateExecutionDAO.update(updateExecution);
        } catch (Exception e) {
            updateExecution.setErrorMessage(e.getMessage());
            if (updateExecution.getRetries() < 3) {
                updateExecution.setStatus("PENDING");
                updateExecution.setRetries(updateExecution.getRetries() + 1);
                updateExecution.setExecutionTime(ZonedDateTime.now().plusHours(updateExecution.getRetries() + 1));
                scheduleTask(updateExecution);
            } else {
                updateExecution.setStatus("ERROR");
            }
            updateExecutionDAO.update(updateExecution);
        }
    }

    @Scheduled(cron = "#{ehmConfiguration.cronAddActiveUpdateExecutions}")
    public void addActiveUpdateExecutions() {
        teamDAO.getActiveTeams().forEach(team -> addUpdateExecution(team.getId()));
    }

    public void addUpdateExecution(int teamId) {
        Team team = teamDAO.get(teamId);
        UpdateExecution updateExecution = UpdateExecution.builder().teamId(team.getId()).executionTime(getNextUpdateExecution(team)).build();
        updateExecutionDAO.insert(updateExecution);
        scheduleTask(updateExecution);
    }

    private ZonedDateTime getNextUpdateExecution(Team team) {
        var league = updateService.getLeague(team.getLeagueId());
        return getNextDayOfWeek(league.getTrainingDate()).plusHours(1);
    }

    private ZonedDateTime getNextDayOfWeek(ZonedDateTime inputDate) {
        DayOfWeek targetDay = inputDate.getDayOfWeek();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = now
            .withHour(inputDate.getHour())
            .withMinute(inputDate.getMinute())
            .withSecond(inputDate.getSecond())
            .withNano(inputDate.getNano());
        int daysToAdd = targetDay.getValue() - now.getDayOfWeek().getValue();
        if (daysToAdd <= 0) {
            daysToAdd += 7;
        }
        return start.plusDays(daysToAdd);
    }

}