package easyhattrickmanager.service.model;

import com.github.scribejava.core.model.OAuth1RequestToken;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginData {

    String username;
    String password;
    OAuth1RequestToken token;
}
