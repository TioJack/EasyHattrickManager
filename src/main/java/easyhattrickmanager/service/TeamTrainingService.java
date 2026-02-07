package easyhattrickmanager.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.PlayerSubSkillInfo;
import easyhattrickmanager.service.model.playertraining.Skill;
import easyhattrickmanager.service.model.playertraining.SkillCoefficient;
import easyhattrickmanager.service.model.playertraining.SkillTrainingRequest;
import easyhattrickmanager.service.model.teamtraining.StagePlayerParticipation;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingPlayer;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingRequest;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingResponse;
import easyhattrickmanager.service.model.teamtraining.TrainingStage;
import easyhattrickmanager.service.model.teamtraining.WeekInfo;
import easyhattrickmanager.utils.HTMSUtils;
import easyhattrickmanager.utils.SeasonWeekUtils;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamTrainingService {

    private final PlayerTrainingService playerTrainingService;
    private final WageService wageService;
    private final TSIFormService tsiFormService;

    public TeamTrainingResponse getTeamTraining(TeamTrainingRequest teamTrainingRequest) {
        AtomicInteger week = new AtomicInteger(0);
        // <week,trainingStageId>
        Map<Integer, Integer> weeks = new HashMap<>();
        teamTrainingRequest.getStages().forEach(trainingStage -> {
            IntStream.range(0, trainingStage.getDuration()).forEach(
                stage -> weeks.put(week.incrementAndGet(), trainingStage.getId())
            );
        });

        Map<Integer, List<PlayerInfo>> weekPlayers = new HashMap<>();
        weeks.forEach((key, value) -> {
            List<PlayerInfo> players = this.getPlayers(key, value, weekPlayers.getOrDefault(key - 1, emptyList()), teamTrainingRequest);
            weekPlayers.put(key, players);
        });

        WeekInfo endWeek = calculateEndWeek(teamTrainingRequest.getIniWeek(), weeks.size());
        return TeamTrainingResponse.builder()
            .weekPlayers(weekPlayers)
            .endWeek(endWeek)
            .build();
    }

    private List<PlayerInfo> getPlayers(int week, int trainingStageId, List<PlayerInfo> previousWeekPlayers, TeamTrainingRequest teamTrainingRequest) {
        TrainingStage trainingStage = this.getTrainingStage(teamTrainingRequest, trainingStageId);
        return Stream.concat(
                teamTrainingRequest.getPlayers().stream()
                    .filter(player -> player.getInclusionWeek() == week)
                    .map(TeamTrainingPlayer::getPlayer),
                previousWeekPlayers.stream())
            .map(player -> this.applyPlayerWeekTraining(player, trainingStage, getParticipation(teamTrainingRequest.getParticipations(), trainingStageId, player.getId())))
            .collect(toList());
    }

    private TrainingStage getTrainingStage(TeamTrainingRequest teamTrainingRequest, int trainingStageId) {
        return teamTrainingRequest.getStages().stream().filter(trainingStage -> trainingStage.getId() == trainingStageId).findFirst().orElseThrow();
    }

    private int getParticipation(List<StagePlayerParticipation> participations, int stageId, int playerId) {
        return participations.stream().filter(participation -> participation.getStageId() == stageId && participation.getPlayerId() == playerId)
            .findFirst().map(StagePlayerParticipation::getParticipation)
            .orElse(0);
    }

    private PlayerInfo applyPlayerWeekTraining(PlayerInfo player, TrainingStage trainingStage, int participation) {
        int days = player.getAgeDays() + 7;
        int age = days > 111 ? player.getAge() + 1 : player.getAge();
        int ageDays = days % 112;

        double staminaOld = player.getStaminaSkill() + player.getPlayerSubSkill().getStamina();
        double keeperOld = player.getKeeperSkill() + player.getPlayerSubSkill().getKeeper();
        double defenderOld = player.getDefenderSkill() + player.getPlayerSubSkill().getDefender();
        double playmakerOld = player.getPlaymakerSkill() + player.getPlayerSubSkill().getPlaymaker();
        double wingerOld = player.getWingerSkill() + player.getPlayerSubSkill().getWinger();
        double passingOld = player.getPassingSkill() + player.getPlayerSubSkill().getPassing();
        double scorerOld = player.getScorerSkill() + player.getPlayerSubSkill().getScorer();
        double setPiecesOld = player.getSetPiecesSkill() + player.getPlayerSubSkill().getSetPieces();

        double stamina = Math.max(0.0, playerTrainingService.getStaminaTraining(age, staminaOld, trainingStage.getStamina(), trainingStage.getIntensity(), 1.0));
        double keeper = Math.max(0.0, keeperOld - playerTrainingService.getDropAge(Skill.GOALKEEPING, age) + this.getSkillTraining(Skill.GOALKEEPING, keeperOld, age, trainingStage, participation));
        double defender = Math.max(0.0, defenderOld - playerTrainingService.getDropAge(Skill.DEFENDING, age) + this.getSkillTraining(Skill.DEFENDING, defenderOld, age, trainingStage, participation));
        double playmaker = Math.max(0.0, playmakerOld - playerTrainingService.getDropAge(Skill.PLAY_MAKING, age) + this.getSkillTraining(Skill.PLAY_MAKING, playmakerOld, age, trainingStage, participation));
        double winger = Math.max(0.0, wingerOld - playerTrainingService.getDropAge(Skill.WINGER, age) + this.getSkillTraining(Skill.WINGER, wingerOld, age, trainingStage, participation));
        double passing = Math.max(0.0, passingOld - playerTrainingService.getDropAge(Skill.PASSING, age) + this.getSkillTraining(Skill.PASSING, passingOld, age, trainingStage, participation));
        double scorer = Math.max(0.0, scorerOld - playerTrainingService.getDropAge(Skill.SCORING, age) + this.getSkillTraining(Skill.SCORING, scorerOld, age, trainingStage, participation));
        double setPieces = Math.max(0.0, setPiecesOld - playerTrainingService.getDropAge(Skill.SET_PIECES, age) + this.getSkillTraining(Skill.SET_PIECES, setPiecesOld, age, trainingStage, participation));

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

    private double getSkillTraining(Skill skill, double skillValue, int age, TrainingStage trainingStage, int participation) {
        if (participation == 0 || trainingStage.getTraining().getSkillCoefficients().stream().map(SkillCoefficient::getSkill).noneMatch(s -> s == skill)) {
            return 0.0;
        }
        return playerTrainingService.getSkillTraining(SkillTrainingRequest.builder()
            .skill(skillValue)
            .age(age)
            .coach(trainingStage.getCoach())
            .assistants(trainingStage.getAssistants())
            .intensity(trainingStage.getIntensity())
            .stamina(trainingStage.getStamina())
            .coefficientSkill(trainingStage.getTraining().getSkillCoefficients().stream().filter(sc -> sc.getSkill() == skill).findFirst().orElseThrow().getCoefficient())
            .minutes(90 * participation / 100)
            .build());
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
