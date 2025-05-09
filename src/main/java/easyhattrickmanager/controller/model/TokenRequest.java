package easyhattrickmanager.controller.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRequest {

    String oauthToken;
    String oauthVerifier;
}
