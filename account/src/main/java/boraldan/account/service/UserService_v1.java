package boraldan.account.service;

import boraldan.account.repository.UserRepository;
import boraldan.account.service.api.FileStorageService;
import boraldan.account.service.api.UserService;

import boraldan.users.domen.dto.CreatUserDto;
import boraldan.users.domen.dto.UserKeycloakDto;
import boraldan.users.domen.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService_v1 implements UserService {

    @Getter
    @Value("${user.photo.default-path}")
    private String defaultPhotoPath;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final KafkaTemplate<String, UserKeycloakDto> kafkaTemplate;
    private final ModelMapper modelMapper;

    /**
     * Получает список всех пользователей из базы данных.
     *
     * @return список пользователей.
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Получает пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя.
     * @return Optional<User> с найденным пользователем или пустой Optional.
     */
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Сохраняет нового пользователя в базе данных.
     *
     * @param user пользователь для сохранения.
     * @return сохраненный пользователь.
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Обновляет данные существующего пользователя в базе данных.
     *
     * @param user пользователь для обновления.
     * @return обновленный пользователь.
     */
    @Transactional
    public User update(User user) {
        return userRepository.save(user);
    }


    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя для удаления.
     */
    @Transactional
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public ResponseEntity<?> deleteByIdKafka(String username) {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(username);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        kafkaTemplate.send("delete-user-topic", maptUserToUserKeycloakDto(user));

        userRepository.deleteById(user.getUserId());
        return ResponseEntity.noContent().build();
    }



    /**
     * Получает пользователя по имени пользователя, игнорируя регистр.
     *
     * @param username имя пользователя.
     * @return Optional<User> с найденным пользователем или пустой Optional.
     */
    public Optional<User> findByUsernameIgnoreCase(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    /**
     * Загружает фотографию пользователя.
     *
     * @param photo файл фотографии.
     * @return путь к загруженной фотографии или путь к фотографии по умолчанию, если файл пустой.
     */
    public String downloadPhoto(MultipartFile photo) {
        return photo.isEmpty() ? defaultPhotoPath : fileStorageService.storeFile(photo);
    }

    /**
     * Обновляет фотографию пользователя в асинхронном потоке.
     *
     * @param photo файл новой фотографии.
     * @return путь к новой фотографии.
     */

    @Async
    public CompletableFuture<ResponseEntity<User>> updatePhoto(UUID id, MultipartFile photo) {

        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
        } else if (photo.isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.noContent().build());
        }

        User user = userOptional.get();
        String urlPhoto = downloadPhoto(photo);
        user.setPhotoUrl(urlPhoto);
        User updatedUser = update(user);
        return CompletableFuture.completedFuture(ResponseEntity.ok(updatedUser));
    }


    /**
     * Обновляет поля пользователя на основе данных из DTO.
     *
     * @param user         пользователь, поля которого необходимо обновить.
     * @param creatUserDto DTO с новыми данными пользователя.
     * @return обновленный пользователь.
     */
    public User updateUserFields(User user, CreatUserDto creatUserDto) {
        if (creatUserDto.getLastName() != null) {
            user.setLastName(creatUserDto.getLastName());
        }
        if (creatUserDto.getFirstName() != null) {
            user.setFirstName(creatUserDto.getFirstName());
        }
        if (creatUserDto.getMiddleName() != null) {
            user.setMiddleName(creatUserDto.getMiddleName());
        }
        if (creatUserDto.getDateBirth() != null) {
            user.setDateBirth(creatUserDto.getDateBirth());
        }
        if (creatUserDto.getEmail() != null) {
            user.setEmail(creatUserDto.getEmail());
        }
        if (creatUserDto.getPhoneNumber() != null) {
            user.setPhoneNumber(creatUserDto.getPhoneNumber());
        }
        return user;
    }

    private UserKeycloakDto maptUserToUserKeycloakDto(User user) {
        return modelMapper.map(user, UserKeycloakDto.class);
    }
}