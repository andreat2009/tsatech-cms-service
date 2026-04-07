package com.newproject.cms.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/cms/information", "/api/cms/information/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/cms/settings/public", "/api/cms/settings/runtime").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/cms/settings", "/api/cms/settings/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/cms/settings", "/api/cms/settings/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/cms/integrations", "/api/cms/integrations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/cms/integrations", "/api/cms/integrations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/cms/integrations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/cms/integrations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/cms/analytics/events").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/cms/analytics", "/api/cms/analytics/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/cms/information", "/api/cms/information/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/cms/information/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/cms/information/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
            for (Object role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase(java.util.Locale.ROOT)));
            }
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((client, access) -> {
                if (access instanceof Map<?, ?> accessMap) {
                    Object rolesObj = accessMap.get("roles");
                    if (rolesObj instanceof List<?> roles) {
                        for (Object role : roles) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase(java.util.Locale.ROOT)));
                        }
                    }
                }
            });
        }

        return authorities;
    }
}
