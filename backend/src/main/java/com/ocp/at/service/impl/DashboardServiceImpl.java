package com.ocp.at.service.impl;

import com.ocp.at.dto.response.DashboardDataResponse;
import com.ocp.at.dto.response.DashboardDataResponse.AtSummaryDto;
import com.ocp.at.dto.response.DashboardDataResponse.KpiStats;
import com.ocp.at.dto.response.DashboardDataResponse.MonthlyStat;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.StatutPermis;
import com.ocp.at.entity.enums.StatutVisa;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.repository.ArchiveRepository;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.PermisRepository;
import com.ocp.at.repository.ReceptionTravauxRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.repository.VisaRepository;
import com.ocp.at.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AutorisationTravailRepository autorisationTravailRepository;
    private final VisaRepository visaRepository;
    private final PermisRepository permisRepository;
    private final ReceptionTravauxRepository receptionTravauxRepository;
    private final ArchiveRepository archiveRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardDataResponse getDashboardStats(String email) {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getNom().equals("ADMIN"));
        
        // 1. KPIs
        long atEnCours;
        long visasEnAttente;
        long permisActifs;
        long receptionsEnAttente;
        long totalArchives;
        
        if (isAdmin) {
            atEnCours = autorisationTravailRepository.countByStatut(StatutAT.VALIDEE);
            if (atEnCours == 0) {
                atEnCours = autorisationTravailRepository.countByStatut(StatutAT.SOUMISE); // Fallback
            }
            visasEnAttente = visaRepository.countByStatut(StatutVisa.EN_ATTENTE);
            permisActifs = permisRepository.countByStatutVerification(StatutPermis.CONFORME);
            receptionsEnAttente = receptionTravauxRepository.countPendingReceptions();
            totalArchives = archiveRepository.count();
        } else {
            atEnCours = autorisationTravailRepository.countByProprietaireBrouillonIdAndStatut(user.getId(), StatutAT.VALIDEE);
            if (atEnCours == 0) {
                atEnCours = autorisationTravailRepository.countByProprietaireBrouillonIdAndStatut(user.getId(), StatutAT.SOUMISE);
            }
            // Approximation for Demandeur (could be refined with custom queries)
            visasEnAttente = 0; // Or create a query
            permisActifs = 0;
            receptionsEnAttente = 0;
            totalArchives = 0;
        }

        KpiStats kpis = KpiStats.builder()
                .autorisationsEnCours(atEnCours)
                .visasEnAttente(visasEnAttente)
                .permisActifs(permisActifs)
                .receptionsEnAttente(receptionsEnAttente)
                .totalArchives(totalArchives)
                .build();

        // 2. Status Distribution
        List<Object[]> statusCounts = isAdmin ? 
            autorisationTravailRepository.countByStatutGrouped() :
            autorisationTravailRepository.countByStatutGroupedForUser(user.getId());
            
        Map<String, Long> distribution = new HashMap<>();
        for (Object[] result : statusCounts) {
            StatutAT statut = (StatutAT) result[0];
            Long count = (Long) result[1];
            if (statut != null) {
                distribution.put(statut.name(), count);
            }
        }

        // 3. Monthly Stats
        List<Object[]> monthlyResults = isAdmin ? 
            autorisationTravailRepository.countAtByMonth() :
            autorisationTravailRepository.countAtByMonthForUser(user.getId());
            
        List<MonthlyStat> monthlyStats = new ArrayList<>();
        for (Object[] result : monthlyResults) {
            String mois = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            monthlyStats.add(new MonthlyStat(mois, count));
        }

        // 4. Recent ATs for this user or all if ADMIN
        List<AutorisationTravail> recentAts = isAdmin ? 
            autorisationTravailRepository.findTop5ByOrderByDateCreationDesc() : 
            autorisationTravailRepository.findTop5ByProprietaireBrouillonIdOrderByDateCreationDesc(user.getId());
        
        List<AtSummaryDto> recentAtDtos = recentAts.stream().map(at -> {
            String installation = "N/A";
            if (at.getDemandeIntervention() != null && at.getDemandeIntervention().getEquipement() != null) {
                installation = at.getDemandeIntervention().getEquipement().getNomEquipement();
            }
            return AtSummaryDto.builder()
                    .id(at.getId())
                    .titre(at.getObjet())
                    .installation(installation)
                    .statut(at.getStatut() != null ? at.getStatut().name() : "N/A")
                    .echeance(at.getDateFin() != null ? at.getDateFin().toString() : "N/A")
                    .build();
        }).collect(Collectors.toList());

        return DashboardDataResponse.builder()
                .kpis(kpis)
                .statusDistribution(distribution)
                .monthlyStats(monthlyStats)
                .recentAutorisations(recentAtDtos)
                .build();
    }
}
