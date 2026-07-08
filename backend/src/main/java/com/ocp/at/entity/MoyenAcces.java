package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "moyens_acces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoyenAcces {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nomMoyen;

    private String descriptionMoyen;
}
