package com.example.coda.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig
{
   @Bean
   public OpenAPI codaOpenAPI()
   {
      return new OpenAPI().info(
            new Info().title("CODA Generator API").description("Generate Belgian CODA-style statements").version(
                  "1.2.0")).externalDocs(new ExternalDocumentation().description("Docs").url("https://example.com"));
   }
}
