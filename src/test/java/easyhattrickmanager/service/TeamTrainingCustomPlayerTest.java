package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.playertraining.Training;
import easyhattrickmanager.service.model.teamtraining.*;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class TeamTrainingCustomPlayerTest {

    @Test
    void trainsCustomAndRealPlayersIdenticallyAndRespectsParticipation() {
        TSIFormService tsi = new TSIFormService();
        TeamTrainingService service = new TeamTrainingService(
            Jackson2ObjectMapperBuilder.json().build(), new PlayerTrainingService(null, null, null, tsi),
            new WageService(), tsi, mock(FormationRatingService.class));
        PlayerInfo real = player(123);
        PlayerInfo custom = player(-1);
        PlayerInfo untrained = player(-2);
        TeamTrainingRequest request = TeamTrainingRequest.builder()
            .iniWeek(WeekInfo.builder().season(90).week(3).date(ZonedDateTime.parse("2026-11-17T12:00:00Z")).build())
            .players(List.of(
                TeamTrainingPlayer.builder().player(real).inclusionWeek(1).build(),
                TeamTrainingPlayer.builder().player(custom).inclusionWeek(1).build(),
                TeamTrainingPlayer.builder().player(untrained).inclusionWeek(1).build()))
            .stages(List.of(TrainingStage.builder().id(1).duration(4).coach(5).assistants(10)
                .intensity(100).stamina(10).training(Training.SCORING).build()))
            .participations(List.of(
                StagePlayerParticipation.builder().stageId(1).playerId(123).participation(100).build(),
                StagePlayerParticipation.builder().stageId(1).playerId(-1).participation(100).build(),
                StagePlayerParticipation.builder().stageId(1).playerId(-2).participation(0).build()))
            .calculateBestFormation(false).build();

        TeamTrainingResponse response = service.getTeamTraining(request);
        assertEquals(4, response.getWeekPlayers().size());
        List<PlayerInfo> players = response.getWeekPlayers().get(4);
        PlayerInfo realResult = players.stream().filter(p -> p.getId() == 123).findFirst().orElseThrow();
        PlayerInfo customResult = players.stream().filter(p -> p.getId() == -1).findFirst().orElseThrow();
        PlayerInfo untrainedResult = players.stream().filter(p -> p.getId() == -2).findFirst().orElseThrow();
        assertEquals(realResult.getScorerSkill(), customResult.getScorerSkill());
        assertEquals(realResult.getPlayerSubSkill(), customResult.getPlayerSubSkill());
        assertEquals(realResult.getAge(), customResult.getAge());
        assertEquals(realResult.getAgeDays(), customResult.getAgeDays());
        assertTrue(customResult.getScorerSkill() + customResult.getPlayerSubSkill().getScorer()
            > untrainedResult.getScorerSkill() + untrainedResult.getPlayerSubSkill().getScorer());
    }

    private PlayerInfo player(int id) {
        return PlayerInfo.builder().id(id).firstName("Prospect").age(17).ageDays(100).salary(1000)
            .playerForm(7).experience(1).loyalty(1).staminaSkill(5).keeperSkill(1).defenderSkill(5)
            .playmakerSkill(5).wingerSkill(5).passingSkill(5).scorerSkill(5).setPiecesSkill(1).build();
    }
}
