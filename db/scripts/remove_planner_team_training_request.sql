DELIMITER //

DROP PROCEDURE IF EXISTS remove_planner_team_training_request//

CREATE PROCEDURE remove_planner_team_training_request()
BEGIN
    DECLARE project_index INT DEFAULT 0;
    DECLARE max_projects INT DEFAULT 0;

    SELECT GREATEST(
        COALESCE((SELECT MAX(JSON_LENGTH(JSON_EXTRACT(config, '$.projects'))) FROM user_planner), 0),
        COALESCE((SELECT MAX(JSON_LENGTH(JSON_EXTRACT(config, '$.projects'))) FROM user_config), 0)
    )
    INTO max_projects;

    WHILE project_index < max_projects DO
        UPDATE user_planner
        SET config = JSON_REMOVE(config, CONCAT('$.projects[', project_index, '].planner.teamTrainingRequest'))
        WHERE JSON_CONTAINS_PATH(config, 'one', CONCAT('$.projects[', project_index, '].planner.teamTrainingRequest'));

        UPDATE user_config
        SET config = JSON_REMOVE(config, CONCAT('$.projects[', project_index, '].planner.teamTrainingRequest'))
        WHERE JSON_CONTAINS_PATH(config, 'one', CONCAT('$.projects[', project_index, '].planner.teamTrainingRequest'));

        SET project_index = project_index + 1;
    END WHILE;
END//

DELIMITER ;

CALL remove_planner_team_training_request();

DROP PROCEDURE IF EXISTS remove_planner_team_training_request;
