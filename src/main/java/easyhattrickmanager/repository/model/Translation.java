package easyhattrickmanager.repository.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Translation {

    int languageId;
    String key;
    String value;
}