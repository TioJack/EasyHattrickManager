package easyhattrickmanager.service;

import com.github.scribejava.core.model.OAuth1AccessToken;
import easyhattrickmanager.client.HattrickClient;
import easyhattrickmanager.client.model.players.Players;
import easyhattrickmanager.client.model.stafflist.Stafflist;
import easyhattrickmanager.client.model.teamdetails.TeamDetails;
import easyhattrickmanager.client.model.training.Training;
import easyhattrickmanager.client.model.worlddetails.WorldDetails;
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

    private OAuth1AccessToken getAccessToken(int teamId) {
        User user = userDAO.getByTeamId(teamId);
        return new OAuth1AccessToken(user.getToken(), user.getTokenSecret());
    }

}
