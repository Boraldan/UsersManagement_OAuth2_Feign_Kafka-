package boraldan.keycloak.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Конфигурация для настройки клиента Keycloak.
 * Этот класс создает бин Keycloak для взаимодействия с сервером Keycloak.
 */
@Configuration
public class KeycloakConfig {

    /**
     * Создает бин Keycloak с настройками для подключения к серверу Keycloak.
     *
     * @return настроенный экземпляр Keycloak.
     */
    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl("http://localhost:8079/")
                .realm("master")
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId("admin-telros")
                .clientSecret("6ayDdqYZKNSGIoPxOyHGh85XJUtx7zSF")
                .build();
    }
}