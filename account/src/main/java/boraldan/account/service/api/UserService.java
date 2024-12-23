package boraldan.account.service.api;


import boraldan.users.domen.dto.CreatUserDto;
import boraldan.users.domen.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserService {

    String getDefaultPhotoPath();

    List<User> findAll();
    Optional<User> findById(UUID id);
    User save(User user);
    User update(User user);
    void deleteById(UUID id);
    Optional<User> findByUsernameIgnoreCase(String username);
    String downloadPhoto(MultipartFile photo);
    CompletableFuture<ResponseEntity<User>> updatePhoto(UUID userId, MultipartFile photo);
    User updateUserFields(User user, CreatUserDto creatUserDto);
    ResponseEntity<?> deleteByIdKafka(String username);
}
