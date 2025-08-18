package easyhattrickmanager.service;

import static java.lang.Integer.parseInt;

import easyhattrickmanager.client.hattrick.model.avatars.Avatar;
import easyhattrickmanager.client.hattrick.model.avatars.Avatars;
import easyhattrickmanager.client.hattrick.model.avatars.Layer;
import easyhattrickmanager.client.hattrick.model.players.Players;
import easyhattrickmanager.client.hattrick.model.stafflist.Stafflist;
import easyhattrickmanager.client.hattrick.model.worlddetails.WorldDetails;
import easyhattrickmanager.configuration.AssetsConfiguration;
import easyhattrickmanager.repository.CountryDAO;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.PlayerDAO;
import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.StaffDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.TrainingDAO;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.model.Country;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.Player;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.Staff;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.repository.model.User;
import easyhattrickmanager.service.model.HTMS;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateService {

    private final HattrickService hattrickService;
    private final CountryDAO countryDAO;
    private final LeagueDAO leagueDAO;
    private final PlayerDAO playerDAO;
    private final PlayerDataDAO playerDataDAO;
    private final TrainingDAO trainingDAO;
    private final StaffDAO staffDAO;
    private final TeamDAO teamDAO;
    private final UserDAO userDAO;
    private final AssetsConfiguration assetsConfiguration;

    private List<String> images = new ArrayList<>();

    public void update(String username) {
        User user = userDAO.get(username);
        teamDAO.getByUserId(user.getId()).forEach(team -> update(team.getId()));
    }

    public void update(int teamId) {
        int adjustmentDays = getAdjustmentDays(teamId);
        String seasonWeek = convertToSeasonWeek(ZonedDateTime.now().plusDays(adjustmentDays));
        Players players = hattrickService.getPlayers(teamId);
        players.getTeam().getPlayers()
            .forEach(playerHT -> {
                playerDAO.insert(getPlayer(playerHT));
                PlayerData playerData = getPlayerData(teamId, playerHT, adjustmentDays, seasonWeek);
                if (playerData.getAge() > 16) {
                    playerDataDAO.insert(playerData);
                }
            });
        trainingDAO.insert(getTraining(hattrickService.getTraining(teamId), seasonWeek));
        staffDAO.insert(getStaff(teamId, hattrickService.getStaff(teamId), seasonWeek));
        saveAvatars(hattrickService.getAvatars(teamId));
    }

    public int getActualWeek(int teamId) {
        int adjustmentDays = getAdjustmentDays(teamId);
        String seasonWeek = convertToSeasonWeek(ZonedDateTime.now().plusDays(adjustmentDays));
        return Integer.parseInt(seasonWeek.substring(5, 7));
    }

    public void updateLeagues() {
        WorldDetails worldDetails = hattrickService.getWorlddetails();
        worldDetails.getLeagues().forEach(leagueHT -> {
            leagueDAO.insert(getLeague(leagueHT));
            if (leagueHT.getCountry().getCountryId() > 0) {
                countryDAO.insert(getCountry(leagueHT.getCountry()));
                countryDAO.insertLeagueCountry(leagueHT.getLeagueId(), leagueHT.getCountry().getCountryId());
            }
        });
    }

    public League getLeague(int id) {
        var league = leagueDAO.get(id);
        if (league.isEmpty()) {
            updateLeagues();
            league = leagueDAO.get(id);
            if (league.isEmpty()) {
                throw new RuntimeException("League not found: " + id);
            }
        }
        return league.get();
    }

    private int getAdjustmentDays(int teamId) {
        Team team = teamDAO.get(teamId);
        League league = getLeague(team.getLeagueId());
        return getAdjustmentDays(league.getTrainingDate());
    }

    private int getAdjustmentDays(ZonedDateTime inputDate) {
        DayOfWeek targetDay = inputDate.getDayOfWeek();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime adjustedNow = now
            .withHour(inputDate.getHour())
            .withMinute(inputDate.getMinute())
            .withSecond(inputDate.getSecond())
            .withNano(inputDate.getNano());
        int daysToSubtract = now.getDayOfWeek().getValue() - targetDay.getValue();
        if (daysToSubtract < 0 || (daysToSubtract == 0 && now.isBefore(adjustedNow))) {
            daysToSubtract += 7;
        }
        return -1 * daysToSubtract;
    }

    private Player getPlayer(easyhattrickmanager.client.hattrick.model.players.Player playerHT) {
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

    private PlayerData getPlayerData(int teamId, easyhattrickmanager.client.hattrick.model.players.Player playerHT, int adjustmentDays, String seasonWeek) {
        int adjustedAgeDays = playerHT.getAgeDays() + adjustmentDays;
        int adjustedAge = playerHT.getAge();
        if (adjustedAgeDays >= 112) {
            adjustedAge++;
            adjustedAgeDays -= 112;
        } else if (adjustedAgeDays < 0) {
            adjustedAge--;
            adjustedAgeDays += 112;
        }
        playerHT.setAge(adjustedAge);
        playerHT.setAgeDays(adjustedAgeDays);
        HTMS htms = calculateHTMS(playerHT);
        return PlayerData.builder()
            .id(playerHT.getPlayerId())
            .seasonWeek(seasonWeek)
            .date(ZonedDateTime.now())
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

    private String convertToSeasonWeek(ZonedDateTime date) {
        ZonedDateTime referenceDate = ZonedDateTime.of(2025, 5, 5, 0, 0, 0, 0, ZoneId.of("Europe/Madrid"));
        int referenceSeason = 91;
        int referenceWeekInSeason = 2;
        ZonedDateTime startOfReferenceSeason = referenceDate.minusWeeks(referenceWeekInSeason - 1);
        long weeksSinceReference = ChronoUnit.WEEKS.between(startOfReferenceSeason, date);
        ZonedDateTime calculatedStartOfWeek = startOfReferenceSeason.plusWeeks(weeksSinceReference);
        if (date.isBefore(calculatedStartOfWeek)) {
            weeksSinceReference -= 1;
        }
        long seasonsSinceReference = weeksSinceReference / 16;
        long weekInSeason = (weeksSinceReference % 16) + 1;
        if (weekInSeason <= 0) {
            weekInSeason += 16;
            seasonsSinceReference -= 1;
        }
        long season = referenceSeason + seasonsSinceReference;
        return String.format("S%03dW%02d", season, weekInSeason);
    }

    public ZonedDateTime convertFromSeasonWeek(String seasonWeek) {
        if (!seasonWeek.matches("S\\d{3}W\\d{2}")) {
            throw new IllegalArgumentException("Incorrect format SxxxWyy");
        }
        int season = parseInt(seasonWeek.substring(1, 4));
        int week = parseInt(seasonWeek.substring(5, 7));
        if (week < 1 || week > 16) {
            throw new IllegalArgumentException("Week will be between 1-16");
        }
        ZonedDateTime referenceDate = ZonedDateTime.of(2025, 5, 5, 0, 0, 0, 0, ZoneId.of("Europe/Madrid"));
        int referenceSeason = 91;
        int referenceWeekInSeason = 2;
        ZonedDateTime startOfReferenceSeason = referenceDate.minusWeeks(referenceWeekInSeason - 1);
        int seasonsDifference = season - referenceSeason;
        ZonedDateTime startOfRequestedSeason = startOfReferenceSeason.plusWeeks(seasonsDifference * 16);
        ZonedDateTime startOfRequestedWeek = startOfRequestedSeason.plusWeeks(week - 1);
        return startOfRequestedWeek;
    }

    private HTMS calculateHTMS(easyhattrickmanager.client.hattrick.model.players.Player playerHT) {
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

    public HTMS calculateHTMS(int years, int days, int keeper, int defending, int playmaking, int winger, int passing, int scoring, int setPieces) {
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

    private Training getTraining(easyhattrickmanager.client.hattrick.model.training.Training training, String seasonWeek) {
        return Training.builder()
            .seasonWeek(seasonWeek)
            .date(ZonedDateTime.now())
            .teamId(training.getTeam().getTeamId())
            .trainingType(training.getTeam().getLastTrainingTrainingType())
            .trainingLevel(training.getTeam().getLastTrainingTrainingLevel())
            .staminaTrainingPart(training.getTeam().getLastTrainingStaminaTrainingPart())
            .build();
    }

    private Staff getStaff(int teamId, Stafflist stafflist, String seasonWeek) {
        var staffs = stafflist.getStaffList().getStaffs();
        var staff1 = Optional.ofNullable(staffs.size() > 0 ? staffs.get(0) : null);
        var staff2 = Optional.ofNullable(staffs.size() > 1 ? staffs.get(1) : null);
        var staff3 = Optional.ofNullable(staffs.size() > 2 ? staffs.get(2) : null);
        var staff4 = Optional.ofNullable(staffs.size() > 3 ? staffs.get(3) : null);
        return Staff.builder()
            .seasonWeek(seasonWeek)
            .date(ZonedDateTime.now())
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

    private League getLeague(easyhattrickmanager.client.hattrick.model.worlddetails.League leagueHT) {
        return League.builder()
            .id(leagueHT.getLeagueId())
            .name(leagueHT.getLeagueName())
            .englishName(leagueHT.getEnglishName())
            .season(leagueHT.getSeason())
            .seasonOffset(leagueHT.getSeasonOffset())
            .trainingDate(leagueHT.getTrainingDate())
            .build();
    }

    private Country getCountry(easyhattrickmanager.client.hattrick.model.worlddetails.Country countryHT) {
        return Country.builder()
            .id(countryHT.getCountryId())
            .name(countryHT.getCountryName())
            .code(countryHT.getCountryCode())
            .currencyName(countryHT.getCurrencyName())
            .currencyRate(new BigDecimal(countryHT.getCurrencyRate().replace(",", ".")))
            .dateFormat(countryHT.getDateFormat())
            .timeFormat(countryHT.getTimeFormat())
            .build();
    }

    private void saveAvatars(Avatars avatars) {
        avatars.getTeam().getPlayers().forEach(
            playerHT -> {
                if (!Files.exists(Paths.get(assetsConfiguration.getAssetsPath() + "/avatars/" + playerHT.getPlayerId() + ".png"))) {
                    saveImage(playerHT.getAvatar().getBackgroundImage());
                    playerHT.getAvatar().getLayers().forEach(layer -> saveImage(layer.getImage()));
                    saveImage(mountImage(playerHT.getAvatar()), playerHT.getPlayerId());
                }
            }
        );
    }

    private BufferedImage mountImage(Avatar avatar) {
        try {
            List<Layer> layers = avatar.getLayers();
            Layer background = layers.get(0);
            int offsetX = background.getX();
            int offsetY = background.getY();
            BufferedImage backgroundImage = ImageIO.read(new File(assetsConfiguration.getAssetsPath() + background.getImage()));
            BufferedImage finalImage = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphic = finalImage.createGraphics();
            graphic.drawImage(backgroundImage, 0, 0, null);
            for (int i = 1; i < avatar.getLayers().size(); i++) {
                Layer layer = layers.get(i);
                if (!layer.getImage().contains("misc")) {
                    String layerImagePath = assetsConfiguration.getAssetsPath() + layer.getImage().replace("http://res.hattrick.org", "/Img/Avatar");
                    BufferedImage layerImage = ImageIO.read(new File(layerImagePath));
                    graphic.drawImage(layerImage, layer.getX() - offsetX, layer.getY() - offsetY, null);
                }
            }
            graphic.dispose();
            return finalImage;
        } catch (Exception e) {
            System.err.printf("Error mountImage. %s%n", e.getMessage());
        }
        return null;
    }

    private void saveImage(BufferedImage image, int id) {
        try {
            String outputPath = assetsConfiguration.getAssetsPath() + "/avatars/" + id + ".png";
            Files.createDirectories(Paths.get(outputPath).getParent());
            ImageIO.write(image, "png", new File(outputPath));
        } catch (Exception e) {
            System.err.printf("Error saveImage %d. %s%n", id, e.getMessage());
        }
    }

    private void saveImage(String url) {
        if (!images.contains(url)) {
            String imageUrl;
            String destinationPath;
            if (url.startsWith("http://res.hattrick.org")) {
                imageUrl = url;
                destinationPath = assetsConfiguration.getAssetsPath() + url.replace("http://res.hattrick.org", "/Img/Avatar");
            } else {
                imageUrl = assetsConfiguration.getHattrickUrl() + url;
                destinationPath = assetsConfiguration.getAssetsPath() + url;
            }
            if (!Files.exists(Paths.get(destinationPath))) {
                downloadFile(imageUrl, destinationPath);
            }
            images.add(url);
        }
    }

    private void downloadFile(String fileUrl, String destinationPath) {
        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.createDirectories(Paths.get(destinationPath).getParent());
            Files.copy(in, Paths.get(destinationPath));
        } catch (Exception e) {
            System.err.printf("Error downloadFile %s %s. %s%n", fileUrl, destinationPath, e.getMessage());
        }
    }

}
