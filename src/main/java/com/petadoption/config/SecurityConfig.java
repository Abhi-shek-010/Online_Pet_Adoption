package com.petadoption.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * Spring Security Configuration
 * 
 * Configures security settings for the PawMatch application.
 * Allows public access to static resources and authentication endpoints.
 * Enables CORS for API requests.
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        /**
         * Configure CORS for API requests
         * 
         * @return CorsConfigurationSource
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // Use allowedOriginPatterns instead of allowedOrigins when allowCredentials is
                // true
                configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*", "*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
                configuration.setAllowedHeaders(
                                Arrays.asList("Content-Type", "Authorization", "Accept", "Origin", "X-Requested-With"));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(Arrays.asList("Content-Type", "Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        /**
         * Configure security filter chain
         * 
         * Allow public access to:
         * - All HTML files (landing page, login, register, etc.)
         * - Static resources (CSS, JS, images)
         * - Authentication endpoints
         * - Pet listing API
         * 
         * Require authentication for:
         * - Admin endpoints
         * - User dashboard
         * - Protected pet operations
         * 
         * @param http HttpSecurity configuration
         * @return SecurityFilterChain
         * @throws Exception if configuration fails
         */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(authorize -> authorize
                                                // Allow public access to static content and HTML pages
                                                .requestMatchers("/", "/index.html", "/login.html", "/register.html",
                                                                "/pets-listing.html",
                                                                "/dashboard.html", "/stories.html", "/shelters.html")
                                                .permitAll()
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**")
                                                .permitAll()

                                                // Allow public access to authentication endpoints
                                                .requestMatchers("/auth/login", "/auth/register", "/auth/logout")
                                                .permitAll()

                                                // Allow public access to pet listing API
                                                .requestMatchers("/pets", "/pets/**").permitAll()

                                                // Allow API endpoints (servlets handle their own auth via session)
                                                .requestMatchers("/api/**").permitAll()
                                                .requestMatchers("/api/applications", "/api/applications/**")
                                                .permitAll()
                                                .requestMatchers("/api/adoptions/**").permitAll()

                                                // All other requests require authentication
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login.html")
                                                .permitAll())
                                .logout(logout -> logout
                                                .permitAll())
                                .csrf(csrf -> csrf.disable()); // Disable CSRF for API testing

                return http.build();
        }
}
