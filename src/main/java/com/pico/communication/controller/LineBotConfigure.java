package com.pico.communication.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class LineBotConfigure implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // String downloadedContentUri = Application.downloadedContentDir.toUri().toASCIIString();
        // log.info("downloaded Uri: {}", downloadedContentUri);
        registry.addResourceHandler("/**");
                // .addResourceLocations(downloadedContentUri);
    }
    
    @EnableWebSecurity
    public class LineLoginSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .oauth2Login();
        }
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.lineClientRegistration());
    }

    private ClientRegistration lineClientRegistration() {
        return ClientRegistration.withRegistrationId("line")
                .clientId("1604634627")
                .clientSecret("2655ff21f794401e1cbcf90984b78af0")
                .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUriTemplate("localhost:8080/{registrationId}")
                .scope("profile")
                .authorizationUri("https://access.line.me/oauth2/v2.1/authorize")
                .tokenUri("https://api.line.me/oauth2/v2.1/token")
                .userNameAttributeName("userId")
                .userInfoUri("https://api.line.me/v2/profile")
                .clientName("LINE")
                .build();
    }
}
