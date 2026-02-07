package easyhattrickmanager.service.model.dataresponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerFormInfo {

    double form;
    double hiddenForm;
    double expectedForm;
}
