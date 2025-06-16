package easyhattrickmanager.service;

import easyhattrickmanager.controller.model.DataResponse;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.PlayerDAO;
import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.StaffDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.TrainingDAO;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.Player;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.Staff;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.repository.model.User;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.StaffInfo;
import easyhattrickmanager.service.model.dataresponse.TeamExtendedInfo;
import easyhattrickmanager.service.model.dataresponse.TrainingInfo;
import easyhattrickmanager.service.model.dataresponse.WeeklyInfo;
import easyhattrickmanager.service.model.dataresponse.mapper.PlayerInfoMapper;
import easyhattrickmanager.service.model.dataresponse.mapper.StaffInfoMapper;
import easyhattrickmanager.service.model.dataresponse.mapper.TeamExtendedInfoMapper;
import easyhattrickmanager.service.model.dataresponse.mapper.TrainingInfoMapper;
import easyhattrickmanager.service.model.dataresponse.mapper.UserInfoMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataService {

    private final UserDAO userDAO;
    private final TeamDAO teamDAO;
    private final LeagueDAO leagueDAO;
    private final TrainingDAO trainingDAO;
    private final StaffDAO staffDAO;
    private final PlayerDAO playerDAO;
    private final PlayerDataDAO playerDataDAO;
    private final UserInfoMapper userInfoMapper;
    private final TeamExtendedInfoMapper teamExtendedInfoMapper;
    private final TrainingInfoMapper trainingInfoMapper;
    private final StaffInfoMapper staffInfoMapper;
    private final PlayerInfoMapper playerInfoMapper;

    @Value("${app.version}")
    private String appVersion;

    public DataResponse getData(String username) {
        User user = userDAO.get(username);
        return DataResponse.builder()
            .version(appVersion)
            .user(userInfoMapper.toInfo(user))
            .teams(getTeams(user.getId()))
            .build();
    }

    private List<TeamExtendedInfo> getTeams(int userId) {
        List<TeamExtendedInfo> teams = teamExtendedInfoMapper.toInfos(teamDAO.getByUserId(userId));
        teams.forEach(team -> {
            League league = leagueDAO.get(team.getLeague().getId()).orElseThrow();
            team.getLeague().setName(league.getName());
            team.setWeeklyData(getWeeklyData(team.getTeam().getId(), league.getSeasonOffset()));
        });
        return teams.stream()
            .sorted(Comparator.comparing(team -> team.getTeam().getFoundedDate()))
            .toList();
    }

    private List<WeeklyInfo> getWeeklyData(int teamId, int seasonOffset) {
        Map<String, TrainingInfo> trainings = trainingDAO.get(teamId).stream().collect(Collectors.toMap(Training::getSeasonWeek, trainingInfoMapper::toInfo));
        Map<String, StaffInfo> staffs = staffDAO.get(teamId).stream().collect(Collectors.toMap(Staff::getSeasonWeek, staffInfoMapper::toInfo));
        Map<Integer, Player> playersBaseInfo = playerDAO.get(teamId).stream().collect(Collectors.toMap(Player::getId, player -> player));
        Map<String, List<PlayerInfo>> players = playerDataDAO.get(teamId).stream().collect(Collectors.groupingBy(PlayerData::getSeasonWeek, Collectors.mapping(playerData -> playerInfoMapper.toInfo(playersBaseInfo.get(playerData.getId()), playerData), Collectors.toList())));

        List<String> seasonWeeks = trainings.keySet().stream().toList();
        return seasonWeeks.stream().map(seasonWeek ->
            WeeklyInfo.builder()
                .season(Integer.parseInt(seasonWeek.substring(1, 4)) + seasonOffset)
                .week(Integer.parseInt(seasonWeek.substring(5, 7)))
                .training(trainings.get(seasonWeek))
                .staff(staffs.get(seasonWeek))
                .players(players.get(seasonWeek))
                .build()
        ).sorted(Comparator.comparingInt(WeeklyInfo::getSeason).thenComparingInt(WeeklyInfo::getWeek)).toList();
    }

}
