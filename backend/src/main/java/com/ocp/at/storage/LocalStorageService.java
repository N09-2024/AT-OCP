package com.ocp.at.storage;

import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class LocalStorageService implements StorageService {

    @Value("${app.storage.location:uploads}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    @Override
    public void init() {
        this.rootLocation = Paths.get(storageLocation);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new BusinessException("Impossible d'initialiser le dossier de stockage");
        }
    }

    @Override
    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new BusinessException("Échec de l'enregistrement d'un fichier vide.");
            }
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path destinationFile = this.rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new BusinessException("Impossible d'enregistrer le fichier en dehors du répertoire actuel.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return newFilename;
        } catch (IOException e) {
            throw new BusinessException("Échec de l'enregistrement du fichier.");
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new BusinessException("Échec de la lecture des fichiers stockés.");
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Impossible de lire le fichier: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Impossible de lire le fichier: " + filename);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = load(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new BusinessException("Impossible de supprimer le fichier: " + filename);
        }
    }

    @Override
    public String saveSignature(MultipartFile signatureFile, String filename) {
        try {
            if (signatureFile.isEmpty()) {
                throw new BusinessException("Échec de l'enregistrement d'une signature vide.");
            }
            Path signaturesDir = this.rootLocation.resolve("signatures");
            Files.createDirectories(signaturesDir);
            
            Path destinationFile = signaturesDir.resolve(filename).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(signaturesDir.toAbsolutePath())) {
                throw new BusinessException("Impossible d'enregistrer la signature en dehors du répertoire prévu.");
            }
            try (InputStream inputStream = signatureFile.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return "signatures/" + filename;
        } catch (IOException e) {
            throw new BusinessException("Échec de l'enregistrement de la signature.");
        }
    }

    @Override
    public Resource loadSignature(String relativePath) {
        return loadAsResource(relativePath);
    }

    @Override
    public void deleteSignature(String relativePath) {
        delete(relativePath);
    }
}
