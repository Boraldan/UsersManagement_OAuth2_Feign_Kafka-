package boraldan.account.controller;

import boraldan.account.controller.feign.KeycloakFeign;
import boraldan.account.service.api.UserService;


import boraldan.users.domen.dto.CreatUserDto;
import boraldan.users.domen.dto.UserKeycloakDto;
import boraldan.users.domen.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final KeycloakFeign keycloakFeign;
    private final ModelMapper modelMapper;

    /**
     * Получает список всех пользователей из локальной базы данных.
     *
     * @return ResponseEntity, содержащий список пользователей.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * Получает список всех пользователей из базы данных Keycloak.
     *
     * @return ResponseEntity, содержащий список DTO пользователей из Keycloak.
     */
    @GetMapping("/keycloak/users")
    public ResponseEntity<List<UserKeycloakDto>> getAllUsersFromKeycloak() {
        return ResponseEntity.ok(keycloakFeign.getUsersFromKeycloak().getBody());
    }

    /**
     * Создает нового пользователя.
     *
     * @param creatUserDto DTO пользователя, содержащий информацию для создания.
     * @return ResponseEntity с сохраненным пользователем или сообщением об ошибке.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestPart("user") CreatUserDto creatUserDto
    ) {
        if (userService.findByUsernameIgnoreCase(creatUserDto.getUsername()).isEmpty()) {
            ResponseEntity<String> response = keycloakFeign.createUser(mapCreatUserDtoToUserKeycloakDto(creatUserDto));
            if (response.getStatusCode().is2xxSuccessful()) {
                creatUserDto.setPhotoUrl(userService.getDefaultPhotoPath());
                User savedUser = userService.save(mapCreatUserDtoToUser(creatUserDto));
                return ResponseEntity.ok(savedUser);
            } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Failed to create user in Keycloak: " + response.getBody());
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User with this username already exists in our service.");
    }

    /**
     * Обновляет пароль пользователя.
     *
     * @param userKeycloakDto DTO пользователя с новыми данными для обновления пароля.
     * @return ResponseEntity с сообщением об успешном обновлении или ошибке.
     */
    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UserKeycloakDto userKeycloakDto) {
        ResponseEntity<String> response = keycloakFeign.updatePassword(userKeycloakDto);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok("Password updated successfully.");
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to update password in Keycloak: " + response.getBody());
        }
    }

    /**
     * Получает пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя.
     * @return ResponseEntity с найденным пользователем или 404, если не найден.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Получает пользователя по имени пользователя.
     *
     * @param username имя пользователя.
     * @return ResponseEntity с найденным пользователем или 404, если не найден.
     */
    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsernameIgnoreCase(username);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Обновляет данные пользователя.
     *
     * @param id           идентификатор пользователя.
     * @param creatUserDto DTO с новыми данными пользователя.
     * @return ResponseEntity с обновленным пользователем или 404, если не найден.
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @Valid @RequestBody CreatUserDto creatUserDto) {
        Optional<User> optionalUser = userService.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();
        user = userService.updateUserFields(user, creatUserDto);
        User updatedUser = userService.update(user);

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Обновляет фотографию пользователя в асинхронном потоке.
     *
     * @param id    идентификатор пользователя.
     * @param photo файл фотографии для загрузки.
     * @return ResponseEntity с обновленным пользователем или 404, если не найден.
     */
    @PutMapping("/{id}/photo")
    public DeferredResult<ResponseEntity<User>> updateUserPhoto(@PathVariable UUID id, @RequestParam("photo") MultipartFile photo) {
        DeferredResult<ResponseEntity<User>> outputUser = new DeferredResult<>();
        userService.updatePhoto(id, photo)
                .thenAccept(outputUser::setResult)
                .exceptionally(ex -> {
                    outputUser.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                    return null;
                });
        return outputUser;
    }

    /**
     * Удаляет ссылку на фотографию пользователя.
     *
     * @param id идентификатор пользователя.
     * @return ResponseEntity без содержимого или 404, если не найден.
     */
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Void> deleteUserPhoto(@PathVariable UUID id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOptional.get();
        user.setPhotoUrl(userService.getDefaultPhotoPath());
        userService.update(user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Удаляет пользователя.
     *
     * @param id идентификатор пользователя.
     * @return ResponseEntity без содержимого или сообщение об ошибке.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        Optional<User> optionalUser = userService.findById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        ResponseEntity<String> response = keycloakFeign.deleteUser(user.getUsername());

        if (response.getStatusCode().is2xxSuccessful()) {
            userService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            userService.deleteById(id);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("User deleted locally but not found in Keycloak.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete user in Keycloak: " + response.getBody());
        }
    }

    /**
     * Преобразует DTO пользователя в сущность User.
     *
     * @param creatUserDto DTO пользователя.
     * @return сущность User.
     */
    private User mapCreatUserDtoToUser(CreatUserDto creatUserDto) {
        return modelMapper.map(creatUserDto, User.class);
    }

    /**
     * Преобразует DTO для Keycloak в соответствующий DTO.
     *
     * @param creatUserDto DTO пользователя.
     * @return DTO пользователя для Keycloak.
     */
    private UserKeycloakDto mapCreatUserDtoToUserKeycloakDto(CreatUserDto creatUserDto) {
        return modelMapper.map(creatUserDto, UserKeycloakDto.class);
    }

}