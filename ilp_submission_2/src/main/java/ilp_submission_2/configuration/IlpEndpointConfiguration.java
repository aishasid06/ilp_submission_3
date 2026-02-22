package ilp_submission_2.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IlpEndpointConfiguration {
    @Value("${ilp.service.url:https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/}")
    private String defaultIlpEndpoint;

    @Bean
    public String getIlpEndpoint() {
        return System.getenv().getOrDefault("ILP_ENDPOINT",  defaultIlpEndpoint);
    }
}
