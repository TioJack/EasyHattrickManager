package easyhattrickmanager.service;

import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.PlayerExpDAO;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.PlayerExp;
import easyhattrickmanager.utils.SeasonWeekUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerExperienceService {

    private final PlayerExpDAO playerExpDAO;
    private final PlayerDataDAO playerDataDAO;
    private final NationalExperienceService nationalExperienceService;

    public List<PlayerExp> calculatePlayerExp(String seasonWeek, int teamId, List<PlayerData> players,
        Map<Integer, Double> experiencePoints) {
        if (players == null || players.isEmpty()) {
            return List.of();
        }
        String previousWeek = SeasonWeekUtils.previous(seasonWeek);
        Map<Integer, PlayerExp> previousExp = playerExpDAO.get(teamId).stream()
            .filter(exp -> previousWeek.equals(exp.getSeasonWeek()))
            .collect(Collectors.toMap(PlayerExp::getId, Function.identity(), (a, b) -> a));
        Map<Integer, PlayerData> previousData = playerDataDAO.get(teamId).stream()
            .filter(player -> previousWeek.equals(player.getSeasonWeek()))
            .collect(Collectors.toMap(PlayerData::getId, Function.identity(), (a, b) -> a));
        Map<Integer, Double> points = new HashMap<>(experiencePoints == null ? Map.of() : experiencePoints);
        nationalExperienceService.calculatePoints(seasonWeek, teamId, players)
            .forEach((id, earned) -> points.merge(id, earned, Double::sum));

        return players.stream().map(player -> {
            PlayerExp previous = previousExp.get(player.getId());
            // An unknown sublevel starts at zero; preserve the known integer experience.
            double initial = previous == null
                ? previousData.getOrDefault(player.getId(), player).getExperience()
                : previous.getExp();
            double calculated = initial + Math.max(0, points.getOrDefault(player.getId(), 0.0)) / 100.0;
            // The observed integer level anchors the estimate when a level-up reveals missing history.
            double exp = Math.max(player.getExperience(), Math.min(Math.nextDown(player.getExperience() + 1.0), calculated));
            return PlayerExp.builder().id(player.getId()).seasonWeek(seasonWeek).teamId(teamId).exp(exp).build();
        }).toList();
    }

    public static double getMatchPoints(int matchType, int cupLevel) {
        return getMatchPoints(matchType, cupLevel, 0);
    }

    public static double getMatchPoints(int matchType, int cupLevel, int cupId) {
        if ((matchType == 3 || matchType == 7) && cupId == 183) {
            return 17.5;
        }
        if (cupId == 137 || cupId == 149) {
            if (matchType == 10) return 28;
            if (matchType == 11) return 56;
        }
        return switch (matchType) {
            case 1, 100 -> 3.5;
            case 2 -> 7;
            case 3 -> cupLevel > 1 ? 1.75 : 7;
            case 4, 5, 101, 103, 105, 106 -> 0.35;
            case 7 -> 17.5;
            case 8, 9 -> 0.7;
            case 12 -> 3.5;
            // National competition matches (10/11) need competition and round metadata.
            // Tournaments, duels, ladder and preparation matches do not grant experience.
            default -> 0;
        };
    }
}
