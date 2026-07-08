package com.ocp.at.service;

public interface IAProvider {
    String extractText(String filePath);
    double calculateConfidence(String text);
}

