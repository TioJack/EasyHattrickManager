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
public class ProjectInfo {

    String name;
    int teamId;
    int iniSeason;
    int iniWeek;
    Integer endSeason;
    Integer endWeek;
    PlayerFilterInfo filter;
    PlayerSortInfo sort;
}
