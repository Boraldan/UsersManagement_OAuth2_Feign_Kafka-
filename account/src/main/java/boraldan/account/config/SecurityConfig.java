package boraldan.account.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
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
     * Настраивает менеджер авторизованных клиентов для OAuth2.
     *
     * @param clientRegistrationRepository репозиторий для хранения регистраций клиентов.
     * @param authorizedClientService      сервис для управления авторизованными клиентами.
     * @return менеджер авторизованных клиентов.
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
    }

    /**
     * Конфигурирует цепочку фильтров безопасности для обработки HTTP-запросов.
     *
     * @param http объект HttpSecurity для настройки параметров безопасности.
     * @return настроенная цепочка фильтров безопасности.
     * @throws Exception если произошла ошибка при конфигурации.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(configurer -> configurer
                        .requestMatchers("/api/users/creat").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .csrf(CsrfConfigurer::disable)
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
//                .oauth2ResourceServer(customizer -> customizer.jwt(Customizer.withDefaults()))
                .oauth2ResourceServer(configurer -> configurer.jwt(jwt -> {
                    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                    jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);

                    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

                    JwtGrantedAuthoritiesConverter customJwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                    customJwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("groups");
                    customJwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(token ->
                            Stream.concat(jwtGrantedAuthoritiesConverter.convert(token).stream(),
                                    customJwtGrantedAuthoritiesConverter.convert(token).stream()).toList());
                }))


                .build();
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oAuth2UserService() {
        OidcUserService oidcUserService = new OidcUserService();
        return userRequest -> {
            OidcUser oidcUser = oidcUserService.loadUser(userRequest);
            List<GrantedAuthority> authorities =
                    Stream.concat(oidcUser.getAuthorities().stream(),
                                    Optional.ofNullable(oidcUser.getClaimAsStringList("groups"))
                                            .orElseGet(List::of)
                                            .stream()
                                            .filter(role -> role.startsWith("ROLE_"))
                                            .map(SimpleGrantedAuthority::new)
                                            .map(GrantedAuthority.class::cast))
                            .toList();
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "preferred_username");
        };
    }

}
