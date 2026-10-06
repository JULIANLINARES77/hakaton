package uniminuto.campusreporta.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OllamaProperties.class)
public class OllamaConfig {
    @Bean
    RestClient ollamaHttpClient(OllamaProperties properties) {
        return RestClient.builder().baseUrl(properties.getBaseUrl()).build();
    }
}
