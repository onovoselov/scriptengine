package com.example.scriptengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(metaData())
            .securitySchemes(Collections.singletonList(new BasicAuth("basic")))
            .securityContexts(Collections.singletonList(securityContext()));
    }



    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex(  "/*"))
            .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList((new SecurityReference("Authorization", authorizationScopes)));
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
            .title("Script Engine REST API")
            .description("Spring Boot REST API for Online Store")
            .version("1.0.0")
            .license("Apache License Version 2.0")
            .build();
    }

    private SecurityScheme securityScheme() {
        GrantType grantType = new AuthorizationCodeGrantBuilder()
            .tokenEndpoint(new TokenEndpoint(  "/token", "oauthtoken"))
            .tokenRequestEndpoint(
                new TokenRequestEndpoint("/authorize", "a", "3"))
            .build();

        SecurityScheme oauth = new OAuthBuilder().name("spring_oauth")
            .grantTypes(Arrays.asList(grantType))
            .scopes(Arrays.asList(scopes()))
            .build();
        return oauth;
    }

//    private SecurityContext securityContext() {
//        return SecurityContext.builder()
//            .securityReferences(
//                Arrays.asList(new SecurityReference("spring_oauth", scopes())))
//            .forPaths(PathSelectors.regex("/foos.*"))
//            .build();
//    }


    private AuthorizationScope[] scopes() {
        AuthorizationScope[] scopes = {
            new AuthorizationScope("read", "for read operations"),
            new AuthorizationScope("write", "for write operations"),
            new AuthorizationScope("foo", "Access foo API") };
        return scopes;
    }

//    @Bean
//    public SecurityConfiguration security() {
//        return SecurityConfigurationBuilder.builder()
//            .clientId(CLIENT_ID)
//            .clientSecret(CLIENT_SECRET)
//            .scopeSeparator(" ")
//            .useBasicAuthenticationWithAccessCodeGrant(true)
//            .build();
//    }

//    @Override
//    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html")
//            .addResourceLocations("classpath:/META-INF/resources/");
//
//        registry.addResourceHandler("/webjars/**")
//            .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
}
