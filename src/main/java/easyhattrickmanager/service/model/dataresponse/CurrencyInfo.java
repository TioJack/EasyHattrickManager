package easyhattrickmanager.service.model.dataresponse;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyInfo {

    int countryId;
    String currencyName;
    String currencyCode;
    BigDecimal currencyRate;
}
