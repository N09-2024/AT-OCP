package com.ocp.at.entity;

import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workflows_at")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowAT {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAT etatDepart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAT etatArrivee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeActionAT action;

    private String roleAutorise;

    private Integer ordreValidation;

    @Builder.Default
    private Boolean validationObligatoire = true;

    @Builder.Default
    private Boolean actif = true;

    private String roleSuivant;

    private String notificationSuivante;
}
