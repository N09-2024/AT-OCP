package com.ocp.at.controller;

import com.ocp.at.dto.response.WorkflowResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@Tag(name = "Workflow", description = "Consultation du workflow de l'Autorisation de Travail")
public class WorkflowController {

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir l'état du workflow pour une AT spécifique")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable String id) {
        // Pour l'instant on retourne un stub, dans un vrai système cela interrogerait
        // WorkflowATService pour déterminer les transitions possibles depuis l'état actuel de l'AT.
        WorkflowResponse response = WorkflowResponse.builder()
                .atId(id)
                .build();
        return ResponseEntity.ok(response);
    }
}
