package easyhattrickmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import easyhattrickmanager.client.hattrick.model.managercompendium.ManagerCompendium;
import easyhattrickmanager.client.hattrick.model.teamdetails.TeamDetails;
import easyhattrickmanager.client.hattrick.model.worldlanguages.WorldLanguages;
import easyhattrickmanager.controller.model.SaveResponse;
import easyhattrickmanager.controller.model.TokenRequest;
import easyhattrickmanager.controller.model.UserRequest;
import easyhattrickmanager.repository.LanguageDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.UserConfigDAO;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.UserEhmDAO;
import easyhattrickmanager.repository.UserEhmHistoryDAO;
import easyhattrickmanager.repository.model.Language;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.User;
import easyhattrickmanager.repository.model.UserEhm;
import easyhattrickmanager.repository.model.UserEhmHistory;
import easyhattrickmanager.service.model.LoginData;
import easyhattrickmanager.service.model.dataresponse.CurrencyInfo;
import easyhattrickmanager.service.model.dataresponse.ProjectInfo;
import easyhattrickmanager.service.model.dataresponse.UserConfig;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final PasswordEncoder passwordEncoder;
    private final OAuth10aService oAuth10aService;
    private final HattrickService hattrickService;
    private final UserEhmDAO userEhmDAO;
    private final UserEhmHistoryDAO userEhmHistoryDAO;
    private final UserDAO userDAO;
    private final TeamDAO teamDAO;
    private final LanguageDAO languageDAO;
    private final Map<String, LoginData> temporaryLoginDataStorage = new HashMap<>();
    private final UpdateService updateService;
    private final UpdateExecutionService updateExecutionService;
    private final UserConfigDAO userConfigDAO;

    public boolean existUserEhm(String username) {
        return userEhmDAO.get(username).isPresent();
    }

    public boolean isValid(String username, String password) {
        Optional<UserEhm> userEhm = userEhmDAO.get(username);
        return userEhm.isPresent() && passwordEncoder.matches(password, userEhm.get().getPassword());
    }

    public void addHistory(String username, String ipAddress, String userAgent) {
        Optional<UserEhm> userEhm = userEhmDAO.get(username);
        userEhm.ifPresent(user -> {
                if (user.getId() > 0) {
                    UserEhmHistory userEhmHistory = UserEhmHistory.builder()
                        .userEhmId(user.getId())
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .build();
                    userEhmHistoryDAO.insert(userEhmHistory);
                }
            }
        );
    }

    public String getAuthorizationUrl(UserRequest request) {
        OAuth1RequestToken requestToken = null;
        try {
            requestToken = oAuth10aService.getRequestToken();
        } catch (IOException | InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
        temporaryLoginDataStorage.put(requestToken.getToken(), LoginData.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .token(requestToken)
            .build());
        return oAuth10aService.getAuthorizationUrl(requestToken);
    }

    @Transactional
    public SaveResponse save(TokenRequest request) {
        LoginData loginData = temporaryLoginDataStorage.get(request.getOauthToken());
        OAuth1AccessToken accessToken;
        try {
            accessToken = oAuth10aService.getAccessToken(loginData.getToken(), request.getOauthVerifier());
        } catch (IOException | InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
        TeamDetails teamDetails = hattrickService.getTeamDetails(accessToken);
        ManagerCompendium managerCompendium = hattrickService.getManagerCompendium(accessToken);
        int userEhmId = saveUserEhm(loginData);
        User user = saveUser(accessToken, teamDetails, managerCompendium);
        saveLanguage(user.getLanguageId());
        saveUserEhmUser(userEhmId, user.getId());
        saveTeams(teamDetails);
        teamDetails.getTeams().forEach(team -> {
            updateService.update(team.getTeamId());
            updateExecutionService.addUpdateExecution(team.getTeamId());
        });
        saveUserConfig(managerCompendium);
        return getSaveResponse(teamDetails);
    }

    private int saveUserEhm(LoginData loginData) {
        UserEhm userEhm = UserEhm.builder()
            .username(loginData.getUsername())
            .password(passwordEncoder.encode(loginData.getPassword()))
            .build();
        userEhmDAO.insert(userEhm);
        return userEhm.getId();
    }

    private User saveUser(OAuth1AccessToken accessToken, TeamDetails teamDetails, ManagerCompendium managerCompendium) {
        User user = User.builder()
            .id(teamDetails.getUser().getUserId())
            .name(teamDetails.getUser().getLoginname())
            .languageId(teamDetails.getUser().getLanguage().getLanguageId())
            .countryId(managerCompendium.getManager().getCountry().getCountryId())
            .currency(managerCompendium.getManager().getCurrency().getCurrencyName())
            .activationDate(teamDetails.getUser().getActivationDate())
            .token(accessToken.getToken())
            .tokenSecret(accessToken.getTokenSecret())
            .active(true)
            .build();
        userDAO.insert(user);
        return user;
    }

    private void saveUserEhmUser(int userEhmId, int userId) {
        userDAO.link(userEhmId, userId);
    }

    private void saveTeams(TeamDetails teamDetails) {
        teamDetails.getTeams().forEach(teamDetail -> {
            Team team = Team.builder()
                .userId(teamDetails.getUser().getUserId())
                .id(teamDetail.getTeamId())
                .name(teamDetail.getTeamName())
                .primaryClub(teamDetail.isPrimaryClub())
                .foundedDate(teamDetail.getFoundedDate())
                .leagueId(teamDetail.getLeague().getLeagueId())
                .countryId(teamDetail.getCountry().getCountryId())
                .bot(teamDetail.getBotStatus().isBot())
                .build();
            teamDAO.insert(team);
        });
    }

    private void saveLanguage(int id) {
        var language = languageDAO.get(id);
        if (language.isEmpty()) {
            updateLanguages();
            language = languageDAO.get(id);
            if (language.isEmpty()) {
                throw new RuntimeException("Language not found: " + id);
            }
        }
    }

    private void updateLanguages() {
        WorldLanguages worldLanguages = hattrickService.getWorldLanguages();
        worldLanguages.getLanguages().forEach(languageHT -> {
            languageDAO.insert(getLanguage(languageHT));
        });
    }

    private Language getLanguage(easyhattrickmanager.client.hattrick.model.worldlanguages.Language languageHT) {
        return Language.builder()
            .id(languageHT.getLanguageId())
            .name(languageHT.getLanguageName())
            .build();
    }

    private SaveResponse getSaveResponse(TeamDetails teamDetails) {
        return SaveResponse.builder()
            .username(teamDetails.getUser().getLoginname())
            .teams(teamDetails.getTeams().stream().map(team -> team.getTeamName()).toList())
            .build();
    }

    private void saveUserConfig(ManagerCompendium managerCompendium) {
        try {
            String config = userConfigDAO.get(managerCompendium.getManager().getUserId());
            if (Objects.nonNull(config)) {
                return;
            }
            UserConfig userConfig = UserConfig.builder()
                .languageId(managerCompendium.getManager().getLanguage().getLanguageId())
                .currency(CurrencyInfo.builder()
                    .countryId(managerCompendium.getManager().getCountry().getCountryId())
                    .currencyCode(managerCompendium.getManager().getCurrency().getCurrencyName())
                    .currencyRate(new BigDecimal(managerCompendium.getManager().getCurrency().getCurrencyRate().replace(",", ".")))
                    .build())
                .projects(createProjects(managerCompendium.getManager().getTeams()))
                .build();
            userConfigDAO.insert(managerCompendium.getManager().getUserId(), new ObjectMapper().writeValueAsString(userConfig));
        } catch (Exception e) {
            System.err.printf("Error saveUserConfig %s%n", e.getMessage());
        }
    }

    private List<ProjectInfo> createProjects(List<easyhattrickmanager.client.hattrick.model.managercompendium.Team> teams) {
        return teams.stream()
            .map(team -> ProjectInfo.builder()
                .name(team.getTeamName())
                .teamId(team.getTeamId())
                .iniSeason(team.getLeague().getSeason())
                .iniWeek(updateService.getActualWeek(team.getTeamId()))
                .build()
            ).toList();
    }
}
