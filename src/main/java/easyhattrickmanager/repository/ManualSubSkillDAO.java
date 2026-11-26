package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.PlayerSubSkill;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ManualSubSkillDAO {
    private final JdbcTemplate jdbc;

    public boolean lockPlayer(int teamId, String week, int playerId) {
        return !jdbc.queryForList("SELECT id FROM player_data WHERE team_id = ? AND season_week = ? AND id = ? FOR UPDATE",
            Integer.class, teamId, week, playerId).isEmpty();
    }

    public void saveSkills(PlayerSubSkill sub) {
        jdbc.update("""
            INSERT INTO player_subskill (id, season_week, team_id, stamina, keeper, defender, playmaker,
                winger, passing, scorer, set_pieces, htms, htms28) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE stamina = VALUES(stamina), keeper = VALUES(keeper), defender = VALUES(defender),
                playmaker = VALUES(playmaker), winger = VALUES(winger), passing = VALUES(passing),
                scorer = VALUES(scorer), set_pieces = VALUES(set_pieces), htms = VALUES(htms), htms28 = VALUES(htms28)
            """, sub.getId(), sub.getSeasonWeek(), sub.getTeamId(), sub.getStamina(), sub.getKeeper(),
            sub.getDefender(), sub.getPlaymaker(), sub.getWinger(), sub.getPassing(), sub.getScorer(),
            sub.getSetPieces(), sub.getHtms(), sub.getHtms28());
    }

    public void saveForm(int teamId, String week, int playerId, double form) {
        jdbc.update("""
            INSERT INTO player_form (id, season_week, team_id, form, hidden_form, expected_form)
            VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE form = VALUES(form)
            """, playerId, week, teamId, form, form, form);
    }

    public void saveExperience(int teamId, String week, int playerId, double exp) {
        jdbc.update("""
            INSERT INTO player_exp (id, season_week, team_id, exp) VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE exp = VALUES(exp)
            """, playerId, week, teamId, exp);
    }

    public void recordChange(int userId, int teamId, String week, int playerId, String skill, double oldValue, double newValue) {
        jdbc.update("""
            INSERT INTO player_subskill_manual_history
                (user_id, team_id, player_id, season_week, skill, old_value, new_value) VALUES (?, ?, ?, ?, ?, ?, ?)
            """, userId, teamId, playerId, week, skill, oldValue, newValue);
    }
}
