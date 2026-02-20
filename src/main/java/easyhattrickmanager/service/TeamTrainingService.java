package easyhattrickmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.PlayerSubSkillInfo;
import easyhattrickmanager.service.model.playertraining.Skill;
import easyhattrickmanager.service.model.playertraining.SkillCoefficient;
import easyhattrickmanager.service.model.teamtraining.BestFormationCriteria;
import easyhattrickmanager.service.model.teamtraining.Formation;
import easyhattrickmanager.service.model.teamtraining.FormationRating;
import easyhattrickmanager.service.model.teamtraining.MatchDetail;
import easyhattrickmanager.service.model.teamtraining.StagePlayerParticipation;
import easyhattrickmanager.service.model.teamtraining.SideMatch;
import easyhattrickmanager.service.model.teamtraining.Tactic;
import easyhattrickmanager.service.model.teamtraining.TeamAttitude;
import easyhattrickmanager.service.model.teamtraining.TeamConfidence;
import easyhattrickmanager.service.model.teamtraining.TeamSpirit;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingPlayer;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingRequest;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingResponse;
import easyhattrickmanager.service.model.teamtraining.TrainingStage;
import easyhattrickmanager.service.model.teamtraining.WeekInfo;
import easyhattrickmanager.utils.HTMSUtils;
import easyhattrickmanager.utils.SeasonWeekUtils;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamTrainingService {

    private final Cache<String, TeamTrainingResponse> responseCache = Caffeine.newBuilder()
        .maximumSize(64)
        .expireAfterWrite(Duration.ofHours(1))
        .build();
    private final Map<String, CompletableFuture<TeamTrainingResponse>> inFlightRequests = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final PlayerTrainingService playerTrainingService;
    private final WageService wageService;
    private final TSIFormService tsiFormService;
    private final FormationRatingService formationRatingService;

    public TeamTrainingResponse getTeamTraining(TeamTrainingRequest teamTrainingRequest) {
        final String requestCacheKey = this.toCacheKey(teamTrainingRequest);
        log.debug("teamTraining request key={}", Integer.toHexString(requestCacheKey.hashCode()));
        final TeamTrainingResponse cachedResponse = this.responseCache.getIfPresent(requestCacheKey);
        if (cachedResponse != null) {
            log.debug("teamTraining cache hit for full request");
            return cachedResponse;
        }
        final CompletableFuture<TeamTrainingResponse> inFlightFuture = new CompletableFuture<>();
        final CompletableFuture<TeamTrainingResponse> existingFuture = this.inFlightRequests.putIfAbsent(requestCacheKey, inFlightFuture);
        if (existingFuture != null) {
            log.debug("teamTraining joined in-flight full request");
            return existingFuture.join();
        }
        try {
            final TeamTrainingResponse response = this.computeTeamTraining(teamTrainingRequest);
            this.responseCache.put(requestCacheKey, response);
            inFlightFuture.complete(response);
            return response;
        } catch (RuntimeException ex) {
            inFlightFuture.completeExceptionally(ex);
            throw ex;
        } catch (Error err) {
            inFlightFuture.completeExceptionally(err);
            throw err;
        } finally {
            this.inFlightRequests.remove(requestCacheKey, inFlightFuture);
        }
    }

    private TeamTrainingResponse computeTeamTraining(final TeamTrainingRequest teamTrainingRequest) {
        final List<TrainingStage> stages = teamTrainingRequest.getStages();
        final PrefixReuseState prefixReuseState = this.findCachedPrefixState(teamTrainingRequest, stages);
        if (prefixReuseState.consumedStageCount() > 0) {
            log.debug("teamTraining prefix reuse hit: {} stages / {} weeks", prefixReuseState.consumedStageCount(), prefixReuseState.consumedWeeks());
        } else {
            log.debug("teamTraining prefix reuse miss");
        }
        final Map<Long, Integer> participationByStageAndPlayer = this.indexParticipations(teamTrainingRequest.getParticipations());
        final Map<Integer, List<PlayerInfo>> playersByInclusionWeek = this.indexPlayersByInclusionWeek(teamTrainingRequest.getPlayers());
        final Map<Integer, Map<Skill, Double>> coefficientsByStageId = stages.stream()
            .collect(Collectors.toMap(TrainingStage::getId, stage -> this.indexSkillCoefficients(stage.getTraining().getSkillCoefficients())));
        final boolean calculateBestFormation = this.shouldCalculateBestFormation(teamTrainingRequest);

        final Map<Integer, List<PlayerInfo>> weekPlayers = new LinkedHashMap<>(prefixReuseState.weekPlayers());
        final MatchDetail matchDetail = calculateBestFormation ? getMatchDetail(teamTrainingRequest.getMatchDetail()) : null;
        final BestFormationCriteria criteria = calculateBestFormation ? getBestFormationCriteria(teamTrainingRequest.getBestFormationCriteria()) : null;
        final Comparator<FormationRating> criteriaComparator = calculateBestFormation ? criteria.getFormationRatingComparator() : null;
        final Formation fixedFormation = calculateBestFormation ? getFixedFormation(teamTrainingRequest.getFixedFormationCode()) : null;

        int currentWeek = prefixReuseState.consumedWeeks();
        int totalWeeks = prefixReuseState.consumedWeeks();
        List<PlayerInfo> previousWeekPlayers = prefixReuseState.previousWeekPlayers();

        for (int stageIndex = prefixReuseState.consumedStageCount(); stageIndex < stages.size(); stageIndex++) {
            final TrainingStage trainingStage = stages.get(stageIndex);
            final int trainingStageId = trainingStage.getId();
            final int duration = trainingStage.getDuration();
            final Map<Skill, Double> coefficientsBySkill = coefficientsByStageId.get(trainingStageId);
            totalWeeks += duration;
            for (int i = 0; i < duration; i++) {
                currentWeek++;
                final List<PlayerInfo> includedPlayers = playersByInclusionWeek.getOrDefault(currentWeek, List.of());
                final List<PlayerInfo> players = this.getPlayers(
                    trainingStage,
                    includedPlayers,
                    previousWeekPlayers,
                    participationByStageAndPlayer,
                    coefficientsBySkill);
                weekPlayers.put(currentWeek, players);
                previousWeekPlayers = players;
            }
        }

        final Map<Integer, FormationRating> weekFormationRatings;
        final FormationRating bestFormationRating;
        final int bestWeek;
        if (calculateBestFormation) {
            weekFormationRatings = this.calculateWeekFormationRatings(
                weekPlayers,
                prefixReuseState,
                matchDetail,
                criteria,
                fixedFormation,
                criteriaComparator);
            final Map.Entry<Integer, FormationRating> bestWeekRating = this.getBestWeekRating(weekFormationRatings, criteriaComparator);
            bestFormationRating = bestWeekRating != null ? bestWeekRating.getValue() : null;
            bestWeek = bestWeekRating != null ? bestWeekRating.getKey() : 0;
        } else {
            weekFormationRatings = Map.of();
            bestFormationRating = null;
            bestWeek = 0;
        }

        final WeekInfo endWeek = calculateEndWeek(teamTrainingRequest.getIniWeek(), totalWeeks);
        final TeamTrainingResponse response = TeamTrainingResponse.builder()
            .weekPlayers(weekPlayers)
            .weekFormationRatings(weekFormationRatings)
            .bestFormationRating(bestFormationRating)
            .bestWeek(bestWeek)
            .endWeek(endWeek)
            .build();
        return response;
    }

    private PrefixReuseState findCachedPrefixState(final TeamTrainingRequest fullRequest, final List<TrainingStage> stages) {
        if (stages == null || stages.size() < 2) {
            return PrefixReuseState.empty();
        }
        for (int prefixStageCount = stages.size() - 1; prefixStageCount >= 1; prefixStageCount--) {
            final TeamTrainingRequest prefixRequest = this.buildPrefixRequest(fullRequest, stages, prefixStageCount);
            final String prefixCacheKey = this.toCacheKey(prefixRequest);
            log.debug("teamTraining prefix candidate stages={} key={}", prefixStageCount, Integer.toHexString(prefixCacheKey.hashCode()));
            TeamTrainingResponse prefixResponse = this.responseCache.getIfPresent(prefixCacheKey);
            if (prefixResponse == null) {
                prefixResponse = this.awaitInFlightResponse(prefixCacheKey);
            }
            if (prefixResponse == null || prefixResponse.getWeekPlayers() == null || prefixResponse.getWeekPlayers().isEmpty()) {
                continue;
            }
            final int prefixWeeks = this.getTotalWeeks(stages, prefixStageCount);
            final List<PlayerInfo> previousWeekPlayers = prefixResponse.getWeekPlayers().get(prefixWeeks);
            if (previousWeekPlayers == null) {
                continue;
            }
            final LinkedHashMap<Integer, List<PlayerInfo>> orderedWeekPlayers = prefixResponse.getWeekPlayers().entrySet().stream()
                .filter(entry -> entry.getKey() <= prefixWeeks)
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (left, right) -> left,
                    LinkedHashMap::new));
            final LinkedHashMap<Integer, FormationRating> orderedWeekFormationRatings = prefixResponse.getWeekFormationRatings() == null
                ? new LinkedHashMap<>()
                : prefixResponse.getWeekFormationRatings().entrySet().stream()
                    .filter(entry -> entry.getKey() <= prefixWeeks)
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new));
            final FormationRating previousWeekFormationRating = orderedWeekFormationRatings.get(prefixWeeks);
            return new PrefixReuseState(
                prefixStageCount,
                prefixWeeks,
                orderedWeekPlayers,
                previousWeekPlayers,
                orderedWeekFormationRatings,
                previousWeekFormationRating);
        }
        return PrefixReuseState.empty();
    }

    private TeamTrainingResponse awaitInFlightResponse(final String cacheKey) {
        final CompletableFuture<TeamTrainingResponse> inFlight = this.inFlightRequests.get(cacheKey);
        if (inFlight == null) {
            return null;
        }
        try {
            return inFlight.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException | TimeoutException ex) {
            return null;
        }
    }

    private String toCacheKey(final TeamTrainingRequest request) {
        final WeekInfo iniWeek = request.getIniWeek();
        final boolean calculateBestFormation = this.shouldCalculateBestFormation(request);
        final BestFormationCriteria criteria = calculateBestFormation ? this.getBestFormationCriteria(request.getBestFormationCriteria()) : null;
        final MatchDetail matchDetail = calculateBestFormation ? this.getMatchDetail(request.getMatchDetail()) : null;
        final Formation fixedFormation = calculateBestFormation ? this.getFixedFormation(request.getFixedFormationCode()) : null;
        final List<TrainingStage> normalizedStages = this.normalizeStages(request.getStages());
        final Map<Integer, Integer> normalizedStageIdByOriginal = this.buildNormalizedStageIds(request.getStages());
        final List<TeamTrainingPlayer> normalizedPlayers = this.normalizePlayers(request.getPlayers());
        final List<StagePlayerParticipation> normalizedParticipations = this.normalizeParticipations(request.getParticipations(), normalizedStageIdByOriginal);
        final TeamTrainingRequest normalizedRequest = TeamTrainingRequest.builder()
            .iniWeek(iniWeek == null
                ? null
                : WeekInfo.builder()
                    .season(iniWeek.getSeason())
                    .week(iniWeek.getWeek())
                    .date(null)
                    .build())
            .players(normalizedPlayers)
            .stages(normalizedStages)
            .participations(normalizedParticipations)
            .bestFormationCriteria(criteria)
            .fixedFormationCode(fixedFormation == null ? null : fixedFormation.name())
            .matchDetail(matchDetail)
            .calculateBestFormation(calculateBestFormation)
            .build();
        try {
            return this.objectMapper.writeValueAsString(normalizedRequest);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not build team training cache key", ex);
        }
    }

    private List<TrainingStage> normalizeStages(final List<TrainingStage> stages) {
        if (stages == null || stages.isEmpty()) {
            return List.of();
        }
        final List<TrainingStage> normalized = new ArrayList<>(stages.size());
        for (int i = 0; i < stages.size(); i++) {
            final TrainingStage stage = stages.get(i);
            normalized.add(TrainingStage.builder()
                .id(i + 1)
                .duration(stage.getDuration())
                .coach(stage.getCoach())
                .assistants(stage.getAssistants())
                .intensity(stage.getIntensity())
                .stamina(stage.getStamina())
                .training(stage.getTraining())
                .build());
        }
        return normalized;
    }

    private Map<Integer, Integer> buildNormalizedStageIds(final List<TrainingStage> stages) {
        if (stages == null || stages.isEmpty()) {
            return Map.of();
        }
        final Map<Integer, Integer> normalized = new HashMap<>(stages.size() * 2);
        for (int i = 0; i < stages.size(); i++) {
            normalized.put(stages.get(i).getId(), i + 1);
        }
        return normalized;
    }

    private List<TeamTrainingPlayer> normalizePlayers(final List<TeamTrainingPlayer> players) {
        if (players == null || players.isEmpty()) {
            return List.of();
        }
        return players.stream()
            .sorted(Comparator
                .comparingInt((TeamTrainingPlayer player) -> player.getPlayer() == null ? Integer.MAX_VALUE : player.getPlayer().getId())
                .thenComparingInt(TeamTrainingPlayer::getInclusionWeek))
            .toList();
    }

    private List<StagePlayerParticipation> normalizeParticipations(final List<StagePlayerParticipation> participations,
                                                                   final Map<Integer, Integer> normalizedStageIdByOriginal) {
        if (participations == null || participations.isEmpty() || normalizedStageIdByOriginal.isEmpty()) {
            return List.of();
        }
        return participations.stream()
            .map(participation -> {
                final Integer normalizedStageId = normalizedStageIdByOriginal.get(participation.getStageId());
                if (normalizedStageId == null) {
                    return null;
                }
                return StagePlayerParticipation.builder()
                    .stageId(normalizedStageId)
                    .playerId(participation.getPlayerId())
                    .participation(participation.getParticipation())
                    .build();
            })
            .filter(participation -> participation != null)
            .sorted(Comparator
                .comparingInt(StagePlayerParticipation::getStageId)
                .thenComparingInt(StagePlayerParticipation::getPlayerId))
            .toList();
    }

    private TeamTrainingRequest buildPrefixRequest(final TeamTrainingRequest fullRequest, final List<TrainingStage> stages, final int prefixStageCount) {
        final List<TrainingStage> prefixStages = new ArrayList<>(stages.subList(0, prefixStageCount));
        final Set<Integer> prefixStageIds = new HashSet<>();
        prefixStages.forEach(stage -> prefixStageIds.add(stage.getId()));
        final List<StagePlayerParticipation> prefixParticipations = fullRequest.getParticipations() == null ? List.of()
            : fullRequest.getParticipations().stream()
                .filter(participation -> prefixStageIds.contains(participation.getStageId()))
                .toList();
        return TeamTrainingRequest.builder()
            .iniWeek(fullRequest.getIniWeek())
            .players(fullRequest.getPlayers())
            .stages(prefixStages)
            .participations(prefixParticipations)
            .bestFormationCriteria(fullRequest.getBestFormationCriteria())
            .fixedFormationCode(fullRequest.getFixedFormationCode())
            .matchDetail(fullRequest.getMatchDetail())
            .calculateBestFormation(fullRequest.getCalculateBestFormation())
            .build();
    }

    private int getTotalWeeks(final List<TrainingStage> stages, final int stageCount) {
        int weeks = 0;
        for (int i = 0; i < stageCount && i < stages.size(); i++) {
            weeks += stages.get(i).getDuration();
        }
        return weeks;
    }

    private record PrefixReuseState(int consumedStageCount,
                                    int consumedWeeks,
                                    Map<Integer, List<PlayerInfo>> weekPlayers,
                                    List<PlayerInfo> previousWeekPlayers,
                                    Map<Integer, FormationRating> weekFormationRatings,
                                    FormationRating previousWeekFormationRating) {
        private static PrefixReuseState empty() {
            return new PrefixReuseState(0, 0, Map.of(), List.of(), Map.of(), null);
        }
    }

    private boolean shouldCalculateBestFormation(TeamTrainingRequest teamTrainingRequest) {
        return Boolean.TRUE.equals(teamTrainingRequest.getCalculateBestFormation());
    }

    private Map<Integer, FormationRating> calculateWeekFormationRatings(final Map<Integer, List<PlayerInfo>> weekPlayers,
                                                                        final PrefixReuseState prefixReuseState,
                                                                        final MatchDetail matchDetail,
                                                                        final BestFormationCriteria criteria,
                                                                        final Formation fixedFormation,
                                                                        final Comparator<FormationRating> criteriaComparator) {
        final int consumedWeeks = prefixReuseState.consumedWeeks();
        final Map<Integer, FormationRating> cachedPrefixRatings = new LinkedHashMap<>(prefixReuseState.weekFormationRatings());
        final Map<Integer, FormationRating> calculated = weekPlayers.entrySet().parallelStream()
            .filter(entry -> entry.getKey() > consumedWeeks || !cachedPrefixRatings.containsKey(entry.getKey()))
            .collect(Collectors.toConcurrentMap(
                Map.Entry::getKey,
                entry -> this.formationRatingService.getRatings(entry.getValue(), matchDetail, criteria, fixedFormation)));

        final Map<Integer, FormationRating> adjusted = new LinkedHashMap<>(weekPlayers.size());
        if (!cachedPrefixRatings.isEmpty()) {
            adjusted.putAll(cachedPrefixRatings);
        }
        FormationRating previousWeekFormationRating = prefixReuseState.previousWeekFormationRating();
        for (Integer week : weekPlayers.keySet()) {
            final List<PlayerInfo> currentWeekPlayers = weekPlayers.getOrDefault(week, List.of());
            FormationRating currentWeekFormationRating = adjusted.getOrDefault(week, calculated.get(week));
            if (currentWeekFormationRating == null) {
                continue;
            }
            if (week > consumedWeeks
                && this.shouldTryPreviousWeekLineup(currentWeekPlayers, currentWeekFormationRating, previousWeekFormationRating, criteriaComparator)) {
                FormationRating previousWeekLineupWithCurrentSkills = this.formationRatingService.getRatingsForPreviousLineup(
                    currentWeekPlayers,
                    previousWeekFormationRating,
                    matchDetail);
                if (previousWeekLineupWithCurrentSkills != null
                    && criteriaComparator.compare(previousWeekLineupWithCurrentSkills, currentWeekFormationRating) < 0) {
                    currentWeekFormationRating = previousWeekLineupWithCurrentSkills;
                }
            }
            adjusted.put(week, currentWeekFormationRating);
            previousWeekFormationRating = currentWeekFormationRating;
        }
        return adjusted;
    }

    private boolean shouldTryPreviousWeekLineup(final List<PlayerInfo> currentWeekPlayers,
                                                final FormationRating currentWeekFormationRating,
                                                final FormationRating previousWeekFormationRating,
                                                final Comparator<FormationRating> criteriaComparator) {
        if (currentWeekPlayers == null || currentWeekPlayers.isEmpty() || currentWeekFormationRating == null || previousWeekFormationRating == null) {
            return false;
        }
        return criteriaComparator.compare(currentWeekFormationRating, previousWeekFormationRating) > 0;
    }

    private Map.Entry<Integer, FormationRating> getBestWeekRating(final Map<Integer, FormationRating> weekFormationRatings,
                                                                  final Comparator<FormationRating> criteriaComparator) {
        return weekFormationRatings.entrySet().stream()
            .min((left, right) -> criteriaComparator.compare(left.getValue(), right.getValue()))
            .orElse(null);
    }

    private MatchDetail getMatchDetail(MatchDetail matchDetail) {
        if (matchDetail != null) {
            return matchDetail;
        }
        return MatchDetail.builder()
            .tactic(Tactic.NORMAL)
            .teamAttitude(TeamAttitude.PIN)
            .teamSpirit(TeamSpirit.CALM)
            .teamSubSpirit(0.5)
            .teamConfidence(TeamConfidence.STRONG)
            .teamSubConfidence(0.5)
            .sideMatch(SideMatch.AWAY)
            .styleOfPlay(0)
            .build();
    }

    private BestFormationCriteria getBestFormationCriteria(BestFormationCriteria criteria) {
        return criteria == null ? BestFormationCriteria.HATSTATS : criteria;
    }

    private Formation getFixedFormation(String fixedFormationCode) {
        if (fixedFormationCode == null || fixedFormationCode.isBlank()) {
            return null;
        }
        try {
            return Formation.valueOf(fixedFormationCode.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private List<PlayerInfo> getPlayers(
        TrainingStage trainingStage,
        List<PlayerInfo> includedPlayers,
        List<PlayerInfo> previousWeekPlayers,
        Map<Long, Integer> participationByStageAndPlayer,
        Map<Skill, Double> coefficientsBySkill) {
        final int trainingStageId = trainingStage.getId();
        final int includedSize = includedPlayers.size();
        final int previousSize = previousWeekPlayers.size();
        final int totalPlayers = includedSize + previousSize;

        if (totalPlayers < 24) {
            final List<PlayerInfo> players = new ArrayList<>(totalPlayers);
            includedPlayers.forEach(player -> players.add(
                this.applyPlayerWeekTraining(
                    player,
                    trainingStage,
                    getParticipation(participationByStageAndPlayer, trainingStageId, player.getId()),
                    coefficientsBySkill)));
            previousWeekPlayers.forEach(player -> players.add(
                this.applyPlayerWeekTraining(
                    player,
                    trainingStage,
                    getParticipation(participationByStageAndPlayer, trainingStageId, player.getId()),
                    coefficientsBySkill)));
            return players;
        }

        final PlayerInfo[] players = new PlayerInfo[totalPlayers];
        IntStream.range(0, totalPlayers).parallel().forEach(index -> {
            final PlayerInfo player = index < includedSize
                ? includedPlayers.get(index)
                : previousWeekPlayers.get(index - includedSize);
            players[index] = this.applyPlayerWeekTraining(
                player,
                trainingStage,
                getParticipation(participationByStageAndPlayer, trainingStageId, player.getId()),
                coefficientsBySkill);
        });
        return new ArrayList<>(Arrays.asList(players));
    }

    private Map<Integer, List<PlayerInfo>> indexPlayersByInclusionWeek(final List<TeamTrainingPlayer> players) {
        final Map<Integer, List<PlayerInfo>> index = new HashMap<>();
        players.forEach(teamTrainingPlayer -> index
            .computeIfAbsent(teamTrainingPlayer.getInclusionWeek(), week -> new ArrayList<>())
            .add(teamTrainingPlayer.getPlayer()));
        return index;
    }

    private int getParticipation(Map<Long, Integer> participationByStageAndPlayer, int stageId, int playerId) {
        return participationByStageAndPlayer.getOrDefault(participationKey(stageId, playerId), 0);
    }

    private Map<Long, Integer> indexParticipations(List<StagePlayerParticipation> participations) {
        final Map<Long, Integer> index = new HashMap<>(participations.size() * 2);
        participations.forEach(participation -> index.put(participationKey(participation.getStageId(), participation.getPlayerId()), participation.getParticipation()));
        return index;
    }

    private long participationKey(int stageId, int playerId) {
        return (((long) stageId) << 32) | (playerId & 0xffffffffL);
    }

    private PlayerInfo applyPlayerWeekTraining(PlayerInfo player, TrainingStage trainingStage, int participation, Map<Skill, Double> coefficientsBySkill) {
        int days = player.getAgeDays() + 7;
        int age = days > 111 ? player.getAge() + 1 : player.getAge();
        int ageDays = days % 112;
        PlayerSubSkillInfo playerSubSkill = player.getPlayerSubSkill() != null ? player.getPlayerSubSkill() : PlayerSubSkillInfo.builder()
            .stamina(0.0)
            .keeper(0.0)
            .defender(0.0)
            .playmaker(0.0)
            .winger(0.0)
            .passing(0.0)
            .scorer(0.0)
            .setPieces(0.0)
            .build();

        double staminaOld = player.getStaminaSkill() + playerSubSkill.getStamina();
        double keeperOld = player.getKeeperSkill() + playerSubSkill.getKeeper();
        double defenderOld = player.getDefenderSkill() + playerSubSkill.getDefender();
        double playmakerOld = player.getPlaymakerSkill() + playerSubSkill.getPlaymaker();
        double wingerOld = player.getWingerSkill() + playerSubSkill.getWinger();
        double passingOld = player.getPassingSkill() + playerSubSkill.getPassing();
        double scorerOld = player.getScorerSkill() + playerSubSkill.getScorer();
        double setPiecesOld = player.getSetPiecesSkill() + playerSubSkill.getSetPieces();

        int minutes = 90 * participation / 100;
        double trainingBaseFactor = participation == 0 ? 0.0 : playerTrainingService.getTrainingBaseFactor(
            trainingStage.getCoach(),
            trainingStage.getAssistants(),
            trainingStage.getIntensity(),
            trainingStage.getStamina(),
            age,
            minutes);

        double dropKeeper = playerTrainingService.getDropAge(Skill.GOALKEEPING, age);
        double dropDefender = playerTrainingService.getDropAge(Skill.DEFENDING, age);
        double dropPlaymaker = playerTrainingService.getDropAge(Skill.PLAY_MAKING, age);
        double dropWinger = playerTrainingService.getDropAge(Skill.WINGER, age);
        double dropPassing = playerTrainingService.getDropAge(Skill.PASSING, age);
        double dropScorer = playerTrainingService.getDropAge(Skill.SCORING, age);
        double dropSetPieces = playerTrainingService.getDropAge(Skill.SET_PIECES, age);

        double stamina = Math.max(0.0, playerTrainingService.getStaminaTraining(age, staminaOld, trainingStage.getStamina(), trainingStage.getIntensity(), 1.0));
        double keeper = Math.max(0.0, keeperOld - dropKeeper + this.getSkillTraining(Skill.GOALKEEPING, keeperOld, age, coefficientsBySkill, trainingBaseFactor));
        double defender = Math.max(0.0, defenderOld - dropDefender + this.getSkillTraining(Skill.DEFENDING, defenderOld, age, coefficientsBySkill, trainingBaseFactor));
        double playmaker = Math.max(0.0, playmakerOld - dropPlaymaker + this.getSkillTraining(Skill.PLAY_MAKING, playmakerOld, age, coefficientsBySkill, trainingBaseFactor));
        double winger = Math.max(0.0, wingerOld - dropWinger + this.getSkillTraining(Skill.WINGER, wingerOld, age, coefficientsBySkill, trainingBaseFactor));
        double passing = Math.max(0.0, passingOld - dropPassing + this.getSkillTraining(Skill.PASSING, passingOld, age, coefficientsBySkill, trainingBaseFactor));
        double scorer = Math.max(0.0, scorerOld - dropScorer + this.getSkillTraining(Skill.SCORING, scorerOld, age, coefficientsBySkill, trainingBaseFactor));
        double setPieces = Math.max(0.0, setPiecesOld - dropSetPieces + this.getSkillTraining(Skill.SET_PIECES, setPiecesOld, age, coefficientsBySkill, trainingBaseFactor));

        HTMS htms = HTMSUtils.calculateHTMS(age, ageDays, (int) Math.floor(keeper), (int) Math.floor(defender), (int) Math.floor(playmaker), (int) Math.floor(winger), (int) Math.floor(passing), (int) Math.floor(scorer), (int) Math.floor(setPieces));
        HTMS htmsWithSubSkills = HTMSUtils.calculateHTMS(age, ageDays, keeper, defender, playmaker, winger, passing, scorer, setPieces);

        double wage = ageDays < 7 ? wageService.calculateWage(age, player.isAbroad(), player.getSpecialty(), keeper, defender, playmaker, winger, passing, scorer, setPieces) : player.getSalary();
        double TSI = tsiFormService.calculateTSI(age, player.getPlayerForm(), stamina, keeper, defender, playmaker, winger, passing, scorer);

        return PlayerInfo.builder()
            .id(player.getId())
            .firstName(player.getFirstName())
            .nickName(player.getNickName())
            .lastName(player.getLastName())
            .agreeability(player.getAgreeability())
            .aggressiveness(player.getAggressiveness())
            .honesty(player.getHonesty())
            .specialty(player.getSpecialty())
            .countryId(player.getCountryId())
            .playerNumber(player.getPlayerNumber())
            .age(age)
            .ageDays(ageDays)
            .arrivalDate(player.getArrivalDate())
            .TSI((int) TSI)
            .playerForm(player.getPlayerForm())
            .experience(player.getExperience())
            .loyalty(player.getLoyalty())
            .motherClubBonus(player.isMotherClubBonus())
            .leadership(player.getLeadership())
            .salary((int) wage)
            .transferListed(player.isTransferListed())
            .cards(player.getCards())
            .injuryLevel(player.getInjuryLevel())
            .staminaSkill((int) Math.floor(stamina))
            .keeperSkill((int) Math.floor(keeper))
            .playmakerSkill((int) Math.floor(playmaker))
            .scorerSkill((int) Math.floor(scorer))
            .passingSkill((int) Math.floor(passing))
            .wingerSkill((int) Math.floor(winger))
            .defenderSkill((int) Math.floor(defender))
            .setPiecesSkill((int) Math.floor(setPieces))
            .htms(htms.getHtms())
            .htms28(htms.getHtms28())
            .playerCategoryId(player.getPlayerCategoryId())
            .playerTraining(null)
            .playerSubSkill(PlayerSubSkillInfo.builder()
                .stamina(stamina - Math.floor(stamina))
                .keeper(keeper - Math.floor(keeper))
                .defender(defender - Math.floor(defender))
                .playmaker(playmaker - Math.floor(playmaker))
                .winger(winger - Math.floor(winger))
                .passing(passing - Math.floor(passing))
                .scorer(scorer - Math.floor(scorer))
                .setPieces(setPieces - Math.floor(setPieces))
                .htms(htmsWithSubSkills.getHtms())
                .htms28(htmsWithSubSkills.getHtms28())
                .build())
            .build();
    }

    private double getSkillTraining(Skill skill, double skillValue, int age, Map<Skill, Double> coefficientsBySkill, double trainingBaseFactor) {
        if (trainingBaseFactor == 0.0) {
            return 0.0;
        }
        Double coefficientSkill = coefficientsBySkill.get(skill);
        if (coefficientSkill == null) {
            return 0.0;
        }
        return playerTrainingService.getSkillTraining(skillValue, age, coefficientSkill, trainingBaseFactor);
    }

    private Map<Skill, Double> indexSkillCoefficients(List<SkillCoefficient> skillCoefficients) {
        Map<Skill, Double> coefficientsBySkill = new EnumMap<>(Skill.class);
        skillCoefficients.forEach(skillCoefficient -> coefficientsBySkill.put(skillCoefficient.getSkill(), skillCoefficient.getCoefficient()));
        return coefficientsBySkill;
    }

    private WeekInfo calculateEndWeek(WeekInfo iniWeek, int trainingWeeks) {
        var totalWeeks = iniWeek.getSeason() * 16 + iniWeek.getWeek() + trainingWeeks;
        return WeekInfo.builder()
            .season(totalWeeks / 16)
            .week(totalWeeks % 16)
            .date(iniWeek.getDate().plusWeeks(trainingWeeks))
            .build();
    }

    private WeekInfo weekInfoFromDate(ZonedDateTime date) {
        String seasonWeek = SeasonWeekUtils.convertToSeasonWeek(date);
        return weekInfoFromSeasonWeek(seasonWeek);
    }

    private WeekInfo weekInfoFromSeasonWeek(String seasonWeek) {
        int season = Integer.parseInt(seasonWeek.substring(1, 4));
        int week = Integer.parseInt(seasonWeek.substring(5, 7));
        ZonedDateTime date = SeasonWeekUtils.convertFromSeasonWeek(seasonWeek);
        return WeekInfo.builder()
            .season(season)
            .week(week)
            .date(date)
            .build();
    }

}
