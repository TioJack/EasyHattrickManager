package easyhattrickmanager.service;

import easyhattrickmanager.client.model.players.Players;
import easyhattrickmanager.client.model.worlddetails.WorldDetails;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.PlayerDAO;
import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.StaffDAO;
import easyhattrickmanager.repository.TrainingDAO;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.Player;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.Staff;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.service.model.HTMS;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateService {

    private final HattrickService hattrickService;
    private final PlayerDAO playerDAO;
    private final PlayerDataDAO playerDataDAO;
    private final TrainingDAO trainingDAO;
    private final StaffDAO staffDAO;
    private final LeagueDAO leagueDAO;

    public void update(int teamId) {
        Players players = hattrickService.getPlayers(teamId);
        players.getTeam().getPlayers()
            .forEach(playerHT -> {
                playerDAO.insert(getPlayer(playerHT));
                playerDataDAO.insert(getPlayerData(teamId, playerHT));
            });
        trainingDAO.insert(getTraining(hattrickService.getTraining(teamId)));
        staffDAO.insert(getStaff(teamId, hattrickService.getStaff(teamId)));
    }

    public void updateLeagues() {
        WorldDetails worldDetails = hattrickService.getWorlddetails();
        worldDetails.getLeagues()
            .forEach(leagueHT -> leagueDAO.insert(getLeague(leagueHT)));
    }

    private Player getPlayer(easyhattrickmanager.client.model.players.Player playerHT) {
        return Player.builder()
            .id(playerHT.getPlayerId())
            .firstName(playerHT.getFirstName())
            .lastName(playerHT.getLastName())
            .aggressiveness(playerHT.getAggressiveness())
            .agreeability(playerHT.getAgreeability())
            .honesty(playerHT.getHonesty())
            .specialty(playerHT.getSpecialty())
            .countryId(playerHT.getCountryId())
            .build();
    }

    private PlayerData getPlayerData(int teamId, easyhattrickmanager.client.model.players.Player playerHT) {
        HTMS htms = calculateHTMS(playerHT);
        return PlayerData.builder()
            .id(playerHT.getPlayerId())
            .seasonWeek(convertToSeasonWeek(LocalDate.now()))
            .date(LocalDateTime.now())
            .teamId(teamId)
            .nickName(playerHT.getNickName())
            .playerNumber(playerHT.getPlayerNumber() < 100 ? playerHT.getPlayerNumber() : null)
            .age(playerHT.getAge())
            .ageDays(playerHT.getAgeDays())
            .TSI(playerHT.getTSI())
            .playerForm(playerHT.getPlayerForm())
            .experience(playerHT.getExperience())
            .loyalty(playerHT.getLoyalty())
            .motherClubBonus(playerHT.isMotherClubBonus())
            .leadership(playerHT.getLeadership())
            .salary(playerHT.getSalary())
            .injuryLevel(playerHT.getInjuryLevel())
            .staminaSkill(playerHT.getStaminaSkill())
            .keeperSkill(playerHT.getKeeperSkill())
            .playmakerSkill(playerHT.getPlaymakerSkill())
            .scorerSkill(playerHT.getScorerSkill())
            .passingSkill(playerHT.getPassingSkill())
            .wingerSkill(playerHT.getWingerSkill())
            .defenderSkill(playerHT.getDefenderSkill())
            .setPiecesSkill(playerHT.getSetPiecesSkill())
            .htms(htms.getHtms())
            .htms28(htms.getHtms28())
            .build();
    }

    private String convertToSeasonWeek(LocalDate date) {
        LocalDate referenceDate = LocalDate.of(2025, 5, 5);
        int referenceSeason = 91;
        int referenceWeekInSeason = 2;
        LocalDate startOfReferenceSeason = referenceDate.minusWeeks(referenceWeekInSeason - 1);
        long weeksBetween = ChronoUnit.WEEKS.between(startOfReferenceSeason, date);
        long seasonsSinceReference = weeksBetween / 16;
        long weekInSeason = (weeksBetween % 16) + 1;
        long season = referenceSeason + seasonsSinceReference;
        return String.format("S%03dW%02d", season, weekInSeason);
    }

    private LocalDate convertFromSeasonWeek(String seasonWeek) {
        if (!seasonWeek.matches("S\\d{3}W\\d{2}")) {
            throw new IllegalArgumentException("Incorrect format SxxxWyy");
        }
        int season = Integer.parseInt(seasonWeek.substring(1, 4));
        int week = Integer.parseInt(seasonWeek.substring(5, 7));
        if (week < 1 || week > 16) {
            throw new IllegalArgumentException("Week will be between 1-16");
        }
        LocalDate referenceDate = LocalDate.of(2025, 5, 5);
        int referenceSeason = 91;
        int referenceWeekInSeason = 2;
        LocalDate startOfReferenceSeason = referenceDate.minusWeeks(referenceWeekInSeason - 1);
        int seasonsDifference = season - referenceSeason;
        LocalDate startOfRequestedSeason = startOfReferenceSeason.plusWeeks(seasonsDifference * 16);
        LocalDate startOfRequestedWeek = startOfRequestedSeason.plusWeeks(week - 1);
        return startOfRequestedWeek;
    }

    private HTMS calculateHTMS(easyhattrickmanager.client.model.players.Player playerHT) {
        return calculateHTMS(
            playerHT.getAge(),
            playerHT.getAgeDays(),
            playerHT.getKeeperSkill(),
            playerHT.getDefenderSkill(),
            playerHT.getPlaymakerSkill(),
            playerHT.getWingerSkill(),
            playerHT.getPassingSkill(),
            playerHT.getScorerSkill(),
            playerHT.getSetPiecesSkill()
        );
    }

    private HTMS calculateHTMS(int years, int days, int keeper, int defending, int playmaking, int winger, int passing, int scoring, int setPieces) {
        double[] WEEK_PTS_PER_AGE = new double[46];
        WEEK_PTS_PER_AGE[17] = 10;
        WEEK_PTS_PER_AGE[18] = 9.92;
        WEEK_PTS_PER_AGE[19] = 9.81;
        WEEK_PTS_PER_AGE[20] = 9.69;
        WEEK_PTS_PER_AGE[21] = 9.54;
        WEEK_PTS_PER_AGE[22] = 9.39;
        WEEK_PTS_PER_AGE[23] = 9.22;
        WEEK_PTS_PER_AGE[24] = 9.04;
        WEEK_PTS_PER_AGE[25] = 8.85;
        WEEK_PTS_PER_AGE[26] = 8.66;
        WEEK_PTS_PER_AGE[27] = 8.47;
        WEEK_PTS_PER_AGE[28] = 8.27;
        WEEK_PTS_PER_AGE[29] = 8.07;
        WEEK_PTS_PER_AGE[30] = 7.87;
        WEEK_PTS_PER_AGE[31] = 7.67;
        WEEK_PTS_PER_AGE[32] = 7.47;
        WEEK_PTS_PER_AGE[33] = 7.27;
        WEEK_PTS_PER_AGE[34] = 7.07;
        WEEK_PTS_PER_AGE[35] = 6.87;
        WEEK_PTS_PER_AGE[36] = 6.67;
        WEEK_PTS_PER_AGE[37] = 6.47;
        WEEK_PTS_PER_AGE[38] = 6.26;
        WEEK_PTS_PER_AGE[39] = 6.06;
        WEEK_PTS_PER_AGE[40] = 5.86;
        WEEK_PTS_PER_AGE[41] = 5.65;
        WEEK_PTS_PER_AGE[42] = 6.45;
        WEEK_PTS_PER_AGE[43] = 6.24;
        WEEK_PTS_PER_AGE[44] = 6.04;
        WEEK_PTS_PER_AGE[45] = 5.83;

        int MAX_AGE = 45;

        int[][] SKILL_PTS_PER_LVL = {
            {0, 0, 0, 0, 0, 0, 0},    // Level 0
            {2, 4, 4, 2, 3, 4, 1},   // Level 1
            {12, 18, 17, 12, 14, 17, 2}, // Level 2
            {23, 39, 34, 25, 31, 36, 5}, // Level 3
            {39, 65, 57, 41, 51, 59, 9}, // Level 4
            {56, 98, 84, 60, 75, 88, 15}, // Level 5
            {76, 134, 114, 81, 104, 119, 21}, // Level 6
            {99, 175, 150, 105, 137, 156, 28}, // Level 7
            {123, 221, 190, 132, 173, 197, 37}, // Level 8
            {150, 271, 231, 161, 213, 240, 46}, // Level 9
            {183, 330, 281, 195, 259, 291, 56}, // Level 10
            {222, 401, 341, 238, 315, 354, 68}, // Level 11
            {268, 484, 412, 287, 381, 427, 81}, // Level 12
            {321, 580, 493, 344, 457, 511, 95}, // Level 13
            {380, 689, 584, 407, 540, 607, 112}, // Level 14
            {446, 809, 685, 478, 634, 713, 131}, // Level 15
            {519, 942, 798, 555, 738, 830, 153}, // Level 16
            {600, 1092, 924, 642, 854, 961, 179}, // Level 17
            {691, 1268, 1070, 741, 988, 1114, 210}, // Level 18
            {797, 1487, 1247, 855, 1148, 1300, 246}, // Level 19
            {924, 1791, 1480, 995, 1355, 1547, 287}, // Level 20
            {1074, 1791, 1791, 1172, 1355, 1547, 334}, // Level 21
            {1278, 1791, 1791, 1360, 1355, 1547, 388}, // Level 22
            {1278, 1791, 1791, 1360, 1355, 1547, 450}  // Level 23
        };

        int current = SKILL_PTS_PER_LVL[keeper][0]
            + SKILL_PTS_PER_LVL[defending][1]
            + SKILL_PTS_PER_LVL[playmaking][2]
            + SKILL_PTS_PER_LVL[winger][3]
            + SKILL_PTS_PER_LVL[passing][4]
            + SKILL_PTS_PER_LVL[scoring][5]
            + SKILL_PTS_PER_LVL[setPieces][6];

        int AGE_FACTOR = 28;
        int WEEKS_IN_SEASON = 16;
        int DAYS_IN_WEEK = 7;
        int DAYS_IN_SEASON = WEEKS_IN_SEASON * DAYS_IN_WEEK;

        double pointsDiff = 0;
        if (years < AGE_FACTOR) {
            double pointsPerWeek = WEEK_PTS_PER_AGE[years];
            pointsDiff += ((DAYS_IN_SEASON - days) / (double) DAYS_IN_WEEK) * pointsPerWeek;
            for (int i = years + 1; i < AGE_FACTOR; i++) {
                pointsDiff += WEEKS_IN_SEASON * WEEK_PTS_PER_AGE[i];
            }
        } else if (years <= MAX_AGE) {
            pointsDiff += (days / (double) DAYS_IN_WEEK) * WEEK_PTS_PER_AGE[years];
            for (int i = years; i > AGE_FACTOR; i--) {
                pointsDiff += WEEKS_IN_SEASON * WEEK_PTS_PER_AGE[i];
            }
            pointsDiff = -pointsDiff;
        } else {
            pointsDiff = -current;
        }

        return HTMS.builder()
            .htms(current)
            .htms28((int) Math.round(current + pointsDiff))
            .build();
    }

    private Training getTraining(easyhattrickmanager.client.model.training.Training training) {
        return Training.builder()
            .seasonWeek(convertToSeasonWeek(LocalDate.now()))
            .date(LocalDateTime.now())
            .teamId(training.getTeam().getTeamId())
            .trainingType(training.getTeam().getLastTrainingTrainingType())
            .trainingLevel(training.getTeam().getLastTrainingTrainingLevel())
            .staminaTrainingPart(training.getTeam().getLastTrainingStaminaTrainingPart())
            .build();
    }

    private Staff getStaff(int teamId, easyhattrickmanager.client.model.stafflist.Stafflist stafflist) {

        var staffs = stafflist.getStaffList().getStaffs();
        var staff1 = Optional.ofNullable(staffs.size() > 0 ? staffs.get(0) : null);
        var staff2 = Optional.ofNullable(staffs.size() > 1 ? staffs.get(1) : null);
        var staff3 = Optional.ofNullable(staffs.size() > 2 ? staffs.get(2) : null);
        var staff4 = Optional.ofNullable(staffs.size() > 3 ? staffs.get(3) : null);
        return Staff.builder()
            .seasonWeek(convertToSeasonWeek(LocalDate.now()))
            .date(LocalDateTime.now())
            .teamId(teamId)
            .trainerId(stafflist.getStaffList().getTrainer().getTrainerId())
            .trainerName(stafflist.getStaffList().getTrainer().getName())
            .trainerType(stafflist.getStaffList().getTrainer().getTrainerType())
            .trainerLeadership(stafflist.getStaffList().getTrainer().getLeadership())
            .trainerSkillLevel(stafflist.getStaffList().getTrainer().getTrainerSkillLevel())
            .trainerStatus(stafflist.getStaffList().getTrainer().getTrainerStatus())
            .staff1Id(staff1.map(staff -> staff.getStaffId()).orElse(null))
            .staff1Name(staff1.map(staff -> staff.getName()).orElse(null))
            .staff1Type(staff1.map(staff -> staff.getStaffType()).orElse(null))
            .staff1Level(staff1.map(staff -> staff.getStaffLevel()).orElse(null))
            .staff1HofPlayerId(staff1.map(staff -> staff.getHofPlayerId()).orElse(null))
            .staff2Id(staff2.map(staff -> staff.getStaffId()).orElse(null))
            .staff2Name(staff2.map(staff -> staff.getName()).orElse(null))
            .staff2Type(staff2.map(staff -> staff.getStaffType()).orElse(null))
            .staff2Level(staff2.map(staff -> staff.getStaffLevel()).orElse(null))
            .staff2HofPlayerId(staff2.map(staff -> staff.getHofPlayerId()).orElse(null))
            .staff3Id(staff3.map(staff -> staff.getStaffId()).orElse(null))
            .staff3Name(staff3.map(staff -> staff.getName()).orElse(null))
            .staff3Type(staff3.map(staff -> staff.getStaffType()).orElse(null))
            .staff3Level(staff3.map(staff -> staff.getStaffLevel()).orElse(null))
            .staff3HofPlayerId(staff3.map(staff -> staff.getHofPlayerId()).orElse(null))
            .staff4Id(staff4.map(staff -> staff.getStaffId()).orElse(null))
            .staff4Name(staff4.map(staff -> staff.getName()).orElse(null))
            .staff4Type(staff4.map(staff -> staff.getStaffType()).orElse(null))
            .staff4Level(staff4.map(staff -> staff.getStaffLevel()).orElse(null))
            .staff4HofPlayerId(staff4.map(staff -> staff.getHofPlayerId()).orElse(null))
            .build();
    }

    private League getLeague(easyhattrickmanager.client.model.worlddetails.League leagueHT) {
        return League.builder()
            .id(leagueHT.getLeagueId())
            .name(leagueHT.getLeagueName())
            .englishName(leagueHT.getEnglishName())
            .season(leagueHT.getSeason())
            .seasonOffset(leagueHT.getSeasonOffset())
            .trainingDate(leagueHT.getTrainingDate())
            .build();
    }

}
