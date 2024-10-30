package com.hadiubaidillah.s3.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "My API",
        version = "1.0",
        description = "Documentation for My API"
))
public class OpenAPIConfig {
    // Additional configuration if needed
}
