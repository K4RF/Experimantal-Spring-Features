package monitoring.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi boardGroupedOpenApi(){
        return GroupedOpenApi
                .builder()
                .group("monitoring")
                .pathsToMatch("/api/**")
                .addOpenApiCustomizer(openApi ->
                        openApi.setInfo(new Info()
                                .title("base project api")
                                .description("추가 기능 개발을 위한 API")
                                .version("1.0.0")
                        )
                ).build();
    }

    @Bean
    public OpenAPI openApi(){
        // SecurityScheme 설정
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .scheme("Bearer")
                .bearerFormat("JWT");

        // SecurityRequirement 설정 (Bearer Token 사용)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Token");

        // OpenAPI 구성 변환
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Bearer Token", apiKey))
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("Base Project api")
                        .description("추가 기능 개발을 위한 API")
                        .version("1.0.0"));
    }
}
