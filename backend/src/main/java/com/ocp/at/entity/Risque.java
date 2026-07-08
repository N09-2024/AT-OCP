package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "risques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Risque {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nomRisque;

    private String descriptionRisque;

    private String niveau;
}
