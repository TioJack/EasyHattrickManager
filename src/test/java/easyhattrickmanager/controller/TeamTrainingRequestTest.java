package easyhattrickmanager.controller;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import easyhattrickmanager.service.CustomPlayerService;
import easyhattrickmanager.service.DataService;
import easyhattrickmanager.service.FormationRatingService;
import easyhattrickmanager.service.PlayerDataService;
import easyhattrickmanager.service.PlayerTrainingService;
import easyhattrickmanager.service.TSIFormService;
import easyhattrickmanager.service.TeamTrainingFollowUpService;
import easyhattrickmanager.service.TeamTrainingService;
import easyhattrickmanager.service.UpdateService;
import easyhattrickmanager.service.WageService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TeamTrainingRequestTest {

    @Test
    void acceptsExperienceSublevelInTrainingAndProgressRequests() throws Exception {
        var mapper = Jackson2ObjectMapperBuilder.json().build();
        var tsi = new TSIFormService();
        var service = new TeamTrainingService(mapper, new PlayerTrainingService(null, null, null, tsi),
            new WageService(), tsi, mock(FormationRatingService.class));
        var controller = new DataController(mock(DataService.class), mock(UpdateService.class),
            mock(PlayerDataService.class), service, mock(TeamTrainingFollowUpService.class),
            mock(CustomPlayerService.class));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        String request = """
            {
              "iniWeek": {"season":84,"week":1,"date":"2026-11-13T00:00:00+01:00"},
              "players": [{
                "player": {
                  "id":488172024,"age":23,"ageDays":72,"salary":330700,
                  "playerForm":6,"experience":4,"playerExpInfo":{"exp":4.742016666666668},
                  "staminaSkill":7,"keeperSkill":1,"defenderSkill":6,"playmakerSkill":14,
                  "wingerSkill":17,"passingSkill":5,"scorerSkill":5,"setPiecesSkill":2
                },
                "inclusionWeek":1,"departureWeek":null
              }],
              "stages": [{"id":1,"duration":16,"coach":5,"assistants":10,
                "intensity":100,"stamina":10,"training":"DEFENDING"}],
              "participations": [{"stageId":1,"playerId":488172024,"participation":100}],
              "calculateBestFormation":false
            }
            """;

        mvc.perform(post("/data/teamTraining/progress").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalWeeks").value(16))
            .andExpect(jsonPath("$.done").value(false));
        mvc.perform(post("/data/teamTraining").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.weekPlayers.16[0].experience").value(4))
            .andExpect(jsonPath("$.weekPlayers.16[0].playerExpInfo.exp").value(4.742016666666668));
        mvc.perform(post("/data/teamTraining/progress").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.calculatedWeeks").value(16))
            .andExpect(jsonPath("$.done").value(true));
    }
}
