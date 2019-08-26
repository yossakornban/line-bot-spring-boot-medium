package com.pico.communication.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAutoConfiguration
public class LineLoginConfig  implements WebMvcConfigurer{

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// String downloadedContentUri =
		// Application.downloadedContentDir.toUri().toASCIIString();
		// log.info("downloaded Uri: {}", downloadedContentUri);
//		registry.addResourceHandler("/**");
		// .addResourceLocations(downloadedContentUri);
	}

//    @EnableWebSecurity
//    public class LineLoginSecurityConfig extends WebSecurityConfigurerAdapter {
//        @Override
//        protected void configure(HttpSecurity http) throws Exception {
//        	System.out.println("llllllllllllllllllllllllllaaaaaaaaaaaaaaaaaaaaaaaaaaa");
//        	  http.authorizeRequests()
//              .anyRequest().authenticated()
//              .and()
//              .oauth2Login();
//        }
//    }

}


//spring:
//	  security:
//	    oauth2:
//	      client:
//	        registration:
//	          line:
//	            client-id: 1610979581
//	            client-secret: 12c8c9fad0e28b66355ac6739cf15371
//	            provider: LINE
//	            scope: profile
//	            expires-in: 2591965
//	            redirect-uri-template: 'https://picos.ssweb.ga/register'
//	            client-authentication-method: basic
//	            authorization-grant-type: authorization_code
//	        provider:
//	          LINE:
//	            authorization-uri: https://access.line.me/oauth2/v2.1/authorize
//	            token-uri: https://api.line.me/oauth2/v2.1/token
//	            user-info-uri: https://api.line.me/v2/profile
//	            user-name-attribute: userId	          
//





//<dependency>
//	<groupId>org.springframework.boot</groupId>
//	<artifactId>spring-boot-starter-security</artifactId>
//</dependency>
//<dependency>
//  <groupId>org.springframework.boot</groupId>
//  <artifactId>spring-boot-starter-thymeleaf</artifactId>
//</dependency>
//<dependency>
//  <groupId>org.springframework.security</groupId>
//  <artifactId>spring-security-oauth2-client</artifactId>
//  <version>5.1.3.RELEASE</version>
//</dependency>