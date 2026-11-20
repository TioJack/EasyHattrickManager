package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import easyhattrickmanager.service.model.dataresponse.PlayerExpInfo;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.teamtraining.BestFormationCriteria;
import easyhattrickmanager.service.model.teamtraining.Formation;
import easyhattrickmanager.service.model.teamtraining.Lineup;
import easyhattrickmanager.service.model.teamtraining.LineupPlayer;
import easyhattrickmanager.service.model.teamtraining.MatchDetail;
import easyhattrickmanager.service.model.teamtraining.Position;
import easyhattrickmanager.service.model.teamtraining.SideMatch;
import easyhattrickmanager.service.model.teamtraining.Tactic;
import easyhattrickmanager.service.model.teamtraining.TeamAttitude;
import easyhattrickmanager.service.model.teamtraining.TeamConfidence;
import easyhattrickmanager.service.model.teamtraining.TeamSpirit;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RatingExperienceSublevelTest {

    private final RatingService ratingService = new RatingService();
    private final PlayerRatingService playerRatingService = new PlayerRatingService();
    private final MatchDetail match = MatchDetail.builder().tactic(Tactic.NORMAL).teamAttitude(TeamAttitude.PIN)
        .teamSpirit(TeamSpirit.CALM).teamSubSpirit(0.5).teamConfidence(TeamConfidence.STRONG)
        .teamSubConfidence(0.5).sideMatch(SideMatch.AWAY).build();

    @Test
    void playerRatingsIncreaseWithinTheSameExperienceLevel() {
        double integer = midfield(player(1, 4, null));
        double lowSublevel = midfield(player(1, 4, 4.1));
        double highSublevel = midfield(player(1, 4, 4.9));
        double nextLevel = midfield(player(1, 5, null));

        assertTrue(integer < lowSublevel);
        assertTrue(lowSublevel < highSublevel);
        assertTrue(highSublevel < nextLevel);
    }

    @Test
    void teamRatingsIncludeExperienceSublevels() {
        var integer = ratingService.getRatings(lineup(player(1, 1, null)));
        var withSublevel = ratingService.getRatings(lineup(player(1, 1, 1.99)));

        assertTrue(withSublevel.getHatStats() > integer.getHatStats());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4, 19})
    void missingSublevelUsesTheIntegerExperience(int experience) {
        PlayerInfo withoutSublevel = player(1, experience, null);
        PlayerInfo withZeroSublevel = player(1, experience, (double) experience);

        assertEquals(playerRatingService.getRatings(withZeroSublevel, match),
            playerRatingService.getRatings(withoutSublevel, match));
        assertEquals(ratingService.getRatings(lineup(withZeroSublevel)),
            ratingService.getRatings(lineup(withoutSublevel)));
    }

    @Test
    void lineupSelectionPrefersThePlayerWithHigherExperienceSublevel() {
        var service = new FormationRatingService(playerRatingService, ratingService);
        List<PlayerInfo> players = IntStream.rangeClosed(1, 12)
            .mapToObj(id -> player(id, 4, id == 12 ? 4.9 : 4.1)).toList();

        var selected = service.getRatings(players, match, BestFormationCriteria.MIDFIELD, Formation.F_3_5_2_A);

        assertEquals(11, selected.getPlayers().size());
        assertTrue(selected.getPlayers().stream().anyMatch(p -> p.getPlayerId() == 12));
    }

    private double midfield(PlayerInfo player) {
        return playerRatingService.getRatings(player, match).stream()
            .filter(rating -> rating.getPosition() == Position.MIM).findFirst().orElseThrow()
            .getRating().getMidfield();
    }

    private PlayerInfo player(int id, int experience, Double detailedExperience) {
        return PlayerInfo.builder().id(id).experience(experience)
            .playerExpInfo(detailedExperience == null ? null : PlayerExpInfo.builder().exp(detailedExperience).build())
            .playerForm(8).loyalty(20).staminaSkill(8).keeperSkill(5).defenderSkill(5)
            .playmakerSkill(5).wingerSkill(5).passingSkill(5).scorerSkill(5).setPiecesSkill(5).build();
    }

    private Lineup lineup(PlayerInfo template) {
        List<Position> positions = List.of(Position.KP, Position.RB, Position.RCD, Position.LCD, Position.LB,
            Position.RW, Position.RIM, Position.LIM, Position.LW, Position.RFW, Position.LFW);
        List<LineupPlayer> players = IntStream.range(0, positions.size()).mapToObj(index -> {
            PlayerInfo player = player(index + 1, template.getExperience(),
                template.getPlayerExpInfo() == null ? null : template.getPlayerExpInfo().getExp());
            Position position = positions.get(index);
            return LineupPlayer.builder().player(player).role(position.getRole()).behaviour(position.getBehaviour()).build();
        }).toList();
        return Lineup.builder().fieldPlayers(players).matchDetail(match).build();
    }
}
