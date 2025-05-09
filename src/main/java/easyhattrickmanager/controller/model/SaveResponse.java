package easyhattrickmanager.controller.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaveResponse {

    String username;
    List<String> teams;
}
