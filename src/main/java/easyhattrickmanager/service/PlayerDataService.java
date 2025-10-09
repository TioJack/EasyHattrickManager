package easyhattrickmanager.service;

import static easyhattrickmanager.utils.SeasonWeekUtils.seasonWeekTrainingDate;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import easyhattrickmanager.controller.model.PlayerDataResponse;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.PlayerDAO;
import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.PlayerTrainingDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.UserConfigDAO;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.Player;
import easyhattrickmanager.repository.model.PlayerTraining;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.User;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.PlayerTrainingInfo;
import easyhattrickmanager.service.model.dataresponse.UserConfig;
import easyhattrickmanager.service.model.dataresponse.mapper.PlayerInfoMapper;
import easyhattrickmanager.service.model.playerdataresponse.PlayerWeeklyInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerDataService {

    private final UserDAO userDAO;
    private final TeamDAO teamDAO;
    private final LeagueDAO leagueDAO;
    private final PlayerDAO playerDAO;
    private final PlayerDataDAO playerDataDAO;
    private final PlayerTrainingDAO playerTrainingDAO;
    private final UserConfigDAO userConfigDAO;
    private final PlayerInfoMapper playerInfoMapper;

    public PlayerDataResponse getPlayerData(String username, int playerId) {
        User user = userDAO.get(username);
        return PlayerDataResponse.builder()
            .weeklyData(getPlayerWeeklyData(user.getId(), playerId))
            .userConfig(getUserConfig(user.getId()))
            .build();
    }

    private List<PlayerWeeklyInfo> getPlayerWeeklyData(int userId, int playerId) {
        Team team = teamDAO.getByUserIdAndPlayerId(userId, playerId);
        League league = leagueDAO.get(team.getLeagueId()).orElseThrow();
        Player playerBaseInfo = playerDAO.getPlayer(playerId);
        List<PlayerTraining> playerTrainingByWeek = playerTrainingDAO.getPlayer(team.getId(), playerId);
        List<PlayerWeeklyInfo> playerWeeklyInfo = playerDataDAO.getPlayer(team.getId(), playerId).stream()
            .map(playerData -> {
                String seasonWeek = playerData.getSeasonWeek();
                PlayerInfo playerInfo = playerInfoMapper.toInfo(playerBaseInfo, playerData, getPlayerTraining(playerTrainingByWeek, seasonWeek));
                return PlayerWeeklyInfo.builder()
                    .season(Integer.parseInt(seasonWeek.substring(1, 4)) + league.getSeasonOffset())
                    .week(Integer.parseInt(seasonWeek.substring(5, 7)))
                    .date(seasonWeekTrainingDate(seasonWeek, league.getTrainingDate()))
                    .player(playerInfo)
                    .build();
            })
            .toList();
        return fillMissingSeasonWeeks(playerWeeklyInfo, league);
    }

    private PlayerTraining getPlayerTraining(List<PlayerTraining> playersTraining, String seasonWeek) {
        return isEmpty(playersTraining) ? null : playersTraining.stream().filter(playerTraining -> seasonWeek.equals(playerTraining.getSeasonWeek())).findFirst().orElse(null);
    }

    private UserConfig getUserConfig(int userId) {
        try {
            return new ObjectMapper().readValue(userConfigDAO.get(userId), UserConfig.class);
        } catch (Exception e) {
            System.err.printf("Error getUserConfig %s. %s%n", userId, e.getMessage());
            return null;
        }
    }

    private List<PlayerWeeklyInfo> fillMissingSeasonWeeks(List<PlayerWeeklyInfo> in, League league) {
        int minSeason = in.stream().mapToInt(PlayerWeeklyInfo::getSeason).min().orElseThrow();
        int maxSeason = in.stream().mapToInt(PlayerWeeklyInfo::getSeason).max().orElseThrow();
        Map<Integer, List<PlayerWeeklyInfo>> weeksBySeason = in.stream().collect(groupingBy(PlayerWeeklyInfo::getSeason));
        List<PlayerWeeklyInfo> completeWeeklyInfo = new ArrayList<>();
        PlayerWeeklyInfo previousWeekInfo = null;
        for (int season = minSeason; season <= maxSeason; season++) {
            List<PlayerWeeklyInfo> seasonWeeks = weeksBySeason.getOrDefault(season, new ArrayList<>());
            int minWeek = season == minSeason ? seasonWeeks.stream().mapToInt(PlayerWeeklyInfo::getWeek).min().orElse(1) : 1;
            int maxWeek = season == maxSeason ? seasonWeeks.stream().mapToInt(PlayerWeeklyInfo::getWeek).max().orElse(16) : 16;
            Map<String, PlayerWeeklyInfo> weekMap = seasonWeeks.stream().collect(toMap(info -> String.format("S%03dW%02d", info.getSeason(), info.getWeek()), info -> info));
            for (int week = minWeek; week <= maxWeek; week++) {
                String seasonWeekKey = String.format("S%03dW%02d", season, week);
                PlayerWeeklyInfo currentWeekInfo = weekMap.get(seasonWeekKey);
                if (currentWeekInfo == null) {
                    if (previousWeekInfo != null) {
                        int newAge = previousWeekInfo.getPlayer().getAge();
                        int newAgeDays = previousWeekInfo.getPlayer().getAgeDays() + 7;
                        if (newAgeDays >= 112) {
                            newAge += 1;
                            newAgeDays -= 112;
                        }
                        PlayerWeeklyInfo filledInfo = PlayerWeeklyInfo.builder()
                            .season(season)
                            .week(week)
                            .date(seasonWeekTrainingDate(seasonWeekKey, league.getTrainingDate()))
                            .player(PlayerInfo.builder()
                                .age(newAge)
                                .ageDays(newAgeDays)
                                .TSI(-1)
                                .playerForm(-1)
                                .experience(-1)
                                .loyalty(-1)
                                .leadership(-1)
                                .salary(-1)
                                .staminaSkill(-1)
                                .keeperSkill(-1)
                                .playmakerSkill(-1)
                                .scorerSkill(-1)
                                .passingSkill(-1)
                                .wingerSkill(-1)
                                .defenderSkill(-1)
                                .setPiecesSkill(-1)
                                .htms(-1)
                                .htms28(-1)
                                .playerTraining(PlayerTrainingInfo.builder()
                                    .keeper(-1)
                                    .defender(-1)
                                    .playmaker(-1)
                                    .winger(-1)
                                    .passing(-1)
                                    .scorer(-1)
                                    .setPieces(-1)
                                    .build())
                                .build())
                            .build();
                        completeWeeklyInfo.add(filledInfo);
                        previousWeekInfo = filledInfo;
                    }
                } else {
                    completeWeeklyInfo.add(currentWeekInfo);
                    previousWeekInfo = currentWeekInfo;
                }
            }
        }
        return completeWeeklyInfo;
    }

}
