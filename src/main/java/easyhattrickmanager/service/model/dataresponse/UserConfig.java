package easyhattrickmanager.service.model.dataresponse;

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
}
