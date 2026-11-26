package easyhattrickmanager.configuration;

import java.util.HashSet;
import java.util.Set;
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
    private String cronCheckData;
    private String cronCheckActive;
    private Set<Integer> privilegedTeamIds = new HashSet<>();

}
