package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.PlayerSubSkillInfo;
import easyhattrickmanager.service.model.playertraining.Training;
import easyhattrickmanager.service.model.teamtraining.StagePlayerParticipation;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingPlayer;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingRequest;
import easyhattrickmanager.service.model.teamtraining.TrainingStage;
import easyhattrickmanager.service.model.teamtraining.WeekInfo;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class TeamTrainingSetPiecesTest {

    @Test
    void appliesTheExactBonusToFourSlotsAndPreservesPartialParticipation() {
        TSIFormService tsi = new TSIFormService();
        TeamTrainingService service = new TeamTrainingService(
            Jackson2ObjectMapperBuilder.json().build(), new PlayerTrainingService(null, null, null, tsi),
            new WageService(), tsi, mock(FormationRatingService.class));
        List<Integer> percentages = List.of(125, 125, 125, 125, 100, 0, 33);
        TeamTrainingRequest request = TeamTrainingRequest.builder()
            .iniWeek(WeekInfo.builder().season(90).week(3).date(ZonedDateTime.parse("2026-11-17T12:00:00Z")).build())
            .players(IntStream.range(0, percentages.size()).mapToObj(index -> TeamTrainingPlayer.builder()
                .player(player(index + 1)).inclusionWeek(1).build()).toList())
            .stages(List.of(TrainingStage.builder().id(1).duration(1).coach(5).assistants(10)
                .intensity(100).stamina(10).training(Training.SET_PIECES).build()))
            .participations(IntStream.range(0, percentages.size()).mapToObj(index -> StagePlayerParticipation.builder()
                .stageId(1).playerId(index + 1).participation(percentages.get(index)).build()).toList())
            .calculateBestFormation(false).build();

        List<PlayerInfo> results = service.getTeamTraining(request).getWeekPlayers().get(1);
        PlayerInfo standard = findPlayer(results, 5);
        PlayerInfo untrained = findPlayer(results, 6);
        double untrainedSkill = setPieces(untrained);
        double standardGain = setPieces(standard) - untrainedSkill;
        assertTrue(standardGain > 0);
        for (int id = 1; id <= 4; id++) {
            PlayerInfo boosted = findPlayer(results, id);
            assertEquals(standardGain * 1.25, setPieces(boosted) - untrainedSkill, 1e-9);
            assertEquals(standard.getStaminaSkill(), boosted.getStaminaSkill());
            assertEquals(standard.getPlayerSubSkill().getStamina(), boosted.getPlayerSubSkill().getStamina(), 1e-9);
            assertEquals(standard.getScorerSkill(), boosted.getScorerSkill());
            assertEquals(standard.getPlayerSubSkill().getScorer(), boosted.getPlayerSubSkill().getScorer(), 1e-9);
        }
        assertEquals(standardGain * 0.33, setPieces(findPlayer(results, 7)) - untrainedSkill, 1e-9);
    }

    private PlayerInfo player(int id) {
        return PlayerInfo.builder().id(id).firstName("Player").age(17).ageDays(50).salary(1000)
            .playerForm(7).experience(1).loyalty(1).staminaSkill(5).keeperSkill(1).defenderSkill(5)
            .playmakerSkill(5).wingerSkill(5).passingSkill(5).scorerSkill(5).setPiecesSkill(12)
            .playerSubSkill(PlayerSubSkillInfo.builder().setPieces(0.25).build()).build();
    }

    private PlayerInfo findPlayer(List<PlayerInfo> players, int id) {
        return players.stream().filter(player -> player.getId() == id).findFirst().orElseThrow();
    }

    private double setPieces(PlayerInfo player) {
        return player.getSetPiecesSkill() + player.getPlayerSubSkill().getSetPieces();
    }
}
