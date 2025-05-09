CREATE DATABASE IF NOT EXISTS ehm CHARACTER SET = 'utf8' COLLATE = 'utf8_general_ci';

CREATE USER IF NOT EXISTS 'ehm'@'%' IDENTIFIED BY 'ehm';
GRANT ALL PRIVILEGES ON ehm.* TO 'ehm'@'%';
FLUSH PRIVILEGES;

USE ehm;

CREATE TABLE IF NOT EXISTS `user` (
    `id` int(11) NOT NULL,
    `name` varchar(50) NOT NULL,
    `language_id` int(11) NOT NULL,
    `activation_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `token` varchar(255) NOT NULL,
    `token_secret` varchar(255) NOT NULL,
    `active` tinyint(1) NOT NULL DEFAULT '1',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `user_ehm` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `username` varchar(50) NOT NULL,
    `password` varchar(255) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `user_ehm_history` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_ehm_id` int(11) NOT NULL,
    `connection_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip_address` varchar(45) DEFAULT NULL,
    `user_agent` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `user_ehm_id` (`user_ehm_id`),
    CONSTRAINT `user_ehm_history_ibfk_1` FOREIGN KEY (`user_ehm_id`) REFERENCES `user_ehm` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `team` (
    `user_id` int(11) NOT NULL,
    `id` int(11) NOT NULL,
    `name` varchar(50) NOT NULL,
    `primary_club` tinyint(1) NOT NULL,
    `founded_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `closure_date` datetime DEFAULT NULL,
    `league_id` int(11) NOT NULL,
    `country_id` int(11) NOT NULL,
    `bot` tinyint(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`user_id`,`id`),
    CONSTRAINT `team_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `user_ehm_user` (
    `user_ehm_id` int(11) NOT NULL,
    `user_id` int(11) NOT NULL,
    PRIMARY KEY (`user_ehm_id`,`user_id`),
    KEY `user_id` (`user_id`),
    CONSTRAINT `user_ehm_user_ibfk_1` FOREIGN KEY (`user_ehm_id`) REFERENCES `user_ehm` (`id`),
    CONSTRAINT `user_ehm_user_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `player` (
    id INT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    agreeability INT NOT NULL,
    aggressiveness INT NOT NULL,
    honesty INT NOT NULL,
    specialty INT NOT NULL,
    country_id INT NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `player_data` (
    id INT,
    season_week VARCHAR(7) NOT NULL,
    date DATETIME NOT NULL,
    team_id INT NOT NULL,
    nickName VARCHAR(255) NULL,
    player_number INT,
    age INT NOT NULL,
    age_days INT NOT NULL,
    TSI INT NOT NULL,
    player_form INT NOT NULL,
    experience INT NOT NULL,
    loyalty INT NOT NULL,
    mother_club_bonus BOOLEAN NOT NULL,
    leadership INT NOT NULL,
    salary INT NOT NULL,
    injury_level FLOAT NOT NULL,
    stamina_skill INT NOT NULL,
    keeper_skill INT NOT NULL,
    playmaker_skill INT NOT NULL,
    scorer_skill INT NOT NULL,
    passing_skill INT NOT NULL,
    winger_skill INT NOT NULL,
    defender_skill INT NOT NULL,
    set_pieces_skill INT NOT NULL,
    htms INT NOT NULL,
    htms28 INT NOT NULL,
    PRIMARY KEY (`id`, `season_week`, `team_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `training` (
    season_week VARCHAR(7) NOT NULL,
    date DATETIME NOT NULL,
    team_id INT NOT NULL,
    training_type INT NOT NULL,
    training_level INT NOT NULL,
    stamina_training_part INT NOT NULL,
    PRIMARY KEY (`season_week`, `team_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `staff` (
    season_week VARCHAR(7) NOT NULL,
    date DATETIME NOT NULL,
    team_id INT NOT NULL,
    trainer_id INT NOT NULL,
    trainer_name VARCHAR(255) NOT NULL,
    trainer_type INT NOT NULL,
    trainer_leadership INT NOT NULL,
    trainer_skill_level INT NOT NULL,
    trainer_status INT NOT NULL,
    staff1_id INT,
    staff1_name VARCHAR(255),
    staff1_type INT,
    staff1_level INT,
    staff1_hof_player_id INT,
    staff2_id INT,
    staff2_name VARCHAR(255),
    staff2_type INT,
    staff2_level INT,
    staff2_hof_player_id INT,
    staff3_id INT,
    staff3_name VARCHAR(255),
    staff3_type INT,
    staff3_level INT,
    staff3_hof_player_id INT,
    staff4_id INT,
    staff4_name VARCHAR(255),
    staff4_type INT,
    staff4_level INT,
    staff4_hof_player_id INT,
    PRIMARY KEY (`season_week`, `team_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `league` (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    english_name VARCHAR(255),
    season INT NOT NULL,
    seasonOffset INT NOT NULL,
    training_date DATETIME NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;