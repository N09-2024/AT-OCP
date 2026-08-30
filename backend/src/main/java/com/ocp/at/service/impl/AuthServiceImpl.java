package com.ocp.at.service.impl;

import com.ocp.at.dto.request.LoginRequest;
import com.ocp.at.dto.request.RegisterRequest;
import com.ocp.at.dto.request.TokenRefreshRequest;
import com.ocp.at.dto.response.JwtResponse;
import com.ocp.at.dto.response.TokenRefreshResponse;
import com.ocp.at.dto.response.UtilisateurResponse;
import com.ocp.at.entity.RefreshToken;
import com.ocp.at.entity.Role;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.exception.UnauthorizedException;
import com.ocp.at.mapper.UtilisateurMapper;
import com.ocp.at.repository.RefreshTokenRepository;
import com.ocp.at.repository.RoleRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.security.JwtUtils;
import com.ocp.at.security.UserDetailsImpl;
import com.ocp.at.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final RoleRepository roleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.ocp.at.service.NotificationService notificationService;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Tentative de connexion avec un email inconnu: {}", request.getEmail());
                    return new UnauthorizedException("Identifiants invalides");
                });

        if (utilisateur.isCompteVerrouille()) {
            logger.warn("Tentative de connexion sur un compte verrouillé: {}", request.getEmail());
            throw new UnauthorizedException("Compte verrouillé. Contactez un administrateur.");
        }

        if (!utilisateur.isActif()) {
            logger.warn("Tentative de connexion sur un compte désactivé: {}", request.getEmail());
            throw new UnauthorizedException("Compte désactivé.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));
        } catch (BadCredentialsException e) {
            int echecs = utilisateur.getCompteurEchecsConnexion() + 1;
            utilisateur.setCompteurEchecsConnexion(echecs);
            if (echecs >= maxLoginAttempts) {
                utilisateur.setCompteVerrouille(true);
                logger.warn("Compte verrouillé après {} tentatives échouées: {}", maxLoginAttempts, request.getEmail());
                try {
                    notificationService.sendNotificationToRole("ADMIN",
                            "Compte utilisateur verrouillé",
                            "Le compte de " + utilisateur.getPrenom() + " " + utilisateur.getNom() + " (" + utilisateur.getEmail() + ") a été verrouillé après " + maxLoginAttempts + " tentatives de connexion échouées.",
                            "WARNING",
                            "/administration/utilisateurs");
                } catch (Exception ex) {
                    logger.warn("Erreur envoi notification admin compte verrouille: {}", ex.getMessage());
                }
            }
            utilisateurRepository.save(utilisateur);
            logger.warn("Échec de connexion ({}/{}) pour: {}", echecs, maxLoginAttempts, request.getEmail());
            throw new UnauthorizedException("Identifiants invalides");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Réinitialiser compteur d'échecs
        utilisateur.setCompteurEchecsConnexion(0);
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        String jwt = jwtUtils.generateJwtToken(authentication);

        // Créer RefreshToken
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .utilisateur(utilisateur)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .dateCreation(Instant.now())
                .adresseIP(ipAddress)
                .userAgent(userAgent)
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.contains("_") || a.startsWith("ROLE"))
                .collect(Collectors.toList());

        List<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE") && a.contains("_"))
                .collect(Collectors.toList());

        logger.info("Connexion réussie pour: {}", request.getEmail());

        return JwtResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .utilisateur(utilisateurMapper.toResponse(utilisateur))
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalide"));

        if (token.isRevoked()) {
            throw new UnauthorizedException("Refresh token révoqué");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token expiré. Veuillez vous reconnecter.");
        }

        String newJwt = jwtUtils.generateTokenFromEmail(token.getUtilisateur().getEmail());

        // Rotation du refresh token
        refreshTokenRepository.delete(token);
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .utilisateur(token.getUtilisateur())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .dateCreation(Instant.now())
                .adresseIP(token.getAdresseIP())
                .userAgent(token.getUserAgent())
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return TokenRefreshResponse.builder()
                .accessToken(newJwt)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.delete(token);
            logger.info("Logout: refresh token révoqué pour utilisateur {}", token.getUtilisateur().getEmail());
        });
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public UtilisateurResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte avec cet email existe déjà");
        }

        // Récupérer le rôle CE par défaut (Chef d'Équipe)
        Role roleCE = roleRepository.findByNom("CE")
                .orElseGet(() -> roleRepository.findByNom("CEEP")
                        .orElseThrow(() -> new RuntimeException("Rôle CE introuvable")));

        // Générer un matricule automatique
        String matricule = "USER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Créer l'utilisateur
        Utilisateur utilisateur = Utilisateur.builder()
                .matricule(matricule)
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .actif(false) // Compte inactif en attendant validation
                .enAttenteValidation(true) // En attente de validation par l'admin
                .roles(java.util.Set.of(roleCE))
                .build();

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        logger.info("Nouvelle inscription en attente de validation: {} <{}>", 
                request.getPrenom() + " " + request.getNom(), request.getEmail());

        try {
            notificationService.sendNotificationToRole("ADMIN",
                    "Nouvelle inscription en attente",
                    "L'utilisateur " + request.getPrenom() + " " + request.getNom() + " (" + request.getEmail() + ") a demandé la création d'un compte et attend validation.",
                    "ACTION",
                    "/administration/inscriptions");
        } catch (Exception ex) {
            logger.warn("Erreur envoi notification admin inscription: {}", ex.getMessage());
        }

        return utilisateurMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse getCurrentUser(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable: " + email));
        return utilisateurMapper.toResponse(utilisateur);
    }
}
