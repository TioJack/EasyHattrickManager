-- A request/response hash is an index for diagnostics, not a unique key:
-- distinct probes can legitimately produce identical JSON.
USE sim;

ALTER TABLE calibration_request
    DROP INDEX uq_calibration_request_hash,
    ADD KEY ix_calibration_request_hash (request_sha256);

ALTER TABLE calibration_response
    DROP INDEX uq_calibration_response_hash,
    ADD KEY ix_calibration_response_hash (response_sha256);
