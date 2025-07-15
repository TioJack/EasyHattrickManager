package easyhattrickmanager.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "assets")
public class AssetsConfiguration {

    private String hattrickUrl;
    private String assetsPath;
}
