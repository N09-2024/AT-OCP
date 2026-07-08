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
import com.ocp.at.entity.enums.TypePermis;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.PermisMapper;
import com.ocp.at.repository.AnalyseIARepository;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.FichierJointRepository;
import com.ocp.at.repository.PermisRepository;
import com.ocp.at.storage.LocalStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisServiceTest {

    @Mock private PermisRepository permisRepository;
    @Mock private AutorisationTravailRepository autorisationTravailRepository;
    @Mock private FichierJointRepository fichierJointRepository;
    @Mock private AnalyseIARepository analyseIARepository;
    @Mock private LocalStorageService localStorageService;
    @Mock private ConformitePermisService conformitePermisService;
    @Mock private PermisMapper permisMapper;
    @Mock private IAProvider iaProvider;

    @InjectMocks
    private PermisService permisService;

    private AutorisationTravail at;
    private Permis permis;

    @BeforeEach
    void setUp() {
        at = new AutorisationTravail();
        at.setId("at-1");

        permis = new Permis();
        permis.setId("permis-1");
        permis.setType(TypePermis.FEU);
        permis.setStatutVerification(StatutPermis.A_VERIFIER);
        permis.setAutorisationTravail(at);
    }

    @Test
    void createPermis_ShouldSucceed() {
        PermisRequest request = new PermisRequest();
        request.setType(TypePermis.FEU);
        request.setAutorisationTravailId("at-1");

        when(autorisationTravailRepository.findById("at-1")).thenReturn(Optional.of(at));
        when(permisMapper.toEntity(request)).thenReturn(permis);
        when(permisRepository.save(any(Permis.class))).thenReturn(permis);
        when(permisMapper.toResponse(permis)).thenReturn(new PermisResponse());

        PermisResponse result = permisService.createPermis(request);

        assertNotNull(result);
        verify(permisRepository).save(any(Permis.class));
    }

    @Test
    void createPermis_ShouldThrow_WhenATNotFound() {
        PermisRequest request = new PermisRequest();
        request.setType(TypePermis.FEU);
        request.setAutorisationTravailId("unknown-at");

        when(autorisationTravailRepository.findById("unknown-at")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> permisService.createPermis(request));
    }

    @Test
    void getPermisById_ShouldSucceed() {
        when(permisRepository.findById("permis-1")).thenReturn(Optional.of(permis));
        when(permisMapper.toResponse(permis)).thenReturn(new PermisResponse());

        PermisResponse result = permisService.getPermisById("permis-1");
        assertNotNull(result);
    }

    @Test
    void getPermisById_ShouldThrow_WhenNotFound() {
        when(permisRepository.findById("bad-id")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> permisService.getPermisById("bad-id"));
    }

    @Test
    void uploadFichier_ShouldRunAnalyseAndUpdateStatut() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "permis.pdf", "application/pdf", "test content".getBytes());

        FichierJoint fichierJoint = new FichierJoint();
        fichierJoint.setId("fj-1");

        AnalyseIA analyseIA = new AnalyseIA();
        analyseIA.setId("analyse-1");

        when(permisRepository.findById("permis-1")).thenReturn(Optional.of(permis));
        when(localStorageService.store(any())).thenReturn("stored_permis.pdf");
        when(fichierJointRepository.save(any())).thenReturn(fichierJoint);
        when(iaProvider.analyserPermis(any(), any())).thenReturn(analyseIA);
        when(analyseIARepository.save(any())).thenReturn(analyseIA);
        when(conformitePermisService.evaluerConformite(any(), any())).thenReturn(StatutPermis.CONFORME);
        when(permisRepository.save(any())).thenReturn(permis);

        UploadPermisResponse response = permisService.uploadFichier("permis-1", file);

        assertNotNull(response);
        verify(iaProvider).analyserPermis(any(), any());
        verify(conformitePermisService).evaluerConformite(any(), any());
    }

    @Test
    void reanalyserPermis_ShouldThrow_WhenNoFileAttached() {
        permis.setFichierJoint(null);
        when(permisRepository.findById("permis-1")).thenReturn(Optional.of(permis));

        assertThrows(BusinessException.class, () -> permisService.reanalyserPermis("permis-1"));
    }
}
