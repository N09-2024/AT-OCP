package com.ocp.at.entity;

import com.ocp.at.entity.enums.ResultatAudit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime date;

    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    private ResultatAudit resultat;

    private String adresseIP;

    private String navigateur;

    private String systemeExploitation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
}
