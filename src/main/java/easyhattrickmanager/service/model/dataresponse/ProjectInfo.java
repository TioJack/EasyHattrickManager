package easyhattrickmanager.service.model.dataresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInfo {

    String name;
    int teamId;
    int iniSeason;
    int iniWeek;
    Integer endSeason;
    Integer endWeek;
}
