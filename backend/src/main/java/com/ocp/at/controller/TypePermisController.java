package com.ocp.at.controller;

import com.ocp.at.dto.request.TypePermisRequest;
import com.ocp.at.dto.response.TypePermisResponse;
import com.ocp.at.service.TypePermisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types-permis")
@RequiredArgsConstructor
public class TypePermisController {

    private final TypePermisService typePermisService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TypePermisResponse>> getAllTypePermis() {
        return ResponseEntity.ok(typePermisService.getAllTypePermis());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TypePermisResponse> getTypePermisById(@PathVariable String id) {
        return ResponseEntity.ok(typePermisService.getTypePermisById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TypePermisResponse> createTypePermis(@Valid @RequestBody TypePermisRequest request) {
        return new ResponseEntity<>(typePermisService.createTypePermis(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TypePermisResponse> updateTypePermis(
            @PathVariable String id,
            @Valid @RequestBody TypePermisRequest request) {
        return ResponseEntity.ok(typePermisService.updateTypePermis(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTypePermis(@PathVariable String id) {
        typePermisService.deleteTypePermis(id);
        return ResponseEntity.noContent().build();
    }
}
