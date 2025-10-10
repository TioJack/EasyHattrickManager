package easyhattrickmanager.service;

import easyhattrickmanager.client.hattrick.model.teamdetails.TeamDetails;
import easyhattrickmanager.configuration.EhmConfiguration;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckActiveService {

    private final UserDAO userDAO;
    private final TeamDAO teamDAO;
    private final HattrickService hattrickService;
    private final EhmConfiguration ehmConfiguration;

    @Scheduled(cron = "#{ehmConfiguration.cronCheckActive}")
    public void checkActive() throws Exception {
        userDAO.getAllUsers()
            .stream().filter(User::isActive)
            .forEach(user -> {
                try {
                    TeamDetails teamDetails = hattrickService.getTeamDetails(user);
                    if (teamDetails.getUser().getUserId() == 0) {
                        userDAO.updateActive(user.getId(), false);
                        return;
                    }
                    if (!user.getName().equals(teamDetails.getUser().getLoginname())) {
                        userDAO.updateName(user.getId(), teamDetails.getUser().getLoginname());
                    }
                    saveTeams(teamDetails);
                    List<Integer> actualTeamIds = teamDetails.getTeams().stream().map(easyhattrickmanager.client.hattrick.model.teamdetails.Team::getTeamId).toList();
                    teamDAO.getByUserId(user.getId()).stream()
                        .filter(team -> !team.isBot() && !actualTeamIds.contains(team.getId()))
                        .forEach(noActiveTeam ->
                            teamDAO.deactivateTeam(user.getId(), noActiveTeam.getId())
                        );
                } catch (Exception e) {
                    System.err.printf("Error checkActive %s. %s%n", user.getId(), e.getMessage());
                }
            });
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

}
