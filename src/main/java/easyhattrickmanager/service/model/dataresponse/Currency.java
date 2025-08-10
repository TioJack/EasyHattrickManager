package easyhattrickmanager.service.model.dataresponse;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Currency {

    String currencyCode;
    BigDecimal currencyRate;
}
