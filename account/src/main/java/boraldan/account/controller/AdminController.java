package boraldan.account.controller;

import boraldan.account.controller.feign.KeycloakFeign;

import boraldan.users.domen.dto.UserKeycloakDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KeycloakFeign keycloakFeign;

    /**
     * Получает список всех пользователей из базы данных Keycloak через FeignClient.
     *
     * @return ResponseEntity, содержащий список представлений пользователей.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserKeycloakDto>> getAllUsers() {
        ResponseEntity<List<UserKeycloakDto>> response = keycloakFeign.getUsersFromKeycloak();
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(response.getBody());
        }
        return ResponseEntity.status(response.getStatusCode()).body(null);
    }



    /**
     * Удаляет пользователя по его имени пользователя.
     *
     * @param username Имя пользователя, которого необходимо удалить.
     * @return ResponseEntity с сообщением об успешном удалении или ошибке.
     */
    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        ResponseEntity<String> response = keycloakFeign.deleteUser(username);
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            return ResponseEntity.ok("User deleted successfully.");
        }
        return ResponseEntity.status(response.getStatusCode()).body("Failed to delete user: " + response.getBody());
    }

    /**
     * Ищет пользователя по имени пользователя.
     *
     * @param username Имя пользователя для поиска.
     * @return ResponseEntity с найденным пользователем или пустым значением, если пользователь не найден.
     */
    @PostMapping("/search")
    public ResponseEntity<UserKeycloakDto> searchUser(@RequestBody String username) {
        ResponseEntity<UserKeycloakDto> response = keycloakFeign.searchUser(username);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(response.getBody());
        }
        return ResponseEntity.status(response.getStatusCode()).body(null);
    }
}
