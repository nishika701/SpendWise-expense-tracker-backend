package com.SpendWise.ExpenseTracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .anyRequest().authenticated()
                );

        http.headers(headers ->
                headers.frameOptions(frame -> frame.disable())
        );

        http.addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://spendwise-expense-tracker-bice.vercel.app"
        ));
        
        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

/*
Cross-Site Request Forgery - csrf
It is a browser-based security protection mostly needed for session/cookie-based apps.


session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
means:
•do not create server-side login sessions
•authentication will be handled per request, usually using JWT

STATELESS = no server session memory for login

headers HTTP headers are metadata sent in request/response.

frameOptions This is a browser security header.
It controls whether your page can be displayed inside a frame/iframe.

Why does it matter?
•H2 console uses frames
•Spring Security blocks frames by default for safety
•if you want H2 console to work, you often disable frame options


Tiny summary
•@Configuration = setup class
•@Bean = create and register object in Spring container
•BCryptPasswordEncoder = secure password hasher
•csrf = browser/session security protection
•disable CSRF = common for stateless JWT APIs
•STATELESS = no server session memory for login
•headers = HTTP response security settings
•frameOptions = controls iframe/frame blocking
•http.build() = finalize and return security config
 */