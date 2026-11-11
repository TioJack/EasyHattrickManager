DELIMITER //

DROP PROCEDURE IF EXISTS add_index_if_missing//

CREATE PROCEDURE add_index_if_missing(
    IN table_name_param VARCHAR(64),
    IN index_name_param VARCHAR(64),
    IN alter_sql_param VARCHAR(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = table_name_param
          AND index_name = index_name_param
    ) THEN
        SET @alter_sql = alter_sql_param;
        PREPARE stmt FROM @alter_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DELIMITER ;

CALL add_index_if_missing('player_data', 'idx_player_data_team_week_id',
    'ALTER TABLE player_data ADD INDEX idx_player_data_team_week_id (team_id, season_week, id)');

CALL add_index_if_missing('player_subskill', 'idx_player_subskill_team_week_id',
    'ALTER TABLE player_subskill ADD INDEX idx_player_subskill_team_week_id (team_id, season_week, id)');

CALL add_index_if_missing('player_form', 'idx_player_form_team_week_id',
    'ALTER TABLE player_form ADD INDEX idx_player_form_team_week_id (team_id, season_week, id)');

CALL add_index_if_missing('player_training', 'idx_player_training_team_week_id',
    'ALTER TABLE player_training ADD INDEX idx_player_training_team_week_id (team_id, season_week, id)');

CALL add_index_if_missing('training', 'idx_training_team_week',
    'ALTER TABLE training ADD INDEX idx_training_team_week (team_id, season_week)');

CALL add_index_if_missing('trainer', 'idx_trainer_team_week',
    'ALTER TABLE trainer ADD INDEX idx_trainer_team_week (team_id, season_week)');

CALL add_index_if_missing('staff_member', 'idx_staff_member_team_week_id',
    'ALTER TABLE staff_member ADD INDEX idx_staff_member_team_week_id (team_id, season_week, id)');

DROP PROCEDURE IF EXISTS add_index_if_missing;
