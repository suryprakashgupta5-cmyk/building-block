package com.block;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@SpringBootApplication
public class BuildingBlockApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuildingBlockApplication.class, args);
	}
	
	 @Bean
		OpenAPI baseoOpenAPI() {

			final String securitySchemeName = "bearerAuth";

			return new OpenAPI().addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
					.components(new Components().addSecuritySchemes(securitySchemeName,
							new SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer")
									.bearerFormat("JWT")))
					.info(new Info().title("Medflow-apis").version("1.6.12")
							.description("This Is Buildig-block doc Swagger"));

		}

}
