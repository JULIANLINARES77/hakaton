package uniminuto.campusreporta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI campusReportaOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("CampusReporta API")
                .description("API para registrar daños y consultar su atención en los campus UNIMINUTO.")
                .version("v1")
                .contact(new Contact().name("Equipo CampusReporta")));
    }
}
