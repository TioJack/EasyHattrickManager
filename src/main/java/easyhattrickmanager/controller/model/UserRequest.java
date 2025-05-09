package easyhattrickmanager.controller.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequest {

    String username;
    String password;
}
