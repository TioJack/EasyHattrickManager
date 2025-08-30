package easyhattrickmanager.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import easyhattrickmanager.controller.model.DataResponse;
import easyhattrickmanager.repository.CountryDAO;
import easyhattrickmanager.repository.LanguageDAO;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.PlayerDAO;
import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.StaffMemberDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.TrainerDAO;
import easyhattrickmanager.repository.TrainingDAO;
import easyhattrickmanager.repository.UserConfigDAO;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.model.Country;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.Player;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.StaffMember;
import easyhattrickmanager.repository.model.Trainer;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.repository.model.User;
import easyhattrickmanager.service.model.dataresponse.CurrencyInfo;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.StaffInfo;
import easyhattrickmanager.service.model.dataresponse.TeamExtendedInfo;
import easyhattrickmanager.service.model.dataresponse.TrainingInfo;
import easyhattrickmanager.service.model.dataresponse.UserConfig;
import easyhattrickmanager.service.model.dataresponse.WeeklyInfo;
import easyhattrickmanager.service.model.dataresponse.mapper.LanguageInfoMapper;
import easyhattrickmanager.service.model.dataresponse.mapper.PlayerInfoMapper;
import easyhattrickmanager.service.model.dataresponse.mapper.StaffInfoMapper;
import easyhattrickmanager.service.model.dataresponse.mapper.TeamExtendedInfoMapper;
import easyhattrickmanager.service.model.dataresponse.mapper.TrainingInfoMapper;
import easyhattrickmanager.service.model.dataresponse.mapper.UserInfoMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static final int BASE_CURRENCY_COUNTRY_ID = 8; // USA

    private final UserDAO userDAO;
    private final TeamDAO teamDAO;
    private final LeagueDAO leagueDAO;
    private final TrainingDAO trainingDAO;
    private final TrainerDAO trainerDAO;
    private final StaffMemberDAO staffMemberDAO;
    private final PlayerDAO playerDAO;
    private final PlayerDataDAO playerDataDAO;
    private final LanguageDAO languageDAO;
    private final UserConfigDAO userConfigDAO;
    private final UserInfoMapper userInfoMapper;
    private final TeamExtendedInfoMapper teamExtendedInfoMapper;
    private final TrainingInfoMapper trainingInfoMapper;
    private final StaffInfoMapper staffInfoMapper;
    private final PlayerInfoMapper playerInfoMapper;
    private final LanguageInfoMapper languageInfoMapper;
    private final CountryDAO countryDAO;

    @Value("${app.version}")
    private String appVersion;

    public DataResponse getData(String username) {
        User user = userDAO.get(username);
        return DataResponse.builder()
            .version(appVersion)
            .user(userInfoMapper.toInfo(user))
            .teams(getTeams(user.getId()))
            .userConfig(getUserConfig(user.getId()))
            .languages(languageInfoMapper.toInfo(languageDAO.getAllLanguages()))
            .currencies(getCurrencies())
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
        Map<String, TrainingInfo> trainings = trainingDAO.get(teamId).stream().collect(toMap(Training::getSeasonWeek, trainingInfoMapper::toInfo));
        Map<String, Trainer> trainersByWeek = trainerDAO.get(teamId).stream().collect(toMap(Trainer::getSeasonWeek, t -> t, (a, b) -> b));
        Map<String, List<StaffMember>> staffMembersByWeek = staffMemberDAO.get(teamId).stream().collect(groupingBy(StaffMember::getSeasonWeek));
        Map<String, StaffInfo> staffs = trainings.keySet().stream().collect(toMap(
            seasonWeek -> seasonWeek,
            seasonWeek -> staffInfoMapper.toInfo(trainersByWeek.get(seasonWeek), staffMembersByWeek.getOrDefault(seasonWeek, List.of()))
        ));
        Map<Integer, Player> playersBaseInfo = playerDAO.get(teamId).stream().collect(toMap(Player::getId, player -> player));
        Map<String, List<PlayerInfo>> players = playerDataDAO
            .get(teamId)
            .stream()
            .collect(groupingBy(
                PlayerData::getSeasonWeek,
                Collectors.mapping(playerData ->
                        playerInfoMapper.toInfo(
                            playersBaseInfo.get(
                                playerData.getId()),
                            playerData),
                    Collectors.toList())));

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

    private UserConfig getUserConfig(int userId) {
        try {
            return new ObjectMapper().readValue(userConfigDAO.get(userId), UserConfig.class);
        } catch (Exception e) {
            System.err.printf("Error getUserConfig %s. %s%n", userId, e.getMessage());
            return null;
        }
    }

    private List<CurrencyInfo> getCurrencies() {
        List<Country> countries = countryDAO.getAllCountries();
        Country baseCountry = countries.stream().filter(country -> country.getId() == BASE_CURRENCY_COUNTRY_ID).findFirst().orElseThrow();
        return countries.stream()
            .map(country -> CurrencyInfo.builder()
                .countryId(country.getId())
                .currencyName(getCurrencyFriendyName(country, baseCountry))
                .currencyCode(country.getCurrencyName())
                .currencyRate(country.getCurrencyRate())
                .build())
            .toList();
    }

    private String getCurrencyFriendyName(Country country, Country baseCountry) {
        BigDecimal rateAgainstUSD = country.getCurrencyRate().divide(baseCountry.getCurrencyRate(), 6, RoundingMode.HALF_UP);
        BigDecimal inverseRate = BigDecimal.ONE.divide(rateAgainstUSD, 4, RoundingMode.HALF_UP);
        return String.format("%s %s = 1 US$", inverseRate.stripTrailingZeros().toPlainString(), country.getCurrencyName());
    }

    public void saveUserConfig(String username, UserConfig userConfig) {
        try {
            User user = userDAO.get(username);
            userConfigDAO.update(user.getId(), new ObjectMapper().writeValueAsString(userConfig));
        } catch (Exception e) {
            System.err.printf("Error saveUserConfig %s %s. %s%n", username, userConfig, e.getMessage());
        }
    }


}
