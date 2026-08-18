from typing import List, Dict

OCP_OFFICIAL_KNOWLEDGE: List[Dict[str, str]] = [
    {
        "id": "DOC_SEC31_01",
        "title": "Standard OCP S-HSE-SEC-31 : Autorisation de Travail et Logigramme",
        "source": "Standard OCP S-HSE-SEC-31 §7-§8",
        "content": (
            "Le Standard S-HSE-SEC-31 définit les règles applicables pour toute intervention sur les sites OCP. "
            "Le workflow standard comprend 9 étapes obligatoires : "
            "1. Demande d'intervention créée (DI/OT/BT) "
            "2. Visite préalable de chantier réalisée conjointement par le CEEP (Chargé d'Exploitation de l'entité Propriétaire) "
            "   et le CEEE (Chargé d'Exécution de l'entité Exécutante) "
            "3. Autorisation de Travail rédigée sur le formulaire F-HSE-SEC-31-04 "
            "4. Signature du visa CEEP (émetteur propriétaire) validant la préparation et les consignations "
            "5. Signature du visa CEEE (preneur exécutant) attestant de la prise en compte des risques et consignes "
            "6. Visas hiérarchiques requis selon le niveau (Niveau 1 : HCEP/HCEE, Niveau 2 : HMEP/HMEE) "
            "7. Démarrage et intervention en cours "
            "8. Fin des travaux et remise en état des installations "
            "9. Réception contradictoire des travaux et archivage du dossier."
        )
    },
    {
        "id": "DOC_SEC31_02",
        "title": "Formulaire F-HSE-SEC-31-04 : Sections A à G",
        "source": "Formulaire F-HSE-SEC-31-04 (OCP)",
        "content": (
            "Le formulaire F-HSE-SEC-31-04 est le document unique d'autorisation de travail. "
            "Section A : Risques évalués (Travail en hauteur, Espaces confinés, Produits inflammables, Electricité, "
            "Produits chimiques, Proximité aux réseaux enterrés, Manutention manuelle/mécanique, Outillage, Bruit, "
            "Co-activité, Machines tournantes, Produits chauds, Equipement sous pression, Zone ATEX, Noyade). "
            "Section B : Mesures de préparation (Vidange de l'équipement, Consignation des Energies, Eclairage, "
            "Dépressurisation, Ventilation, Nettoyage, Balisage). "
            "Section C : Moyens d'accès (Échafaudage, Échelle, Nacelle, Plateforme). "
            "Section D : EPI obligatoires (Casque soudure, Masque à gaz, Masque panoramique, Masque à poussières, "
            "Lunettes étanches, Harnais de sécurité, ARI, Stop bruit, Gants antiacides, Gants de manutention, "
            "Tenue antiacide, Tablier soudure, Guêtres, Ecran facial, Bottes de sécurité). "
            "Section E : Permis complémentaires obligatoires (Permis pour espace confiné, Permis de feu, "
            "Plan de consignation, Permis pour travail en hauteur, Permis de fouille). "
            "Section F : Mesures de sécurité de l'exécutant. "
            "Section G : Visas et signatures formelles."
        )
    },
    {
        "id": "DOC_SEC31_03",
        "title": "Règles de croisement et permis obligatoires",
        "source": "Standard OCP S-HSE-SEC-31 §10 (Permis spécifiques)",
        "content": (
            "Croisements obligatoires selon le standard HSE OCP : "
            "- 'Travail en hauteur' (>1.80m ou risque de chute) : Permis pour travail en hauteur obligatoire, "
            "port du Harnais de sécurité avec double longe et point d'ancrage vérifié. "
            "- 'Espaces confinés' (bacs, cuves, fosses, canalisations) : Permis pour espace confiné obligatoire, "
            "mesure de Ventilation préalable, contrôle d'atmosphère (O2, explosimétrie, toxicité) et présence d'un surveillant à l'extérieur + ARI si nécessaire. "
            "- 'Produits inflammables' ou travaux à points chauds (soudure, meulage, découpe) : Permis de feu obligatoire, "
            "extincteurs à proximité immédiate, écran de protection et surveillance post-intervention (au moins 2h). "
            "- 'Electricité' : Consignation des Énergies obligatoire avec cadenas de consignation, vérification d'absence de tension (VAT) et Plan de consignation joint. "
            "- 'Proximité aux réseaux enterrés' : Permis de fouille obligatoire, repérage préalable des réseaux et interdiction d'engins mécaniques à moins d'un mètre des canalisations. "
            "- 'Produits chimiques / Acides' : Gants antiacides, Lunettes étanches, Visière faciale et Tenue antiacide complètes."
        )
    },
    {
        "id": "DOC_SEC31_04",
        "title": "Rôles et Responsabilités dans le processus AT",
        "source": "Standard OCP S-HSE-SEC-31 §6 (Gouvernance et Acteurs)",
        "content": (
            "Les acteurs clés de l'Autorisation de Travail : "
            "- CEEP (Chargé d'Exploitation de l'Entité Propriétaire) : responsable de la mise en sécurité de l'installation, "
            "de la visite préalable, de la définition des mesures de préparation et de la délivrance initiale de l'AT. "
            "- CEEE (Chargé d'Exécution de l'Entité Exécutante) : responsable de l'équipe d'intervention, du respect des consignes HSE, "
            "de la dotation des EPI à son personnel et de la déclaration de fin de travaux. "
            "- HCEP / HCEE (Hiérarchie Chargé Exploitation / Exécution Propriétaire & Exécutant) : validation hiérarchique niveau 1. "
            "- HMEP / HMEE (Hiérarchie Maintenance / Entité Propriétaire & Exécutante) : validation niveau 2 pour travaux critiques. "
            "- L'IA est un outil d'aide et de recommandation : elle ne peut en aucun cas se substituer à la signature d'un responsable humain."
        )
    }
]
