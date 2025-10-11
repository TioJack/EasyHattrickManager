package easyhattrickmanager.controller;

import static java.lang.Integer.parseInt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.PlayerDAO;
import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.StaffMemberDAO;
import easyhattrickmanager.repository.TrainerDAO;
import easyhattrickmanager.repository.TrainingDAO;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.Player;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.StaffMember;
import easyhattrickmanager.repository.model.Trainer;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.utils.HTMSUtils;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@RestController
@RequestMapping("shar")
public class SharController {

    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    private static final int DEFAULT_COST = 99999;

    private final LeagueDAO leagueDAO;
    private final TrainingDAO trainingDAO;
    private final PlayerDAO playerDAO;
    private final PlayerDataDAO playerDataDAO;
    private final TrainerDAO trainerDAO;
    private final StaffMemberDAO staffMemberDAO;

    @GetMapping()
    public ResponseEntity<String> migrate(@RequestPart("file") MultipartFile file) {
        List<Player> players = new ArrayList<>();
        List<Map<String, Object>> playersObj = new ArrayList<>();
        List<Training> trainings = new ArrayList<>();
        List<PlayerData> playersData = new ArrayList<>();
        List<Trainer> trainers = new ArrayList<>();
        List<StaffMember> staffMembers = new ArrayList<>();

        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("empty file");
            }
            String contentType = file.getContentType();
            String filename = file.getOriginalFilename();
            boolean isJsonByMime = "application/json".equalsIgnoreCase(contentType);
            boolean isJsonByName = filename != null && filename.toLowerCase().endsWith(".json");
            if (!isJsonByMime || !isJsonByName) {
                return ResponseEntity.badRequest().body("invalid file");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonData;
            try (InputStream inputStream = file.getInputStream()) {
                jsonData = objectMapper.readValue(inputStream, new TypeReference<>() {
                });
            }
            int teamId = parseInt(getFileNameWithoutExtension(filename));
            Integer leagueId = (Integer) ((Map<String, Object>) jsonData.get("League")).get("LeagueID");
            League league = leagueDAO.get(leagueId).orElseThrow();

            List<Map<String, Object>> playerList = (List<Map<String, Object>>) jsonData.get("PlayerList");
            playerList.forEach(playerObj -> {
                playersObj.add(playerObj);
                players.add(Player.builder()
                    .id((int) playerObj.get("PlayerID"))
                    .firstName(((String) playerObj.get("FirstName")).replace("'", "''"))
                    .lastName(((String) playerObj.get("LastName")).replace("'", "''"))
                    .agreeability((int) playerObj.get("Agreeability"))
                    .aggressiveness((int) playerObj.get("Aggressiveness"))
                    .honesty((int) playerObj.get("Honesty"))
                    .specialty((int) playerObj.get("SpecialtyID"))
                    .countryId((int) playerObj.get("CountryID"))
                    .build()
                );
            });

            List<Map<String, Object>> weeklyData = (List<Map<String, Object>>) jsonData.get("WeeklyData");
            weeklyData.forEach(entry -> {
                int season = (int) entry.get("Season") - league.getSeasonOffset();
                int week = (int) entry.get("Week");
                Map<String, Object> training = (Map<String, Object>) entry.get("Training");
                String seasonWeek = String.format("S%03dW%02d", season, week);
                trainings.add(Training.builder()
                    .seasonWeek(seasonWeek)
                    .teamId(teamId)
                    .trainingType((int) training.get("TrainingType"))
                    .trainingLevel((int) training.get("TrainingLevel"))
                    .staminaTrainingPart((int) training.get("StaminaTrainingPart"))
                    .build());

                Map<String, Object> staffList = (Map<String, Object>) entry.get("StaffList");
                if (staffList != null) {
                    Map<String, Object> trainer = (Map<String, Object>) staffList.get("Trainer");
                    if (trainer != null) {
                        trainers.add(Trainer.builder()
                            .seasonWeek(seasonWeek)
                            .teamId(teamId)
                            .id(((Number) trainer.get("TrainerId")).intValue())
                            .name((String) trainer.get("Name"))
                            .type(((Number) trainer.get("TrainerType")).intValue())
                            .leadership(((Number) trainer.getOrDefault("Leadership", 0)).intValue())
                            .skillLevel(((Number) trainer.getOrDefault("TrainerSkillLevel", 0)).intValue())
                            .status(((Number) trainer.getOrDefault("TrainerStatus", 0)).intValue())
                            .startDate(DEFAULT_DATE)
                            .cost(DEFAULT_COST)
                            .build());

                        List<Map<String, Object>> staffMemberList = (List<Map<String, Object>>) staffList.get("StaffMembers");
                        staffMemberList.forEach(staffMemberObj -> {
                            staffMembers.add(StaffMember.builder()
                                .seasonWeek(seasonWeek)
                                .teamId(teamId)
                                .id(((Number) staffMemberObj.get("StaffId")).intValue())
                                .name((String) staffMemberObj.get("Name"))
                                .type(((Number) staffMemberObj.get("StaffType")).intValue())
                                .level(((Number) staffMemberObj.get("StaffLevel")).intValue())
                                .hofPlayerId(0)
                                .startDate(DEFAULT_DATE)
                                .cost(DEFAULT_COST)
                                .build());
                        });
                    }
                }
                List<Map<String, Object>> playerDataList = (List<Map<String, Object>>) entry.get("PlayerData");
                playerDataList.forEach(player -> {
                    int playerId = (int) player.get("PlayerID");
                    Map<String, Object> playerInfo = playersObj.stream().filter(p -> (int) p.get("PlayerID") == playerId).findFirst().orElseThrow();
                    PlayerData playerData = PlayerData.builder()
                        .id(playerId)
                        .seasonWeek(seasonWeek)
                        .teamId(teamId)
                        .nickName((String) playerInfo.get("NickName"))
                        .playerNumber((int) player.get("PlayerNumber"))
                        .age((int) player.get("Age"))
                        .ageDays((int) player.get("AgeDays"))
                        .arrivalDate(DEFAULT_DATE)
                        .ownerNotes(null)
                        .TSI((int) player.get("TSI"))
                        .playerForm((int) player.get("PlayerForm"))
                        .statement(null)
                        .experience((int) player.get("Experience"))
                        .loyalty((int) player.get("Loyalty"))
                        .motherClubBonus((boolean) playerInfo.get("MotherClubBonus"))
                        .leadership((int) player.get("Leadership"))
                        .salary((int) player.get("Salary") * 10)
                        .abroad((boolean) playerInfo.get("IsAbroad"))
                        .leagueGoals(0)
                        .cupGoals(0)
                        .friendliesGoals(0)
                        .careerGoals(0)
                        .careerHattricks(0)
                        .matchesCurrentTeam(0)
                        .goalsCurrentTeam(0)
                        .assistsCurrentTeam(0)
                        .careerAssists(0)
                        .transferListed(false)
                        .nationalTeamId(0)
                        .caps(0)
                        .capsU21(0)
                        .cards(0)
                        .injuryLevel((int) player.get("InjuryLevel"))
                        .staminaSkill((int) player.get("StaminaSkill"))
                        .keeperSkill((int) player.get("KeeperSkill"))
                        .playmakerSkill((int) player.get("PlaymakerSkill"))
                        .scorerSkill((int) player.get("ScorerSkill"))
                        .passingSkill((int) player.get("PassingSkill"))
                        .wingerSkill((int) player.get("WingerSkill"))
                        .defenderSkill((int) player.get("DefenderSkill"))
                        .setPiecesSkill((int) player.get("SetPiecesSkill"))
                        .playerCategoryId((int) player.get("PlayerCategoryId"))
                        .build();
                    HTMS htms = HTMSUtils.calculateHTMS(playerData);
                    playerData.setHtms(htms.getHtms());
                    playerData.setHtms28(htms.getHtms28());
                    playersData.add(playerData);
                });
            });
            trainings.forEach(trainingDAO::insert);
            players.forEach(playerDAO::insert);
            playersData.forEach(playerDataDAO::insert);
            trainers.forEach(trainerDAO::insert);
            staffMembers.forEach(staffMemberDAO::insert);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error reading JSON file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    private String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

}
