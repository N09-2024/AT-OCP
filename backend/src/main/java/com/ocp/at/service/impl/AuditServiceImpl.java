package com.ocp.at.service.impl;

import com.ocp.at.entity.AuditLog;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.enums.ResultatAudit;
import com.ocp.at.repository.AuditLogRepository;
import com.ocp.at.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logAction(String action, String resultat, Utilisateur utilisateur, String ip, String navigateur) {
        String systemeExploitation = extractOS(navigateur);
        
        AuditLog auditLog = AuditLog.builder()
                .date(LocalDateTime.now())
                .action(action)
                .resultat(ResultatAudit.valueOf(resultat))
                .utilisateur(utilisateur)
                .adresseIP(ip)
                .navigateur(navigateur)
                .systemeExploitation(systemeExploitation)
                .build();
                
        auditLogRepository.save(auditLog);
        log.info("Audit: {} - {} - User: {}", action, resultat, utilisateur != null ? utilisateur.getMatricule() : "SYSTEM");
    }
    
    private String extractOS(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.toLowerCase().contains("windows")) return "Windows";
        if (userAgent.toLowerCase().contains("mac")) return "Mac OS";
        if (userAgent.toLowerCase().contains("linux")) return "Linux";
        if (userAgent.toLowerCase().contains("android")) return "Android";
        if (userAgent.toLowerCase().contains("iphone")) return "iOS";
        return "Other";
    }
}
