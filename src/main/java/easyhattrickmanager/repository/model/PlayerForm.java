package easyhattrickmanager.repository.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerForm {

    int id;
    String seasonWeek;
    int teamId;
    double form;
    double hiddenForm;
    double expectedForm;
}
