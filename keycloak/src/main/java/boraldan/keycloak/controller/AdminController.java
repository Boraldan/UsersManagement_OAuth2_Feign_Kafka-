package boraldan.keycloak.controller;


import boraldan.users.domen.dto.UserKeycloakDto;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final String realm = "master";
    private final Keycloak keycloak;
    private final ModelMapper modelMapper;

    /**
     * Получает список всех пользователей из Keycloak.
     *
     * @return Список представлений пользователей.
     */
    @GetMapping("/users")
    public List<UserRepresentation> getAllUsers() {
        return keycloak.realm(realm).users().list();
    }

    /**
     * Обновляет информацию о пользователе по его идентификатору.
     *
     * @param userId        Идентификатор пользователя, которого нужно обновить.
     * @param updatedUser   Объект UserRepresentation с новыми данными пользователя.
     * @return ResponseEntity с сообщением об успешном обновлении пользователя.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable String userId, @RequestBody UserRepresentation updatedUser) {
        UsersResource usersResource = keycloak.realm(realm).users();
        UserResource userResource = usersResource.get(userId);

        userResource.update(updatedUser);
        return ResponseEntity.ok("User updated successfully.");
    }

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param userId Идентификатор пользователя, которого необходимо удалить.
     * @return ResponseEntity с сообщением об успешном удалении или ошибке.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        try (Response response = keycloak.realm(realm).users().delete(userId)) {
            if (response.getStatus() == 204) {
                return ResponseEntity.ok("User deleted successfully.");
            } else {
                return ResponseEntity.status(response.getStatus()).body("Failed to delete user: " + response.readEntity(String.class));
            }
        }
    }

    /**
     * Ищет пользователя по имени пользователя.
     *
     * @param username Имя пользователя для поиска.
     * @return ResponseEntity с найденным пользователем или пустым значением, если пользователь не найден.
     */
    @PostMapping("search")
    public ResponseEntity<UserKeycloakDto> searchUser(@RequestBody String username) {
        UserRepresentation userRepresentation = keycloak.realm(realm).users().search(username).stream().findFirst().orElse(null);
        if (userRepresentation != null) {
            UserKeycloakDto userKeycloakDto = mapUserRepresentationToUserDto(userRepresentation);
            return ResponseEntity.ok(userKeycloakDto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    private  UserKeycloakDto  mapUserRepresentationToUserDto(UserRepresentation userRepresentation) {
        return  modelMapper. map(userRepresentation, UserKeycloakDto.class);

    }

}