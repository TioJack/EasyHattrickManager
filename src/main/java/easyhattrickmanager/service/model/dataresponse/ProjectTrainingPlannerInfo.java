package easyhattrickmanager.service.model.dataresponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingRequest;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingResponse;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectTrainingPlannerInfo {

    Integer iniSeason;
    Integer iniWeek;
    PlayerFilterInfo filter;
    PlayerSortInfo sort;
    List<ProjectTrainingStageInfo> trainingPlans;
    Map<Integer, List<Integer>> trainingPlanPercents;
    Boolean autoRefreshBestFormation;
    String bestFormationCriteria;
    String fixedFormationCode;
    ProjectTrainingMatchDetailInfo matchDetail;
    TeamTrainingRequest teamTrainingRequest;
    TeamTrainingResponse teamTrainingResponse;
}
