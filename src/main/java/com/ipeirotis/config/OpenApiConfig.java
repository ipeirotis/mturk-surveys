package com.ipeirotis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI mturkSurveysOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("MTurk Demographics Survey API")
						.description("API for accessing Amazon Mechanical Turk worker demographics survey data. "
								+ "This survey has been running continuously since 2015, collecting demographic "
								+ "information from MTurk workers including gender, year of birth, education, "
								+ "income, languages spoken, and more.")
						.version("1.0")
						.contact(new Contact()
								.name("Panos Ipeirotis")
								.url("https://github.com/ipeirotis/mturk-surveys")));
	}
}
