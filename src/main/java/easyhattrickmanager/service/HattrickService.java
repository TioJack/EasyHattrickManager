package easyhattrickmanager.service;

import com.github.scribejava.core.model.OAuth1AccessToken;
import easyhattrickmanager.client.hattrick.HattrickClient;
import easyhattrickmanager.client.hattrick.model.avatars.Avatars;
import easyhattrickmanager.client.hattrick.model.managercompendium.ManagerCompendium;
import easyhattrickmanager.client.hattrick.model.players.Players;
import easyhattrickmanager.client.hattrick.model.stafflist.Stafflist;
import easyhattrickmanager.client.hattrick.model.teamdetails.TeamDetails;
import easyhattrickmanager.client.hattrick.model.training.Training;
import easyhattrickmanager.client.hattrick.model.translations.Translations;
import easyhattrickmanager.client.hattrick.model.worlddetails.WorldDetails;
import easyhattrickmanager.client.hattrick.model.worldlanguages.WorldLanguages;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HattrickService {

    private final int DEFAULT_TEAM_ID = 1333746;

    private final HattrickClient hattrickClient;
    private final UserDAO userDAO;

    public TeamDetails getTeamDetails(OAuth1AccessToken accessToken) {
        return this.hattrickClient.getTeamDetails(accessToken);
    }

    public Players getPlayers(int teamId) {
        return this.hattrickClient.getPlayers(getAccessToken(teamId), teamId);
    }

    public Training getTraining(int teamId) {
        return this.hattrickClient.getTraining(getAccessToken(teamId), teamId);
    }

    public Stafflist getStaff(int teamId) {
        return this.hattrickClient.getStafflist(getAccessToken(teamId), teamId);
    }

    public WorldDetails getWorlddetails() {
        return this.hattrickClient.getWorlddetails(getAccessToken(DEFAULT_TEAM_ID));
    }

    public ManagerCompendium getManagerCompendium(OAuth1AccessToken accessToken) {
        return this.hattrickClient.getManagerCompendium(accessToken);
    }

    public WorldLanguages getWorldLanguages() {
        return this.hattrickClient.getWorldLanguages(getAccessToken(DEFAULT_TEAM_ID));
    }

    private OAuth1AccessToken getAccessToken(int teamId) {
        User user = userDAO.getByTeamId(teamId);
        return new OAuth1AccessToken(user.getToken(), user.getTokenSecret());
    }

    public Avatars getAvatars(int teamId) {
        return this.hattrickClient.getAvatars(getAccessToken(teamId), teamId);
    }

    public Translations getTranslations(int languageId) {
        return this.hattrickClient.getTranslations(getAccessToken(DEFAULT_TEAM_ID), languageId);
    }

}
