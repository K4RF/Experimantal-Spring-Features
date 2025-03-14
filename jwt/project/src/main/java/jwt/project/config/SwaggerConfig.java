package jwt.project.config;

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
    public GroupedOpenApi boardGroupedOpenApi() {
        return GroupedOpenApi
                .builder()
                .group("jwt") // group 설정 (API들을 그룹화시켜 그룹에 속한 API들만 확인할 수 있도록 도와줌)
                .pathsToMatch("/api/**") // group에 포함될 API endpoint 경로
                .addOpenApiCustomizer(openApi ->
                        openApi.setInfo(new Info()
                                .title("base project api") // API 제목
                                .description("추가 기능 개발을 위한 API") // API 설명
                                .version("1.0.0") // API 버전
                        )
                )
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        // SecurityScheme 설정 (JWT Bearer Token)
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .scheme("Bearer")
                .bearerFormat("JWT");

        // SecurityRequirement 설정 (Bearer Token 사용)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Token");

        // OpenAPI 구성 반환
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Bearer Token", apiKey)) // SecurityScheme 등록
                .addSecurityItem(securityRequirement) // SecurityRequirement 추가
                .info(new Info()
                        .title("Base Project api") // API 제목
                        .description("추가 기능 개발을 위한 API") // API 설명
                        .version("1.0.0") // API 버전
                );
    }
}