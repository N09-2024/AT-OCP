package com.ocp.at.service.impl;

import com.ocp.at.dto.request.UtilisateurRequest;
import com.ocp.at.dto.request.UtilisateurUpdateRequest;
import com.ocp.at.dto.response.RoleResponse;
import com.ocp.at.dto.response.UtilisateurResponse;
import com.ocp.at.entity.Role;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.RoleMapper;
import com.ocp.at.mapper.UtilisateurMapper;
import com.ocp.at.repository.RoleRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurServiceImpl.class);

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UtilisateurResponse creer(UtilisateurRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un utilisateur avec l'email " + request.getEmail() + " existe déjà");
        }
        // Auto-générer le matricule si non fourni
        if (request.getMatricule() == null || request.getMatricule().isBlank()) {
            request.setMatricule("USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else if (utilisateurRepository.existsByMatricule(request.getMatricule())) {
            throw new BusinessException("Un utilisateur avec le matricule " + request.getMatricule() + " existe déjà");
        }

        Utilisateur utilisateur = utilisateurMapper.toEntity(request);
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setActif(true);

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        logger.info("Utilisateur créé par admin: {} ({})", saved.getEmail(), saved.getId());
        return utilisateurMapper.toResponse(saved);
    }

    @Override
    public UtilisateurResponse trouverParId(String id) {
        return utilisateurMapper.toResponse(findUtilisateurById(id));
    }

    @Override
    public Page<UtilisateurResponse> listerTous(String search, Pageable pageable) {
        Page<Utilisateur> page = (search != null && !search.isBlank())
                ? utilisateurRepository.findBySearchTerm(search, pageable)
                : utilisateurRepository.findAll(pageable);
        return page.map(utilisateurMapper::toResponse);
    }

    @Override
    @Transactional
    public UtilisateurResponse modifier(String id, UtilisateurUpdateRequest request) {
        Utilisateur utilisateur = findUtilisateurById(id);
        utilisateurMapper.updateEntityFromRequest(request, utilisateur);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        logger.info("Utilisateur modifié: {}", saved.getEmail());
        return utilisateurMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void supprimer(String id) {
        Utilisateur utilisateur = findUtilisateurById(id);
        utilisateurRepository.delete(utilisateur);
        logger.info("Utilisateur supprimé: {}", utilisateur.getEmail());
    }

    @Override
    @Transactional
    public UtilisateurResponse activer(String id) {
        Utilisateur utilisateur = findUtilisateurById(id);
        utilisateur.setActif(true);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        logger.info("Compte activé: {}", saved.getEmail());
        return utilisateurMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UtilisateurResponse desactiver(String id) {
        Utilisateur utilisateur = findUtilisateurById(id);
        utilisateur.setActif(false);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        logger.info("Compte désactivé: {}", saved.getEmail());
        return utilisateurMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UtilisateurResponse deverrouiller(String id) {
        Utilisateur utilisateur = findUtilisateurById(id);
        utilisateur.setCompteVerrouille(false);
        utilisateur.setCompteurEchecsConnexion(0);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        logger.info("Compte déverrouillé par admin: {}", saved.getEmail());
        return utilisateurMapper.toResponse(saved);
    }

    @Override
    public Set<RoleResponse> getRoles(String id) {
        Utilisateur utilisateur = findUtilisateurById(id);
        return utilisateur.getRoles().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public UtilisateurResponse affecterRole(String id, String roleId) {
        Utilisateur utilisateur = findUtilisateurById(id);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable: " + roleId));
        utilisateur.getRoles().add(role);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        logger.info("Rôle '{}' affecté à l'utilisateur: {}", role.getNom(), utilisateur.getEmail());
        return utilisateurMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UtilisateurResponse retirerRole(String id, String roleId) {
        Utilisateur utilisateur = findUtilisateurById(id);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable: " + roleId));
        utilisateur.getRoles().remove(role);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        logger.info("Rôle '{}' retiré de l'utilisateur: {}", role.getNom(), utilisateur.getEmail());
        return utilisateurMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerEnAttente() {
        return utilisateurRepository.findByEnAttenteValidationTrue()
                .stream()
                .map(utilisateurMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UtilisateurResponse approuverInscription(String id) {
        Utilisateur utilisateur = findUtilisateurById(id);
        if (!utilisateur.isEnAttenteValidation()) {
            throw new BusinessException("Cette inscription n'est pas en attente de validation");
        }
        utilisateur.setActif(true);
        utilisateur.setEnAttenteValidation(false);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        logger.info("Inscription approuvée: {} <{}>", saved.getEmail(), saved.getId());
        return utilisateurMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void rejeterInscription(String id) {
        Utilisateur utilisateur = findUtilisateurById(id);
        if (!utilisateur.isEnAttenteValidation()) {
            throw new BusinessException("Cette inscription n'est pas en attente de validation");
        }
        utilisateurRepository.delete(utilisateur);
        logger.info("Inscription rejetée et supprimée: {} <{}>", utilisateur.getEmail(), utilisateur.getId());
    }

    private Utilisateur findUtilisateurById(String id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable: " + id));
    }
}
