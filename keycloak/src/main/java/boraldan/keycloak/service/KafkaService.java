package boraldan.keycloak.service;

import boraldan.users.domen.dto.UserKeycloakDto;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {

    private static final String REALM = "master";
    private final Keycloak keycloak;

    @KafkaListener(
            topics = "delete-user-topic",
            groupId = "delete-user-group",
            properties = {"auto.offset.reset=earliest"}
    )
    public void deleteUser(UserKeycloakDto userKeycloakDto) {
        String username = userKeycloakDto.getUsername();
        log.info("Received request to delete user with username: {}", username);

        try {
            keycloak.realm(REALM).users().search(username).stream()
                    .findFirst()
                    .ifPresentOrElse(
                            userRepresentation -> {
                                String userId = userRepresentation.getId();
                                keycloak.realm(REALM).users().delete(userId);
                                log.info("User with username '{}' and ID '{}' successfully deleted.", username, userId);
                            },
                            () -> log.warn("User with username '{}' not found in realm '{}'.", username, REALM)
                    );
        } catch (Exception e) {
            log.error("Failed to delete user with username '{}' due to an error: {}", username, e.getMessage(), e);
        }
    }
}
