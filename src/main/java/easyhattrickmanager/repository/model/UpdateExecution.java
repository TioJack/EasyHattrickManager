package easyhattrickmanager.repository.model;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateExecution {

    private int id;
    private int teamId;
    private String status; // PENDING, ERROR, OK
    private int retries;
    private String errorMessage;
    private ZonedDateTime executionTime;
}