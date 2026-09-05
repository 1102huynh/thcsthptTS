package com.schoolmanagement.config;

import com.schoolmanagement.security.AdmissionRateLimitFilter;
import com.schoolmanagement.security.ChangePasswordRateLimitFilter;
import com.schoolmanagement.security.ContactRateLimitFilter;
import com.schoolmanagement.security.ForgotPasswordRateLimitFilter;
import com.schoolmanagement.security.LoginRateLimitFilter;
import com.schoolmanagement.security.JwtAuthenticationFilter;
import com.schoolmanagement.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private UserDetailsService userDetailsService;
    private JwtTokenProvider jwtTokenProvider;
    private AdmissionRateLimitFilter admissionRateLimitFilter;
    private ForgotPasswordRateLimitFilter forgotPasswordRateLimitFilter;
    private ContactRateLimitFilter contactRateLimitFilter;
    private ChangePasswordRateLimitFilter changePasswordRateLimitFilter;
    private LoginRateLimitFilter loginRateLimitFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}") String allowedOrigins) {
        // Comma-separated; set APP_CORS_ALLOWED_ORIGINS to the real
        // public-portal domain(s) in prod — the portal serves anonymous
        // users so its browser calls to this API must be allowed.
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Auth endpoints
                .requestMatchers("/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Public portal read API + contact form (permitAll; the CMS
                // write endpoints /v1/news, /v1/events, /v1/media are NOT
                // under /public and stay @PreAuthorize ADMIN/PRINCIPAL).
                .requestMatchers("/v1/public/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                // Public admission submission (3.7) — rate-limited by AdmissionRateLimitFilter,
                // added to the chain below. GET/PUT/other /v1/admissions paths stay authenticated.
                .requestMatchers(HttpMethod.POST, "/v1/admissions").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/admissions").permitAll()
                // Swagger UI - note: swagger-ui is outside /api context path
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs.yaml").permitAll()
                // Static resources
                .requestMatchers("/error").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(401, "Unauthorized");
                })
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(admissionRateLimitFilter, JwtAuthenticationFilter.class)
            .addFilterBefore(forgotPasswordRateLimitFilter, JwtAuthenticationFilter.class)
            .addFilterBefore(contactRateLimitFilter, JwtAuthenticationFilter.class)
            .addFilterBefore(loginRateLimitFilter, JwtAuthenticationFilter.class)
            // Runs AFTER JwtAuthenticationFilter (not before, like the others above) —
            // it needs the authenticated principal already in the SecurityContext to key
            // the limit by user id. See ChangePasswordRateLimitFilter's Javadoc.
            .addFilterAfter(changePasswordRateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
