package com.ocp.at.service;

import com.ocp.at.ai.IAProvider;
import com.ocp.at.dto.request.PermisRequest;
import com.ocp.at.dto.response.PermisResponse;
import com.ocp.at.dto.response.UploadPermisResponse;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;
import com.ocp.at.entity.enums.StatutPermis;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.PermisMapper;
import com.ocp.at.repository.AnalyseIARepository;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.FichierJointRepository;
import com.ocp.at.repository.PermisRepository;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.storage.LocalStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermisService {

    private final PermisRepository permisRepository;
    private final AutorisationTravailRepository autorisationTravailRepository;
    private final FichierJointRepository fichierJointRepository;
    private final AnalyseIARepository analyseIARepository;
    
    private final LocalStorageService localStorageService;
    private final ConformitePermisService conformitePermisService;
    private final PermisMapper permisMapper;
    
    // Pour l'instant on injecte directement le mock
    private final IAProvider iaProvider;

    @Transactional
    public PermisResponse createPermis(PermisRequest request) {
        AutorisationTravail at = autorisationTravailRepository.findById(request.getAutorisationTravailId())
                .orElseThrow(() -> new ResourceNotFoundException("Autorisation de Travail non trouvée"));

        Permis permis = permisMapper.toEntity(request);
        permis.setAutorisationTravail(at);
        permis.setStatutVerification(StatutPermis.A_VERIFIER);

        permis = permisRepository.save(permis);
        return permisMapper.toResponse(permis);
    }

    @Transactional(readOnly = true)
    public PermisResponse getPermisById(String id) {
        Permis permis = findPermisEntity(id);
        return permisMapper.toResponse(permis);
    }

    @Transactional(readOnly = true)
    public List<PermisResponse> getPermisByAT(String atId) {
        return permisRepository.findByAutorisationTravailId(atId).stream()
                .map(permisMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermisResponse> getAllPermis() {
        return permisRepository.findAll().stream()
                .map(permisMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PermisResponse updatePermis(String id, PermisRequest request) {
        Permis permis = findPermisEntity(id);
        permisMapper.updateEntityFromRequest(request, permis);
        permis = permisRepository.save(permis);
        return permisMapper.toResponse(permis);
    }

    @Transactional
    public void deletePermis(String id) {
        Permis permis = findPermisEntity(id);
        if (permis.getFichierJoint() != null) {
            try {
                // Delete file from disk if necessary
                // In actual code, we could call localStorageService.delete(...)
            } catch (Exception e) {
                log.error("Erreur lors de la suppression du fichier", e);
            }
        }
        permisRepository.delete(permis);
    }

    public org.springframework.core.io.Resource downloadFichier(String permisId) {
        Permis permis = findPermisEntity(permisId);
        if (permis.getFichierJoint() == null) {
            throw new ResourceNotFoundException("Aucun fichier joint pour ce permis");
        }
        // We load the file from the stored path (just the filename, stored by LocalStorageService)
        String storedPath = permis.getFichierJoint().getPath();
        return localStorageService.loadAsResource(storedPath);
    }

    @Transactional
    public UploadPermisResponse uploadFichier(String permisId, MultipartFile file) throws IOException {
        Permis permis = findPermisEntity(permisId);
        
        // 1. Sauvegarde du fichier physique
        String path = localStorageService.store(file);

        // 2. Création de l'entité FichierJoint
        FichierJoint fichierJoint = FichierJoint.builder()
                .nom(file.getOriginalFilename())
                .path(path)
                .type(file.getContentType())
                .taille(file.getSize())
                .dateImport(LocalDateTime.now())
                .uploadedBy(SecurityUtils.getCurrentUtilisateurId().orElse("system"))
                .permis(permis)
                .build();
                
        // Si un fichier existait déjà, on le remplace
        if (permis.getFichierJoint() != null) {
            fichierJointRepository.delete(permis.getFichierJoint());
        }
        
        fichierJoint = fichierJointRepository.save(fichierJoint);
        permis.setFichierJoint(fichierJoint);
        
        // 3. Lancer l'analyse IA
        return executerAnalyseIA(permis, fichierJoint);
    }

    @Transactional
    public UploadPermisResponse reanalyserPermis(String permisId) {
        Permis permis = findPermisEntity(permisId);
        if (permis.getFichierJoint() == null) {
            throw new BusinessException("Aucun fichier joint à analyser pour ce permis.");
        }
        return executerAnalyseIA(permis, permis.getFichierJoint());
    }

    private UploadPermisResponse executerAnalyseIA(Permis permis, FichierJoint fichierJoint) {
        // 1. Appel du provider IA
        AnalyseIA analyseIA = iaProvider.analyserPermis(fichierJoint, permis);
        
        // On remplace l'ancienne analyse si elle existe
        if (permis.getAnalyseIA() != null) {
            analyseIARepository.delete(permis.getAnalyseIA());
        }
        
        analyseIA = analyseIARepository.save(analyseIA);
        permis.setAnalyseIA(analyseIA);
        
        // 2. Evaluation de la conformité
        StatutPermis statut = conformitePermisService.evaluerConformite(permis, analyseIA);
        permis.setStatutVerification(statut);
        
        permisRepository.save(permis);
        
        return UploadPermisResponse.builder()
                .id(permis.getId())
                .message("Upload et analyse réussis")
                .fichierJointId(fichierJoint.getId())
                .analyseIAId(analyseIA.getId())
                .statutVerification(statut.name())
                .build();
    }

    private Permis findPermisEntity(String id) {
        return permisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permis non trouvé avec l'id: " + id));
    }
}
