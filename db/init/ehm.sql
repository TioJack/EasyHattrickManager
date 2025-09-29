CREATE DATABASE IF NOT EXISTS ehm CHARACTER SET = 'utf8' COLLATE = 'utf8_general_ci';

CREATE USER IF NOT EXISTS 'ehm'@'%' IDENTIFIED BY 'ehm';
GRANT ALL PRIVILEGES ON ehm.* TO 'ehm'@'%';
FLUSH PRIVILEGES;

USE ehm;

CREATE TABLE IF NOT EXISTS `user` (
    `id` int(11) NOT NULL,
    `name` varchar(50) NOT NULL,
    `language_id` int(11) NOT NULL,
    `country_id` int(11) NOT NULL,
    `currency` varchar(50) NOT NULL,
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
    team_id INT NOT NULL,
    nickName VARCHAR(255) NULL,
    player_number INT,
    age INT NOT NULL,
    age_days INT NOT NULL,
    arrival_date DATETIME NULL,
    owner_notes VARCHAR(500) NULL,
    TSI INT NOT NULL,
    player_form INT NOT NULL,
    statement VARCHAR(255) NULL,
    experience INT NOT NULL,
    loyalty INT NOT NULL,
    mother_club_bonus BOOLEAN NOT NULL,
    leadership INT NOT NULL,
    salary INT NOT NULL,
    abroad BOOLEAN NOT NULL,
    league_goals INT NOT NULL,
    cup_goals INT NOT NULL,
    friendlies_goals INT NOT NULL,
    career_goals INT NOT NULL,
    career_hattricks INT NOT NULL,
    matches_current_team INT NOT NULL,
    goals_current_team INT NOT NULL,
    assists_current_team INT NOT NULL,
    career_assists INT NOT NULL,
    transfer_listed BOOLEAN NOT NULL,
    national_team_id INT NOT NULL,
    caps INT NOT NULL,
    caps_u21 INT NOT NULL,
    cards INT NOT NULL,
    injury_level INT NOT NULL,
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
    player_category_id INT NOT NULL,
    PRIMARY KEY (`id`, `season_week`, `team_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS player_training (
    id INT NOT NULL,
    season_week VARCHAR(7) NOT NULL,
    team_id INT NOT NULL,
    keeper DOUBLE NOT NULL,
    defender DOUBLE NOT NULL,
    playmaker DOUBLE NOT NULL,
    winger DOUBLE NOT NULL,
    passing DOUBLE NOT NULL,
    scorer DOUBLE NOT NULL,
    set_pieces DOUBLE NOT NULL,
    PRIMARY KEY (id, season_week, team_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `training` (
    season_week VARCHAR(7) NOT NULL,
    team_id INT NOT NULL,
    training_type INT NOT NULL,
    training_level INT NOT NULL,
    stamina_training_part INT NOT NULL,
    PRIMARY KEY (`season_week`, `team_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `trainer` (
    season_week VARCHAR(7) NOT NULL,
    team_id INT NOT NULL,
    id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type INT NOT NULL,
    leadership INT NOT NULL,
    skill_level INT NOT NULL,
    status INT NOT NULL,
    start_date DATETIME NOT NULL,
    cost INT NOT NULL,
    PRIMARY KEY (`season_week`, `team_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `staff_member` (
    season_week VARCHAR(7) NOT NULL,
    team_id INT NOT NULL,
    id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type INT NOT NULL,
    level INT NOT NULL,
    hof_player_id INT NOT NULL,
    start_date DATETIME NOT NULL,
    cost INT NOT NULL,
    PRIMARY KEY (`season_week`, `team_id`, `id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `league` (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    english_name VARCHAR(255),
    season INT NOT NULL,
    seasonOffset INT NOT NULL,
    training_date DATETIME NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `country` (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(2) NOT NULL,
    currency_name VARCHAR(10) NOT NULL,
    currency_rate DECIMAL(10, 4) NOT NULL,
    date_format VARCHAR(20) NOT NULL,
    time_format VARCHAR(20) NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `league_country` (
    league_id INT PRIMARY KEY,
    country_id INT NOT NULL,
    CONSTRAINT `league_country_ibfk_1` FOREIGN KEY (`league_id`) REFERENCES `league` (`id`),
    CONSTRAINT `league_country_ibfk_2` FOREIGN KEY (`country_id`) REFERENCES `country` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `language` (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    friendly_name VARCHAR(255)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `translation` (
    language_id INT NOT NULL,
    `key` VARCHAR(255) NOT NULL,
    `value` TEXT NOT NULL,
    PRIMARY KEY (`language_id`, `key`),
    CONSTRAINT `translation_ibfk_1` FOREIGN KEY (`language_id`) REFERENCES `language` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `update_execution` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    team_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ERROR, OK
    retries INT NOT NULL DEFAULT 0,
    error_message TEXT,
    execution_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_team_execution_time (team_id, execution_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_config (
    user_id INT PRIMARY KEY,
    config JSON NOT NULL,
    CONSTRAINT `user_config_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
