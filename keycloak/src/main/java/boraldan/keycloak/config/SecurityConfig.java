package boraldan.keycloak.config;


import jakarta.annotation.Priority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
public class SecurityConfig {

    /**
     * Настройка цепочки фильтров безопасности.
     *
     * @param http объект HttpSecurity для настройки безопасности.
     * @return настроенная цепочка фильтров безопасности.
     * @throws Exception если происходит ошибка настройки безопасности.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(configurer -> configurer
                                .requestMatchers("/api/account/creat").permitAll()
                                .requestMatchers("/api/account/**").hasAuthority("SCOPE_account_scope")
                                .requestMatchers("/api/admin/**").hasAuthority("SCOPE_keycloak_scope")
                                .anyRequest().authenticated()
                )
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(CsrfConfigurer::disable)
                .oauth2Client(Customizer.withDefaults())
                .oauth2ResourceServer(customizer -> customizer.jwt(Customizer.withDefaults()))
                .build();
    }

}
