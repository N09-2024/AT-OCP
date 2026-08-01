-- ============================================================
-- V23 — Insertion des référentiels officiels du Formulaire S-HSE-SEC-31-04
-- Source : Formulaire Autorisation de Travail (F-HSE-SEC-31-04 Edition 1.0)
-- ============================================================

-- ------------------------------------------------------------
-- CADRAN A : RISQUES ÉVALUÉS LIÉS À / AU
-- ------------------------------------------------------------
INSERT INTO risques (id, nom_risque, description_risque, niveau) VALUES
    ('r_hauteur',             'Travail en hauteur',               'Chute de hauteur, travail sur échafaudage/passerelle', 'HAUT'),
    ('r_reseaux_enterres',    'Proximité aux réseaux enterrés',    'Réseaux électriques, gaz, eau ou tuyauteries enterrées', 'HAUT'),
    ('r_inflammables',        'Produits inflammables',            'Présence ou manipulation de liquides/gaz inflammables', 'HAUT'),
    ('r_manutention_manuelle','Manutention manuelle',            'Port de charges lourdes, gestes répétitifs', 'MOYEN'),
    ('r_manutention_meca',    'Manutention mécanique',            'Levage, grues, ponts roulants, engins de levage', 'HAUT'),
    ('r_outillage',           'Outillage',                        'Utilisation d outils portatifs, tranchants ou mécaniques', 'MOYEN'),
    ('r_bruit',               'Bruit (> 80 dB)',                  'Exposition à un niveau sonore élevé supérieur à 80 dB', 'MOYEN'),
    ('r_circulation_pers',    'Circulation personnes',            'Flux de piétons, coactivité sur les voies de passage', 'FAIBLE'),
    ('r_produits_chimiques',  'Produits chimiques',              'Produits corrosifs, toxiques, acides ou réactifs', 'HAUT'),
    ('r_eclairage',           'Éclairage insuffisant',            'Travail en zone sombre ou visibilité réduite', 'FAIBLE'),
    ('r_intemperies',         'Intempéries',                      'Pluie, vent fort, chaleur extrême, foudre', 'MOYEN'),
    ('r_poussiere',           'Ambiance poussiéreuse',            'Inhalation de poussières fines, minérales ou acides', 'MOYEN'),
    ('r_circulation_veh',     'Circulation véhicules',            'Engins de chantier, camions, chariots élévateurs', 'MOYEN'),
    ('r_coactivite',          'Co-activité',                      'Intervention simultanée de plusieurs entreprises/équipes', 'HAUT'),
    ('r_machines_tournantes', 'Machines tournantes',              'Organes en mouvement, pièces en rotation sans carter', 'HAUT'),
    ('r_produits_chauds',     'Produits chauds',                  'Surfaces brûlantes, vapeur, fluides thermiques', 'HAUT'),
    ('r_pression',            'Équipement sous pression',         'Tuyauteries, réservoirs, cuves sous pression', 'HAUT'),
    ('r_electricite',         'Électricité',                      'Risque d électrocution, armoires électriques, lignes', 'HAUT'),
    ('r_espaces_confines',    'Espaces confinés',                 'Cuves, silos, puits, capacités fermées', 'HAUT'),
    ('r_atex',                'Zone ATEX',                        'Atmosphère explosive (gaz, vapeurs, poussières)', 'HAUT'),
    ('r_noyade',              'Noyade',                           'Proximité de bassins, décanteurs, conduites d eau', 'HAUT')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- CADRAN B : MESURES PRISES POUR PRÉPARER L'INTERVENTION
-- ------------------------------------------------------------
INSERT INTO mesures_preparation (id, nom_mesure, description_mesure) VALUES
    ('m_vidange',         'Vidange de l équipement et ses circuits', 'Vidange complète des liquides/gaz avant intervention'),
    ('m_consignation',     'Consignation des Énergies',                'Consignation électrique, mécanique, fluide avec cadenas'),
    ('m_eclairage',       'Éclairage',                                'Mise en place d un éclairage d appoint sécurisé'),
    ('m_depressurisation', 'Dépressurisation',                         'Chute de la pression à zéro dans les conduites/capacités'),
    ('m_ventilation',     'Ventilation',                              'Ventilation forcée ou naturelle de la zone d intervention'),
    ('m_nettoyage',       'Nettoyage',                                'Lavage, décapage ou dégazage préalable'),
    ('m_balisage',        'Balisage',                                 'Mise en place de bandes de balisage et panneaux d avertissement')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- CADRAN C : MOYENS D'ACCÈS NÉCESSAIRES
-- ------------------------------------------------------------
INSERT INTO moyens_acces (id, nom_moyen, description_moyen) VALUES
    ('ma_escabeau',    'Escabeau',       'Escabeau sécurisé conforme aux normes'),
    ('ma_echafaudage', 'Échafaudage',    'Échafaudage fixe ou roulant réceptionné et contrôlé'),
    ('ma_passerelle',  'Passerelle',     'Passerelle d accès avec garde-corps'),
    ('ma_pemp',        'Nacelle, PEMP',  'Plateforme élévatrice mobile de personnel (PEMP / Nacelle)')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- CADRAN D : EPI SPÉCIFIQUES NÉCESSAIRES
-- ------------------------------------------------------------
INSERT INTO epis (id, nomepi, descriptionepi) VALUES
    ('epi_casque_soudure',  'Casque soudure',         'Masque/cagoule de soudeur avec verre teinté'),
    ('epi_masque_gaz',      'Masque à gaz',           'Masque avec cartouche filtrante spécifique gaz'),
    ('epi_masque_pano',     'Masque panoramique',     'Masque complet de protection faciale et respiratoire'),
    ('epi_masque_poussiere','Masque à poussières',    'Masque FFP2 / FFP3 contre les poussières'),
    ('epi_lunettes',        'Lunettes étanches',      'Lunettes de sécurité étanches contre projections'),
    ('epi_harnais',         'Harnais de sécurité',    'Harnais anticaute avec longe et absorbeur d énergie'),
    ('epi_ari',             'ARI',                    'Appareil Respiratoire Isolant à autonomie d air'),
    ('epi_stop_bruit',      'Stop bruit',             'Bouchons d oreilles ou casque antibruit'),
    ('epi_cagoule',         'Cagoule',                'Cagoule de protection thermique ou chimique'),
    ('epi_gants_antiacides','Gants antiacides',       'Gants étanches résistant aux acides et bases'),
    ('epi_gants_manut',     'Gants de manutention',   'Gants de protection mécanique et coupure'),
    ('epi_tenue_antiacide', 'Tenue antiacide',        'Combinaison ou tablier de protection chimique'),
    ('epi_tablier_soudure', 'Tablier soudure',        'Tablier en cuir de protection soudeur'),
    ('epi_guetres',         'Guêtres',                'Guêtres de protection des pieds et jambes'),
    ('epi_ecran_facial',    'Écran facial',           'Visière de protection intégrale du visage'),
    ('epi_bottes_securite', 'Bottes de sécurité',     'Bottes de sécurité coquées et anti-perforation')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- CADRAN E : PERMIS NÉCESSAIRES
-- ------------------------------------------------------------
INSERT INTO types_permis (id, nom, description) VALUES
    ('tp_confine',     'ESPACE_CONFINE',   'Permis pour espace confiné'),
    ('tp_hauteur',     'TRAVAIL_HAUTEUR',  'Permis pour travail en hauteur'),
    ('tp_feu',         'FEU',              'Permis de feu'),
    ('tp_fouille',     'FOUILLE',          'Permis de fouille'),
    ('tp_consignation', 'CONSIGNATION',    'Plan de consignation')
ON CONFLICT (id) DO NOTHING;
