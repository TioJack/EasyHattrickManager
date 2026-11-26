package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import easyhattrickmanager.controller.DataController;
import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.PlayerSubSkillInfo;
import easyhattrickmanager.utils.HTMSUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CustomPlayerServiceTest {

    private final WageService wage = new WageService();
    private final TSIFormService tsi = new TSIFormService();
    private final CustomPlayerService service = new CustomPlayerService(wage, tsi);

    @Test
    void supportsAPlayerStartingWithZeroSkillsAndCharacteristics() {
        PlayerInfo player = PlayerInfo.builder().id(-1).age(17).build();
        service.calculate(player);
        assertEquals(0, player.getTSI());
        assertEquals(0, player.getHtms());
        assertEquals(2500, player.getSalary());
        assertEquals(0, player.getPlayerSubSkill().getHtms());
        assertTrue(player.getHtms28() > 0);
    }

    @Test
    void endpointReturnsCalculatedStatsUsingTheFrontendJsonNamesAndRejectsRealPlayerIds() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DataController(null, null, null, null, null, service, null)).build();
        String body = """
            {"id":-1,"firstName":"jugador A","age":19,"ageDays":35,"playerForm":7,"specialty":1,
             "staminaSkill":5,"keeperSkill":1,"defenderSkill":6,"playmakerSkill":7,"wingerSkill":8,
             "passingSkill":9,"scorerSkill":10,"setPiecesSkill":11,"tsi":1,"salary":1,"htms":1}
            """;
        PlayerInfo expected = service.calculate(player());
        mvc.perform(post("/data/custom-player/calculate").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tsi").value(expected.getTSI()))
            .andExpect(jsonPath("$.salary").value(expected.getSalary()))
            .andExpect(jsonPath("$.htms").value(expected.getHtms()))
            .andExpect(jsonPath("$.htms28").value(expected.getHtms28()));
        mvc.perform(post("/data/custom-player/calculate").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.replace("\"id\":-1", "\"id\":123")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void calculatesStatsWithTheSameFormulaAndSubskillsAsTraining() {
        PlayerInfo player = player();
        player.setPlayerSubSkill(PlayerSubSkillInfo.builder().keeper(0.25).defender(0.5)
            .playmaker(0.75).winger(0.1).passing(0.2).scorer(0.3).setPieces(0.4).stamina(0.5).build());
        PlayerInfo result = service.calculate(player);
        HTMS whole = HTMSUtils.calculateHTMS(19, 35, 1, 6, 7, 8, 9, 10, 11);
        HTMS sub = HTMSUtils.calculateHTMS(19, 35, 1.25, 6.5, 7.75, 8.1, 9.2, 10.3, 11.4);
        assertEquals(whole.getHtms(), result.getHtms());
        assertEquals(whole.getHtms28(), result.getHtms28());
        assertEquals(sub.getHtms(), result.getPlayerSubSkill().getHtms());
        assertEquals(sub.getHtms28(), result.getPlayerSubSkill().getHtms28());
        assertEquals((int) tsi.calculateTSI(19, 7, 5.5, 1.25, 6.5, 7.75, 8.1, 9.2, 10.3), result.getTSI());
        assertEquals((int) wage.calculateWage(19, false, 1, 1.25, 6.5, 7.75, 8.1, 9.2, 10.3, 11.4), result.getSalary());
        assertTrue(result.getTSI() > 0);
        assertTrue(result.getSalary() > 0);
    }

    @Test
    void recalculatesAfterSkillsAgeAndFormChangeAndSupportsMissingSubskills() {
        PlayerInfo player = service.calculate(player());
        int beforeTSI = player.getTSI();
        int beforeSalary = player.getSalary();
        int beforeHTMS = player.getHtms();
        int beforeHTMS28 = player.getHtms28();
        player.setScorerSkill(15);
        service.calculate(player);
        assertTrue(player.getTSI() > beforeTSI);
        assertTrue(player.getSalary() > beforeSalary);
        assertTrue(player.getHtms() > beforeHTMS);
        assertTrue(player.getHtms28() > beforeHTMS28);
        beforeTSI = player.getTSI();
        beforeHTMS28 = player.getHtms28();
        player.setAge(33);
        player.setPlayerForm(4);
        service.calculate(player);
        assertTrue(player.getTSI() < beforeTSI);
        assertTrue(player.getHtms28() < beforeHTMS28);
    }

    @Test
    void validatesInputsBeforeUsingTheSkillTables() {
        PlayerInfo player = player();
        player.setKeeperSkill(21);
        assertThrows(IllegalArgumentException.class, () -> service.calculate(player));
        player.setKeeperSkill(1);
        player.setPlayerSubSkill(PlayerSubSkillInfo.builder().scorer(Double.NaN).build());
        assertThrows(IllegalArgumentException.class, () -> service.calculate(player));
        player.setPlayerSubSkill(null);
        player.setAgeDays(112);
        assertThrows(IllegalArgumentException.class, () -> service.calculate(player));
        player.setAgeDays(0);
        player.setId(123);
        assertThrows(IllegalArgumentException.class, () -> service.calculate(player));
    }

    private PlayerInfo player() {
        return PlayerInfo.builder().id(-1).firstName("jugador A").age(19).ageDays(35).playerForm(7).specialty(1)
            .staminaSkill(5).keeperSkill(1).defenderSkill(6).playmakerSkill(7).wingerSkill(8)
            .passingSkill(9).scorerSkill(10).setPiecesSkill(11).TSI(1).salary(1).htms(1).htms28(1).build();
    }
}
