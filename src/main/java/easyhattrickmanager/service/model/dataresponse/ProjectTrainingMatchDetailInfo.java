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
public class ProjectTrainingMatchDetailInfo {

    String tactic;
    String teamAttitude;
    String teamSpirit;
    Double teamSubSpirit;
    String teamConfidence;
    Double teamSubConfidence;
    String sideMatch;
    Integer styleOfPlay;
}
