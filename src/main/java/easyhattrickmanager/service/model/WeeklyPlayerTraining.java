package easyhattrickmanager.service.model;

import easyhattrickmanager.repository.model.PlayerTraining;
import java.util.List;
import java.util.Map;

public record WeeklyPlayerTraining(List<PlayerTraining> players, Map<Integer, Double> experiencePoints) {
}
