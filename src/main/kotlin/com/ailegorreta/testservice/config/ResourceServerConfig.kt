package com.ailegorreta.testservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@EnableMethodSecurity
@Configuration(proxyBeanMethods = false)
class ResourceServerConfig {

    /**
     *  -- This code is we want for develop purpose to use all REST calls without a token --
     *  -- For example: if want to run the REST from swagger and test the micro service
     * http.authorizeHttpRequests{ auth ->  auth
     *     .requestMatchers("/ **").permitAll()
     *     .anyRequest().authenticated()
     *
     * note: erse white space between '/ **' ) just for comment
     *
     **/

    // @formatter:off
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain( http:HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests{ auth ->  auth
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/testsecurity/**").hasAnyAuthority("SCOPE_iam.facultad")
            .requestMatchers("/testrolesecurity/**").permitAll()
            .requestMatchers("/nosecurity/**").permitAll()
            .anyRequest().authenticated()
        }
        .oauth2ResourceServer{ server -> server.jwt { Customizer.withDefaults<Any>() }}
        .sessionManagement{ sessionManagement ->
                            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                          }
        // ^ Each request must include an Access Token, so there’s no need to keep a user session alive between
        // requests. We want it to be stateless.
        .csrf { configuration -> configuration.disable() }
        // ^ Since the authentication strategy is stateless and does not involve a browser-based client, we can safely
        // disable the CSRF protection
        return http.build()
    }
    // @formatter:on

    /**
     * Extracting roles from the Access Token
     */
    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        // ^ Applies the “ROLE_” prefix to each user role
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles")
        // ^ Extracts the list of roles from the roles claim

        var jwtAuthenticationConverter = JwtAuthenticationConverter()
        // ^ Defines a converter to map claims to GrantedAuthority objects

        jwtAuthenticationConverter .setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter)

        return jwtAuthenticationConverter
    }
}