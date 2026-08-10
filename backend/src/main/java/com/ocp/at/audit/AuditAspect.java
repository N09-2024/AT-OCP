package com.ocp.at.audit;

import com.ocp.at.entity.Utilisateur;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * Journalise automatiquement toute action d'écriture (POST/PUT/PATCH/DELETE)
 * effectuée via les contrôleurs REST, pour que le journal d'audit ne soit
 * plus jamais vide, sans dépendre d'un appel manuel dans chaque service.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
        private final UtilisateurRepository utilisateurRepository;  


    @Around("execution(* com.ocp.at.controller..*(..))")
    public Object auditControllerCall(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        boolean isWrite = method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);

        // GET et endpoints d'auth ne sont pas bruités dans le journal
        if (!isWrite || pjp.getTarget().getClass().getSimpleName().contains("Auth")) {
            return pjp.proceed();
        }

        String action = pjp.getTarget().getClass().getSimpleName() + "." + method.getName();
        String ip = extractIp();
        String userAgent = extractUserAgent();

        Utilisateur currentUser = safeCurrentUser();

        try {
            Object result = pjp.proceed();
            auditService.logAction(action, "SUCCES", currentUser, ip, userAgent);
            return result;
        } catch (Throwable ex) {
            auditService.logAction(action + " [" + ex.getClass().getSimpleName() + "]", "ECHEC", currentUser, ip, userAgent);
            throw ex;
        }
    }

private Utilisateur safeCurrentUser() {
    try {
        return SecurityUtils.getCurrentUtilisateurId() // email
                .flatMap(utilisateurRepository::findByEmail)
                .orElse(null);
    } catch (Exception e) {
        return null; // action anonyme (ex: tentative sur endpoint public)
    }
}

    private String extractIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = attrs != null ? attrs.getRequest() : null;
            if (req == null) return "N/A";
            String forwarded = req.getHeader("X-Forwarded-For");
            return forwarded != null ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String extractUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = attrs != null ? attrs.getRequest() : null;
            return req != null ? req.getHeader("User-Agent") : "N/A";
        } catch (Exception e) {
            return "N/A";
        }
    }
}