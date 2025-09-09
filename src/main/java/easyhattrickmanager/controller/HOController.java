package easyhattrickmanager.controller;

import static easyhattrickmanager.utils.SeasonWeekUtils.convertToSeasonWeek;
import static easyhattrickmanager.utils.SeasonWeekUtils.getAdjustmentDays;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.RequiredArgsConstructor;
import org.ini4j.Ini;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("ho")
public class HOController {

    private final LeagueDAO leagueDAO;
    private final TrainingDAO trainingDAO;
    private final PlayerDAO playerDAO;
    private final PlayerDataDAO playerDataDAO;
    private final TrainerDAO trainerDAO;
    private final StaffMemberDAO staffMemberDAO;

    private Map<Integer, ZonedDateTime> startDate = new HashMap<>();

    @GetMapping()
    public ResponseEntity<String> migrate(@RequestPart("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("empty file");
            }
            String contentType = file.getContentType();
            String filename = file.getOriginalFilename();
            boolean isZipByMime = "application/zip".equalsIgnoreCase(contentType);
            boolean isZipByName = filename != null && filename.toLowerCase().endsWith(".zip");
            if (!isZipByMime && !isZipByName) {
                return ResponseEntity.badRequest().body("invalid file");
            }
            List<String> results = new ArrayList<>();
            Path tempZip = Files.createTempFile("upload-", ".zip");
            try {
                file.transferTo(tempZip);
                try (ZipFile zipFile = new ZipFile(tempZip.toFile())) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    List<ZipEntry> entryList = new ArrayList<>();
                    while (entries.hasMoreElements()) {
                        entryList.add(entries.nextElement());
                    }
                    for (ZipEntry entry : entryList.stream().sorted(comparing(ZipEntry::getName)).toList()) {
                        if (entry.isDirectory()) {
                            continue;
                        }
                        String name = entry.getName();
                        if (!name.toLowerCase().endsWith(".hrf")) {
                            continue;
                        }
                        try (InputStream entryStream = zipFile.getInputStream(entry); InputStreamReader reader = new InputStreamReader(entryStream, UTF_8)) {
                            migrateHRF(reader);
                        }
                    }
                }
            } finally {
                try {
                    Files.deleteIfExists(tempZip);
                } catch (Exception ignore) {
                }
            }
            return ResponseEntity.ok(String.join("\n", results));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    private void migrateHRF(InputStreamReader reader) throws IOException {
        Ini ini = getIni(reader);
        int teamId = parseInt(ini.get("basics").get("teamID"));
        ZonedDateTime date = parseDate(ini.get("basics").get("date"));
        int leagueId = parseInt(ini.get("basics").get("leagueID"));
        League league = leagueDAO.get(leagueId).orElseThrow();
        int adjustmentDays = getAdjustmentDays(league.getTrainingDate(), date);
        String seasonWeek = convertToSeasonWeek(date.plusDays(adjustmentDays));

        Training training = getTraining(ini, teamId, seasonWeek);
        List<Player> players = getPlayers(ini);
        List<PlayerData> playersData = getPlayersData(ini, teamId, seasonWeek, adjustmentDays);
        Trainer trainer = getTrainer(ini, teamId, seasonWeek);
        List<StaffMember> staffMembers = getStaffMembers(ini, teamId, seasonWeek, date);

        trainingDAO.insert(training);
        players.forEach(playerDAO::insert);
        playersData.forEach(playerDataDAO::insert);
        if (trainer != null) {
            trainerDAO.insert(trainer);
        }
        staffMembers.forEach(staffMemberDAO::insert);
    }

    private Training getTraining(Ini ini, int teamId, String seasonWeek) {
        return Training.builder()
            .seasonWeek(seasonWeek)
            .teamId(teamId)
            .trainingType(parseInt(ini.get("team").get("trTypeValue")))
            .trainingLevel(parseInt(ini.get("team").get("trLevel")))
            .staminaTrainingPart(parseInt(ini.get("team").get("staminaTrainingPart")))
            .build();
    }

    private List<Player> getPlayers(Ini ini) {
        List<Player> players = new ArrayList<>();
        for (String sectionName : ini.keySet()) {
            if (sectionName.startsWith("player")) {
                Ini.Section ply = ini.get(sectionName);
                if (!Objects.equals(ply.get("gentleness"), EMPTY)) {
                    players.add(Player.builder()
                        .id(parseInt(sectionName.substring("player".length())))
                        .firstName(ply.get("firstname"))
                        .lastName(ply.get("lastname"))
                        .agreeability(parseInt(ply.get("gentleness")))
                        .aggressiveness(parseInt(ply.get("Aggressiveness")))
                        .honesty(parseInt(ply.get("honesty")))
                        .specialty(parseInt(ply.get("speciality")))
                        .countryId(parseInt(ply.get("CountryID")))
                        .build()
                    );
                }
            }
        }
        return players;
    }

    private List<PlayerData> getPlayersData(Ini ini, int teamId, String seasonWeek, int adjustmentDays) {
        List<PlayerData> playersData = new ArrayList<>();
        for (String sectionName : ini.keySet()) {
            if (sectionName.startsWith("player")) {
                Ini.Section ply = ini.get(sectionName);
                if (!Objects.equals(ply.get("gentleness"), EMPTY)) {
                    PlayerData playerData = PlayerData.builder()
                        .id(parseInt(sectionName.substring("player".length())))
                        .seasonWeek(seasonWeek)
                        .teamId(teamId)
                        .nickName(ply.get("nickname"))
                        .playerNumber(parseInt(ply.get("PlayerNumber")) == 100 ? null : parseInt(ply.get("PlayerNumber")))
                        .age(parseInt(ply.get("ald")))
                        .ageDays(parseInt(ply.get("agedays")))
                        .arrivalDate(parseDate(ply.get("arrivaldate")))
                        .ownerNotes(ply.get("OwnerNotes"))
                        .TSI(parseInt(ply.get("mkt")))
                        .playerForm(parseInt(ply.get("for")))
                        .statement(ply.get("Statement"))
                        .experience(parseInt(ply.get("rut")))
                        .loyalty(parseInt(ply.get("loy")))
                        .motherClubBonus(parseBoolean(ply.get("homegr")))
                        .leadership(parseInt(ply.get("led")))
                        .salary(parseInt(ply.get("sal")))
                        .abroad(Objects.equals(ply.get("CountryID"), ini.get("basics").get("CountryID")))
                        .leagueGoals(parseInt(ply.get("gtl")))
                        .cupGoals(parseInt(ply.get("gtc")))
                        .friendliesGoals(parseInt(ply.get("gtt")))
                        .careerGoals(parseInt(ply.get("gev")))
                        .careerHattricks(parseInt(ply.get("hat")))
                        .matchesCurrentTeam(ply.get("MatchesCurrentTeam") == null ? 0 : parseInt(ply.get("MatchesCurrentTeam")))
                        .goalsCurrentTeam(parseInt(ply.get("GoalsCurrentTeam")))
                        .assistsCurrentTeam(0)
                        .careerAssists(0)
                        .transferListed(parseBoolean(ply.get("TransferListed")))
                        .nationalTeamId(parseInt(ply.get("NationalTeamID")))
                        .caps(parseInt(ply.get("Caps")))
                        .capsU21(parseInt(ply.get("CapsU20")))
                        .cards(parseInt(ply.get("warnings")))
                        .injuryLevel(parseInt(ply.get("ska")))
                        .staminaSkill(parseInt(ply.get("uth")))
                        .keeperSkill(parseInt(ply.get("mlv")))
                        .playmakerSkill(parseInt(ply.get("spe")))
                        .scorerSkill(parseInt(ply.get("mal")))
                        .passingSkill(parseInt(ply.get("fra")))
                        .wingerSkill(parseInt(ply.get("ytt")))
                        .defenderSkill(parseInt(ply.get("bac")))
                        .setPiecesSkill(parseInt(ply.get("fas")))
                        .playerCategoryId(parseInt(ply.get("PlayerCategoryId")))
                        .build();
                    HTMS htms = HTMSUtils.calculateHTMS(playerData);
                    playerData.setHtms(htms.getHtms());
                    playerData.setHtms28(htms.getHtms28());
                    int adjustedAgeDays = playerData.getAgeDays() + adjustmentDays;
                    int adjustedAge = playerData.getAge();
                    if (adjustedAgeDays >= 112) {
                        adjustedAge++;
                        adjustedAgeDays -= 112;
                    } else if (adjustedAgeDays < 0) {
                        adjustedAge--;
                        adjustedAgeDays += 112;
                    }
                    playerData.setAge(adjustedAge);
                    playerData.setAgeDays(adjustedAgeDays);
                    if (playerData.getAge() > 16) {
                        playersData.add(playerData);
                    }
                }
            }
        }
        return playersData;
    }

    private Trainer getTrainer(Ini ini, int teamId, String seasonWeek) {
        for (String sectionName : ini.keySet()) {
            if (sectionName.startsWith("player")) {
                Ini.Section ply = ini.get(sectionName);
                if (!Objects.equals(ply.get("TrainerType"), EMPTY)) {
                    return Trainer.builder()
                        .seasonWeek(seasonWeek)
                        .teamId(teamId)
                        .id(parseInt(sectionName.substring("player".length())))
                        .name(ply.get("name").startsWith("null") ? ply.get("lastname") : ply.get("name"))
                        .type(parseInt(ply.get("TrainerType")))
                        .leadership(parseInt(ply.get("led")))
                        .skillLevel(ply.get("TrainerSkillLevel") == null ? parseInt(ply.get("TrainerSkill")) - 3 : parseInt(ply.get("TrainerSkillLevel")))
                        .status(ply.get("TrainerStatus") == null ? 1 : parseInt(ply.get("TrainerStatus")))
                        .startDate(ply.get("ContractDate") == null ? parseDate(ply.get("arrivaldate")) : parseDate(ply.get("ContractDate")))
                        .cost(ply.get("Cost") == null ? parseInt(ply.get("sal")) : parseInt(ply.get("Cost")))
                        .build();
                }
            }
        }
        return null;
    }

    private List<StaffMember> getStaffMembers(Ini ini, int teamId, String seasonWeek, ZonedDateTime date) {
        Ini.Section staff = ini.get("staff");
        List<StaffMember> staffMembers = new ArrayList<>();
        if (staff == null) {
            return staffMembers;
        }
        for (int i = 1; ; i++) {
            String prefix = "staff" + i;
            String idStr = staff.get(prefix + "StaffId");
            if (idStr == null) {
                break;
            }
            int id = parseInt(idStr);
            if (!startDate.containsKey(id)) {
                startDate.put(id, date);
            }
            staffMembers.add(StaffMember.builder()
                .seasonWeek(seasonWeek)
                .teamId(teamId)
                .id(id)
                .name(staff.get(prefix + "Name"))
                .type(parseInt(staff.get(prefix + "StaffType")))
                .level(parseInt(staff.get(prefix + "StaffLevel")))
                .hofPlayerId(0)
                .startDate(startDate.get(id))
                .cost(parseInt(staff.get(prefix + "Cost")))
                .build());
        }
        return staffMembers;
    }

    private Ini getIni(InputStreamReader reader) throws IOException {
        Ini ini = new Ini();
        ini.getConfig().setMultiSection(true);
        ini.getConfig().setEscape(false);
        ini.getConfig().setStrictOperator(false);
        ini.load(new StringReader(sanitizeIni(reader)));
        return ini;
    }

    private String sanitizeIni(InputStreamReader reader) throws java.io.IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            StringBuilder out = new StringBuilder();
            String line;
            boolean firstLine = true;
            String pendingKey = null;
            StringBuilder pendingVal = null;
            while ((line = br.readLine()) != null) {
                if (firstLine && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    line = line.substring(1);
                }
                firstLine = false;
                line = line.replace("\u0000", "").replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith(";") || trimmed.startsWith("#")) {
                    flushPair(out, pendingKey, pendingVal);
                    pendingKey = null;
                    pendingVal = null;
                    out.append(line).append('\n');
                    continue;
                }
                if (trimmed.startsWith("[") && trimmed.endsWith("]") && trimmed.length() > 2) {
                    flushPair(out, pendingKey, pendingVal);
                    pendingKey = null;
                    pendingVal = null;
                    out.append(trimmed).append('\n');
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq > 0) {
                    flushPair(out, pendingKey, pendingVal);
                    pendingKey = null;
                    pendingVal = null;
                    String key = trimmed.substring(0, eq).trim();
                    String value = trimmed.substring(eq + 1);
                    if (key.matches("[A-Za-z0-9_.\\-]+")) {
                        pendingKey = key;
                        pendingVal = new StringBuilder(value);
                    } else {
                        out.append("; discarded (bad key): ").append(line).append('\n');
                    }
                    continue;
                }
                if (pendingKey != null) {
                    pendingVal.append('\n').append(line);
                } else {
                    out.append("; discarded: ").append(line).append('\n');
                }
            }
            flushPair(out, pendingKey, pendingVal);
            return out.toString();
        }
    }

    private void flushPair(StringBuilder out, String key, StringBuilder val) {
        if (key == null) {
            return;
        }
        String v = (val == null) ? "" : val.toString();
        String[] parts = v.split("\\R", -1); // conservar posibles líneas vacías intermedias
        if (parts.length == 0) {
            out.append(key).append("=").append('\n');
            return;
        }
        out.append(key).append("=").append(parts[0]);
        if (parts.length == 1) {
            out.append('\n');
            return;
        }
        out.append(" \\").append('\n');
        for (int i = 1; i < parts.length; i++) {
            String seg = parts[i];
            boolean isLast = (i == parts.length - 1);
            out.append(seg);
            if (!isLast) {
                out.append(" \\");
            }
            out.append('\n');
        }
    }

    private ZonedDateTime parseDate(String date) {
        return LocalDateTime.parse(date, ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.systemDefault());
    }

}
