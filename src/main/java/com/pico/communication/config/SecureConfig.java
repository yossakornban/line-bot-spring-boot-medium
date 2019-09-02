package com.pico.communication.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import com.pico.communication.Application;

@Configuration
@EnableWebSecurity
@EnableAutoConfiguration
public class SecureConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.requestMatcher(new RequestHeaderRequestMatcher("Authorization")).authorizeRequests().anyRequest()
				.fullyAuthenticated();
//		http.oauth2ResourceServer().jwt().jwkSetUri("https://picos.ssweb.ga/identity/.well-known/openid-configuration/jwks");
	}

	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(HttpMethod.OPTIONS)
		.antMatchers("/**");
//		.antMatchers("/apploan/testline");

	}
	
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String downloadedContentUri = Application.downloadedContentDir.toUri().toASCIIString();
        System.out.println("[>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
        System.out.println(downloadedContentUri);
        registry.addResourceHandler("/downloaded/**")
                .addResourceLocations(downloadedContentUri);
    }
}
