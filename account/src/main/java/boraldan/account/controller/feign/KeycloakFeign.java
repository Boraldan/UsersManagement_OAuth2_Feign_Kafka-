package boraldan.account.controller.feign;

import boraldan.account.config.FeignConfig;

import boraldan.users.domen.dto.UserKeycloakDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Primary
@FeignClient(name = "keycloak", fallback = KeycloakFeignFallback.class, configuration = FeignConfig.class)
public interface KeycloakFeign {

    @GetMapping("/api/account/users")
    ResponseEntity<List<UserKeycloakDto>> getUsersFromKeycloak();

    @PostMapping("/api/account/create")
    ResponseEntity<String> createUser(@RequestBody UserKeycloakDto userKeycloakDto);

    @DeleteMapping("/api/account/{username}")
    ResponseEntity<String> deleteUser(@PathVariable String username);

    @PutMapping("/api/account/password")
    ResponseEntity<String> updatePassword(@RequestBody UserKeycloakDto userKeycloakDto);

    @PostMapping("/api/admin/search")
    ResponseEntity<UserKeycloakDto> searchUser(@RequestBody String username);
}

@Component
class KeycloakFeignFallback implements KeycloakFeign {

    @Override
    public ResponseEntity<List<UserKeycloakDto>> getUsersFromKeycloak() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Collections.emptyList());
    }

    @Override
    public ResponseEntity<String> createUser(UserKeycloakDto userKeycloakDto) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service is currently unavailable.");
    }

    @Override
    public ResponseEntity<String> deleteUser(String username) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service is currently unavailable.");
    }

    @Override
    public ResponseEntity<String> updatePassword(UserKeycloakDto userKeycloakDto) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service is currently unavailable.");
    }

    @Override
    public ResponseEntity<UserKeycloakDto> searchUser(String username) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
    }


}