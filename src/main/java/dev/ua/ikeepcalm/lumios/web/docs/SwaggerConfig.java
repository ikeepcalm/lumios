package dev.ua.ikeepcalm.lumios.web.docs;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@SecurityScheme(
        type = SecuritySchemeType.APIKEY,
        name = "bearerAuth",
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-KEY",
        description = "API Key for accessing the API"
)
public class SwaggerConfig {

    @Bean
    GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/**")
                .displayName("Lumios API")
//                .addOperationCustomizer((operation, handlerMethod) -> {
//                    operation.addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("Authorization"));
//                    return operation;
//                })
                .build();
    }

}