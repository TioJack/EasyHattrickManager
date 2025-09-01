package easyhattrickmanager.service;

import static easyhattrickmanager.utils.FileUtils.downloadFile;
import static easyhattrickmanager.utils.HTMSUtils.calculateHTMS;
import static easyhattrickmanager.utils.SeasonWeekUtils.convertFromSeasonWeek;
import static easyhattrickmanager.utils.SeasonWeekUtils.convertToSeasonWeek;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

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
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.Trainer;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.repository.model.User;
import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.service.model.dataresponse.UserConfig;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final TrainerDAO trainerDAO;
    private final StaffMemberDAO staffMemberDAO;
    private final TeamDAO teamDAO;
    private final UserDAO userDAO;
    private final AssetsConfiguration assetsConfiguration;
    private final UserConfigDAO userConfigDAO;

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
        Stafflist staff = hattrickService.getStaff(teamId);
        trainerDAO.insert(getTrainer(teamId, staff.getStaffList().getTrainer(), seasonWeek));
        staff.getStaffList().getStaffs().forEach(staffHT -> staffMemberDAO.insert(getStaffMember(teamId, staffHT, seasonWeek)));
        saveAvatars(hattrickService.getAvatars(teamId));
        saveStaffAvatars(hattrickService.getStaffAvatars(teamId));
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

    public void getAvatar(int playerId) {
        PlayerDetails playerDetails = hattrickService.getPlayerDetails(playerId);
        int teamId = playerDetails.getPlayer().getOwningTeam().getTeamId();
        saveAvatars(hattrickService.getAvatarsTDT(teamId), playerId);
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

    public void extendStaff(int teamId) {
        int adjustmentDays = getAdjustmentDays(teamId);
        List<PlayerData> playerData = playerDataDAO.get(teamId);

        List<Trainer> trainers = trainerDAO.get(teamId);
        List<Trainer> oldestPerTrainerId = trainers.stream()
            .collect(groupingBy(Trainer::getId))
            .values().stream()
            .map(list -> list.stream()
                .min(comparing(Trainer::getStartDate))
                .orElse(null))
            .filter(java.util.Objects::nonNull)
            .toList();
        oldestPerTrainerId.forEach(trainer -> {
            ZonedDateTime cursor = convertFromSeasonWeek(trainer.getSeasonWeek()).plusDays(adjustmentDays);
            while (cursor.isAfter(trainer.getStartDate())) {
                String seasonWeek = convertToSeasonWeek(cursor);
                trainer.setSeasonWeek(seasonWeek);
                trainer.setLeadership(getLeadership(playerData, trainer, seasonWeek));
                trainerDAO.insert(trainer);
                cursor = cursor.minusWeeks(1);
            }
        });
        List<StaffMember> staffMembers = staffMemberDAO.get(teamId);
        List<StaffMember> oldestPerStaffId = staffMembers.stream()
            .collect(groupingBy(StaffMember::getId))
            .values().stream()
            .map(list -> list.stream()
                .min(comparing(StaffMember::getStartDate))
                .orElse(null))
            .filter(java.util.Objects::nonNull)
            .toList();
        oldestPerStaffId.forEach(staffMember -> {
            ZonedDateTime cursor = convertFromSeasonWeek(staffMember.getSeasonWeek()).plusDays(adjustmentDays);
            while (cursor.isAfter(staffMember.getStartDate())) {
                staffMember.setSeasonWeek(convertToSeasonWeek(cursor));
                staffMemberDAO.insert(staffMember);
                cursor = cursor.minusWeeks(1);
            }
        });
    }

    private int getLeadership(List<PlayerData> playerData, Trainer trainer, String seasonWeek) {
        Optional<PlayerData> found = playerData.stream()
            .filter(pd -> (Objects.equals(pd.getSeasonWeek(), seasonWeek)) && (pd.getId() == trainer.getId()))
            .findFirst();
        return found.map(PlayerData::getLeadership).orElseGet(trainer::getLeadership);
    }

    public void updateUserConfig() {
        List<Country> countries = countryDAO.getAllCountries();
        userDAO.getAllUsers()
            .forEach(user -> {
                try {
                    UserConfig userConfig = new ObjectMapper().readValue(userConfigDAO.get(user.getId()), UserConfig.class);
                    if (userConfig.getDateFormat() == null) {
                        Country country = countries.stream().filter(cntry -> cntry.getId() == user.getCountryId()).findFirst().orElseThrow();
                        userConfig.setDateFormat(country.getDateFormat().replace('D', 'd').replace('Y', 'y'));
                        userConfigDAO.update(user.getId(), new ObjectMapper().writeValueAsString(userConfig));
                    }
                } catch (Exception e) {
                    System.err.printf("Error updateUserConfig %s. %s%n", user.getId(), e.getMessage());
                }
            });
    }

}
