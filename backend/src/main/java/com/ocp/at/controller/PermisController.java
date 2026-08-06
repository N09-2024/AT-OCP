package com.ocp.at.controller;

import com.ocp.at.dto.request.PermisRequest;
import com.ocp.at.dto.response.AnalyseIAResponse;
import com.ocp.at.dto.response.PermisResponse;
import com.ocp.at.dto.response.UploadPermisResponse;
import com.ocp.at.mapper.AnalyseIAMapper;
import com.ocp.at.repository.PermisRepository;
import com.ocp.at.service.PermisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/permis")
@RequiredArgsConstructor
@Tag(name = "Permis", description = "Gestion des permis de travail liés aux Autorisations de Travail")
public class PermisController {

    private final PermisService permisService;
    private final PermisRepository permisRepository;
    private final AnalyseIAMapper analyseIAMapper;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer un nouveau permis pour une AT")
    public ResponseEntity<PermisResponse> createPermis(@Valid @RequestBody PermisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(permisService.createPermis(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consulter un permis par son ID")
    public ResponseEntity<PermisResponse> getPermis(@PathVariable String id) {
        return ResponseEntity.ok(permisService.getPermisById(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister tous les permis")
    public ResponseEntity<List<PermisResponse>> getAllPermis() {
        return ResponseEntity.ok(permisService.getAllPermis());
    }

    @GetMapping("/at/{atId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister tous les permis d'une Autorisation de Travail")
    public ResponseEntity<List<PermisResponse>> getPermisByAT(@PathVariable String atId) {
        return ResponseEntity.ok(permisService.getPermisByAT(atId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Modifier un permis existant")
    public ResponseEntity<PermisResponse> updatePermis(
            @PathVariable String id,
            @Valid @RequestBody PermisRequest request) {
        return ResponseEntity.ok(permisService.updatePermis(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Supprimer un permis")
    public ResponseEntity<Void> deletePermis(@PathVariable String id) {
        permisService.deletePermis(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Uploader un fichier de permis (PDF, PNG, JPEG, WEBP) et lancer l'analyse IA")
    public ResponseEntity<UploadPermisResponse> uploadFichier(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // Validation du type MIME
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf")
                && !contentType.equals("image/png")
                && !contentType.equals("image/jpeg")
                && !contentType.equals("image/webp"))) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }
        
        return ResponseEntity.ok(permisService.uploadFichier(id, file));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Télécharger le fichier joint d'un permis")
    public ResponseEntity<Resource> downloadFichier(@PathVariable String id) {
        var permis = permisRepository.findById(id)
                .orElseThrow(() -> new com.ocp.at.exception.ResourceNotFoundException("Permis non trouvé"));
        
        if (permis.getFichierJoint() == null) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = permisService.downloadFichier(id);
        String filename = permis.getFichierJoint().getNom();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PutMapping("/{id}/reanalyser")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Relancer l'analyse IA sur le fichier joint d'un permis")
    public ResponseEntity<UploadPermisResponse> reanalyser(@PathVariable String id) {
        return ResponseEntity.ok(permisService.reanalyserPermis(id));
    }

    @GetMapping("/{id}/analyse")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consulter le résultat de l'analyse IA d'un permis")
    public ResponseEntity<AnalyseIAResponse> getAnalyse(@PathVariable String id) {
        var permis = permisRepository.findById(id)
                .orElseThrow(() -> new com.ocp.at.exception.ResourceNotFoundException("Permis non trouvé"));
        
        if (permis.getAnalyseIA() == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(analyseIAMapper.toResponse(permis.getAnalyseIA()));
    }
}
