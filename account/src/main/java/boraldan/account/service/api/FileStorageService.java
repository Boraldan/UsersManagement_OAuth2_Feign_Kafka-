package boraldan.account.service.api;

import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface FileStorageService {

     String storeFile(MultipartFile file);
}
