package easyhattrickmanager.service;

import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.UpdateExecutionDAO;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.UpdateExecution;
import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateExecutionService {

    private final TaskScheduler taskScheduler;
    private final UpdateService updateService;
    private final UpdateExecutionDAO updateExecutionDAO;
    private final TeamDAO teamDAO;

    @PostConstruct
    public void initializePendingTasks() {
        updateExecutionDAO.getPending().forEach(this::scheduleTask);
    }

    public void scheduleTask(UpdateExecution updateExecution) {
        Runnable task = () -> processExecution(updateExecution);
        taskScheduler.schedule(task, updateExecution.getExecutionTime().atZone(ZoneId.systemDefault()).toInstant());
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
                updateExecution.setExecutionTime(LocalDateTime.now().plusHours(updateExecution.getRetries() + 1));
                scheduleTask(updateExecution);
            } else {
                updateExecution.setStatus("ERROR");
            }
            updateExecutionDAO.update(updateExecution);
        }
    }

    private void insert(Team team) {
        UpdateExecution updateExecution = UpdateExecution.builder().teamId(team.getId()).executionTime(getNextUpdateExecution(team)).build();
        updateExecution.setExecutionTime(LocalDateTime.now().plusMinutes(-10));
        updateExecutionDAO.insert(updateExecution);
    }

    private LocalDateTime getNextUpdateExecution(Team team) {
        var league = updateService.getLeague(team.getLeagueId());
        return getNextDayOfWeek(league.getTrainingDate()).plusHours(1);
    }

    private LocalDateTime getNextDayOfWeek(LocalDateTime inputDate) {
        DayOfWeek targetDay = inputDate.getDayOfWeek();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now
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