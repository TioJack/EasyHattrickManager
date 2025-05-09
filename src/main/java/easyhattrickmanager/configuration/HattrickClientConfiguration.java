package easyhattrickmanager.configuration;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.oauth.OAuth10aService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EqualsAndHashCode(callSuper = true)
@Data
@Configuration
@ComponentScan(basePackages = "easyhattrickmanager")
public class HattrickClientConfiguration extends DefaultApi10a {

    public static final String URL = "http://chpp.hattrick.org/chppxml.ashx";

    private static final String OAUTH_SERVICE_HATTRICK_CLIENT = "oauthServiceHattrickClient";

    private final EhmConfiguration ehmConfiguration;

    @Bean(OAUTH_SERVICE_HATTRICK_CLIENT)
    public OAuth10aService oAuth10aService() {
        return new ServiceBuilder(ehmConfiguration.getOauthConsumerKey())
            .apiSecret(ehmConfiguration.getOauthConsumerSecret())
            .callback(ehmConfiguration.getRegisterCallbackUrl())
            .build(new HattrickClientConfiguration(ehmConfiguration));
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://chpp.hattrick.org/oauth/access_token.ashx";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://chpp.hattrick.org/oauth/authorize.aspx";
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "https://chpp.hattrick.org/oauth/request_token.ashx";
    }

}
