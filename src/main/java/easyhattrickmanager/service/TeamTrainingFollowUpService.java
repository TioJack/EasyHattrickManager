package easyhattrickmanager.service;

import easyhattrickmanager.controller.model.DataResponse;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.StaffInfo;
import easyhattrickmanager.service.model.dataresponse.StaffMemberInfo;
import easyhattrickmanager.service.model.dataresponse.TeamExtendedInfo;
import easyhattrickmanager.service.model.dataresponse.TrainerInfo;
import easyhattrickmanager.service.model.dataresponse.TrainingInfo;
import easyhattrickmanager.service.model.dataresponse.WeeklyInfo;
import easyhattrickmanager.service.model.teamtraining.StagePlayerParticipation;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingPlayer;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingRequest;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingResponse;
import easyhattrickmanager.service.model.teamtraining.TrainingStage;
import easyhattrickmanager.service.model.teamtraining.WeekInfo;
import easyhattrickmanager.service.model.playertraining.Training;
import easyhattrickmanager.service.model.teamtrainingfollowup.TeamTrainingFollowUpRequest;
import easyhattrickmanager.service.model.teamtrainingfollowup.TeamTrainingFollowUpResponse;
import easyhattrickmanager.service.model.teamtrainingfollowup.WeekTraining;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamTrainingFollowUpService {

    private static final Map<Integer, Double> PER_TRAINER = Map.ofEntries(
        Map.entry(5, 1.0375 / 1.0375),
        Map.entry(4, 1.0000 / 1.0375),
        Map.entry(3, 0.9200 / 1.0375),
        Map.entry(2, 0.8324 / 1.0375),
        Map.entry(1, 0.7343 / 1.0375)
    );

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

    private final TeamTrainingService teamTrainingService;

    public TeamTrainingFollowUpResponse getTeamTrainingFollowUp(final TeamTrainingFollowUpRequest teamTrainingFollowUpRequest) {
        final TeamTrainingRequest request = teamTrainingFollowUpRequest.getTeamTrainingRequest();
        final TeamTrainingResponse response = teamTrainingFollowUpRequest.getTeamTrainingResponse();
        final WeekInfo iniWeek = request != null ? request.getIniWeek() : null;
        final TeamExtendedInfo team = this.getTeam(teamTrainingFollowUpRequest.getDataResponse(), teamTrainingFollowUpRequest.getTeamId());
        final List<WeeklyInfo> weeklyData = team != null && team.getWeeklyData() != null
            ? team.getWeeklyData().stream()
            .sorted(Comparator.comparingInt(WeeklyInfo::getSeason).thenComparingInt(WeeklyInfo::getWeek))
            .toList()
            : List.of();
        final int totalWeeks = this.getTotalWeeks(request, response);
        final int startIndex = this.getStartIndex(weeklyData, iniWeek);
        final int completedWeeks = this.getCompletedWeeks(weeklyData, startIndex, totalWeeks);

        final Map<Integer, WeekInfo> weekInfoPlanned = new LinkedHashMap<>();
        final Map<Integer, WeekInfo> weekInfo = new LinkedHashMap<>();
        final Map<Integer, WeekTraining> weekTrainingPlanned = new LinkedHashMap<>();
        final Map<Integer, WeekTraining> weekTraining = new LinkedHashMap<>();
        final Map<Integer, List<PlayerInfo>> weekPlayersPlanned = this.copyWeekPlayers(response != null ? response.getWeekPlayers() : null);
        final Map<Integer, List<PlayerInfo>> weekPlayers = new LinkedHashMap<>();
        final Map<Integer, Map<Integer, Integer>> weekParticipationPlanned = this.buildWeekParticipationPlanned(request, totalWeeks);
        final Map<Integer, Map<Integer, Double>> weekParticipationExpected = this.buildWeekParticipationExpected(request, totalWeeks);

        for (int offsetWeek = 1; offsetWeek <= totalWeeks; offsetWeek++) {
            weekInfoPlanned.put(offsetWeek, this.calculateWeekInfo(iniWeek, offsetWeek, weeklyData));
            weekTrainingPlanned.put(offsetWeek, this.getPlannedWeekTraining(request, offsetWeek));

            if (offsetWeek <= completedWeeks && startIndex >= 0 && startIndex + offsetWeek < weeklyData.size()) {
                final WeeklyInfo actualWeeklyInfo = weeklyData.get(startIndex + offsetWeek);
                weekInfo.put(offsetWeek, this.toWeekInfo(actualWeeklyInfo));
                weekTraining.put(offsetWeek, WeekTraining.builder()
                    .training(actualWeeklyInfo.getTraining())
                    .staff(actualWeeklyInfo.getStaff())
                    .build());
                weekPlayers.put(offsetWeek, List.copyOf(actualWeeklyInfo.getPlayers() != null ? actualWeeklyInfo.getPlayers() : List.of()));
            }
        }

        return TeamTrainingFollowUpResponse.builder()
            .iniWeek(iniWeek)
            .actualWeek(completedWeeks > 0 ? weekInfo.get(completedWeeks) : iniWeek)
            .endWeek(response != null ? response.getEndWeek() : this.calculateWeekInfo(iniWeek, totalWeeks, weeklyData))
            .initialPlayers(this.getInitialPlayers(request, weeklyData, startIndex))
            .weekInfoPlanned(weekInfoPlanned)
            .weekInfo(weekInfo)
            .weekTrainingPlanned(weekTrainingPlanned)
            .weekTraining(weekTraining)
            .weekPlayersPlanned(weekPlayersPlanned)
            .weekPlayers(weekPlayers)
            .weekPlayersPlannedFromActual(this.buildWeekPlayersPlannedFromActual(
                teamTrainingFollowUpRequest,
                weeklyData,
                startIndex,
                completedWeeks,
                totalWeeks,
                weekPlayersPlanned
            ))
            .weekParticipationPlanned(weekParticipationPlanned)
            .weekParticipationExpected(weekParticipationExpected)
            .weekPlayerIndicators(this.buildWeekPlayerIndicators(weekTrainingPlanned, weekTraining, weekPlayers, weekParticipationExpected))
            .build();
    }

    private TeamExtendedInfo getTeam(final DataResponse dataResponse, final Integer teamId) {
        if (dataResponse == null || dataResponse.getTeams() == null || teamId == null) {
            return null;
        }
        return dataResponse.getTeams().stream()
            .filter(team -> team.getTeam() != null && Objects.equals(team.getTeam().getId(), teamId))
            .findFirst()
            .orElse(null);
    }

    private int getTotalWeeks(final TeamTrainingRequest request, final TeamTrainingResponse response) {
        if (response != null && response.getWeekPlayers() != null && !response.getWeekPlayers().isEmpty()) {
            return response.getWeekPlayers().keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        }
        if (request == null || request.getStages() == null) {
            return 0;
        }
        return request.getStages().stream().mapToInt(TrainingStage::getDuration).sum();
    }

    private int getStartIndex(final List<WeeklyInfo> weeklyData, final WeekInfo iniWeek) {
        if (iniWeek == null || weeklyData.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < weeklyData.size(); i++) {
            final WeeklyInfo week = weeklyData.get(i);
            if (week.getSeason() == iniWeek.getSeason() && week.getWeek() == iniWeek.getWeek()) {
                return i;
            }
        }
        return -1;
    }

    private int getCompletedWeeks(final List<WeeklyInfo> weeklyData, final int startIndex, final int totalWeeks) {
        if (startIndex < 0 || totalWeeks <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(totalWeeks, weeklyData.size() - startIndex - 1));
    }

    private List<PlayerInfo> getInitialPlayers(final TeamTrainingRequest request, final List<WeeklyInfo> weeklyData, final int startIndex) {
        if (request != null && request.getPlayers() != null && !request.getPlayers().isEmpty()) {
            return request.getPlayers().stream()
                .map(TeamTrainingPlayer::getPlayer)
                .filter(Objects::nonNull)
                .toList();
        }
        if (startIndex >= 0 && startIndex < weeklyData.size()) {
            return List.copyOf(weeklyData.get(startIndex).getPlayers() != null ? weeklyData.get(startIndex).getPlayers() : List.of());
        }
        return List.of();
    }

    private WeekTraining getPlannedWeekTraining(final TeamTrainingRequest request, final int offsetWeek) {
        final TrainingStage stage = this.getStageForWeek(request != null ? request.getStages() : null, offsetWeek);
        if (stage == null) {
            return null;
        }
        return WeekTraining.builder()
            .training(TrainingInfo.builder()
                .trainingType(this.mapTrainingType(stage.getTraining()))
                .trainingLevel(stage.getIntensity())
                .staminaTrainingPart(stage.getStamina())
                .build())
            .staff(StaffInfo.builder()
                .trainer(TrainerInfo.builder()
                    .skillLevel(stage.getCoach())
                    .build())
                .staffMembers(stage.getAssistants() > 0
                    ? List.of(StaffMemberInfo.builder().type(1).level(stage.getAssistants()).build())
                    : List.of())
                .build())
            .build();
    }

    private TrainingStage getStageForWeek(final List<TrainingStage> stages, final int week) {
        if (stages == null || stages.isEmpty() || week <= 0) {
            return null;
        }
        int accumulated = 0;
        for (TrainingStage stage : stages) {
            accumulated += stage.getDuration();
            if (week <= accumulated) {
                return stage;
            }
        }
        return stages.get(stages.size() - 1);
    }

    private int mapTrainingType(final Training training) {
        if (training == null) {
            return 0;
        }
        return switch (training) {
            case SET_PIECES -> 2;
            case DEFENDING -> 3;
            case SCORING -> 4;
            case WINGER -> 5;
            case SCORING_SET_PIECES -> 6;
            case PASSING -> 7;
            case PLAY_MAKING -> 8;
            case GOALKEEPING -> 9;
            case PASSING_EXTENSIVE -> 10;
            case DEFENDING_EXTENSIVE -> 11;
            case WINGER_EXTENSIVE -> 12;
            default -> 0;
        };
    }

    private WeekInfo calculateWeekInfo(final WeekInfo iniWeek, final int offsetWeek, final List<WeeklyInfo> weeklyData) {
        if (iniWeek == null) {
            return null;
        }
        int season = iniWeek.getSeason();
        int week = iniWeek.getWeek();
        for (int i = 0; i < offsetWeek; i++) {
            week++;
            if (week > 16) {
                week = 1;
                season++;
            }
        }
        final int targetSeason = season;
        final int targetWeek = week;
        final ZonedDateTime explicitDate = weeklyData.stream()
            .filter(data -> data.getSeason() == targetSeason && data.getWeek() == targetWeek)
            .map(WeeklyInfo::getDate)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        final ZonedDateTime date = explicitDate != null
            ? explicitDate
            : (iniWeek.getDate() != null ? iniWeek.getDate().plusWeeks(offsetWeek) : null);
        return WeekInfo.builder()
            .season(season)
            .week(week)
            .date(date)
            .build();
    }

    private WeekInfo toWeekInfo(final WeeklyInfo weeklyInfo) {
        if (weeklyInfo == null) {
            return null;
        }
        return WeekInfo.builder()
            .season(weeklyInfo.getSeason())
            .week(weeklyInfo.getWeek())
            .date(weeklyInfo.getDate())
            .build();
    }

    private Map<Integer, List<PlayerInfo>> copyWeekPlayers(final Map<Integer, List<PlayerInfo>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        final Map<Integer, List<PlayerInfo>> result = new LinkedHashMap<>();
        source.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> result.put(entry.getKey(), List.copyOf(entry.getValue() != null ? entry.getValue() : List.of())));
        return result;
    }

    private Map<Integer, List<PlayerInfo>> buildWeekPlayersPlannedFromActual(
        final TeamTrainingFollowUpRequest followUpRequest,
        final List<WeeklyInfo> weeklyData,
        final int startIndex,
        final int completedWeeks,
        final int totalWeeks,
        final Map<Integer, List<PlayerInfo>> plannedWeekPlayers
    ) {
        if (completedWeeks <= 0) {
            return plannedWeekPlayers;
        }
        if (completedWeeks >= totalWeeks || startIndex < 0 || startIndex + completedWeeks >= weeklyData.size()) {
            return Map.of();
        }
        final TeamTrainingRequest requestFromActual = this.buildRequestFromActual(followUpRequest.getTeamTrainingRequest(), weeklyData, startIndex, completedWeeks);
        if (requestFromActual == null) {
            return Map.of();
        }
        final TeamTrainingResponse responseFromActual = this.teamTrainingService.getTeamTraining(requestFromActual);
        final Map<Integer, List<PlayerInfo>> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<PlayerInfo>> entry : this.copyWeekPlayers(responseFromActual.getWeekPlayers()).entrySet()) {
            result.put(completedWeeks + entry.getKey(), entry.getValue());
        }
        return result;
    }

    private Map<Integer, Map<Integer, Integer>> buildWeekParticipationPlanned(final TeamTrainingRequest request, final int totalWeeks) {
        if (request == null || request.getStages() == null || request.getParticipations() == null) {
            return Map.of();
        }
        final Map<Integer, Map<Integer, Integer>> byWeek = new LinkedHashMap<>();
        for (int week = 1; week <= totalWeeks; week++) {
            final TrainingStage stage = this.getStageForWeek(request.getStages(), week);
            if (stage == null) {
                continue;
            }
            final int stageId = stage.getId();
            final Map<Integer, Integer> byPlayer = new LinkedHashMap<>();
            for (StagePlayerParticipation participation : request.getParticipations()) {
                if (participation.getStageId() == stageId) {
                    byPlayer.put(participation.getPlayerId(), participation.getParticipation());
                }
            }
            byWeek.put(week, byPlayer);
        }
        return byWeek;
    }

    private Map<Integer, Map<Integer, Double>> buildWeekParticipationExpected(final TeamTrainingRequest request, final int totalWeeks) {
        if (request == null || request.getStages() == null || request.getParticipations() == null) {
            return Map.of();
        }
        final Map<Integer, Map<Integer, Double>> byWeek = new LinkedHashMap<>();
        for (int week = 1; week <= totalWeeks; week++) {
            final TrainingStage stage = this.getStageForWeek(request.getStages(), week);
            if (stage == null) {
                continue;
            }
            final double stageFactor = this.getStageTrainingFactor(stage);
            final int stageId = stage.getId();
            final Map<Integer, Double> byPlayer = new LinkedHashMap<>();
            for (StagePlayerParticipation participation : request.getParticipations()) {
                if (participation.getStageId() == stageId) {
                    byPlayer.put(participation.getPlayerId(), participation.getParticipation() * stageFactor);
                }
            }
            byWeek.put(week, byPlayer);
        }
        return byWeek;
    }

    private Map<Integer, Map<Integer, String>> buildWeekPlayerIndicators(
        final Map<Integer, WeekTraining> weekTrainingPlanned,
        final Map<Integer, WeekTraining> weekTraining,
        final Map<Integer, List<PlayerInfo>> weekPlayers,
        final Map<Integer, Map<Integer, Double>> weekParticipationExpected
    ) {
        final Map<Integer, Map<Integer, String>> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<PlayerInfo>> entry : weekPlayers.entrySet()) {
            final Integer week = entry.getKey();
            final WeekTraining plannedWeekTraining = weekTrainingPlanned.get(week);
            final WeekTraining actualWeekTraining = weekTraining.get(week);
            final Integer plannedTypeId = plannedWeekTraining != null && plannedWeekTraining.getTraining() != null
                ? plannedWeekTraining.getTraining().getTrainingType()
                : null;
            final Integer actualTypeId = actualWeekTraining != null && actualWeekTraining.getTraining() != null
                ? actualWeekTraining.getTraining().getTrainingType()
                : null;
            final boolean trainingTypeMismatch = actualTypeId != null && !Objects.equals(plannedTypeId, actualTypeId);
            final Map<Integer, Double> plannedParticipationByPlayer = weekParticipationExpected.getOrDefault(week, Map.of());
            final Map<Integer, String> indicatorsByPlayer = new LinkedHashMap<>();
            for (PlayerInfo player : entry.getValue() != null ? entry.getValue() : List.<PlayerInfo>of()) {
                if (player == null) {
                    continue;
                }
                final Double plannedParticipation = plannedParticipationByPlayer.get(player.getId());
                final Double actualParticipation = this.getActualTrainingPercent(player, actualTypeId);
                final String indicator = this.getPlayerIndicator(trainingTypeMismatch, plannedParticipation, actualParticipation);
                if (indicator != null) {
                    indicatorsByPlayer.put(player.getId(), indicator);
                }
            }
            result.put(week, indicatorsByPlayer);
        }
        return result;
    }

    private String getPlayerIndicator(final boolean trainingTypeMismatch, final Double plannedParticipation, final Double actualParticipation) {
        if (trainingTypeMismatch) {
            return "warning";
        }
        if (plannedParticipation == null || actualParticipation == null) {
            return null;
        }
        final long roundedPlannedParticipation = Math.round(plannedParticipation);
        final long roundedActualParticipation = Math.round(actualParticipation);
        if (roundedActualParticipation < roundedPlannedParticipation) {
            return "warning";
        }
        if (roundedActualParticipation > roundedPlannedParticipation) {
            return "positive";
        }
        return null;
    }

    private double getStageTrainingFactor(final TrainingStage stage) {
        if (stage == null) {
            return 1.0;
        }
        final double trainingLevel = Math.max(0.0, stage.getIntensity() / 100.0);
        final double staminaPart = this.getStaminaTrainingPartCoefficient(stage.getStamina());
        final double trainerPer = PER_TRAINER.getOrDefault(stage.getCoach(), 1.0);
        final double assistantsPer = PER_ASSISTANTS.getOrDefault(stage.getAssistants(), 1.0);
        return trainingLevel * staminaPart * trainerPer * assistantsPer;
    }

    private double getStaminaTrainingPartCoefficient(final int staminaTrainingPart) {
        return (100.0 - (((staminaTrainingPart - 10.0) * 100.0) / (100.0 - 10.0))) / 100.0;
    }

    private Double getActualTrainingPercent(final PlayerInfo player, final Integer trainingTypeId) {
        if (player == null || player.getPlayerTraining() == null || trainingTypeId == null) {
            return null;
        }
        return switch (trainingTypeId) {
            case 2 -> player.getPlayerTraining().getSetPieces();
            case 3, 11 -> player.getPlayerTraining().getDefender();
            case 4 -> player.getPlayerTraining().getScorer();
            case 5, 12 -> player.getPlayerTraining().getWinger();
            case 6 -> Math.max(player.getPlayerTraining().getScorer(), player.getPlayerTraining().getSetPieces());
            case 7, 10 -> player.getPlayerTraining().getPassing();
            case 8 -> player.getPlayerTraining().getPlaymaker();
            case 9 -> player.getPlayerTraining().getKeeper();
            default -> null;
        };
    }

    private TeamTrainingRequest buildRequestFromActual(final TeamTrainingRequest originalRequest, final List<WeeklyInfo> weeklyData, final int startIndex, final int completedWeeks) {
        if (originalRequest == null || originalRequest.getStages() == null || originalRequest.getStages().isEmpty()) {
            return null;
        }
        final WeeklyInfo actualWeekData = weeklyData.get(startIndex + completedWeeks);
        final List<TrainingStage> remainingStages = this.getRemainingStages(originalRequest.getStages(), completedWeeks);
        if (remainingStages.isEmpty()) {
            return null;
        }
        final Map<Integer, PlayerInfo> actualPlayersById = new LinkedHashMap<>();
        for (PlayerInfo player : actualWeekData.getPlayers() != null ? actualWeekData.getPlayers() : List.<PlayerInfo>of()) {
            actualPlayersById.put(player.getId(), player);
        }
        final List<TeamTrainingPlayer> players = new ArrayList<>();
        for (TeamTrainingPlayer originalPlayer : originalRequest.getPlayers() != null ? originalRequest.getPlayers() : List.<TeamTrainingPlayer>of()) {
            if (originalPlayer.getPlayer() == null) {
                continue;
            }
            final int adjustedInclusionWeek = Math.max(1, originalPlayer.getInclusionWeek() - completedWeeks);
            final Integer adjustedDepartureWeek = originalPlayer.getDepartureWeek() == null
                ? null
                : originalPlayer.getDepartureWeek() - completedWeeks;
            if (adjustedDepartureWeek != null && adjustedDepartureWeek <= 0) {
                continue;
            }
            players.add(TeamTrainingPlayer.builder()
                .player(actualPlayersById.getOrDefault(originalPlayer.getPlayer().getId(), originalPlayer.getPlayer()))
                .inclusionWeek(adjustedInclusionWeek)
                .departureWeek(adjustedDepartureWeek)
                .build());
        }
        final List<Integer> remainingStageIds = remainingStages.stream().map(TrainingStage::getId).toList();
        final List<StagePlayerParticipation> participations = (originalRequest.getParticipations() != null ? originalRequest.getParticipations() : List.<StagePlayerParticipation>of()).stream()
            .filter(participation -> remainingStageIds.contains(participation.getStageId()))
            .toList();

        return TeamTrainingRequest.builder()
            .iniWeek(this.toWeekInfo(actualWeekData))
            .players(players)
            .stages(remainingStages)
            .participations(participations)
            .calculateBestFormation(false)
            .build();
    }

    private List<TrainingStage> getRemainingStages(final List<TrainingStage> stages, final int completedWeeks) {
        int consumedWeeks = 0;
        final List<TrainingStage> result = new ArrayList<>();
        for (TrainingStage stage : stages) {
            final int duration = stage.getDuration();
            if (consumedWeeks + duration <= completedWeeks) {
                consumedWeeks += duration;
                continue;
            }
            final int remainingDuration = Math.max(0, duration - Math.max(0, completedWeeks - consumedWeeks));
            if (remainingDuration > 0) {
                result.add(TrainingStage.builder()
                    .id(stage.getId())
                    .duration(remainingDuration)
                    .coach(stage.getCoach())
                    .assistants(stage.getAssistants())
                    .intensity(stage.getIntensity())
                    .stamina(stage.getStamina())
                    .training(stage.getTraining())
                    .build());
            }
            consumedWeeks += duration;
        }
        return result;
    }
}
