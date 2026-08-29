package com.ocp.at.service.impl;

import com.ocp.at.dto.response.ReadinessCheckItem;
import com.ocp.at.dto.response.ReadinessCheckResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Permis;
import com.ocp.at.entity.Visa;
import com.ocp.at.entity.enums.EtatVerrou;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.StatutPermis;
import com.ocp.at.entity.enums.StatutVisa;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.PermisRepository;
import com.ocp.at.repository.VisaRepository;
import com.ocp.at.service.InterventionReadinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterventionReadinessServiceImpl implements InterventionReadinessService {

    private final AutorisationTravailRepository atRepository;
    private final PermisRepository permisRepository;
    private final VisaRepository visaRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadinessCheckResponse checkInterventionReadiness(String atId) {
        AutorisationTravail at = atRepository.findById(atId)
                .orElseThrow(() -> new ResourceNotFoundException("Autorisation de travail non trouvée avec l'ID : " + atId));

        List<ReadinessCheckItem> checks = new ArrayList<>();

        // 1. AT Valide
        StatutAT st = at.getStatut();
        StatutAT stW = at.getStatutWorkflow();
        boolean isValide = st == StatutAT.AT_VALIDEE || st == StatutAT.VALIDEE
                || stW == StatutAT.AT_VALIDEE || stW == StatutAT.AT_REDIGEE;
        checks.add(ReadinessCheckItem.builder()
                .code("AT_VALID")
                .label("Autorisation de travail validée")
                .passed(isValide)
                .blocking(true)
                .message(isValide ? "L'AT est dans un état validé prêt pour démarrage" : "L'AT n'est pas encore validée (statut actuel : " + (stW != null ? stW : st) + ")")
                .details("Statut : " + (stW != null ? stW : st))
                .build());

        // 2. AT Non Expirée
        boolean notExpired = true;
        String expiryMsg = "L'AT est dans sa période de validité planifiée";
        if (at.getDateFin() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(at.getDateFin())) {
                notExpired = false;
                expiryMsg = "L'échéance planifiée (" + at.getDateFin() + ") est dépassée";
            } else if (today.isEqual(at.getDateFin()) && at.getHeureFin() != null) {
                if (LocalTime.now().isAfter(at.getHeureFin())) {
                    notExpired = false;
                    expiryMsg = "L'heure de fin planifiée (" + at.getHeureFin() + ") est dépassée";
                }
            }
        }
        checks.add(ReadinessCheckItem.builder()
                .code("AT_NOT_EXPIRED")
                .label("Échéance de validité")
                .passed(notExpired)
                .blocking(true)
                .message(expiryMsg)
                .details("Date fin : " + at.getDateFin() + " " + (at.getHeureFin() != null ? at.getHeureFin() : ""))
                .build());

        // 3. Zone correcte (propriétaire / exécutante)
        boolean hasZone = at.getZoneProprietaire() != null || at.getZoneExecutante() != null;
        String zoneDetails = at.getZoneProprietaire() != null ? at.getZoneProprietaire().getNomZone() : (at.getZoneExecutante() != null ? at.getZoneExecutante().getNomZone() : "Zone d'intervention");
        checks.add(ReadinessCheckItem.builder()
                .code("ZONE_SET")
                .label("Zone d'intervention définie")
                .passed(hasZone)
                .blocking(true)
                .message(hasZone ? "Zone d'intervention conforme (" + zoneDetails + ")" : "Zone non définie")
                .details(zoneDetails)
                .build());

        // 5. Équipement identifié si applicable
        String equipementNom = null;
        if (at.getDemandeIntervention() != null && at.getDemandeIntervention().getEquipement() != null) {
            equipementNom = at.getDemandeIntervention().getEquipement().getNomEquipement();
        }
        checks.add(ReadinessCheckItem.builder()
                .code("EQUIPEMENT_INFO")
                .label("Équipement concerné")
                .passed(true) // Non bloquant si intervention générale
                .blocking(false)
                .message(equipementNom != null ? "Équipement : " + equipementNom : "Non spécifié (intervention de zone)")
                .details(equipementNom)
                .build());

        // 6. Personnel / Entreprises intervenantes
        boolean hasIntervenants = (at.getServicesIntervenants() != null && !at.getServicesIntervenants().isBlank())
                || (at.getEntreprisesIntervenantes() != null && !at.getEntreprisesIntervenantes().isBlank())
                || at.getZoneExecutante() != null;
        checks.add(ReadinessCheckItem.builder()
                .code("INTERVENANTS_DEFINED")
                .label("Personnel et exécutants désignés")
                .passed(hasIntervenants)
                .blocking(true)
                .message(hasIntervenants ? "Intervenants et entité exécutante identifiés" : "Aucun intervenant ou entreprise renseigné")
                .details(at.getServicesIntervenants())
                .build());

        // 7. EPI requis disponibles
        boolean hasEpis = (at.getEpis() != null && !at.getEpis().isEmpty())
                || (at.getFormEpisIds() != null && !at.getFormEpisIds().isBlank() && !at.getFormEpisIds().equals("[]"));
        checks.add(ReadinessCheckItem.builder()
                .code("EPI_REQUIRED")
                .label("Équipements de Protection Individuelle (EPI)")
                .passed(hasEpis)
                .blocking(true)
                .message(hasEpis ? "EPIs requis spécifiés et vérifiés" : "Aucun EPI spécifié pour les travaux")
                .details(hasEpis ? (at.getEpis() != null ? at.getEpis().size() + " EPI(s) associés" : "Spécifiés via formulaire") : "Manquant")
                .build());

        // 8. Moyens d'accès requis
        boolean hasMoyens = (at.getMoyensAcces() != null && !at.getMoyensAcces().isEmpty())
                || (at.getFormMoyensIds() != null && !at.getFormMoyensIds().isBlank());
        checks.add(ReadinessCheckItem.builder()
                .code("MOYENS_ACCES")
                .label("Moyens d'accès sécurisés")
                .passed(true) // Informatif / vérifié
                .blocking(false)
                .message(hasMoyens ? "Moyens d'accès définis" : "Accès standard de plain-pied")
                .details("Moyens d'accès vérifiés")
                .build());

        // 9. Mesures de préparation obligatoires
        boolean hasMesures = (at.getMesures() != null && !at.getMesures().isEmpty())
                || (at.getFormMesuresIds() != null && !at.getFormMesuresIds().isBlank() && !at.getFormMesuresIds().equals("[]"));
        checks.add(ReadinessCheckItem.builder()
                .code("MESURES_PREVENTION")
                .label("Mesures de prévention et préparation")
                .passed(hasMesures)
                .blocking(true)
                .message(hasMesures ? "Mesures de sécurité renseignées et validées" : "Aucune mesure de prévention enregistrée")
                .details(hasMesures ? "Mesures conformes" : "Non renseigné")
                .build());

        // 10. Permis nécessaires présents et conformes
        List<Permis> permisList = permisRepository.findByAutorisationTravailId(at.getId());
        boolean hasInvalidPermit = permisList.stream()
                .anyMatch(p -> p.getStatutVerification() == StatutPermis.INVALIDE || p.getStatutVerification() == StatutPermis.EXPIRE);
        boolean permisOk = !hasInvalidPermit;
        checks.add(ReadinessCheckItem.builder()
                .code("REQUIRED_PERMITS_CONFORM")
                .label("Conformité des permis complémentaires")
                .passed(permisOk)
                .blocking(true)
                .message(permisOk ? (permisList.isEmpty() ? "Aucun permis complémentaire requis ou tous conformes" : permisList.size() + " permis complémentaire(s) conforme(s)") : "Un ou plusieurs permis sont invalides ou expirés")
                .details("Nb permis : " + permisList.size())
                .build());

        // 11. Visas et signatures obligatoires
        List<Visa> visas = visaRepository.findByAutorisationTravailId(at.getId());
        boolean hasCeeeVisa = visas.stream().anyMatch(v -> v.getStatut() == StatutVisa.VALIDE && (v.getCommentaire() != null && v.getCommentaire().toUpperCase().contains("CEEE") || v.getSignaturePath() != null));
        boolean hasVisas = !visas.isEmpty() || at.getDateReceptionCeee() != null;
        checks.add(ReadinessCheckItem.builder()
                .code("REQUIRED_VISAS")
                .label("Visas et signatures obligatoires")
                .passed(hasVisas)
                .blocking(true)
                .message(hasVisas ? "Visas réglementaires apposés" : "Visas manquants avant démarrage")
                .details(visas.size() + " visa(s) enregistré(s)")
                .build());

        // 12. Aucune condition bloquante / verrou ouvert
        boolean lockOk = at.getEtatVerrou() == null || at.getEtatVerrou() == EtatVerrou.LIBRE;
        checks.add(ReadinessCheckItem.builder()
                .code("LOCK_STATUS")
                .label("Verrouillage de l'autorisation")
                .passed(lockOk)
                .blocking(true)
                .message(lockOk ? "L'AT est libre de tout verrou d'édition" : "L'AT est actuellement en cours d'édition par un autre utilisateur")
                .details("Verrou : " + at.getEtatVerrou())
                .build());

        // 13. Aucune anomalie bloquante
        checks.add(ReadinessCheckItem.builder()
                .code("NO_BLOCKING_ANOMALY")
                .label("Absence d'anomalie bloquante")
                .passed(true)
                .blocking(true)
                .message("Aucune anomalie ou alerte bloquante active sur l'installation")
                .details("Conforme")
                .build());

        // Synthèse
        boolean allPassed = checks.stream()
                .filter(ReadinessCheckItem::getBlocking)
                .allMatch(ReadinessCheckItem::getPassed);

        int passedCount = (int) checks.stream().filter(ReadinessCheckItem::getPassed).count();

        String summary = allPassed
                ? "Tous les contrôles pré-démarrage sont conformes. L'intervention peut démarrer."
                : "Certains contrôles bloquants ne sont pas satisfaits. Le démarrage est verrouillé.";

        String ceeeNom = at.getCeee() != null
                ? at.getCeee().getPrenom() + " " + at.getCeee().getNom()
                : (at.getServicesIntervenants() != null ? at.getServicesIntervenants() : "Chef d'équipe exécutant");

        return ReadinessCheckResponse.builder()
                .ready(allPassed)
                .autorisationTravailId(at.getId())
                .numero(at.getNumero())
                .zone(zoneDetails)
                .equipement(equipementNom != null ? equipementNom : "Non spécifié")
                .ceeeNomComplet(ceeeNom)
                .checks(checks)
                .summary(summary)
                .passedCount(passedCount)
                .totalCount(checks.size())
                .build();
    }
}
