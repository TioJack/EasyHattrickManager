package easyhattrickmanager.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import easyhattrickmanager.repository.CountryDAO;
import easyhattrickmanager.repository.model.LeagueCountry;
import easyhattrickmanager.service.UpdateService;
import easyhattrickmanager.service.model.HTMS;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("shar")
public class SharController {

    private final UpdateService updateService;
    private final CountryDAO countryDAO;

    @GetMapping()
    public ResponseEntity<List<String>> migrate() {
        try {
            // select t.id, l.seasonOffset from team t  join league l on t.league_id = l.id where t.id in (797465,2067039,2130843) order by t.id;

            int teamId = 797465;
            int seasonOffset = 12;

            //int teamId = 2067039;
            //int seasonOffset = 75;

            //int teamId = 2130843;
            //int seasonOffset = 87;

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(teamId + "_data.json");
            if (inputStream == null) {
                return ResponseEntity.badRequest().body(List.of("File not found"));
            }
            List<String> result = new ArrayList<>();
            Map<Integer, Integer> l2c = countryDAO.getAllLeagueCountry().stream().collect(Collectors.toMap(LeagueCountry::getLeagueId, LeagueCountry::getCountryId));
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonData = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            List<Map<String, Object>> players = (List<Map<String, Object>>) jsonData.get("PlayerList");
            players.forEach(player -> {
                int id = (int) player.get("PlayerID");
                String firstName = ((String) player.get("FirstName")).replace("'", "''");
                String lastName = ((String) player.get("LastName")).replace("'", "''");
                int agreeability = (int) player.get("Agreeability");
                int aggressiveness = (int) player.get("Aggressiveness");
                int honesty = (int) player.get("Honesty");
                int specialty = (int) player.get("SpecialtyID");
                int countryId = (int) player.get("CountryID");
//                result.add(String.format(
//                    "INSERT IGNORE INTO player (id, first_name, last_name, agreeability, aggressiveness, honesty, specialty, country_id) VALUES (%d, '%s', '%s', %d, %d, %d, %d, %d);",
//                    id, firstName, lastName, agreeability, aggressiveness, honesty, specialty, getCountryIdByLeagueId(l2c, countryId)
//                ));
            });
            List<Map<String, Object>> weeklyData = (List<Map<String, Object>>) jsonData.get("WeeklyData");
            weeklyData.forEach(entry -> {
                int season = (int) entry.get("Season") + seasonOffset;
                int week = (int) entry.get("Week");
                Map<String, Object> training = (Map<String, Object>) entry.get("Training");
                int trainingLevel = (int) training.get("TrainingLevel");
                int trainingType = (int) training.get("TrainingType");
                int staminaTrainingPart = (int) training.get("StaminaTrainingPart");
                String seasonWeek = String.format("S%03dW%02d", season, week);
                ZonedDateTime zdt = updateService.convertFromSeasonWeek(seasonWeek);
//                result.add(String.format("INSERT IGNORE INTO training (season_week, `date`, team_id, training_type, training_level, stamina_training_part) VALUES('%s', '%s', %d, %d, %d, %d);",
//                    seasonWeek,
//                    zdt.plusDays(4).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 02:00:00",
//                    teamId,
//                    trainingType,
//                    trainingLevel,
//                    staminaTrainingPart
//                ));
                Map<String, Object> staffList = (Map<String, Object>) entry.get("StaffList");
                if (staffList != null) {
                    Map<String, Object> trainer = (Map<String, Object>) staffList.get("Trainer");
                    if (trainer != null) {
                        int trainerId = ((Number) trainer.get("TrainerId")).intValue();
                        String trainerName = (String) trainer.get("Name");
                        int trainerType = ((Number) trainer.get("TrainerType")).intValue();
                        int trainerLeadership = ((Number) trainer.getOrDefault("Leadership", 0)).intValue();
                        int trainerSkillLevel = ((Number) trainer.getOrDefault("TrainerSkillLevel", 0)).intValue();
                        int trainerStatus = ((Number) trainer.getOrDefault("TrainerStatus", 0)).intValue(); // por si no viene en JSON

                        List<Map<String, Object>> staffMembers = (List<Map<String, Object>>) staffList.get("StaffMembers");
                        Map<String, Object> s1 = (staffMembers != null && staffMembers.size() >= 1) ? staffMembers.get(0) : null;
                        Map<String, Object> s2 = (staffMembers != null && staffMembers.size() >= 2) ? staffMembers.get(1) : null;
                        Map<String, Object> s3 = (staffMembers != null && staffMembers.size() >= 3) ? staffMembers.get(2) : null;
                        Map<String, Object> s4 = (staffMembers != null && staffMembers.size() >= 4) ? staffMembers.get(3) : null;

                        result.add(String.format(
                            "INSERT IGNORE INTO staff (" +
                                "season_week, `date`, team_id, " +
                                "trainer_id, trainer_name, trainer_type, trainer_leadership, trainer_skill_level, trainer_status, " +
                                "staff1_id, staff1_name, staff1_type, staff1_level, staff1_hof_player_id, " +
                                "staff2_id, staff2_name, staff2_type, staff2_level, staff2_hof_player_id, " +
                                "staff3_id, staff3_name, staff3_type, staff3_level, staff3_hof_player_id, " +
                                "staff4_id, staff4_name, staff4_type, staff4_level, staff4_hof_player_id" +
                                ") VALUES ('%s', '%s', %d, %d, %s, %d, %d, %d, %d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);",
                            seasonWeek,
                            zdt.plusDays(4).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 02:00:00",
                            teamId,
                            trainerId, toSqlStr(trainerName), trainerType, trainerLeadership, trainerSkillLevel, trainerStatus,
                            toSqlInt(getSafe(s1, "StaffId")), toSqlStr(getSafeStr(s1, "Name")), toSqlInt(getSafe(s1, "StaffType")), toSqlInt(getSafe(s1, "StaffLevel")), toSqlInt(getSafe(s1, "HofPlayerId")),
                            toSqlInt(getSafe(s2, "StaffId")), toSqlStr(getSafeStr(s2, "Name")), toSqlInt(getSafe(s2, "StaffType")), toSqlInt(getSafe(s2, "StaffLevel")), toSqlInt(getSafe(s2, "HofPlayerId")),
                            toSqlInt(getSafe(s3, "StaffId")), toSqlStr(getSafeStr(s3, "Name")), toSqlInt(getSafe(s3, "StaffType")), toSqlInt(getSafe(s3, "StaffLevel")), toSqlInt(getSafe(s3, "HofPlayerId")),
                            toSqlInt(getSafe(s4, "StaffId")), toSqlStr(getSafeStr(s4, "Name")), toSqlInt(getSafe(s4, "StaffType")), toSqlInt(getSafe(s4, "StaffLevel")), toSqlInt(getSafe(s4, "HofPlayerId"))
                        ));
                    }
                }
                List<Map<String, Object>> playerDataList = (List<Map<String, Object>>) entry.get("PlayerData");
                playerDataList.forEach(player -> {
                    int id = (int) player.get("PlayerID");
                    int playerNumber = (int) player.get("PlayerNumber");
                    int age = (int) player.get("Age");
                    int ageDays = (int) player.get("AgeDays");
                    int tsi = (int) player.get("TSI");
                    int playerForm = (int) player.get("PlayerForm");
                    int experience = (int) player.get("Experience");
                    int loyalty = (int) player.get("Loyalty");
                    int leadership = (int) player.get("Leadership");
                    int salary = (int) player.get("Salary");
                    int injuryLevel = (int) player.get("InjuryLevel");
                    int staminaSkill = (int) player.get("StaminaSkill");
                    int keeperSkill = (int) player.get("KeeperSkill");
                    int playmakerSkill = (int) player.get("PlaymakerSkill");
                    int scorerSkill = (int) player.get("ScorerSkill");
                    int passingSkill = (int) player.get("PassingSkill");
                    int wingerSkill = (int) player.get("WingerSkill");
                    int defenderSkill = (int) player.get("DefenderSkill");
                    int setPiecesSkill = (int) player.get("SetPiecesSkill");
                    HTMS htms = updateService.calculateHTMS(age, ageDays, keeperSkill, defenderSkill, playmakerSkill, wingerSkill, passingSkill, scorerSkill, setPiecesSkill);
//                    result.add(String.format(
//                        "INSERT IGNORE INTO player_data (id, season_week, `date`, team_id, nickName, player_number, age, age_days, TSI, player_form, experience, loyalty, mother_club_bonus, leadership, salary, injury_level, stamina_skill, keeper_skill, playmaker_skill, scorer_skill, passing_skill, winger_skill, defender_skill, set_pieces_skill, htms, htms28) VALUES (%d,'%s','%s',%d,'', %d, %d, %d, %d,%d,%d,%d, %d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d);",
//                        id,
//                        seasonWeek,
//                        zdt.plusDays(4).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 02:00:00",
//                        teamId,
//                        playerNumber,
//                        age,
//                        ageDays,
//                        tsi,
//                        playerForm,
//                        experience,
//                        loyalty,
//                        getMotherClubBonus(players, id),
//                        leadership,
//                        salary,
//                        injuryLevel,
//                        staminaSkill,
//                        keeperSkill,
//                        playmakerSkill,
//                        scorerSkill,
//                        passingSkill,
//                        wingerSkill,
//                        defenderSkill,
//                        setPiecesSkill,
//                        htms.getHtms(),
//                        htms.getHtms28()
//                    ));
                });
            });
            saveToFile(String.join("\n", result), teamId + "_data.sql");
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(List.of("Error reading JSON file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of("Unexpected error: " + e.getMessage()));
        }
    }

    private int getMotherClubBonus(List<Map<String, Object>> players, int id) {
        return players.stream()
            .filter(player -> player.get("PlayerID").equals(id))
            .map(player -> (boolean) player.get("MotherClubBonus") ? 1 : 0)
            .findFirst()
            .orElse(0);
    }

    private int getCountryIdByLeagueId(Map<Integer, Integer> l2c, int input) {
        if (!l2c.containsKey(input)) {
            throw new RuntimeException("Key not found: " + input);
        }
        return l2c.get(input);
    }

    private void saveToFile(String content, String fileName) {
        try {
            Files.writeString(Paths.get(fileName), content);
        } catch (IOException e) {
            System.err.println("Error saveToFile: " + e.getMessage());
        }
    }

    private static Object getSafe(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object v = map.get(key);
        return v;
    }

    private static String getSafeStr(Map<String, Object> map, String key) {
        Object v = getSafe(map, key);
        return v == null ? null : String.valueOf(v);
    }

    private static String toSqlInt(Object v) {
        if (v == null) {
            return "NULL";
        }
        return String.valueOf(((Number) v).intValue());
    }

    private static String toSqlStr(String v) {
        if (v == null) {
            return "NULL";
        }
        return "'" + v.replace("'", "''") + "'";
    }

}
