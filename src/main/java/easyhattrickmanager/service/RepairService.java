package easyhattrickmanager.service;

import static easyhattrickmanager.utils.SeasonWeekUtils.convertFromSeasonWeek;
import static easyhattrickmanager.utils.SeasonWeekUtils.convertToSeasonWeek;
import static easyhattrickmanager.utils.SeasonWeekUtils.next;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.PlayerSubSkillDAO;
import easyhattrickmanager.repository.PlayerTrainingDAO;
import easyhattrickmanager.repository.StaffMemberDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.TrainerDAO;
import easyhattrickmanager.repository.TrainingDAO;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.PlayerSubSkill;
import easyhattrickmanager.repository.model.PlayerTraining;
import easyhattrickmanager.repository.model.StaffMember;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.Trainer;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.service.model.PlayerDataGap;
import easyhattrickmanager.service.model.StaffMemberGap;
import easyhattrickmanager.service.model.TrainerGap;
import easyhattrickmanager.service.model.TrainingGap;
import easyhattrickmanager.utils.HTMSUtils;
import easyhattrickmanager.utils.SeasonWeekUtils;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final TeamDAO teamDAO;
    private final TrainingDAO trainingDAO;
    private final PlayerDataDAO playerDataDAO;
    private final LeagueDAO leagueDAO;
    private final TrainerDAO trainerDAO;
    private final StaffMemberDAO staffMemberDAO;
    private final PlayerTrainingDAO playerTrainingDAO;
    private final PlayerSubSkillDAO playerSubSkillDAO;
    private final CalculateTrainingPercentageService calculateTrainingPercentageService;
    private final CalculateSubSkillTrainingService calculateSubSkillTrainingService;

    public void fillInGaps() {
        teamDAO.getActiveTeams().forEach(team -> {
            detectTrainingGaps(team).forEach(this::fillInTrainingGap);
            detectPlayerDataGaps(team).forEach(this::fillInPlayerDataGap);
            extendStaff(team.getId());
            detectTrainerGaps(team).forEach(this::fillInTrainerGap);
            detectStaffMemberGaps(team).forEach(this::fillInStaffMemberGap);
        });
    }

    private List<TrainingGap> detectTrainingGaps(Team team) {
        List<Training> trainings = trainingDAO.get(team.getId());
        List<TrainingGap> gaps = new ArrayList<>();
        String previous = null;
        Training previousTraining = null;
        for (Training training : trainings) {
            String current = training.getSeasonWeek();
            if (previous != null) {
                String nextExpected = next(previous);
                if (!current.equals(nextExpected)) {
                    int missingWeeks = calculateMissingWeeks(previous, current);
                    gaps.add(TrainingGap.builder()
                        .teamId(team.getId())
                        .seasonWeekStart(previous)
                        .seasonWeekEnd(current)
                        .missingWeeks(missingWeeks)
                        .trainingStart(previousTraining)
                        .trainingEnd(training)
                        .build());
                }
            }
            previous = current;
            previousTraining = training;
        }
        return gaps;
    }

    private int calculateMissingWeeks(String start, String end) {
        int startSeason = Integer.parseInt(start.substring(1, 4));
        int startWeek = Integer.parseInt(start.substring(5));
        int endSeason = Integer.parseInt(end.substring(1, 4));
        int endWeek = Integer.parseInt(end.substring(5));
        int seasonGap = (endSeason - startSeason) * 16;
        return (endWeek + seasonGap) - startWeek - 1;
    }

    private void fillInTrainingGap(TrainingGap gap) {
        Training training = gap.getTrainingStart();
        int trainingType = getTrainingTypeFromPlayers(gap.getTeamId(), gap).orElse(training.getTrainingType());
        String seasonWeek = gap.getSeasonWeekStart();
        for (int i = 0; i < gap.getMissingWeeks(); i++) {
            seasonWeek = next(seasonWeek);
            trainingDAO.insert(Training.builder()
                .seasonWeek(seasonWeek)
                .teamId(gap.getTeamId())
                .trainingType(trainingType)
                .trainingLevel(training.getTrainingLevel())
                .staminaTrainingPart(training.getStaminaTrainingPart())
                .build());
        }
    }

    private Optional<Integer> getTrainingTypeFromPlayers(int teamId, TrainingGap gap) {
        List<PlayerData> playerData = playerDataDAO.get(teamId);
        List<PlayerData> startPlayerData = playerData.stream().filter(pd -> pd.getSeasonWeek().equals(gap.getSeasonWeekStart())).toList();
        List<PlayerData> endPlayerData = playerData.stream().filter(pd -> pd.getSeasonWeek().equals(gap.getSeasonWeekEnd())).toList();
        Map<Integer, PlayerData> startDataMap = startPlayerData.stream().collect(Collectors.toMap(PlayerData::getId, pd -> pd));
        Map<Integer, PlayerData> endDataMap = endPlayerData.stream().collect(Collectors.toMap(PlayerData::getId, pd -> pd));
        int keeperSkillIncr = 0;
        int playmakerSkillIncr = 0;
        int scorerSkillIncr = 0;
        int passingSkillIncr = 0;
        int wingerSkillIncr = 0;
        int defenderSkillIncr = 0;
        int setPiecesSkillIncr = 0;
        for (Integer playerId : startDataMap.keySet()) {
            if (endDataMap.containsKey(playerId)) {
                PlayerData startData = startDataMap.get(playerId);
                PlayerData endData = endDataMap.get(playerId);
                keeperSkillIncr += Math.max(0, endData.getKeeperSkill() - startData.getKeeperSkill());
                playmakerSkillIncr += Math.max(0, endData.getPlaymakerSkill() - startData.getPlaymakerSkill());
                scorerSkillIncr += Math.max(0, endData.getScorerSkill() - startData.getScorerSkill());
                passingSkillIncr += Math.max(0, endData.getPassingSkill() - startData.getPassingSkill());
                wingerSkillIncr += Math.max(0, endData.getWingerSkill() - startData.getWingerSkill());
                defenderSkillIncr += Math.max(0, endData.getDefenderSkill() - startData.getDefenderSkill());
                setPiecesSkillIncr += Math.max(0, endData.getSetPiecesSkill() - startData.getSetPiecesSkill());
            }
        }
        Map<Integer, Integer> trainingImpactMap = Map.of(
            2, setPiecesSkillIncr,   // 2 = Set Pieces Training
            3, defenderSkillIncr,       // 3 = Defending Training
            4, scorerSkillIncr,         // 4 = Scoring Training
            5, wingerSkillIncr,         // 5 = Winger Training
            7, passingSkillIncr,        // 7 = Passing Training
            8, playmakerSkillIncr,      // 8 = Playmaking Training
            9, keeperSkillIncr          // 9 = Keeper Training
        );
        return trainingImpactMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .filter(entry -> entry.getValue() > 0)
            .map(Map.Entry::getKey);
    }

    private List<PlayerDataGap> detectPlayerDataGaps(Team team) {
        List<PlayerData> playersData = playerDataDAO.get(team.getId());
        Map<Integer, List<PlayerData>> playerDataByPlayerId = playersData.stream().collect(Collectors.groupingBy(PlayerData::getId));
        List<PlayerDataGap> gaps = new ArrayList<>();
        for (Map.Entry<Integer, List<PlayerData>> entry : playerDataByPlayerId.entrySet()) {
            int playerId = entry.getKey();
            List<PlayerData> playerEntries = entry.getValue();
            playerEntries.sort(comparing(PlayerData::getSeasonWeek));
            String previous = null;
            PlayerData previousPlayerData = null;
            for (PlayerData data : playerEntries) {
                String current = data.getSeasonWeek();
                if (previous != null) {
                    String nextExpected = next(previous);
                    if (!current.equals(nextExpected)) {
                        int missingWeeks = calculateMissingWeeks(previous, current);
                        if (data.getArrivalDate() != null && !current.equals(convertToSeasonWeek(data.getArrivalDate()))) {
                            gaps.add(PlayerDataGap.builder()
                                .teamId(team.getId())
                                .playerId(playerId)
                                .seasonWeekStart(previous)
                                .seasonWeekEnd(current)
                                .missingWeeks(missingWeeks)
                                .playerDataStart(previousPlayerData)
                                .playerDataEnd(data)
                                .build());
                        }
                    }
                }
                previous = current;
                previousPlayerData = data;
            }
        }
        return gaps;
    }

    private void fillInPlayerDataGap(PlayerDataGap gap) {
        PlayerData ini = gap.getPlayerDataStart();
        PlayerData end = gap.getPlayerDataEnd();
        String seasonWeek = gap.getSeasonWeekStart();
        int newAgeDays = ini.getAgeDays();
        int newAge = ini.getAge();
        int salary = ini.getSalary();
        for (int i = 0; i < gap.getMissingWeeks(); i++) {
            seasonWeek = next(seasonWeek);
            newAgeDays += 7;
            if (newAgeDays >= 112) {
                newAge++;
                newAgeDays -= 112;
                salary = fillValue(ini.getSalary() / 10, end.getSalary() / 10, i + 1, gap.getMissingWeeks()) * 10;
            }
            PlayerData playerData = PlayerData.builder()
                .id(ini.getId())
                .seasonWeek(seasonWeek)
                .teamId(gap.getTeamId())
                .nickName(ini.getNickName())
                .playerNumber(ini.getPlayerNumber())
                .age(newAge)
                .ageDays(newAgeDays)
                .arrivalDate(ini.getArrivalDate())
                .ownerNotes(ini.getOwnerNotes())
                .TSI(fillValue(ini.getTSI() / 10, end.getTSI() / 10, i + 1, gap.getMissingWeeks()) * 10)
                .playerForm(fillValue(ini.getPlayerForm(), end.getPlayerForm(), i + 1, gap.getMissingWeeks()))
                .statement(ini.getStatement())
                .experience(fillValue(ini.getExperience(), end.getExperience(), i + 1, gap.getMissingWeeks()))
                .loyalty(fillValue(ini.getLoyalty(), end.getLoyalty(), i + 1, gap.getMissingWeeks()))
                .motherClubBonus(ini.isMotherClubBonus())
                .leadership(fillValue(ini.getLeadership(), end.getLeadership(), i + 1, gap.getMissingWeeks()))
                .salary(salary)
                .abroad(ini.isAbroad())
                .leagueGoals(fillValue(ini.getLeagueGoals(), end.getLeagueGoals(), i + 1, gap.getMissingWeeks()))
                .cupGoals(fillValue(ini.getCupGoals(), end.getCupGoals(), i + 1, gap.getMissingWeeks()))
                .friendliesGoals(fillValue(ini.getFriendliesGoals(), end.getFriendliesGoals(), i + 1, gap.getMissingWeeks()))
                .careerGoals(fillValue(ini.getCareerGoals(), end.getCareerGoals(), i + 1, gap.getMissingWeeks()))
                .careerHattricks(fillValue(ini.getCareerHattricks(), end.getCareerHattricks(), i + 1, gap.getMissingWeeks()))
                .matchesCurrentTeam(fillValue(ini.getMatchesCurrentTeam(), end.getMatchesCurrentTeam(), i + 1, gap.getMissingWeeks()))
                .goalsCurrentTeam(fillValue(ini.getGoalsCurrentTeam(), end.getGoalsCurrentTeam(), i + 1, gap.getMissingWeeks()))
                .assistsCurrentTeam(fillValue(ini.getAssistsCurrentTeam(), end.getAssistsCurrentTeam(), i + 1, gap.getMissingWeeks()))
                .careerAssists(fillValue(ini.getCareerAssists(), end.getCareerAssists(), i + 1, gap.getMissingWeeks()))
                .transferListed(false)
                .nationalTeamId(ini.getNationalTeamId())
                .caps(fillValue(ini.getCaps(), end.getCaps(), i + 1, gap.getMissingWeeks()))
                .capsU21(fillValue(ini.getCapsU21(), end.getCapsU21(), i + 1, gap.getMissingWeeks()))
                .cards(0)
                .injuryLevel(-1)
                .staminaSkill(fillValue(ini.getStaminaSkill(), end.getStaminaSkill(), i + 1, gap.getMissingWeeks()))
                .keeperSkill(fillValue(ini.getKeeperSkill(), end.getKeeperSkill(), i + 1, gap.getMissingWeeks()))
                .playmakerSkill(fillValue(ini.getPlaymakerSkill(), end.getPlaymakerSkill(), i + 1, gap.getMissingWeeks()))
                .scorerSkill(fillValue(ini.getScorerSkill(), end.getScorerSkill(), i + 1, gap.getMissingWeeks()))
                .passingSkill(fillValue(ini.getPassingSkill(), end.getPassingSkill(), i + 1, gap.getMissingWeeks()))
                .wingerSkill(fillValue(ini.getWingerSkill(), end.getWingerSkill(), i + 1, gap.getMissingWeeks()))
                .defenderSkill(fillValue(ini.getDefenderSkill(), end.getDefenderSkill(), i + 1, gap.getMissingWeeks()))
                .setPiecesSkill(fillValue(ini.getSetPiecesSkill(), end.getSetPiecesSkill(), i + 1, gap.getMissingWeeks()))
                .playerCategoryId(ini.getPlayerCategoryId())
                .build();
            HTMS htms = HTMSUtils.calculateHTMS(playerData);
            playerData.setHtms(htms.getHtms());
            playerData.setHtms28(htms.getHtms28());
            playerDataDAO.insert(playerData);
        }
    }

    private int fillValue(int iniValue, int endValue, int missingWeek, int missingWeeks) {
        if (iniValue == endValue || iniValue + 1 == endValue) {
            return iniValue;
        }
        return iniValue + (int) Math.floor((endValue - iniValue) * (double) missingWeek / (missingWeeks + 1));
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

    private int getAdjustmentDays(int teamId) {
        Team team = teamDAO.get(teamId);
        League league = leagueDAO.get(team.getLeagueId()).orElseThrow();
        return SeasonWeekUtils.getAdjustmentDays(league.getTrainingDate(), ZonedDateTime.now());
    }

    private int getLeadership(List<PlayerData> playerData, Trainer trainer, String seasonWeek) {
        Optional<PlayerData> found = playerData.stream()
            .filter(pd -> (Objects.equals(pd.getSeasonWeek(), seasonWeek)) && (pd.getId() == trainer.getId()))
            .findFirst();
        return found.map(PlayerData::getLeadership).orElseGet(trainer::getLeadership);
    }

    private List<TrainerGap> detectTrainerGaps(Team team) {
        List<Trainer> trainers = trainerDAO.get(team.getId());
        List<TrainerGap> gaps = new ArrayList<>();
        String previous = null;
        Trainer previousTrainer = null;
        for (Trainer trainer : trainers) {
            String current = trainer.getSeasonWeek();
            if (previous != null) {
                String nextExpected = next(previous);
                if (!current.equals(nextExpected)) {
                    int missingWeeks = calculateMissingWeeks(previous, current);
                    gaps.add(TrainerGap.builder()
                        .teamId(team.getId())
                        .seasonWeekStart(previous)
                        .seasonWeekEnd(current)
                        .missingWeeks(missingWeeks)
                        .trainerStart(previousTrainer)
                        .trainerEnd(trainer)
                        .build());
                }
            }
            previous = current;
            previousTrainer = trainer;
        }
        return gaps;
    }

    private void fillInTrainerGap(TrainerGap gap) {
        Trainer ini = gap.getTrainerStart();
        Trainer end = gap.getTrainerEnd();
        String seasonWeek = gap.getSeasonWeekStart();
        for (int i = 0; i < gap.getMissingWeeks(); i++) {
            seasonWeek = next(seasonWeek);
            trainerDAO.insert(Trainer.builder()
                .seasonWeek(seasonWeek)
                .teamId(gap.getTeamId())
                .id(ini.getId())
                .name(ini.getName())
                .type(ini.getType())
                .leadership(fillValue(ini.getLeadership(), end.getLeadership(), i + 1, gap.getMissingWeeks()))
                .skillLevel(ini.getSkillLevel())
                .status(ini.getStatus())
                .startDate(ini.getStartDate())
                .cost(ini.getCost())
                .build());
        }
    }

    private List<StaffMemberGap> detectStaffMemberGaps(Team team) {
        List<StaffMember> staffMembers = staffMemberDAO.get(team.getId());
        Map<Integer, List<StaffMember>> staffMemberById = staffMembers.stream().collect(Collectors.groupingBy(StaffMember::getId));
        List<StaffMemberGap> gaps = new ArrayList<>();
        for (Map.Entry<Integer, List<StaffMember>> entry : staffMemberById.entrySet()) {
            int playerId = entry.getKey();
            List<StaffMember> staffMemberEntries = entry.getValue();
            staffMemberEntries.sort(comparing(StaffMember::getSeasonWeek));
            String previous = null;
            StaffMember previousStaffMember = null;
            for (StaffMember data : staffMemberEntries) {
                String current = data.getSeasonWeek();
                if (previous != null) {
                    String nextExpected = next(previous);
                    if (!current.equals(nextExpected)) {
                        int missingWeeks = calculateMissingWeeks(previous, current);
                        gaps.add(StaffMemberGap.builder()
                            .teamId(team.getId())
                            .playerId(playerId)
                            .seasonWeekStart(previous)
                            .seasonWeekEnd(current)
                            .missingWeeks(missingWeeks)
                            .staffMemberStart(previousStaffMember)
                            .staffMemberEnd(data)
                            .build());
                    }
                }
                previous = current;
                previousStaffMember = data;
            }
        }
        return gaps;
    }

    private void fillInStaffMemberGap(StaffMemberGap gap) {
        StaffMember ini = gap.getStaffMemberStart();
        String seasonWeek = gap.getSeasonWeekStart();
        for (int i = 0; i < gap.getMissingWeeks(); i++) {
            seasonWeek = next(seasonWeek);
            staffMemberDAO.insert(StaffMember.builder()
                .seasonWeek(seasonWeek)
                .teamId(gap.getTeamId())
                .id(ini.getId())
                .name(ini.getName())
                .type(ini.getType())
                .level(ini.getLevel())
                .hofPlayerId(0)
                .startDate(ini.getStartDate())
                .cost(ini.getCost())
                .build());
        }
    }

    public void getPlayerTraining() {
        teamDAO.getActiveTeams().forEach(team -> {
            //Team team = Team.builder().id(1333746).build();
            List<String> playerTrainingWeeks = playerTrainingDAO.get(team.getId()).stream().map(PlayerTraining::getSeasonWeek).distinct().toList();
            List<String> dataWeeks = playerDataDAO.get(team.getId()).stream().map(PlayerData::getSeasonWeek).distinct().toList();

            Map<String, Training> trainings = trainingDAO.get(team.getId()).stream().collect(Collectors.toMap(Training::getSeasonWeek, training -> training));
            Map<String, Trainer> trainers = trainerDAO.get(team.getId()).stream().collect(Collectors.toMap(Trainer::getSeasonWeek, trainer -> trainer));
            Map<String, List<StaffMember>> staffMembersByWeek = staffMemberDAO.get(team.getId()).stream().collect(groupingBy(StaffMember::getSeasonWeek));

            dataWeeks.stream().filter(seasonWeek -> !playerTrainingWeeks.contains(seasonWeek)).forEach(seasonWeek -> {
                List<PlayerTraining> playerTrainings = calculateTrainingPercentageService.calculateTrainingPercentage(seasonWeek, team.getId(), trainings.get(seasonWeek), trainers.get(seasonWeek).getSkillLevel(), getAssistantsLevel(staffMembersByWeek.get(seasonWeek)));
                playerTrainings.forEach(playerTrainingDAO::insert);
            });
        });
    }

    public void getPlayerSubSkill() {
        teamDAO.getActiveTeams().forEach(team -> {
            //Team team = Team.builder().id(1333746).build();
            List<String> playerSubSkillWeeks = playerSubSkillDAO.get(team.getId()).stream().map(PlayerSubSkill::getSeasonWeek).distinct().toList();
            List<String> dataWeeks = playerDataDAO.get(team.getId()).stream().map(PlayerData::getSeasonWeek).distinct().toList();

            Map<String, Training> trainings = trainingDAO.get(team.getId()).stream().collect(Collectors.toMap(Training::getSeasonWeek, training -> training));
            Map<String, Trainer> trainers = trainerDAO.get(team.getId()).stream().collect(Collectors.toMap(Trainer::getSeasonWeek, trainer -> trainer));
            Map<String, List<StaffMember>> staffMembersByWeek = staffMemberDAO.get(team.getId()).stream().collect(groupingBy(StaffMember::getSeasonWeek));
            Map<String, List<PlayerTraining>> playerTrainingsByWeek = playerTrainingDAO.get(team.getId()).stream().collect(groupingBy(PlayerTraining::getSeasonWeek));
            Map<String, List<PlayerData>> playerDataByWeek = playerDataDAO.get(team.getId()).stream().collect(groupingBy(PlayerData::getSeasonWeek));

            dataWeeks.stream()
                .sorted()
                .filter(seasonWeek -> !playerSubSkillWeeks.contains(seasonWeek))
                .forEach(seasonWeek -> {
                    List<PlayerSubSkill> playerSubSkills = calculateSubSkillTrainingService.calculateSubSkillTraining(
                        seasonWeek,
                        team.getId(),
                        trainings.get(seasonWeek),
                        trainers.get(seasonWeek).getSkillLevel(),
                        getAssistantsLevel(staffMembersByWeek.get(seasonWeek)),
                        playerDataByWeek.getOrDefault(seasonWeek, List.of()),
                        playerTrainingsByWeek.getOrDefault(seasonWeek, List.of())
                    );
                    playerSubSkills.forEach(playerSubSkillDAO::insert);
                });
        });
    }

    private int getAssistantsLevel(List<StaffMember> staffMembers) {
        return isEmpty(staffMembers) ? 0 : staffMembers.stream()
            .filter(stf -> stf.getType() == 1)
            .map(StaffMember::getLevel)
            .reduce(0, Integer::sum);
    }
}
