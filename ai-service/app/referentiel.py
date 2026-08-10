"""
Référentiel officiel extrait du formulaire F-HSE-SEC-31-04 fourni.
Injecté dans les prompts pour que l'IA reste alignée sur les libellés
exacts du standard OCP (et non des libellés inventés).
"""

RISQUES_OFFICIELS = [
    "Travail en hauteur", "Proximité aux réseaux enterrés", "Produits inflammables",
    "Manutention manuelle", "Manutention mécanique", "Outillage", "Bruit (> 80 dB)",
    "Circulation personnes", "Produits chimiques", "Eclairage insuffisant",
    "Intempéries", "Ambiance poussiéreuse", "Circulation véhicules", "Co-activité",
    "Machines tournantes", "Produits chauds", "Equipement sous pression",
    "Electricité", "Espaces confinés", "Zone ATEX", "Noyade",
]

MESURES_OFFICIELLES = [
    "Vidange de l'équipement et ses circuits", "Consignation des Energies",
    "Eclairage", "Dépressurisation", "Ventilation", "Nettoyage", "Balisage",
]

EPIS_OFFICIELS = [
    "Casque soudure", "Masque à gaz", "Masque panoramique", "Masque à poussières",
    "Lunettes étanches", "Harnais de sécurité", "ARI", "Stop bruit", "Cagoule",
    "Gants antiacides", "Gants de manutention", "Tenue antiacide",
    "Tablier soudure", "Guêtres", "Ecran facial", "Bottes de sécurité",
]

PERMIS_OFFICIELS = [
    "Permis pour espace confiné", "Permis de feu", "Plan de consignation",
    "Permis pour travail en hauteur", "Permis de fouille",
]

REGLES_HSE = """
Règles de croisement risque → permis obligatoire (Standard S-HSE-SEC-31 §10) :
- "Travail en hauteur" ⇒ permis "Permis pour travail en hauteur" + EPI "Harnais de sécurité"
- "Espaces confinés" ⇒ permis "Permis pour espace confiné" + EPI "ARI" + mesure "Ventilation"
- "Produits inflammables" ou travaux par points chauds (soudure, meulage) ⇒ permis "Permis de feu"
- "Electricité" ⇒ mesure "Consignation des Energies" + permis "Plan de consignation"
- "Proximité aux réseaux enterrés" / fouille / tranchée ⇒ permis "Permis de fouille"
- "Produits chimiques" ⇒ EPI "Gants antiacides" + "Lunettes étanches" + "Tenue antiacide"
"""
