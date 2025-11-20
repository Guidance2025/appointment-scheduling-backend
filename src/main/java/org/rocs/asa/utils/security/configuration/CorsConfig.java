package org.rocs.asa.utils.security.configuration;

import org.rocs.asa.utils.security.jwt.filter.authentication.access.denied.JwtAccessDeniedHandler;
import org.rocs.asa.utils.security.jwt.filter.authentication.forbidden.AuthenticationEntryPoint;
import org.rocs.asa.utils.security.jwt.filter.authorization.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.rocs.asa.utils.security.constant.SecurityConstant.PUBLIC_URLS;

@Configuration
public class CorsConfig {
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private JwtAuthorizationFilter jwtAuthorizationFilter;
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public CorsConfig(JwtAccessDeniedHandler jwtAccessDeniedHandler,
                      JwtAuthorizationFilter jwtAuthorizationFilter,
                      AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * Provides bean and Configuration for security filters, session management, and request authorizations.
     *
     * @param httpSecurity the security configuration for HTTP requests
     * @return the configured security filter chain
     * @throws Exception if an error occurs during security setup
     */
    @Bean
    public SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and()
                .csrf(AbstractHttpConfigurer::disable).
                sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).
                authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/counselor").hasAnyRole("GUIDANCE","ADMIN")
                        .anyRequest().authenticated()).
                exceptionHandling(e -> {
                    e.authenticationEntryPoint(authenticationEntryPoint);
                    e.accessDeniedHandler(jwtAccessDeniedHandler);
                }).addFilterBefore(jwtAuthorizationFilter, BasicAuthenticationFilter.class);
        return httpSecurity.build();
    }
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
