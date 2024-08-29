package com.blog.application.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebMvc
public class SwaggerConfig implements WebMvcConfigurer {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.blog.application.blog"))
                .paths(PathSelectors.regex("/.*"))
                .build()
                .apiInfo(apiInfo())
                .securitySchemes(Arrays.asList(apiKey()))
                .securityContexts(Arrays.asList(securityContext()));

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Blog Application API")
                .description("API documentation for the Blog application")
                .version("1.0.0")
                .build();
    }
    private ApiKey apiKey() {
        return new ApiKey("authkey", "Authorization", "header");
    }
    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(SecurityReference.builder()
                        .reference("JWT")
                        .scopes(new AuthorizationScope[0])
                        .build()))
                .build();
    }
}
