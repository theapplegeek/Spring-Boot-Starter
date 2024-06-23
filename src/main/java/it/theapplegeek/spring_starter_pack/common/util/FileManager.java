package it.theapplegeek.spring_starter_pack.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@AllArgsConstructor
public class FileManager {
  public Resource loadFileAsResource(String first, String... filePaths) throws IOException {
    Path filePath = Paths.get(first, filePaths);
    if (!Files.exists(filePath)) {
      throw new IOException("File not found");
    }

    InputStream inputStream = Files.newInputStream(filePath);
    return new InputStreamResource(inputStream);
  }

  public void deleteAllFilesInDirectory(String directoryPath) throws IOException {
    Path dirPath = Paths.get(directoryPath);

    if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
      throw new IllegalArgumentException("The provided path is not a valid directory.");
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
      for (Path filePath : stream) {
        if (Files.isRegularFile(filePath)) {
          Files.delete(filePath);
        }
      }
    }
  }

  public void saveMultipartFile(MultipartFile multipartFile, String first, String... filePaths)
      throws IOException {
    Path path = Paths.get(first, filePaths);

    if (Files.notExists(path.getParent())) {
      Files.createDirectories(path.getParent());
    }

    Files.write(path, multipartFile.getBytes());
  }
}
