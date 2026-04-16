package com.SpendWise.ExpenseTracker.security;

//SecurityConfig decides the security rules for the whole app

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration// this class contains application setup , Spring must read it and create objects from this class
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean// run this method, take returned object, store it in Spring container, make it available for DI, uses wherever it needs
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();//This is Spring Security’s password encoder implementation using BCrypt,
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/v1/auth/**").permitAll()
                                .anyRequest().authenticated());

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
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