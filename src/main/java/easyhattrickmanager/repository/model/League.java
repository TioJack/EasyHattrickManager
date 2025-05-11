package easyhattrickmanager.repository.model;

import lombok.Builder;
import lombok.Data;
import java.time.ZonedDateTime;

@Data
@Builder
public class League {

    int id;
    String name;
    String englishName;
    int season;
    int seasonOffset;
    ZonedDateTime trainingDate;
}