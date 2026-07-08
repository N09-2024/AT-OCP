package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "installations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nomInstallation;

    private String atelier;
    
    private String localisation;

    @Column(unique = true, nullable = false)
    private String codeInstallation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;
}
