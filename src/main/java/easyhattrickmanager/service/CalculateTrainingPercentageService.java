package easyhattrickmanager.service;

import static java.lang.Double.compare;
import static java.lang.Integer.compare;

import easyhattrickmanager.client.hattrick.model.matchdetail.MatchDetail;
import easyhattrickmanager.client.hattrick.model.matchesarchive.MatchesArchive;
import easyhattrickmanager.client.hattrick.model.matchlineup.MatchLineup;
import easyhattrickmanager.client.hattrick.model.matchlineup.Player;
import easyhattrickmanager.client.hattrick.model.matchlineup.Substitution;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.PlayerTraining;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.service.model.PercentageBySkill;
import easyhattrickmanager.service.model.PlayerPerTrained;
import easyhattrickmanager.service.model.PlayerRole;
import easyhattrickmanager.utils.SeasonWeekUtils;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateTrainingPercentageService {

    private final TeamDAO teamDAO;
    private final LeagueDAO leagueDAO;
    private final HattrickService hattrickService;

    public List<PlayerTraining> calculateTrainingPercentage(String seasonWeek, int teamId, Training training, int trainerLevel, int assistantsLevel) {
        Team team = teamDAO.get(teamId);
        League league = leagueDAO.get(team.getLeagueId()).orElseThrow();

        ZonedDateTime trainingDate = SeasonWeekUtils.seasonWeekTrainingDate(seasonWeek, league.getTrainingDate());
        MatchesArchive matchesArchive = hattrickService.getMatchesArchive(teamId, trainingDate.minusWeeks(1), trainingDate);

        List<PlayerRole> playerRoles = new ArrayList<>();
        matchesArchive.getTeam().getMatches()
            .stream().filter(matchHT -> matchHT.getMatchType() < 10)
            .forEach(matchHT -> {
                List<PlayerRole> playerRolesMatch = new ArrayList<>();
                MatchDetail matchDetail = hattrickService.getMatchDetail(teamId, matchHT.getMatchId());
                MatchLineup matchLineup = hattrickService.getMatchLineup(teamId, matchHT.getMatchId());
                int matchMinutes = getMatchMinutes(matchDetail);
                Optional<Integer> setPiecesPlayerId = matchLineup.getTeam().getLineup().stream()
                    .filter(player -> player.getRoleId() == 17)
                    .map(Player::getPlayerId)
                    .findFirst();

                matchLineup.getTeam().getStartingLineup().stream()
                    .filter(player -> !List.of(17, 18).contains(player.getRoleId()))
                    .forEach(player ->
                        playerRolesMatch.add(PlayerRole.builder()
                            .playerId(player.getPlayerId())
                            .roleId(player.getRoleId())
                            .start(0)
                            .end(matchMinutes)
                            .perTrainingByRole(getTrainedSkills(training.getTrainingType(), player.getRoleId(), setPiecesPlayerId.isPresent() && setPiecesPlayerId.get() == player.getPlayerId()))
                            .build()));

                matchLineup.getTeam().getSubstitutions().forEach(substitution -> {
                    if (substitution.getOrderType() == 1) {
                        handleSubstitution(playerRolesMatch, substitution, matchMinutes, training.getTrainingType(), setPiecesPlayerId);
                    } else if (substitution.getOrderType() == 3) {
                        handlePlayerSwap(playerRolesMatch, substitution, matchMinutes, training.getTrainingType(), setPiecesPlayerId);
                    }
                });
                playerRoles.addAll(playerRolesMatch);
            });

        playerRoles.sort((a, b) -> {
            int playerIdComparison = compare(a.getPlayerId(), b.getPlayerId());
            if (playerIdComparison != 0) {
                return playerIdComparison;
            }
            return compare(getMaxSkillValue(b.getPerTrainingByRole()), getMaxSkillValue(a.getPerTrainingByRole()));
        });

        List<PlayerPerTrained> min = calculatePlayerPerTrainedMinutes(playerRoles);
        List<PlayerPerTrained> minLev = applyTrainingLevel(min, training.getTrainingLevel());
        List<PlayerPerTrained> minLevSta = applyStaminaTrainingPart(minLev, training.getStaminaTrainingPart());
        List<PlayerPerTrained> minLevStaTra = applyTrainerLevel(minLevSta, trainerLevel);
        List<PlayerPerTrained> minLevStaTraAss = applyAssistantsLevel(minLevStaTra, assistantsLevel);

        Map<Integer, Integer> playerMinutes = calculatePlayerMinutes(playerRoles);

        return minLevStaTraAss.stream()
            .map(playerPerTrained -> PlayerTraining.builder()
                .teamId(teamId)
                .seasonWeek(seasonWeek)
                .id(playerPerTrained.getPlayerId())
                .keeper(playerPerTrained.getPerTrained().getKeeper())
                .defender(playerPerTrained.getPerTrained().getDefender())
                .playmaker(playerPerTrained.getPerTrained().getPlaymaker())
                .winger(playerPerTrained.getPerTrained().getWinger())
                .passing(playerPerTrained.getPerTrained().getPassing())
                .scorer(playerPerTrained.getPerTrained().getScorer())
                .setPieces(playerPerTrained.getPerTrained().getSetPieces())
                .minutes(playerMinutes.getOrDefault(playerPerTrained.getPlayerId(), 0))
                .build())
            .toList();
    }

    private int getMatchMinutes(MatchDetail matchDetail) {
        int matchMinutes = 90 + matchDetail.getMatch().getAddedMinutes();
        if (Duration.between(matchDetail.getMatch().getMatchDate(), matchDetail.getMatch().getFinishedDate()).toMinutes() - 15 > matchMinutes) {
            matchMinutes += 30;
        }
        return matchMinutes;
    }

    private static final int MAX_TRAINING_TYPE = 12;
    private static final int MAX_ROLE_ID = 113;
    private static final PercentageBySkill[][] TRAINING_PERCENTAGE = new PercentageBySkill[MAX_TRAINING_TYPE + 1][MAX_ROLE_ID + 1];

    static {
        // TrainingType
        //  0	General (Deprecated)
        //  1	Stamina (Deprecated)
        //  2	Set Pieces
        //  3	Defending
        //  4	Scoring
        //  5	Winger
        //  6	Scoring and Set Pieces
        //  7	Passing
        //  8	Playmaking
        //  9	Keeper
        // 10	Passing (Defenders + Midfielders)
        // 11	Defending (Defenders + Midfielders)
        // 12	Winger (Winger + Attackers)

        // MatchRoleID
        // 100	Keeper
        // 101	Right back
        // 102	Right central defender
        // 103	Middle central defender
        // 104	Left central defender
        // 105	Left back
        // 106	Right winger
        // 107	Right inner midfield
        // 108	Middle inner midfield
        // 109	Left inner midfield
        // 110	Left winger
        // 111	Right forward
        // 112	Middle forward
        // 113	Left forward

        //  2	Set Pieces
        TRAINING_PERCENTAGE[2][100] = PercentageBySkill.builder().setPieces(125.0).build();
        TRAINING_PERCENTAGE[2][101] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][102] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][103] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][104] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][105] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][106] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][107] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][108] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][109] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][110] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][111] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][112] = PercentageBySkill.builder().setPieces(100.0).build();
        TRAINING_PERCENTAGE[2][113] = PercentageBySkill.builder().setPieces(100.0).build();

        //  3	Defending
        TRAINING_PERCENTAGE[3][100] = PercentageBySkill.builder().defender(16.67).build();
        TRAINING_PERCENTAGE[3][101] = PercentageBySkill.builder().defender(100.0).build();
        TRAINING_PERCENTAGE[3][102] = PercentageBySkill.builder().defender(100.0).build();
        TRAINING_PERCENTAGE[3][103] = PercentageBySkill.builder().defender(100.0).build();
        TRAINING_PERCENTAGE[3][104] = PercentageBySkill.builder().defender(100.0).build();
        TRAINING_PERCENTAGE[3][105] = PercentageBySkill.builder().defender(100.0).build();
        TRAINING_PERCENTAGE[3][106] = PercentageBySkill.builder().defender(16.67).build();
        TRAINING_PERCENTAGE[3][107] = PercentageBySkill.builder().defender(16.67).build();
        TRAINING_PERCENTAGE[3][108] = PercentageBySkill.builder().defender(16.67).build();
        TRAINING_PERCENTAGE[3][109] = PercentageBySkill.builder().defender(16.67).build();
        TRAINING_PERCENTAGE[3][110] = PercentageBySkill.builder().defender(16.67).build();
        TRAINING_PERCENTAGE[3][111] = PercentageBySkill.builder().defender(16.67).build();
        TRAINING_PERCENTAGE[3][112] = PercentageBySkill.builder().defender(16.67).build();
        TRAINING_PERCENTAGE[3][113] = PercentageBySkill.builder().defender(16.67).build();

        //  4	Scoring
        TRAINING_PERCENTAGE[4][100] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][101] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][102] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][103] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][104] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][105] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][106] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][107] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][108] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][109] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][110] = PercentageBySkill.builder().scorer(16.67).build();
        TRAINING_PERCENTAGE[4][111] = PercentageBySkill.builder().scorer(100.0).build();
        TRAINING_PERCENTAGE[4][112] = PercentageBySkill.builder().scorer(100.0).build();
        TRAINING_PERCENTAGE[4][113] = PercentageBySkill.builder().scorer(100.0).build();

        //  5	Winger
        TRAINING_PERCENTAGE[5][100] = PercentageBySkill.builder().winger(12.5).build();
        TRAINING_PERCENTAGE[5][101] = PercentageBySkill.builder().winger(50.0).build();
        TRAINING_PERCENTAGE[5][102] = PercentageBySkill.builder().winger(12.5).build();
        TRAINING_PERCENTAGE[5][103] = PercentageBySkill.builder().winger(12.5).build();
        TRAINING_PERCENTAGE[5][104] = PercentageBySkill.builder().winger(12.5).build();
        TRAINING_PERCENTAGE[5][105] = PercentageBySkill.builder().winger(50.0).build();
        TRAINING_PERCENTAGE[5][106] = PercentageBySkill.builder().winger(100.0).build();
        TRAINING_PERCENTAGE[5][107] = PercentageBySkill.builder().winger(12.5).build();
        TRAINING_PERCENTAGE[5][108] = PercentageBySkill.builder().winger(12.5).build();
        TRAINING_PERCENTAGE[5][109] = PercentageBySkill.builder().winger(12.5).build();
        TRAINING_PERCENTAGE[5][110] = PercentageBySkill.builder().winger(100.0).build();
        TRAINING_PERCENTAGE[5][111] = PercentageBySkill.builder().winger(12.5).build();
        TRAINING_PERCENTAGE[5][112] = PercentageBySkill.builder().winger(12.5).build();
        TRAINING_PERCENTAGE[5][113] = PercentageBySkill.builder().winger(12.5).build();

        //  6	Scoring and Set Pieces
        TRAINING_PERCENTAGE[6][100] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][101] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][102] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][103] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][104] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][105] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][106] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][107] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][108] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][109] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][110] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][111] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][112] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();
        TRAINING_PERCENTAGE[6][113] = PercentageBySkill.builder().scorer(46.3).setPieces(10.2).build();

        //  7	Passing
        TRAINING_PERCENTAGE[7][100] = PercentageBySkill.builder().passing(16.67).build();
        TRAINING_PERCENTAGE[7][101] = PercentageBySkill.builder().passing(16.67).build();
        TRAINING_PERCENTAGE[7][102] = PercentageBySkill.builder().passing(16.67).build();
        TRAINING_PERCENTAGE[7][103] = PercentageBySkill.builder().passing(16.67).build();
        TRAINING_PERCENTAGE[7][104] = PercentageBySkill.builder().passing(16.67).build();
        TRAINING_PERCENTAGE[7][105] = PercentageBySkill.builder().passing(16.67).build();
        TRAINING_PERCENTAGE[7][106] = PercentageBySkill.builder().passing(100.0).build();
        TRAINING_PERCENTAGE[7][107] = PercentageBySkill.builder().passing(100.0).build();
        TRAINING_PERCENTAGE[7][108] = PercentageBySkill.builder().passing(100.0).build();
        TRAINING_PERCENTAGE[7][109] = PercentageBySkill.builder().passing(100.0).build();
        TRAINING_PERCENTAGE[7][110] = PercentageBySkill.builder().passing(100.0).build();
        TRAINING_PERCENTAGE[7][111] = PercentageBySkill.builder().passing(100.0).build();
        TRAINING_PERCENTAGE[7][112] = PercentageBySkill.builder().passing(100.0).build();
        TRAINING_PERCENTAGE[7][113] = PercentageBySkill.builder().passing(100.0).build();

        //  8	Playmaking
        TRAINING_PERCENTAGE[8][100] = PercentageBySkill.builder().playmaker(12.5).build();
        TRAINING_PERCENTAGE[8][101] = PercentageBySkill.builder().playmaker(12.5).build();
        TRAINING_PERCENTAGE[8][102] = PercentageBySkill.builder().playmaker(12.5).build();
        TRAINING_PERCENTAGE[8][103] = PercentageBySkill.builder().playmaker(12.5).build();
        TRAINING_PERCENTAGE[8][104] = PercentageBySkill.builder().playmaker(12.5).build();
        TRAINING_PERCENTAGE[8][105] = PercentageBySkill.builder().playmaker(12.5).build();
        TRAINING_PERCENTAGE[8][106] = PercentageBySkill.builder().playmaker(50.0).build();
        TRAINING_PERCENTAGE[8][107] = PercentageBySkill.builder().playmaker(100.0).build();
        TRAINING_PERCENTAGE[8][108] = PercentageBySkill.builder().playmaker(100.0).build();
        TRAINING_PERCENTAGE[8][109] = PercentageBySkill.builder().playmaker(100.0).build();
        TRAINING_PERCENTAGE[8][110] = PercentageBySkill.builder().playmaker(50.0).build();
        TRAINING_PERCENTAGE[8][111] = PercentageBySkill.builder().playmaker(12.5).build();
        TRAINING_PERCENTAGE[8][112] = PercentageBySkill.builder().playmaker(12.5).build();
        TRAINING_PERCENTAGE[8][113] = PercentageBySkill.builder().playmaker(12.5).build();

        //  9	Keeper
        TRAINING_PERCENTAGE[9][100] = PercentageBySkill.builder().keeper(100.0).build();
        TRAINING_PERCENTAGE[9][101] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][102] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][103] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][104] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][105] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][106] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][107] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][108] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][109] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][110] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][111] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][112] = PercentageBySkill.builder().build();
        TRAINING_PERCENTAGE[9][113] = PercentageBySkill.builder().build();

        // 10	Passing (Defenders + Midfielders)
        TRAINING_PERCENTAGE[10][100] = PercentageBySkill.builder().passing(14.58).build();
        TRAINING_PERCENTAGE[10][101] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][102] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][103] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][104] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][105] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][106] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][107] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][108] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][109] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][110] = PercentageBySkill.builder().passing(87.5).build();
        TRAINING_PERCENTAGE[10][111] = PercentageBySkill.builder().passing(14.58).build();
        TRAINING_PERCENTAGE[10][112] = PercentageBySkill.builder().passing(14.58).build();
        TRAINING_PERCENTAGE[10][113] = PercentageBySkill.builder().passing(14.58).build();

        // 11	Defending (Defenders + Midfielders)
        TRAINING_PERCENTAGE[11][100] = PercentageBySkill.builder().defender(7.99).build();
        TRAINING_PERCENTAGE[11][101] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][102] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][103] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][104] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][105] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][106] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][107] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][108] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][109] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][110] = PercentageBySkill.builder().defender(47.92).build();
        TRAINING_PERCENTAGE[11][111] = PercentageBySkill.builder().defender(7.99).build();
        TRAINING_PERCENTAGE[11][112] = PercentageBySkill.builder().defender(7.99).build();
        TRAINING_PERCENTAGE[11][113] = PercentageBySkill.builder().defender(7.99).build();

        // 12	Winger (Winger + Attackers)
        TRAINING_PERCENTAGE[12][100] = PercentageBySkill.builder().winger(8.33).build();
        TRAINING_PERCENTAGE[12][101] = PercentageBySkill.builder().winger(8.33).build();
        TRAINING_PERCENTAGE[12][102] = PercentageBySkill.builder().winger(8.33).build();
        TRAINING_PERCENTAGE[12][103] = PercentageBySkill.builder().winger(8.33).build();
        TRAINING_PERCENTAGE[12][104] = PercentageBySkill.builder().winger(8.33).build();
        TRAINING_PERCENTAGE[12][105] = PercentageBySkill.builder().winger(8.33).build();
        TRAINING_PERCENTAGE[12][106] = PercentageBySkill.builder().winger(65.0).build();
        TRAINING_PERCENTAGE[12][107] = PercentageBySkill.builder().winger(8.33).build();
        TRAINING_PERCENTAGE[12][108] = PercentageBySkill.builder().winger(8.33).build();
        TRAINING_PERCENTAGE[12][109] = PercentageBySkill.builder().winger(8.33).build();
        TRAINING_PERCENTAGE[12][110] = PercentageBySkill.builder().winger(65.0).build();
        TRAINING_PERCENTAGE[12][111] = PercentageBySkill.builder().winger(65.0).build();
        TRAINING_PERCENTAGE[12][112] = PercentageBySkill.builder().winger(65.0).build();
        TRAINING_PERCENTAGE[12][113] = PercentageBySkill.builder().winger(65.0).build();
    }

    private PercentageBySkill getTrainedSkills(int trainingType, int matchRoleId, boolean isSetPieces) {
        if (trainingType == 2 && isSetPieces) {
            return PercentageBySkill.builder().setPieces(125.0).build();
        }
        if (trainingType >= 0 && trainingType <= MAX_TRAINING_TYPE && matchRoleId >= 0 && matchRoleId <= MAX_ROLE_ID) {
            return TRAINING_PERCENTAGE[trainingType][matchRoleId];
        }
        return PercentageBySkill.builder().build();
    }

    private void handleSubstitution(List<PlayerRole> playerRoles, Substitution substitution, int matchMinutes, int trainingType, Optional<Integer> setPiecesPlayerId) {
        int playerOutIndex = findPlayerIndex(playerRoles, substitution.getSubjectPlayerId());
        if (playerOutIndex >= 0) {
            playerRoles.get(playerOutIndex).setEnd(substitution.getMatchMinute());
            if (substitution.getObjectPlayerId() != 0) {
                playerRoles.add(PlayerRole.builder()
                    .playerId(substitution.getObjectPlayerId())
                    .roleId(substitution.getNewPositionId())
                    .start(substitution.getMatchMinute())
                    .end(matchMinutes)
                    .perTrainingByRole(getTrainedSkills(trainingType, substitution.getNewPositionId(), setPiecesPlayerId.isPresent() && setPiecesPlayerId.get() == substitution.getObjectPlayerId()))
                    .build());
            }
        }
    }

    private void handlePlayerSwap(List<PlayerRole> playerRoles, Substitution substitution, int matchMinutes, int trainingType, Optional<Integer> setPiecesPlayerId) {
        int playerAIndex = findPlayerIndex(playerRoles, substitution.getObjectPlayerId());
        int playerBIndex = findPlayerIndex(playerRoles, substitution.getSubjectPlayerId());
        if (playerAIndex >= 0 && playerBIndex >= 0) {
            PlayerRole playerA = playerRoles.get(playerAIndex);
            PlayerRole playerB = playerRoles.get(playerBIndex);
            playerA.setEnd(substitution.getMatchMinute());
            playerB.setEnd(substitution.getMatchMinute());
            playerRoles.add(PlayerRole.builder()
                .playerId(playerA.getPlayerId())
                .roleId(playerB.getRoleId())
                .start(substitution.getMatchMinute())
                .end(matchMinutes)
                .perTrainingByRole(getTrainedSkills(trainingType, playerB.getRoleId(), setPiecesPlayerId.isPresent() && setPiecesPlayerId.get() == playerA.getPlayerId()))
                .build());
            playerRoles.add(PlayerRole.builder()
                .playerId(playerB.getPlayerId())
                .roleId(playerA.getRoleId())
                .start(substitution.getMatchMinute())
                .end(matchMinutes)
                .perTrainingByRole(getTrainedSkills(trainingType, playerA.getRoleId(), setPiecesPlayerId.isPresent() && setPiecesPlayerId.get() == playerB.getPlayerId()))
                .build());
        }
    }

    private int findPlayerIndex(List<PlayerRole> playerRoles, int playerId) {
        for (int i = playerRoles.size() - 1; i > -1; i--) {
            if (playerRoles.get(i).getPlayerId() == playerId) {
                return i;
            }
        }
        return -1;
    }

    private double getMaxSkillValue(PercentageBySkill percentageBySkill) {
        return Stream.of(percentageBySkill.getKeeper(), percentageBySkill.getDefender(), percentageBySkill.getPlaymaker(), percentageBySkill.getWinger(), percentageBySkill.getPassing(), percentageBySkill.getScorer(), percentageBySkill.getSetPieces()).max(Double::compareTo).orElseThrow();
    }

    private List<PlayerPerTrained> calculatePlayerPerTrainedMinutes(List<PlayerRole> playerRoles) {
        List<PlayerPerTrained> playerPerTrainedMinutes = new ArrayList<>();
        int currentPlayerId = -1;
        int remainingMinutes = 0;
        for (PlayerRole role : playerRoles) {
            if (currentPlayerId != role.getPlayerId()) {
                int minutesTrained = Math.min(90, role.getEnd() - role.getStart());
                playerPerTrainedMinutes.add(PlayerPerTrained.builder()
                    .playerId(role.getPlayerId())
                    .perTrained(getPerTrainedMinutes(minutesTrained, role.getPerTrainingByRole()))
                    .build());
                currentPlayerId = role.getPlayerId();
                remainingMinutes = 90 - minutesTrained;
            } else if (remainingMinutes > 0) {
                int minutesTrained = Math.min(remainingMinutes, role.getEnd() - role.getStart());
                PlayerPerTrained current = playerPerTrainedMinutes.get(playerPerTrainedMinutes.size() - 1);
                PercentageBySkill actual = current.getPerTrained();
                PercentageBySkill added = getPerTrainedMinutes(minutesTrained, role.getPerTrainingByRole());
                current.setPerTrained(addPerBySkill(actual, added));
                remainingMinutes -= minutesTrained;
            }
        }
        return playerPerTrainedMinutes;
    }

    private PercentageBySkill getPerTrainedMinutes(int minutesTrained, PercentageBySkill percentageBySkill) {
        return PercentageBySkill.builder()
            .keeper(((double) minutesTrained / 90) * percentageBySkill.getKeeper())
            .defender(((double) minutesTrained / 90) * percentageBySkill.getDefender())
            .playmaker(((double) minutesTrained / 90) * percentageBySkill.getPlaymaker())
            .winger(((double) minutesTrained / 90) * percentageBySkill.getWinger())
            .passing(((double) minutesTrained / 90) * percentageBySkill.getPassing())
            .scorer(((double) minutesTrained / 90) * percentageBySkill.getScorer())
            .setPieces(((double) minutesTrained / 90) * percentageBySkill.getSetPieces())
            .build();
    }

    private PercentageBySkill addPerBySkill(PercentageBySkill a, PercentageBySkill b) {
        return PercentageBySkill.builder()
            .keeper(a.getKeeper() + b.getKeeper())
            .defender(a.getDefender() + b.getDefender())
            .playmaker(a.getPlaymaker() + b.getPlaymaker())
            .winger(a.getWinger() + b.getWinger())
            .passing(a.getPassing() + b.getPassing())
            .scorer(a.getScorer() + b.getScorer())
            .setPieces(a.getSetPieces() + b.getSetPieces())
            .build();
    }

    private PercentageBySkill applyPerBySkill(PercentageBySkill a, double per) {
        return per == 1
            ? a
            : PercentageBySkill.builder()
                .keeper(a.getKeeper() * per)
                .defender(a.getDefender() * per)
                .playmaker(a.getPlaymaker() * per)
                .winger(a.getWinger() * per)
                .passing(a.getPassing() * per)
                .scorer(a.getScorer() * per)
                .setPieces(a.getSetPieces() * per)
                .build();
    }

    private List<PlayerPerTrained> applyTrainingLevel(List<PlayerPerTrained> playersPer, int trainingLevel) {
        playersPer.forEach(playerPer -> {
            PercentageBySkill actual = playerPer.getPerTrained();
            double per = (double) trainingLevel / 100;
            playerPer.setPerTrained(applyPerBySkill(actual, per));
        });
        return playersPer;
    }

    private List<PlayerPerTrained> applyStaminaTrainingPart(List<PlayerPerTrained> playersPer, int staminaTrainingPart) {
        playersPer.forEach(playerPer -> {
            PercentageBySkill actual = playerPer.getPerTrained();
            double per = (100.0 - (((staminaTrainingPart - 10.0) * 100.0) / (100.0 - 10.0))) / 100.0;
            playerPer.setPerTrained(applyPerBySkill(actual, per));
        });
        return playersPer;
    }

    private static final Map<Integer, Double> PER_TRAINER = Map.ofEntries(
        Map.entry(5, 1.0375 / 1.0375),
        Map.entry(4, 1.0000 / 1.0375),
        Map.entry(3, 0.9200 / 1.0375),
        Map.entry(2, 0.8324 / 1.0375),
        Map.entry(1, 0.7343 / 1.0375)
    );

    private List<PlayerPerTrained> applyTrainerLevel(List<PlayerPerTrained> playersPer, int trainerLevel) {
        playersPer.forEach(playerPer -> {
            PercentageBySkill actual = playerPer.getPerTrained();
            playerPer.setPerTrained(applyPerBySkill(actual, PER_TRAINER.get(trainerLevel)));
        });
        return playersPer;
    }

    private static final Map<Integer, Double> PER_ASSISTANTS = Map.ofEntries(
        Map.entry(10, 1.350 / 1.350),
        Map.entry(9, 1.315 / 1.350),
        Map.entry(8, 1.280 / 1.350),
        Map.entry(7, 1.245 / 1.350),
        Map.entry(6, 1.210 / 1.350),
        Map.entry(5, 1.175 / 1.350),
        Map.entry(4, 1.140 / 1.350),
        Map.entry(3, 1.105 / 1.350),
        Map.entry(2, 1.070 / 1.350),
        Map.entry(1, 1.035 / 1.350),
        Map.entry(0, 1.000 / 1.350)
    );

    private List<PlayerPerTrained> applyAssistantsLevel(List<PlayerPerTrained> playersPer, int assistantsLevel) {
        playersPer.forEach(playerPer -> {
            PercentageBySkill actual = playerPer.getPerTrained();
            playerPer.setPerTrained(applyPerBySkill(actual, PER_ASSISTANTS.get(assistantsLevel)));
        });
        return playersPer;
    }

    private Map<Integer, Integer> calculatePlayerMinutes(List<PlayerRole> playerRoles) {
        Map<Integer, Integer> playerMinutes = new HashMap<>();
        for (PlayerRole role : playerRoles) {
            int minutes = role.getEnd() - role.getStart();
            playerMinutes.merge(role.getPlayerId(), minutes, Integer::sum);
        }
        return playerMinutes;
    }

}
