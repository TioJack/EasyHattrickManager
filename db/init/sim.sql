-- Local storage for simulator calibration. Browser credentials are never stored.
CREATE DATABASE IF NOT EXISTS sim CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
USE sim;

CREATE TABLE schema_version (
    version INT NOT NULL PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
INSERT INTO schema_version (version) VALUES (1);

-- A batch is only an organizational label. Every simulation parameter belongs
-- to the exact request JSON in calibration_probe.
CREATE TABLE calibration_batch (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    metadata_json JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_calibration_batch_name (name)
) ENGINE=InnoDB;

-- The one and only simulation queue: single-player sweeps and full XIs both
-- carry a complete, sanitised API request template here.
CREATE TABLE calibration_probe (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    request_template_json JSON NOT NULL,
    request_template_sha256 CHAR(64) NOT NULL,
    metadata_json JSON NULL,
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    attempt_count INT UNSIGNED NOT NULL DEFAULT 0,
    error_message VARCHAR(1000) NULL,
    started_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_calibration_probe_batch FOREIGN KEY (batch_id) REFERENCES calibration_batch(id),
    UNIQUE KEY uq_calibration_probe_template (batch_id, request_template_sha256),
    KEY ix_calibration_probe_queue (status, id)
) ENGINE=InnoDB;

CREATE TABLE calibration_request (
    probe_id BIGINT NOT NULL PRIMARY KEY,
    request_json JSON NOT NULL,
    request_sha256 CHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_calibration_request_probe FOREIGN KEY (probe_id) REFERENCES calibration_probe(id),
    KEY ix_calibration_request_hash (request_sha256)
) ENGINE=InnoDB;

CREATE TABLE calibration_response (
    probe_id BIGINT NOT NULL PRIMARY KEY,
    response_json JSON NOT NULL,
    response_sha256 CHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_calibration_response_probe FOREIGN KEY (probe_id) REFERENCES calibration_probe(id),
    KEY ix_calibration_response_hash (response_sha256)
) ENGINE=InnoDB;

CREATE TABLE calibration_prediction (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    probe_id BIGINT NOT NULL,
    minute SMALLINT UNSIGNED NOT NULL,
    second SMALLINT UNSIGNED NOT NULL,
    tactic_rating DECIMAL(12,6) NOT NULL,
    ht_rating DECIMAL(12,6) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_calibration_prediction_probe FOREIGN KEY (probe_id) REFERENCES calibration_probe(id),
    UNIQUE KEY uq_calibration_prediction_time (probe_id, minute, second),
    KEY ix_calibration_prediction_probe (probe_id)
) ENGINE=InnoDB;

CREATE TABLE calibration_sector_rating (
    prediction_id BIGINT NOT NULL,
    sector_id TINYINT UNSIGNED NOT NULL,
    rating INT NOT NULL,
    PRIMARY KEY (prediction_id, sector_id),
    CONSTRAINT fk_calibration_sector_prediction FOREIGN KEY (prediction_id) REFERENCES calibration_prediction(id)
) ENGINE=InnoDB;

CREATE TABLE calibration_player_rating (
    prediction_id BIGINT NOT NULL,
    player_slot TINYINT UNSIGNED NOT NULL,
    player_id BIGINT NOT NULL DEFAULT 0,
    position_id SMALLINT UNSIGNED NOT NULL,
    stars DECIMAL(5,2) NOT NULL,
    stamina DECIMAL(18,15) NOT NULL,
    PRIMARY KEY (prediction_id, player_slot),
    CONSTRAINT fk_calibration_player_prediction FOREIGN KEY (prediction_id) REFERENCES calibration_prediction(id)
) ENGINE=InnoDB;
