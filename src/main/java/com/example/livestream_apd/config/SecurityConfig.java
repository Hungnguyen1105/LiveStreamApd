package com.example.livestream_apd.config;

import com.example.livestream_apd.infrastructure.security.JwtAuthenticationEntryPoint;
import com.example.livestream_apd.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.xml.crypto.Data;
import java.lang.reflect.Array;
import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // mã hoá password

    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(); // khoi tao
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/auth/**",
                                "/public/**",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error",
                                "/payments/vnpay/callback",
                                "/api/payments/vnpay/callback",
                                "/posts/**",
                                "/api/v1/posts/**"
                        ).permitAll() //cho pheps tat ca dg dan tren

                        // admin endpoint
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // moderator endpoint
                        .requestMatchers("/api/moderator/**").hasAnyRole("ADMIN","MODERATOR")

                        // user management
                        .requestMatchers("/api/users/*/roles/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/*/status/**").hasAnyRole("ADMIN","MODERATOR")

                        //streamer endpoint
                        .requestMatchers("/api/streams/create").hasAnyRole("ADMIN","STREAMER")
                        .requestMatchers("/api/streams/manage/**").hasAnyRole("ADMIN","STREAMER")

                        //payment endpoint
                        .requestMatchers("/api/payments/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/payments/**").hasRole("USER")

                        //user profile
                        .requestMatchers("/api/users/profile").hasRole("USER")
                        .requestMatchers("/api/users/privacy").hasRole("USER")
                        .requestMatchers("/api/users/search").hasRole("USER")
                        .requestMatchers("/api/users/suggestions").hasRole("USER")
                        .requestMatchers("/api/users/*/follow").hasRole("USER")
                        .requestMatchers("/api/users/*/followers").hasRole("USER")
                        .requestMatchers("/api/users/*/following").hasRole("USER")
                        .requestMatchers("/api/users/block/*").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("USER")


                        // posts endpoints
                        .requestMatchers("/posts/**").hasRole("USER")
                        .requestMatchers("/api/posts/**").hasRole("USER")
                        .requestMatchers("/api/v1/posts/**").hasRole("USER")
                        //content creation endpoints
                        .requestMatchers("/api/content/create").hasAuthority("CONTENT_CREATE")
                        .requestMatchers("/api/content/edit/**").hasAuthority("CONTENT_EDIT")
                        .requestMatchers("/api/content/delete/**").hasAuthority("CONTENT_DELETE")
                        .requestMatchers("/api/content/moderate/**").hasAuthority("CONTENT_MODIERATE")

                        //all above endpoints require authentication
                        .anyRequest().authenticated()
                );
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080"
        ));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
