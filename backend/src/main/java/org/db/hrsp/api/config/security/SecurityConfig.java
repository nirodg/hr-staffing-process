package org.db.hrsp.api.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    private static final String ROLE_ADMIN = "CLIENT_PUBLIC_ADMIN";
    private static final String ROLE_USER = "CLIENT_PUBLIC_USER";

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    private final JwtClaimsConverter jwtAuthConverter;

    public SecurityConfig(JwtClaimsConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer = cors -> cors
                .configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOrigin("http://localhost:4200");
                    config.addAllowedMethod("*");
                    config.addAllowedHeader("*");
                    config.setAllowCredentials(true);
                    return config;
                });

        http.cors(corsCustomizer)
                .authorizeHttpRequests(auth -> auth
                		.requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/graphql/**").permitAll()
                        .requestMatchers("/graphql").permitAll()
                        .requestMatchers("/api/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)
                        .requestMatchers("/ws/**").permitAll() // WebSocket endpoints
                        .anyRequest().authenticated()// All other paths need auth
                )
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

}
