package easyhattrickmanager.service;

import static easyhattrickmanager.utils.FileUtils.downloadFile;
import static easyhattrickmanager.utils.HTMSUtils.calculateHTMS;
import static easyhattrickmanager.utils.SeasonWeekUtils.convertToSeasonWeek;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import easyhattrickmanager.client.hattrick.model.avatars.Avatar;
import easyhattrickmanager.client.hattrick.model.avatars.Avatars;
import easyhattrickmanager.client.hattrick.model.avatars.Layer;
import easyhattrickmanager.client.hattrick.model.playerdetails.PlayerDetails;
import easyhattrickmanager.client.hattrick.model.players.Players;
import easyhattrickmanager.client.hattrick.model.staffavatars.StaffAvatars;
import easyhattrickmanager.client.hattrick.model.stafflist.Staff;
import easyhattrickmanager.client.hattrick.model.stafflist.Stafflist;
import easyhattrickmanager.client.hattrick.model.worlddetails.WorldDetails;
import easyhattrickmanager.configuration.AssetsConfiguration;
import easyhattrickmanager.repository.CountryDAO;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.PlayerDAO;
import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.PlayerSubSkillDAO;
import easyhattrickmanager.repository.PlayerTrainingDAO;
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
import easyhattrickmanager.repository.model.PlayerSubSkill;
import easyhattrickmanager.repository.model.PlayerTraining;
import easyhattrickmanager.repository.model.StaffMember;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.Trainer;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.repository.model.User;
import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.service.model.dataresponse.UserConfig;
import easyhattrickmanager.utils.SeasonWeekUtils;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final PlayerTrainingDAO playerTrainingDAO;
    private final PlayerSubSkillDAO playerSubSkillDAO;
    private final TrainingDAO trainingDAO;
    private final TrainerDAO trainerDAO;
    private final StaffMemberDAO staffMemberDAO;
    private final TeamDAO teamDAO;
    private final UserDAO userDAO;
    private final AssetsConfiguration assetsConfiguration;
    private final UserConfigDAO userConfigDAO;
    private final CalculateTrainingPercentageService calculateTrainingPercentageService;
    private final CalculateSubSkillTrainingService calculateSubSkillTrainingService;

    private List<String> images = new ArrayList<>();

    public void update() {
        teamDAO.getActiveTeams().forEach(team -> {
            try {
                update(team.getId());
            } catch (Exception e) {
                System.err.printf("Error update team %s%n", team.getId());
            }
        });

    }

    public void update(String username) {
        User user = userDAO.get(username);
        teamDAO.getByUserId(user.getId()).stream()
            .filter(team -> !team.isBot())
            .forEach(team -> update(team.getId()));
    }

    public void update(int teamId) {
        int adjustmentDays = getAdjustmentDays(teamId);
        String seasonWeek = convertToSeasonWeek(ZonedDateTime.now().plusDays(adjustmentDays));
        Players players = hattrickService.getPlayers(teamId);
        List<PlayerData> playerDatas = new ArrayList<>();
        players.getTeam().getPlayers()
            .forEach(playerHT -> {
                playerDAO.insert(getPlayer(playerHT));
                PlayerData playerData = getPlayerData(teamId, playerHT, adjustmentDays, seasonWeek);
                if (playerData.getAge() > 16) {
                    playerDatas.add(playerData);
                }
            });
        playerDatas.forEach(playerDataDAO::insert);
        Training training = getTraining(hattrickService.getTraining(teamId), seasonWeek);
        trainingDAO.insert(training);
        Stafflist staff = hattrickService.getStaff(teamId);
        trainerDAO.insert(getTrainer(teamId, staff.getStaffList().getTrainer(), seasonWeek));
        staff.getStaffList().getStaffs().forEach(staffHT -> staffMemberDAO.insert(getStaffMember(teamId, staffHT, seasonWeek)));
        saveAvatars(hattrickService.getAvatars(teamId));
        saveStaffAvatars(hattrickService.getStaffAvatars(teamId));
        List<PlayerTraining> playerTrainings = calculateTrainingPercentageService.calculateTrainingPercentage(seasonWeek, teamId, training, getTrainerLevel(staff), getAssistantsLevel(staff));
        playerTrainings.forEach(playerTrainingDAO::insert);
        List<PlayerSubSkill> playerSubSkills = calculateSubSkillTrainingService.calculateSubSkillTraining(seasonWeek, teamId, training, getTrainerLevel(staff), getAssistantsLevel(staff), playerDatas, playerTrainings);
        playerSubSkills.forEach(playerSubSkillDAO::insert);
    }

    private int getTrainerLevel(Stafflist staff) {
        return staff.getStaffList().getTrainer().getTrainerSkillLevel();
    }

    private int getAssistantsLevel(Stafflist staff) {
        return isEmpty(staff.getStaffList().getStaffs()) ? 0 : staff.getStaffList().getStaffs().stream()
            .filter(stf -> stf.getStaffType() == 1)
            .map(Staff::getStaffLevel)
            .reduce(0, Integer::sum);
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
        return SeasonWeekUtils.getAdjustmentDays(league.getTrainingDate(), ZonedDateTime.now());
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
            .teamId(teamId)
            .nickName(playerHT.getNickName())
            .playerNumber(playerHT.getPlayerNumber() < 100 ? playerHT.getPlayerNumber() : null)
            .age(playerHT.getAge())
            .ageDays(playerHT.getAgeDays())
            .arrivalDate(playerHT.getArrivalDate())
            .ownerNotes(playerHT.getOwnerNotes())
            .TSI(playerHT.getTSI())
            .playerForm(playerHT.getPlayerForm())
            .statement(playerHT.getStatement())
            .experience(playerHT.getExperience())
            .loyalty(playerHT.getLoyalty())
            .motherClubBonus(playerHT.isMotherClubBonus())
            .leadership(playerHT.getLeadership())
            .salary(playerHT.getSalary())
            .abroad(playerHT.getAbroad() == 1)
            .leagueGoals(playerHT.getLeagueGoals())
            .cupGoals(playerHT.getCupGoals())
            .friendliesGoals(playerHT.getFriendliesGoals())
            .careerGoals(playerHT.getCareerGoals())
            .careerHattricks(playerHT.getCareerHattricks())
            .matchesCurrentTeam(playerHT.getMatchesCurrentTeam())
            .goalsCurrentTeam(playerHT.getGoalsCurrentTeam())
            .assistsCurrentTeam(playerHT.getAssistsCurrentTeam())
            .careerAssists(playerHT.getCareerAssists())
            .transferListed(playerHT.isTransferListed())
            .nationalTeamId(playerHT.getNationalTeamId())
            .caps(playerHT.getCaps())
            .capsU21(playerHT.getCapsU21())
            .cards(playerHT.getCards())
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
            .playerCategoryId(playerHT.getPlayerCategoryId())
            .build();
    }

    private Training getTraining(easyhattrickmanager.client.hattrick.model.training.Training training, String seasonWeek) {
        return Training.builder()
            .seasonWeek(seasonWeek)
            .teamId(training.getTeam().getTeamId())
            .trainingType(training.getTeam().getLastTrainingTrainingType())
            .trainingLevel(training.getTeam().getLastTrainingTrainingLevel())
            .staminaTrainingPart(training.getTeam().getLastTrainingStaminaTrainingPart())
            .build();
    }

    private Trainer getTrainer(int teamId, easyhattrickmanager.client.hattrick.model.stafflist.Trainer trainerHT, String seasonWeek) {
        return Trainer.builder()
            .seasonWeek(seasonWeek)
            .teamId(teamId)
            .id(trainerHT.getTrainerId())
            .name(trainerHT.getName())
            .type(trainerHT.getTrainerType())
            .leadership(trainerHT.getLeadership())
            .skillLevel(trainerHT.getTrainerSkillLevel())
            .status(trainerHT.getTrainerStatus())
            .startDate(trainerHT.getContractDate())
            .cost(trainerHT.getCost())
            .build();
    }

    private StaffMember getStaffMember(int teamId, Staff staffHT, String seasonWeek) {
        return StaffMember.builder()
            .seasonWeek(seasonWeek)
            .teamId(teamId)
            .id(staffHT.getStaffId())
            .name(staffHT.getName())
            .type(staffHT.getStaffType())
            .level(staffHT.getStaffLevel())
            .hofPlayerId(staffHT.getHofPlayerId())
            .startDate(staffHT.getHiredDate())
            .cost(staffHT.getCost())
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
        avatars.getTeam().getPlayers().forEach(playerHT -> saveAvatar(playerHT.getAvatar(), playerHT.getPlayerId()));
    }

    private void saveStaffAvatars(StaffAvatars staffAvatars) {
        saveAvatar(staffAvatars.getTrainer().getAvatar(), staffAvatars.getTrainer().getTrainerId());
        staffAvatars.getStaffs().forEach(staffHT -> saveAvatar(staffHT.getAvatar(), staffHT.getStaffId()));
    }

    private void saveAvatar(Avatar avatar, int id) {
        if (!Files.exists(Paths.get(assetsConfiguration.getAssetsPath() + "/avatars/" + id + ".png"))) {
            saveImage(avatar.getBackgroundImage());
            avatar.getLayers().forEach(layer -> saveImage(layer.getImage()));
            saveImage(mountImage(avatar), id);
        }
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

    public void getAvatars(int teamId) {
        playerDAO.get(teamId).stream()
            .map(Player::getId)
            .distinct()
            .forEach(this::getAvatar);
    }

    public void getAvatar(int playerId) {
        try {
            PlayerDetails playerDetails = hattrickService.getPlayerDetails(playerId);
            int teamId = playerDetails.getPlayer().getOwningTeam().getTeamId();
            saveAvatars(hattrickService.getAvatarsTDT(teamId), playerId);
        } catch (Exception e) {
            System.err.printf("Error getAvatar %d. %s%n", playerId, e.getMessage());
        }
    }

    private void saveAvatars(Avatars avatars, int playerId) {
        avatars.getTeam().getPlayers().stream()
            .filter(playerHT -> playerHT.getPlayerId() == playerId)
            .forEach(
                playerHT -> {
                    if (!Files.exists(Paths.get(assetsConfiguration.getAssetsPath() + "/avatars/" + playerHT.getPlayerId() + ".png"))) {
                        saveImage(playerHT.getAvatar().getBackgroundImage());
                        playerHT.getAvatar().getLayers().forEach(layer -> saveImage(layer.getImage()));
                        saveImage(mountImage(playerHT.getAvatar()), playerHT.getPlayerId());
                    }
                }
            );
    }

    public void updateHTMS() {
        String seasonWeek = "S092W01";
        List<Integer> teamIds = List.of(44976, 116432, 308702, 436939, 636036, 652555, 714238, 896476, 2055732, 2067277, 2131425, 2153225, 2351664, 2462933);
        teamIds.forEach(teamId -> {
            List<PlayerData> playersData = playerDataDAO.get(teamId);
            playersData.stream()
                .filter(playerData -> Objects.equals(playerData.getSeasonWeek(), seasonWeek))
                .forEach(playerData -> {
                    HTMS newHTMS = calculateHTMS(playerData);
                    playerData.setHtms(newHTMS.getHtms());
                    playerData.setHtms28(newHTMS.getHtms28());
                    playerDataDAO.updateHTMS(playerData);
                });
        });
    }

    public void updateUserConfig() {
        userDAO.getAllUsers().forEach(user -> {
            try {
                UserConfig userConfig = new ObjectMapper().readValue(userConfigDAO.get(user.getId()), UserConfig.class);
                userConfig.setShowSubSkills(true);
                userConfigDAO.update(user.getId(), new ObjectMapper().writeValueAsString(userConfig));
            } catch (Exception e) {
                System.err.printf("Error updateUserConfig %s. %s%n", user.getId(), e.getMessage());
            }
        });
    }

}
