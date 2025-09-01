package easyhattrickmanager.service.model.dataresponse;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfig {

    Integer languageId;
    CurrencyInfo currency;
    String dateFormat;
    List<ProjectInfo> projects;
}
