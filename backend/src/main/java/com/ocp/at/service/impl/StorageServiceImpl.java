package com.ocp.at.service.impl;

import com.ocp.at.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageServiceImpl implements StorageService {

    @Value("${app.storage.location:uploads}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(storageLocation);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String storeFile(String filePath, byte[] content, String contentType) throws IOException {
        Path destinationFile = this.rootLocation.resolve(Paths.get(filePath)).normalize().toAbsolutePath();
        if (!destinationFile.getParent().startsWith(this.rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot store file outside current directory.");
        }
        Files.createDirectories(destinationFile.getParent());
        Files.write(destinationFile, content);
        return destinationFile.toString();
    }

    @Override
    public byte[] loadFile(String filePath) throws IOException {
        Path file = this.rootLocation.resolve(Paths.get(filePath)).normalize().toAbsolutePath();
        if (!file.startsWith(this.rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot load file outside current directory.");
        }
        return Files.readAllBytes(file);
    }
}
