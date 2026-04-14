package easyhattrickmanager.service.model.dataresponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectTrainingStageInfo {

    Integer typeId;
    Integer weeks;
    Integer coach;
    Integer assistants;
    Integer intensity;
    Integer stamina;
}
