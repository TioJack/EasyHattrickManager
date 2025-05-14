package easyhattrickmanager.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ehm")
public class EhmConfiguration {

    private String oauthConsumerKey;
    private String oauthConsumerSecret;
    private String registerCallbackUrl;
    private String cronAddActiveUpdateExecutions;

}
