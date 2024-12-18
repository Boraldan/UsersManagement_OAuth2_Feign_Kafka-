package boraldan.account.service;

import boraldan.account.service.api.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService_v1 implements FileStorageService {

    private final Path fileStorageLocation;

    /**
     * Конструктор класса FileStorageService.
     *
     * @param uploadDir директория, в которую будут загружаться файлы.
     */
    public FileStorageService_v1(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory for file upload", ex);
        }
    }

    /**
     * Сохраняет файл на сервере.
     *
     * @param file загружаемый файл.
     * @return относительный путь к сохраненному файлу.
     */
    public String storeFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + (file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);
            return "/uploads/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName, ex);
        }
    }


}
