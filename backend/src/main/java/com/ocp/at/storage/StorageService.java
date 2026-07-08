package com.ocp.at.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    void init();
    String store(MultipartFile file);
    Stream<Path> loadAll();
    Path load(String filename);
    Resource loadAsResource(String filename);
    void deleteAll();
    void delete(String filename);

    /** Sauvegarde une signature PNG dans le sous-dossier signatures/. Retourne le chemin relatif. */
    String saveSignature(MultipartFile signatureFile, String filename);

    /** Charge une signature par son chemin relatif (signatures/xxx.png). */
    Resource loadSignature(String relativePath);

    /** Supprime une signature par son chemin relatif. */
    void deleteSignature(String relativePath);
}
