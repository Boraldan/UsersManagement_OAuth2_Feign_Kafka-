package boraldan.keycloak.controller;


import boraldan.users.domen.dto.UserKeycloakDto;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final String realm = "master";
    private final Keycloak keycloak;
    private final ModelMapper modelMapper;

    /**
     * Получает список пользователей из Keycloak.
     *
     * @return ResponseEntity, содержащий список DTO пользователей.
     */
    @GetMapping("/users")
    ResponseEntity<List<UserKeycloakDto>> getUsersFromKeycloak() {
        return ResponseEntity.ok(mapUserRepresentationListToUserDtoList(keycloak.realm(realm).users().list()));
    }

    /**
     * Создает нового пользователя в Keycloak.
     *
     * @param userKeycloakDto DTO пользователя, содержащий имя пользователя и пароль.
     * @return ResponseEntity, содержащий сообщение об успешном создании пользователя или ошибке.
     */
    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody UserKeycloakDto userKeycloakDto) {

        if (keycloak.realm(realm).users().search(userKeycloakDto.getUsername()).isEmpty()) {
            UsersResource usersResource = keycloak.realm(realm).users();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(userKeycloakDto.getUsername());
            user.setEnabled(true);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(userKeycloakDto.getPassword());
            credential.setTemporary(false);

            user.setCredentials(Collections.singletonList(credential));

            try (Response response = usersResource.create(user)) {
                if (response.getStatus() == 201) {
                    return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");
                } else {
                    return ResponseEntity.status(response.getStatus())
                            .body("Failed to create user: " + response.readEntity(String.class));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("User with this username already exists.");
    }

    /**
     * Удаляет пользователя из Keycloak по имени пользователя.
     *
     * @param username Имя пользователя, которого нужно удалить.
     * @return ResponseEntity, содержащий сообщение об успешном удалении пользователя или ошибке.
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        UserRepresentation userRepresentation = keycloak.realm(realm).users().search(username).stream().findFirst().orElse(null);

        if (userRepresentation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        try (Response response = keycloak.realm(realm).users().delete(userRepresentation.getId())) {
            if (response.getStatus() == 204) {
                return ResponseEntity.ok("User deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete user: " + response.readEntity(String.class));
            }
        }
    }

    /**
     * Обновляет пароль пользователя по его имени пользователя.
     *
     * @param userKeycloakDto Объект UserKeycloakDto, содержащий имя пользователя и новый пароль.
     * @return ResponseEntity с сообщением об успешном обновлении пароля или ошибке.
     */
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody UserKeycloakDto userKeycloakDto) {
        UsersResource usersResource = keycloak.realm(realm).users();

        List<UserRepresentation> users = usersResource.search(userKeycloakDto.getUsername());
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        UserRepresentation userRepresentation = users.stream().findFirst().get();
        UserResource userResource = usersResource.get(userRepresentation.getId());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userKeycloakDto.getPassword());
        credential.setTemporary(false);

        userResource.resetPassword(credential);
        return ResponseEntity.ok("Password updated successfully.");
    }


    /**
     * Преобразует список представлений пользователей Keycloak в список DTO пользователей.
     *
     * @param userRepresentationList Список представлений пользователей Keycloak.
     * @return Список DTO пользователей.
     */
    private List<UserKeycloakDto> mapUserRepresentationListToUserDtoList(List<UserRepresentation> userRepresentationList) {
        return userRepresentationList.stream()
                .map(userRepresentation -> modelMapper.map(userRepresentation, UserKeycloakDto.class))
                .collect(Collectors.toList());
    }
}