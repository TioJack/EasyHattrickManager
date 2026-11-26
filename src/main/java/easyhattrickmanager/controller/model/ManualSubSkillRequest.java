package easyhattrickmanager.controller.model;

import java.util.List;

public record ManualSubSkillRequest(Integer teamId, Integer season, Integer week, List<Change> changes) {
    public record Change(Integer playerId, String skill, Double oldValue, Double newValue) {}
}
