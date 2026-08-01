package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String path;

    private String typeMime;

    private Long taille;

    private LocalDateTime dateCreation;

    private Integer ordre;

    private String legende;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visite_prealable_id")
    @ToString.Exclude
    private VisitePrealable visitePrealable;
}
