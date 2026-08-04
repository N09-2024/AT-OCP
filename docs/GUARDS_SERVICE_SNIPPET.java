/**
 * Exemples d'appels des guards ATContextService
 * à placer dans AutorisationTravailServiceImpl (et services liés).
 *
 * Injecter : private final ATContextService atContextService;
 */

/*

// Création demande / AT
@Override
@Transactional
public AutorisationTravailResponse createDirect() {
    atContextService.requireCreerDemande();
    atContextService.verifierServiceRattache();
    // ... reste de la méthode
}

// Classification
@Override
@Transactional
public AutorisationTravailResponse classifierIntervention(...) {
    atContextService.requireClassifier();
    // ...
}

// Visite
@Override
@Transactional
public AutorisationTravailResponse marquerVisiteRealisee(String id) {
    atContextService.requireVisite(id);
    // ...
}

// Rédaction / soumission
@Override
@Transactional
public AutorisationTravailResponse soumettreAT(String id) {
    atContextService.requireRedaction(id);
    // ...
}

// Démarrage
@Override
@Transactional
public AutorisationTravailResponse demarrerIntervention(String id) {
    atContextService.requireDemarrer(id);
    // ...
}

// Fin travaux
@Override
@Transactional
public AutorisationTravailResponse declarerFinTravaux(String id) {
    atContextService.requireDeclarerFin(id);
    // ...
}

// Réception
@Override
@Transactional
public AutorisationTravailResponse receptionnerTravauxStandard(String id) {
    atContextService.requireReception(id);
    // ...
}

// Reconduction
@Override
@Transactional
public AutorisationTravailResponse reconduireAT(String id, boolean depasse24h) {
    atContextService.requireReconduction(id);
    // ...
}

*/
