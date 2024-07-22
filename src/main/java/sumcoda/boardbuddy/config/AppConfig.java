package sumcoda.boardbuddy.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.enviroment}")
    private String enviroment;
}