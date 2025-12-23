package tw.pers.allen.ajax_tutorial_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Allow all requests for Ch1-4 demos
            )
            .csrf(csrf -> csrf.disable()) // Disable CSRF for easier Postman testing
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // Allow H2 Console in iframe

        return http.build();
    }
}
