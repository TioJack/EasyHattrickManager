package easyhattrickmanager.repository.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Country {

    int id;
    String name;
    String code;
    String currencyName;
    BigDecimal currencyRate;
    String dateFormat;
    String timeFormat;
}