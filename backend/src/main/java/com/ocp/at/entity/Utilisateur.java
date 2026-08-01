package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(unique = true, nullable = false)
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    private String telephone;

    @Column(nullable = false)
    @ToString.Exclude
    private String motDePasse;

    private String photo;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    private LocalDateTime derniereConnexion;

    @Builder.Default
    private int compteurEchecsConnexion = 0;

    @Builder.Default
    private boolean compteVerrouille = false;

    @Builder.Default
    private boolean motDePasseExpire = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean enAttenteValidation = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean inscriptionRejetee = false;

    /**
     * Service d'appartenance de l'utilisateur.
     *
     * FONDAMENTAL pour la résolution contextuelle P/E :
     * - Si service == zoneProprietaire.service de l'AT → position Propriétaire (P)
     * - Si service == zoneExecutante.service de l'AT → position Exécutant (E)
     *
     * Un même utilisateur peut être côté P sur une AT et côté E sur une autre.
     * Nullable à la création du compte ; obligatoire pour toute action de workflow sur une AT.
     *
     * @see com.ocp.at.security.ATContextService
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Service service;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "utilisateur_roles",
        joinColumns = @JoinColumn(name = "utilisateur_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Fetch(FetchMode.JOIN)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>();
}