package com.ocp.at.service;

import com.ocp.at.entity.Utilisateur;

public interface AuditService {
    void logAction(String action, String resultat, Utilisateur utilisateur, String ip, String navigateur);
}
