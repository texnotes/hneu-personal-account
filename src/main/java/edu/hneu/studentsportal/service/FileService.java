package edu.hneu.studentsportal.service;

import javaslang.control.Try;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

@Log4j
@Service
public class FileService {

    @SneakyThrows
    public File getFile(MultipartFile multipartFile) {
        byte[] bytes = multipartFile.getBytes();
        Path path = Paths.get(multipartFile.getOriginalFilename());
        Files.deleteIfExists(path);
        Files.write(path, bytes);
        return path.toFile();
    }

    @SneakyThrows
    public <E> E perform(File file, Function<File, E> function) {
        E response = Try.of(() -> function.apply(file)).getOrElseThrow(() -> new IllegalStateException("Cannot perform operation on the file!"));
        Files.deleteIfExists(file.toPath());
        return response;
    }
}
