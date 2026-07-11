package com.ocp.at.service;

import java.io.IOException;

public interface StorageService {
    String storeFile(String filePath, byte[] content, String contentType) throws IOException;
    byte[] loadFile(String filePath) throws IOException;
}
