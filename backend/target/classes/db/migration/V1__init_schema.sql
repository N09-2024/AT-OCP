-- ============================================================
-- V1__init_schema.sql
-- Schema initial généré depuis les entités Java (source de vérité)
-- OCP Autorisation de Travail - Backend Spring Boot
-- ============================================================

-- -----------------------------------------------
-- Tables sans dépendances (entités autonomes)
-- -----------------------------------------------

create table zones (
    id varchar(255) not null,
    code_zone varchar(255) not null unique,
    nom_zone varchar(255) not null,
    description_zone varchar(255),
    primary key (id)
);

create table services (
    id varchar(255) not null,
    code_service varchar(255) not null unique,
    nom_service varchar(255) not null,
    description_service varchar(255),
    zone_id varchar(255),
    primary key (id)
);

create table installations (
    id varchar(255) not null,
    code_installation varchar(255) not null unique,
    nom_installation varchar(255) not null,
    atelier varchar(255),
    localisation varchar(255),
    service_id varchar(255),
    primary key (id)
);

create table equipements (
    id varchar(255) not null,
    code_equipement varchar(255) not null unique,
    nom_equipement varchar(255) not null,
    description_equipement varchar(255),
    installation_id varchar(255),
    primary key (id)
);

create table entreprises_externes (
    id varchar(255) not null,
    nom_entreprise varchar(255) not null,
    responsable varchar(255),
    adresse varchar(255),
    telephone varchar(255),
    primary key (id)
);

create table roles (
    id varchar(255) not null,
    nom varchar(255) not null unique,
    description varchar(255),
    primary key (id)
);

create table permissions (
    id varchar(255) not null,
    nom varchar(255) not null unique,
    description varchar(255),
    primary key (id)
);

create table epis (
    id varchar(255) not null,
    nomepi varchar(255) not null,
    descriptionepi varchar(255),
    primary key (id)
);

create table mesures_preparation (
    id varchar(255) not null,
    nom_mesure varchar(255) not null,
    description_mesure varchar(255),
    primary key (id)
);

create table moyens_acces (
    id varchar(255) not null,
    nom_moyen varchar(255) not null,
    description_moyen varchar(255),
    primary key (id)
);

create table risques (
    id varchar(255) not null,
    nom_risque varchar(255) not null,
    description_risque varchar(255),
    niveau varchar(255),
    primary key (id)
);

-- -----------------------------------------------
-- Utilisateurs et sécurité
-- -----------------------------------------------

create table utilisateurs (
    id varchar(255) not null,
    matricule varchar(255) not null unique,
    nom varchar(255) not null,
    prenom varchar(255) not null,
    email varchar(255) not null unique,
    telephone varchar(255),
    mot_de_passe varchar(255) not null,
    photo varchar(255),
    actif boolean not null,
    date_creation timestamp(6) not null,
    date_modification timestamp(6),
    derniere_connexion timestamp(6),
    compteur_echecs_connexion integer not null,
    compte_verrouille boolean not null,
    mot_de_passe_expire boolean not null,
    primary key (id)
);

create table utilisateur_roles (
    utilisateur_id varchar(255) not null,
    role_id varchar(255) not null,
    primary key (utilisateur_id, role_id)
);

create table role_permissions (
    role_id varchar(255) not null,
    permission_id varchar(255) not null,
    primary key (role_id, permission_id)
);

create table refresh_tokens (
    id varchar(255) not null,
    token varchar(512) not null unique,
    expiry_date timestamp(6) with time zone not null,
    date_creation timestamp(6) with time zone not null,
    adresseip varchar(255),
    user_agent varchar(255),
    revoked boolean not null,
    utilisateur_id varchar(255) not null,
    primary key (id)
);

-- -----------------------------------------------
-- Documents sources (DI, OT, BT)
-- -----------------------------------------------

create table visites_prealables (
    id varchar(255) not null,
    effectuee boolean not null,
    date_heure_debut timestamp(6),
    date_heure_fin timestamp(6),
    latitude float(53),
    longitude float(53),
    commentaire TEXT,
    visiteur_id varchar(255),
    primary key (id)
);

create table demandes_intervention (
    id varchar(255) not null,
    numero varchar(30) not null unique,
    objet varchar(255) not null,
    description TEXT,
    priorite varchar(255),
    date_demande timestamp(6),
    statut varchar(255) not null check (statut in ('BROUILLON','SOUMIS','EN_COURS','EN_ATTENTE_VISITE','EN_ATTENTE_ANALYSE','AT_GENEREE','CLOS','ANNULE')),
    type_intervention varchar(255) not null check (type_intervention in ('CURATIVE','PREVENTIVE','PLANIFIEE','URGENTE','EXCEPTIONNELLE','MECANIQUE')),
    niveau_intervention varchar(255) not null check (niveau_intervention in ('NIVEAU_1','NIVEAU_2')),
    demandeur_id varchar(255),
    installation_id varchar(255),
    equipement_id varchar(255),
    visite_prealable_id varchar(255) unique,
    primary key (id)
);

create table ordres_travail (
    id varchar(255) not null,
    numero varchar(30) not null unique,
    objet varchar(255) not null,
    description TEXT,
    date_creation timestamp(6),
    date_execution timestamp(6),
    statut varchar(255) not null check (statut in ('BROUILLON','SOUMIS','EN_COURS','EN_ATTENTE_VISITE','EN_ATTENTE_ANALYSE','AT_GENEREE','CLOS','ANNULE')),
    type_intervention varchar(255) not null check (type_intervention in ('CURATIVE','PREVENTIVE','PLANIFIEE','URGENTE','EXCEPTIONNELLE','MECANIQUE')),
    niveau_intervention varchar(255) not null check (niveau_intervention in ('NIVEAU_1','NIVEAU_2')),
    demandeur_id varchar(255),
    installation_id varchar(255),
    visite_prealable_id varchar(255) unique,
    primary key (id)
);

create table bons_travail (
    id varchar(255) not null,
    numero varchar(30) not null unique,
    objet varchar(255) not null,
    description TEXT,
    date_emission timestamp(6),
    statut varchar(255) not null check (statut in ('BROUILLON','SOUMIS','EN_COURS','EN_ATTENTE_VISITE','EN_ATTENTE_ANALYSE','AT_GENEREE','CLOS','ANNULE')),
    type_intervention varchar(255) not null check (type_intervention in ('CURATIVE','PREVENTIVE','PLANIFIEE','URGENTE','EXCEPTIONNELLE','MECANIQUE')),
    niveau_intervention varchar(255) not null check (niveau_intervention in ('NIVEAU_1','NIVEAU_2')),
    entreprise_externe_id varchar(255) not null,
    demandeur_id varchar(255),
    installation_id varchar(255),
    visite_prealable_id varchar(255) unique,
    primary key (id)
);

-- -----------------------------------------------
-- Analyse de risques
-- -----------------------------------------------

create table analyses_risques (
    id varchar(255) not null,
    date_analyse timestamp(6),
    commentaire TEXT,
    visite_prealable_id varchar(255) unique,
    analyseur_id varchar(255),
    primary key (id)
);

create table analyse_risque_risques (
    analyse_risque_id varchar(255) not null,
    risque_id varchar(255) not null,
    primary key (analyse_risque_id, risque_id)
);

create table analyse_risque_mesures (
    analyse_risque_id varchar(255) not null,
    mesure_id varchar(255) not null,
    primary key (analyse_risque_id, mesure_id)
);

create table analyse_risque_epis (
    analyse_risque_id varchar(255) not null,
    epi_id varchar(255) not null,
    primary key (analyse_risque_id, epi_id)
);

create table analyse_risque_moyens_acces (
    analyse_risque_id varchar(255) not null,
    moyen_id varchar(255) not null,
    primary key (analyse_risque_id, moyen_id)
);

create table visite_prealable_risques (
    visite_prealable_id varchar(255) not null,
    risque_id varchar(255) not null,
    primary key (visite_prealable_id, risque_id)
);

create table photos (
    id varchar(255) not null,
    nom varchar(255) not null,
    path varchar(255) not null,
    type_mime varchar(255),
    taille bigint,
    legende varchar(255),
    ordre integer,
    date_creation timestamp(6),
    visite_prealable_id varchar(255),
    primary key (id)
);

-- -----------------------------------------------
-- Autorisation de Travail
-- -----------------------------------------------

create table autorisations_travail (
    id varchar(255) not null,
    numero varchar(30) not null unique,
    objet varchar(255) not null,
    description_travaux varchar(255),
    date_debut date,
    date_fin date,
    heure_debut time(6),
    heure_fin time(6),
    date_creation timestamp(6),
    date_modification timestamp(6),
    version integer,
    statut varchar(255) check (statut in ('BROUILLON','SOUMISE','VALIDEE','REJETEE','RENOUVELEE','CLOTUREE','ARCHIVEE','ANNULEE')),
    etat_verrou varchar(255) check (etat_verrou in ('LIBRE','EN_COURS_EDITION','TRANSFERE')),
    proprietaire_brouillon_id varchar(255),
    date_prise_verrou timestamp(6),
    date_liberation_verrou timestamp(6),
    di_id varchar(255) unique,
    ot_id varchar(255) unique,
    bt_id varchar(255) unique,
    primary key (id)
);

create table visas (
    id varchar(255) not null,
    statut varchar(255) not null check (statut in ('EN_ATTENTE','VALIDE','REFUSE','VALIDATION','SIGNATURE','REFUS')),
    commentaire varchar(255),
    ordre integer,
    date_visa timestamp(6),
    date_signature timestamp(6),
    signature_path varchar(255),
    signature_hash varchar(255),
    adresseip varchar(255),
    navigateur varchar(255),
    utilisateur_id varchar(255) not null,
    at_id varchar(255) not null,
    primary key (id)
);

create table permis (
    id varchar(255) not null,
    type varchar(255) not null check (type in ('FEU','FOUILLE','TRAVAIL_HAUTEUR','ESPACE_CONFINE','CONSIGNATION')),
    numero varchar(255),
    date_emission date,
    date_expiration date,
    statut_verification varchar(255) check (statut_verification in ('A_VERIFIER','CONFORME','NON_CONFORME','EXPIRE','INVALIDE')),
    est_obligatoire boolean,
    commentaire TEXT,
    at_id varchar(255),
    primary key (id)
);

create table fichiers_joints (
    id varchar(255) not null,
    nom varchar(255) not null,
    path varchar(255) not null,
    type varchar(255),
    taille bigint,
    date_import timestamp(6),
    hashsha256 varchar(255),
    uploaded_by varchar(255),
    permis_id varchar(255) unique,
    primary key (id)
);

create table historiques_at (
    id varchar(255) not null,
    date_action timestamp(6),
    action varchar(255) not null check (action in ('CREATION','MODIFICATION','AUTO_SAVE','TRANSFERT','SOUMISSION','VALIDATION','REFUS','RENOUVELLEMENT','CLOTURE','EXPORT_PDF','ANNULATION','RECEPTION_TRAVAUX','VALIDATION_RECEPTION')),
    ancien_statut varchar(255) check (ancien_statut in ('BROUILLON','SOUMISE','VALIDEE','REJETEE','RENOUVELEE','CLOTUREE','ARCHIVEE','ANNULEE')),
    nouveau_statut varchar(255) check (nouveau_statut in ('BROUILLON','SOUMISE','VALIDEE','REJETEE','RENOUVELEE','CLOTUREE','ARCHIVEE','ANNULEE')),
    commentaire varchar(255),
    utilisateur_id varchar(255),
    at_id varchar(255) not null,
    primary key (id)
);

-- -----------------------------------------------
-- Réception des travaux
-- -----------------------------------------------

create table receptions_travaux (
    id varchar(255) not null,
    autorisation_travail_id varchar(255) not null unique,
    responsable_id varchar(255),
    date_reception timestamp(6),
    date_debut_travaux_reelle timestamp(6),
    date_fin_travaux_reelle timestamp(6),
    travaux_realises TEXT,
    travaux_conformes boolean not null,
    equipement_remis_en_service boolean not null,
    zone_nettoyee boolean not null,
    consignation_retiree boolean not null,
    essais_effectues boolean not null,
    resultat_essais TEXT,
    observations TEXT,
    commentaire_responsable TEXT,
    signature_path varchar(255),
    signature_date timestamp(6),
    signature_by varchar(255),
    signature_responsable varchar(255),
    date_signature timestamp(6),
    validee boolean,
    essais_conformes boolean,
    installation_remise_en_etat boolean,
    created_at timestamp(6),
    updated_at timestamp(6),
    primary key (id)
);

create table photos_reception (
    id varchar(255) not null,
    nom varchar(255) not null,
    path varchar(500) not null,
    taille bigint,
    mime_type varchar(100),
    ordre integer,
    legende TEXT,
    created_at timestamp(6),
    reception_travaux_id varchar(255) not null,
    primary key (id)
);

create table historiques_reception (
    id varchar(255) not null,
    date_action timestamp(6) not null,
    action varchar(100) not null,
    commentaire TEXT,
    utilisateur_id varchar(255),
    reception_travaux_id varchar(255) not null,
    primary key (id)
);

create table essais (
    id varchar(255) not null,
    reception_id varchar(255) not null,
    nom varchar(255) not null,
    description TEXT,
    resultat varchar(500),
    conforme boolean not null,
    commentaire TEXT,
    primary key (id)
);

create table remises_etat (
    id varchar(255) not null,
    reception_id varchar(255) not null unique,
    zone_nettoyee boolean not null,
    materiel_retire boolean not null,
    protections_retirees boolean not null,
    consignation_retiree boolean not null,
    commentaire TEXT,
    primary key (id)
);

-- -----------------------------------------------
-- IA, Audit, Notifications, Workflow
-- -----------------------------------------------

create table analyses_ia (
    id varchar(255) not null,
    date_analyse timestamp(6),
    ocr_text TEXT,
    taux_confiance float(53),
    resultat varchar(255),
    commentaireia TEXT,
    temps_execution bigint,
    modele_utilise varchar(255),
    version_modele varchar(255),
    json_extraction TEXT,
    permis_id varchar(255) unique,
    primary key (id)
);

create table audit_logs (
    id varchar(255) not null,
    date timestamp(6),
    action varchar(255) not null,
    resultat varchar(255) check (resultat in ('SUCCES','ECHEC')),
    adresseip varchar(255),
    navigateur varchar(255),
    systeme_exploitation varchar(255),
    utilisateur_id varchar(255),
    primary key (id)
);

create table notifications (
    id varchar(255) not null,
    titre varchar(255) not null,
    message varchar(255) not null,
    date_creation timestamp(6),
    date_lecture timestamp(6),
    lu boolean not null,
    type varchar(255),
    lien varchar(255),
    utilisateur_id varchar(255) not null,
    primary key (id)
);

create table workflows_at (
    id varchar(255) not null,
    etat_depart varchar(255) not null check (etat_depart in ('BROUILLON','SOUMISE','VALIDEE','REJETEE','RENOUVELEE','CLOTUREE','ARCHIVEE','ANNULEE')),
    etat_arrivee varchar(255) not null check (etat_arrivee in ('BROUILLON','SOUMISE','VALIDEE','REJETEE','RENOUVELEE','CLOTUREE','ARCHIVEE','ANNULEE')),
    action varchar(255) not null check (action in ('CREATION','MODIFICATION','AUTO_SAVE','TRANSFERT','SOUMISSION','VALIDATION','REFUS','RENOUVELLEMENT','CLOTURE','EXPORT_PDF','ANNULATION','RECEPTION_TRAVAUX','VALIDATION_RECEPTION')),
    role_autorise varchar(255),
    ordre_validation integer,
    validation_obligatoire boolean,
    actif boolean,
    role_suivant varchar(255),
    notification_suivante varchar(255),
    primary key (id)
);

-- ============================================================
-- FOREIGN KEY CONSTRAINTS
-- ============================================================

-- Référentiel
alter table if exists services add constraint fk_services_zone foreign key (zone_id) references zones;
alter table if exists installations add constraint fk_installations_service foreign key (service_id) references services;
alter table if exists equipements add constraint fk_equipements_installation foreign key (installation_id) references installations;

-- Utilisateurs
alter table if exists utilisateur_roles add constraint fk_ur_utilisateur foreign key (utilisateur_id) references utilisateurs;
alter table if exists utilisateur_roles add constraint fk_ur_role foreign key (role_id) references roles;
alter table if exists role_permissions add constraint fk_rp_role foreign key (role_id) references roles;
alter table if exists role_permissions add constraint fk_rp_permission foreign key (permission_id) references permissions;
alter table if exists refresh_tokens add constraint fk_rt_utilisateur foreign key (utilisateur_id) references utilisateurs;

-- Visites
alter table if exists visites_prealables add constraint fk_vp_visiteur foreign key (visiteur_id) references utilisateurs;
alter table if exists visite_prealable_risques add constraint fk_vpr_visite foreign key (visite_prealable_id) references visites_prealables;
alter table if exists visite_prealable_risques add constraint fk_vpr_risque foreign key (risque_id) references risques;
alter table if exists photos add constraint fk_photos_visite foreign key (visite_prealable_id) references visites_prealables;

-- Documents sources
alter table if exists demandes_intervention add constraint fk_di_demandeur foreign key (demandeur_id) references utilisateurs;
alter table if exists demandes_intervention add constraint fk_di_installation foreign key (installation_id) references installations;
alter table if exists demandes_intervention add constraint fk_di_equipement foreign key (equipement_id) references equipements;
alter table if exists demandes_intervention add constraint fk_di_visite foreign key (visite_prealable_id) references visites_prealables;
alter table if exists ordres_travail add constraint fk_ot_demandeur foreign key (demandeur_id) references utilisateurs;
alter table if exists ordres_travail add constraint fk_ot_installation foreign key (installation_id) references installations;
alter table if exists ordres_travail add constraint fk_ot_visite foreign key (visite_prealable_id) references visites_prealables;
alter table if exists bons_travail add constraint fk_bt_entreprise foreign key (entreprise_externe_id) references entreprises_externes;
alter table if exists bons_travail add constraint fk_bt_demandeur foreign key (demandeur_id) references utilisateurs;
alter table if exists bons_travail add constraint fk_bt_installation foreign key (installation_id) references installations;
alter table if exists bons_travail add constraint fk_bt_visite foreign key (visite_prealable_id) references visites_prealables;

-- Analyses risques
alter table if exists analyses_risques add constraint fk_ar_visite foreign key (visite_prealable_id) references visites_prealables;
alter table if exists analyses_risques add constraint fk_ar_analyseur foreign key (analyseur_id) references utilisateurs;
alter table if exists analyse_risque_risques add constraint fk_arr_analyse foreign key (analyse_risque_id) references analyses_risques;
alter table if exists analyse_risque_risques add constraint fk_arr_risque foreign key (risque_id) references risques;
alter table if exists analyse_risque_mesures add constraint fk_arm_analyse foreign key (analyse_risque_id) references analyses_risques;
alter table if exists analyse_risque_mesures add constraint fk_arm_mesure foreign key (mesure_id) references mesures_preparation;
alter table if exists analyse_risque_epis add constraint fk_arepi_analyse foreign key (analyse_risque_id) references analyses_risques;
alter table if exists analyse_risque_epis add constraint fk_arepi_epi foreign key (epi_id) references epis;
alter table if exists analyse_risque_moyens_acces add constraint fk_arma_analyse foreign key (analyse_risque_id) references analyses_risques;
alter table if exists analyse_risque_moyens_acces add constraint fk_arma_moyen foreign key (moyen_id) references moyens_acces;

-- AT
alter table if exists autorisations_travail add constraint fk_at_proprietaire foreign key (proprietaire_brouillon_id) references utilisateurs;
alter table if exists autorisations_travail add constraint fk_at_di foreign key (di_id) references demandes_intervention;
alter table if exists autorisations_travail add constraint fk_at_ot foreign key (ot_id) references ordres_travail;
alter table if exists autorisations_travail add constraint fk_at_bt foreign key (bt_id) references bons_travail;
alter table if exists visas add constraint fk_visas_utilisateur foreign key (utilisateur_id) references utilisateurs;
alter table if exists visas add constraint fk_visas_at foreign key (at_id) references autorisations_travail;
alter table if exists permis add constraint fk_permis_at foreign key (at_id) references autorisations_travail;
alter table if exists fichiers_joints add constraint fk_fj_permis foreign key (permis_id) references permis;
alter table if exists historiques_at add constraint fk_hat_utilisateur foreign key (utilisateur_id) references utilisateurs;
alter table if exists historiques_at add constraint fk_hat_at foreign key (at_id) references autorisations_travail;

-- Réception travaux
alter table if exists receptions_travaux add constraint fk_rt_at foreign key (autorisation_travail_id) references autorisations_travail;
alter table if exists receptions_travaux add constraint fk_rt_responsable foreign key (responsable_id) references utilisateurs;
alter table if exists photos_reception add constraint fk_pr_reception foreign key (reception_travaux_id) references receptions_travaux;
alter table if exists historiques_reception add constraint fk_hr_reception foreign key (reception_travaux_id) references receptions_travaux;
alter table if exists historiques_reception add constraint fk_hr_utilisateur foreign key (utilisateur_id) references utilisateurs;
alter table if exists essais add constraint fk_essai_reception foreign key (reception_id) references receptions_travaux;
alter table if exists remises_etat add constraint fk_remise_reception foreign key (reception_id) references receptions_travaux;

-- IA, Audit, Notifications
alter table if exists analyses_ia add constraint fk_ai_permis foreign key (permis_id) references permis;
alter table if exists audit_logs add constraint fk_al_utilisateur foreign key (utilisateur_id) references utilisateurs;
alter table if exists notifications add constraint fk_notif_utilisateur foreign key (utilisateur_id) references utilisateurs;
