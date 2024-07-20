package com.wallhack.clouddrive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/home").permitAll()
                .requestMatchers("/sign-up").permitAll()
                .requestMatchers("/sign-in").permitAll()
                .requestMatchers("/logout").permitAll()
                .anyRequest().authenticated())
                .formLogin(withDefaults())
                .logout(config -> config
                        .logoutSuccessUrl("/home"))
                .build();
    }
}
