package com.ocp.at.service.impl;

import com.ocp.at.entity.*;
import com.ocp.at.entity.enums.TypeDocumentSource;
import com.ocp.at.repository.EPIRepository;
import com.ocp.at.repository.MesurePreparationRepository;
import com.ocp.at.repository.MoyenAccesRepository;
import com.ocp.at.repository.RisqueRepository;
import com.ocp.at.repository.TypePermisRepository;
import com.ocp.at.service.PdfGeneratorService;
import com.ocp.at.storage.StorageService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implémentation du service de génération PDF utilisant OpenPDF (com.lowagie).
 * Génère des PDFs conformes au Formulaire F-HSE-SEC-31-04 (Édition 1.0)
 * incluant les signatures HCEP, HCEE, HMEP, HMEE, la géolocalisation et les photos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    private final StorageService storageService;
    private final RisqueRepository risqueRepository;
    private final EPIRepository epiRepository;
    private final MesurePreparationRepository mesureRepository;
    private final MoyenAccesRepository moyenAccesRepository;
    private final TypePermisRepository typePermisRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Couleurs OCP
    private static final Color COULEUR_OCP_VERT = new Color(0, 128, 0);
    private static final Color COULEUR_ENTETE = new Color(0, 80, 160);
    private static final Color COULEUR_SECTION = new Color(0, 100, 200);
    private static final Color COULEUR_GRIS = new Color(100, 100, 100);
    private static final Color COULEUR_BLANC = Color.WHITE;
    private static final Color COULEUR_FOND_SECTION = new Color(230, 240, 255);

    // =========================================================================
    // MÉTHODES DE L'INTERFACE
    // =========================================================================

    @Override
    public byte[] generateATPdf(AutorisationTravail at) {
        return generateFormulairePdf(at);
    }

    @Override
    public byte[] generateFormulairePdf(AutorisationTravail at) {
        log.info("Génération du formulaire officiel F-HSE-SEC-31-04 pour AT : {}", at.getNumero());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Marges serrées (18pt) pour garantir un ajustement parfait sur 2 pages
            Document doc = new Document(PageSize.A4, 18f, 18f, 18f, 18f);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            doc.open();

            // ==========================================
            // PAGE 1 : LE FORMULAIRE DE TRAVAIL OFFICIEL
            // ==========================================
            ajouterEnTeteFormulaire(doc, at, 1);
            ajouterBlocIdentification(doc, at);
            ajouterFormulaireSectionA(doc, at);
            ajouterFormulaireSectionB(doc, at);
            ajouterFormulaireSectionC(doc, at);
            ajouterFormulaireSectionD(doc, at);
            ajouterFormulaireSectionE(doc, at);
            ajouterFormulaireSectionF(doc, at);
            ajouterFormulaireSectionG(doc, at);
            ajouterFormulaireSectionReception(doc, at);

            // ==========================================
            // PAGE 2 : INSTRUCTION D'ÉTABLISSEMENT
            // ==========================================
            doc.newPage();
            ajouterEnTeteFormulaire(doc, at, 2);
            ajouterPageInstructions(doc);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur génération formulaire F-HSE-SEC-31-04 pour AT {}", at.getNumero(), e);
            throw new RuntimeException("Erreur génération formulaire PDF F-HSE-SEC-31-04", e);
        }
    }

    @Override
    public byte[] generateReceptionPdf(ReceptionTravaux reception) {
        log.info("Génération du PDF réception : {}", reception.getId());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = creerDocument();
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new PiedDePageHandler("OCP - Réception des Travaux"));
            doc.open();
            ajouterSectionReception(doc, reception);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur génération PDF réception", e);
            throw new RuntimeException("Erreur génération PDF réception", e);
        }
    }

    @Override
    public byte[] generatePermisPdf(AutorisationTravail at) {
        log.info("Génération du PDF permis pour AT : {}", at.getNumero());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = creerDocument();
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new PiedDePageHandler("OCP - Permis AT " + at.getNumero()));
            doc.open();
            ajouterSectionPermis(doc, at);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur génération PDF permis AT {}", at.getNumero(), e);
            throw new RuntimeException("Erreur génération PDF permis", e);
        }
    }

    @Override
    public byte[] generateCompleteDossierPdf(AutorisationTravail at) {
        log.info("Génération du PDF dossier complet pour AT : {}", at.getNumero());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = creerDocument();
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new PiedDePageHandler("OCP - Dossier Complet AT " + at.getNumero()));
            doc.open();

            // 1. Page de garde
            ajouterPageDeGarde(doc, at);
            doc.newPage();

            // 2. Informations générales
            ajouterInfosGenerales(doc, at);
            doc.newPage();

            // 3. Document source (DI / OT / BT)
            ajouterInfosDocumentSource(doc, at);
            doc.newPage();

            // 4. Visite préalable
            ajouterSectionVisitePrealable(doc, at);
            doc.newPage();

            // 5. Analyse des risques
            ajouterSectionAnalyseRisques(doc, at);
            doc.newPage();

            // 6. Permis
            ajouterSectionPermis(doc, at);
            doc.newPage();

            // 7. Visas & Signatures
            ajouterSectionVisas(doc, at);
            doc.newPage();

            // 8. Historique Workflow
            ajouterSectionHistorique(doc, at);
            doc.newPage();

            // 9. Réception des travaux
            if (at.getReceptionTravaux() != null) {
                ajouterSectionReception(doc, at.getReceptionTravaux());
                doc.newPage();
            }

            // 10. Pied de dossier (hash, date export)
            ajouterPiedDeDossier(doc, at);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur génération PDF dossier complet AT {}", at.getNumero(), e);
            throw new RuntimeException("Erreur génération PDF dossier complet", e);
        }
    }

    @Override
    public String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur calcul SHA-256", e);
        }
    }

    // =========================================================================
    // CRÉATION DOCUMENT
    // =========================================================================

    private Document creerDocument() {
        Document doc = new Document(PageSize.A4, 40, 40, 60, 60);
        return doc;
    }

    // =========================================================================
    // PAGE DE GARDE
    // =========================================================================

    private void ajouterPageDeGarde(Document doc, AutorisationTravail at) throws DocumentException {
        Font fontTitrePrincipal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, COULEUR_ENTETE);
        Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COULEUR_OCP_VERT);
        Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
        Font fontNumero = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COULEUR_ENTETE);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA, 11, COULEUR_GRIS);
        Font fontValeur = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);

        // En-tête OCP
        Paragraph enteteOCP = new Paragraph("OCP S.A.", fontTitrePrincipal);
        enteteOCP.setAlignment(Element.ALIGN_CENTER);
        enteteOCP.setSpacingAfter(5);
        doc.add(enteteOCP);

        Paragraph sousEntete = new Paragraph("Système Intelligent de Gestion des Autorisations de Travail", fontSousTitre);
        sousEntete.setAlignment(Element.ALIGN_CENTER);
        sousEntete.setSpacingAfter(40);
        doc.add(sousEntete);

        // Ligne de séparation
        doc.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(2f, 100f, COULEUR_ENTETE, Element.ALIGN_CENTER, -2)));
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        // Titre du document
        Paragraph titrePDF = new Paragraph("AUTORISATION DE TRAVAIL", fontTitrePrincipal);
        titrePDF.setAlignment(Element.ALIGN_CENTER);
        titrePDF.setSpacingAfter(10);
        doc.add(titrePDF);

        // Numéro AT
        Paragraph numero = new Paragraph(at.getNumero() != null ? at.getNumero() : "N/A", fontNumero);
        numero.setAlignment(Element.ALIGN_CENTER);
        numero.setSpacingAfter(30);
        doc.add(numero);

        // Tableau des informations clés
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(30);
        table.setWidths(new float[]{1f, 2f});

        ajouterLigneCellule(table, "Objet :", at.getObjet() != null ? at.getObjet() : "-", fontLabel, fontValeur);
        ajouterLigneCellule(table, "Statut :", at.getStatut() != null ? at.getStatut().name() : "-", fontLabel, fontValeur);
        ajouterLigneCellule(table, "Date début :",
                at.getDateDebut() != null ? at.getDateDebut().format(DATE_FMT) : "-", fontLabel, fontValeur);
        ajouterLigneCellule(table, "Date fin :",
                at.getDateFin() != null ? at.getDateFin().format(DATE_FMT) : "-", fontLabel, fontValeur);
        ajouterLigneCellule(table, "Créée le :",
                at.getDateCreation() != null ? at.getDateCreation().format(DATETIME_FMT) : "-", fontLabel, fontValeur);
        ajouterLigneCellule(table, "Version :",
                at.getVersion() != null ? String.valueOf(at.getVersion()) : "1", fontLabel, fontValeur);

        doc.add(table);

        // Pied de page de garde
        doc.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(1f, 100f, COULEUR_GRIS, Element.ALIGN_CENTER, -2)));
        doc.add(Chunk.NEWLINE);

        Paragraph dateExport = new Paragraph(
                "Document exporté le : " + java.time.LocalDateTime.now().format(DATETIME_FMT),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, COULEUR_GRIS));
        dateExport.setAlignment(Element.ALIGN_CENTER);
        doc.add(dateExport);

        Paragraph confidentiel = new Paragraph(
                "DOCUMENT CONFIDENTIEL - OCP S.A. - Tous droits réservés",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COULEUR_OCP_VERT));
        confidentiel.setAlignment(Element.ALIGN_CENTER);
        doc.add(confidentiel);
    }

    // =========================================================================
    // INFORMATIONS GÉNÉRALES AT
    // =========================================================================

    private void ajouterInfosGenerales(Document doc, AutorisationTravail at) throws DocumentException {
        ajouterTitreSection(doc, "1. INFORMATIONS GÉNÉRALES");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2f});
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        Font fl = FontFactory.getFont(FontFactory.HELVETICA, 10, COULEUR_GRIS);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        ajouterLigneCellule(table, "Numéro AT :", at.getNumero() != null ? at.getNumero() : "-", fl, fv);
        ajouterLigneCellule(table, "Objet :", at.getObjet() != null ? at.getObjet() : "-", fl, fv);
        ajouterLigneCellule(table, "Description :", at.getDescriptionTravaux() != null ? at.getDescriptionTravaux() : "-", fl, fv);
        ajouterLigneCellule(table, "Statut :", at.getStatut() != null ? at.getStatut().name() : "-", fl, fv);
        ajouterLigneCellule(table, "Date début :", at.getDateDebut() != null ? at.getDateDebut().format(DATE_FMT) : "-", fl, fv);
        ajouterLigneCellule(table, "Date fin :", at.getDateFin() != null ? at.getDateFin().format(DATE_FMT) : "-", fl, fv);
        ajouterLigneCellule(table, "Heure début :", at.getHeureDebut() != null ? at.getHeureDebut().toString() : "-", fl, fv);
        ajouterLigneCellule(table, "Heure fin :", at.getHeureFin() != null ? at.getHeureFin().toString() : "-", fl, fv);
        ajouterLigneCellule(table, "Version :", at.getVersion() != null ? String.valueOf(at.getVersion()) : "1", fl, fv);
        ajouterLigneCellule(table, "Créée le :", at.getDateCreation() != null ? at.getDateCreation().format(DATETIME_FMT) : "-", fl, fv);
        ajouterLigneCellule(table, "Modifiée le :", at.getDateModification() != null ? at.getDateModification().format(DATETIME_FMT) : "-", fl, fv);

        // Demandeur / Propriétaire
        if (at.getProprietaireBrouillon() != null) {
            Utilisateur u = at.getProprietaireBrouillon();
            ajouterLigneCellule(table, "Demandeur :", u.getPrenom() + " " + u.getNom() + " (" + u.getMatricule() + ")", fl, fv);
            ajouterLigneCellule(table, "Email demandeur :", u.getEmail() != null ? u.getEmail() : "-", fl, fv);
            ajouterLigneCellule(table, "Tél. demandeur :", u.getTelephone() != null ? u.getTelephone() : "-", fl, fv);
        }

        doc.add(table);
    }

    // =========================================================================
    // DOCUMENT SOURCE (DI / OT / BT)
    // =========================================================================

    private void ajouterInfosDocumentSource(Document doc, AutorisationTravail at) throws DocumentException {
        ajouterTitreSection(doc, "2. DOCUMENT SOURCE");

        Font fl = FontFactory.getFont(FontFactory.HELVETICA, 10, COULEUR_GRIS);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2f});
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        if (at.getDemandeIntervention() != null) {
            DemandeIntervention di = at.getDemandeIntervention();
            ajouterLigneCellule(table, "Type :", "Demande d'Intervention (DI)", fl, fv);
            ajouterLigneCellule(table, "Numéro DI :", di.getNumero() != null ? di.getNumero() : "-", fl, fv);
            ajouterLigneCellule(table, "Objet :", di.getObjet() != null ? di.getObjet() : "-", fl, fv);
            ajouterLigneCellule(table, "Type intervention :", di.getTypeIntervention() != null ? di.getTypeIntervention().name() : "-", fl, fv);
            ajouterLigneCellule(table, "Niveau :", di.getNiveauIntervention() != null ? di.getNiveauIntervention().name() : "-", fl, fv);
            ajouterLigneCellule(table, "Priorité :", di.getPriorite() != null ? di.getPriorite() : "-", fl, fv);
            if (di.getDemandeur() != null) {
                ajouterLigneCellule(table, "Demandeur DI :", di.getDemandeur().getPrenom() + " " + di.getDemandeur().getNom(), fl, fv);
            }
            if (di.getEquipement() != null) {
                ajouterLigneCellule(table, "Équipement :", di.getEquipement().getNomEquipement() + " (" + di.getEquipement().getCodeEquipement() + ")", fl, fv);
                if (di.getEquipement().getDescriptionEquipement() != null) {
                    ajouterLigneCellule(table, "Desc. équipement :", di.getEquipement().getDescriptionEquipement(), fl, fv);
                }
            }

        } else if (at.getOrdreTravail() != null) {
            OrdreTravail ot = at.getOrdreTravail();
            ajouterLigneCellule(table, "Type :", "Ordre de Travail (OT)", fl, fv);
            ajouterLigneCellule(table, "Numéro OT :", ot.getNumero() != null ? ot.getNumero() : "-", fl, fv);
            ajouterLigneCellule(table, "Objet :", ot.getObjet() != null ? ot.getObjet() : "-", fl, fv);
            ajouterLigneCellule(table, "Type intervention :", ot.getTypeIntervention() != null ? ot.getTypeIntervention().name() : "-", fl, fv);
            ajouterLigneCellule(table, "Niveau :", ot.getNiveauIntervention() != null ? ot.getNiveauIntervention().name() : "-", fl, fv);
            if (ot.getDemandeur() != null) {
                ajouterLigneCellule(table, "Demandeur OT :", ot.getDemandeur().getPrenom() + " " + ot.getDemandeur().getNom(), fl, fv);
            }

        } else if (at.getBonTravail() != null) {
            BonTravail bt = at.getBonTravail();
            ajouterLigneCellule(table, "Type :", "Bon de Travail (BT)", fl, fv);
            ajouterLigneCellule(table, "Numéro BT :", bt.getNumero() != null ? bt.getNumero() : "-", fl, fv);
            ajouterLigneCellule(table, "Objet :", bt.getObjet() != null ? bt.getObjet() : "-", fl, fv);
            ajouterLigneCellule(table, "Type intervention :", bt.getTypeIntervention() != null ? bt.getTypeIntervention().name() : "-", fl, fv);
            ajouterLigneCellule(table, "Niveau :", bt.getNiveauIntervention() != null ? bt.getNiveauIntervention().name() : "-", fl, fv);
            if (bt.getEntrepriseExterne() != null) {
                ajouterLigneCellule(table, "Entreprise externe :", bt.getEntrepriseExterne().getNomEntreprise(), fl, fv);
                if (bt.getEntrepriseExterne().getTelephone() != null) {
                    ajouterLigneCellule(table, "Tél. entreprise :", bt.getEntrepriseExterne().getTelephone(), fl, fv);
                }
                if (bt.getEntrepriseExterne().getResponsable() != null) {
                    ajouterLigneCellule(table, "Responsable :", bt.getEntrepriseExterne().getResponsable(), fl, fv);
                }
            }
        } else {
            ajouterLigneCellule(table, "Document source :", "Aucun document source associé", fl, fv);
        }

        doc.add(table);
    }

    // =========================================================================
    // VISITE PRÉALABLE
    // =========================================================================

    private void ajouterSectionVisitePrealable(Document doc, AutorisationTravail at) throws DocumentException {
        ajouterTitreSection(doc, "3. VISITE PRÉALABLE");

        VisitePrealable vp = getVisitePrealable(at);

        if (vp == null) {
            Paragraph p = new Paragraph("Aucune visite préalable enregistrée.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, COULEUR_GRIS));
            doc.add(p);
            return;
        }

        Font fl = FontFactory.getFont(FontFactory.HELVETICA, 10, COULEUR_GRIS);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2f});
        table.setSpacingBefore(10);
        table.setSpacingAfter(15);

        ajouterLigneCellule(table, "Date/heure début :",
                vp.getDateHeureDebut() != null ? vp.getDateHeureDebut().format(DATETIME_FMT) : "-", fl, fv);
        ajouterLigneCellule(table, "Date/heure fin :",
                vp.getDateHeureFin() != null ? vp.getDateHeureFin().format(DATETIME_FMT) : "-", fl, fv);
        ajouterLigneCellule(table, "Visiteur :",
                vp.getVisiteur() != null ? vp.getVisiteur().getPrenom() + " " + vp.getVisiteur().getNom() : "-", fl, fv);
        ajouterLigneCellule(table, "Effectuée :", vp.isEffectuee() ? "Oui" : "Non", fl, fv);
        ajouterLigneCellule(table, "Commentaire :", vp.getCommentaire() != null ? vp.getCommentaire() : "-", fl, fv);

        if (vp.getLatitude() != null && vp.getLongitude() != null) {
            ajouterLigneCellule(table, "Coordonnées GPS :",
                    "Lat: " + vp.getLatitude() + " / Lon: " + vp.getLongitude(), fl, fv);
        }

        doc.add(table);

        // Photos de visite
        if (vp.getPhotos() != null && !vp.getPhotos().isEmpty()) {
            Paragraph titrePhotos = new Paragraph("Photos de la visite (" + vp.getPhotos().size() + " photo(s)) :",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COULEUR_SECTION));
            titrePhotos.setSpacingBefore(5);
            titrePhotos.setSpacingAfter(5);
            doc.add(titrePhotos);

            for (Photo photo : vp.getPhotos()) {
                Paragraph photoInfo = new Paragraph(
                        "  • " + (photo.getNom() != null ? photo.getNom() : "Photo") +
                                (photo.getLegende() != null ? " - " + photo.getLegende() : ""),
                        FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY));
                doc.add(photoInfo);
            }
        }

        // Risques identifiés
        if (vp.getRisquesIdentifies() != null && !vp.getRisquesIdentifies().isEmpty()) {
            Paragraph titreRisques = new Paragraph("Risques pré-identifiés :",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COULEUR_SECTION));
            titreRisques.setSpacingBefore(10);
            titreRisques.setSpacingAfter(5);
            doc.add(titreRisques);

            for (Risque r : vp.getRisquesIdentifies()) {
                Paragraph risque = new Paragraph(
                        "  • " + r.getNomRisque() + (r.getNiveau() != null ? " [Niveau: " + r.getNiveau() + "]" : ""),
                        FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY));
                doc.add(risque);
            }
        }
    }

    // =========================================================================
    // ANALYSE DES RISQUES
    // =========================================================================

    private void ajouterSectionAnalyseRisques(Document doc, AutorisationTravail at) throws DocumentException {
        ajouterTitreSection(doc, "4. ANALYSE DES RISQUES");

        AnalyseRisque ar = getAnalyseRisque(at);

        if (ar == null) {
            Paragraph p = new Paragraph("Aucune analyse des risques enregistrée.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, COULEUR_GRIS));
            doc.add(p);
            return;
        }

        Font fl = FontFactory.getFont(FontFactory.HELVETICA, 10, COULEUR_GRIS);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font ftitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COULEUR_SECTION);

        PdfPTable tableInfo = new PdfPTable(2);
        tableInfo.setWidthPercentage(100);
        tableInfo.setWidths(new float[]{1f, 2f});
        tableInfo.setSpacingBefore(10);
        tableInfo.setSpacingAfter(15);

        ajouterLigneCellule(tableInfo, "Date analyse :",
                ar.getDateAnalyse() != null ? ar.getDateAnalyse().format(DATETIME_FMT) : "-", fl, fv);
        ajouterLigneCellule(tableInfo, "Analyseur :",
                ar.getAnalyseur() != null ? ar.getAnalyseur().getPrenom() + " " + ar.getAnalyseur().getNom() : "-", fl, fv);
        ajouterLigneCellule(tableInfo, "Commentaire :", ar.getCommentaire() != null ? ar.getCommentaire() : "-", fl, fv);
        doc.add(tableInfo);

        // Risques formels
        if (ar.getRisques() != null && !ar.getRisques().isEmpty()) {
            Paragraph t = new Paragraph("Risques identifiés :", ftitle);
            t.setSpacingBefore(5);
            t.setSpacingAfter(5);
            doc.add(t);
            for (Risque r : ar.getRisques()) {
                doc.add(new Paragraph("  • " + r.getNomRisque()
                        + (r.getNiveau() != null ? " [Niveau: " + r.getNiveau() + "]" : "")
                        + (r.getDescriptionRisque() != null ? " - " + r.getDescriptionRisque() : ""),
                        FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
            }
        }

        // Mesures de préparation
        if (ar.getMesures() != null && !ar.getMesures().isEmpty()) {
            Paragraph t = new Paragraph("Mesures de préparation :", ftitle);
            t.setSpacingBefore(10);
            t.setSpacingAfter(5);
            doc.add(t);
            for (MesurePreparation m : ar.getMesures()) {
                doc.add(new Paragraph("  ✓ " + m.getNomMesure()
                        + (m.getDescriptionMesure() != null ? " - " + m.getDescriptionMesure() : ""),
                        FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
            }
        }

        // EPIs
        if (ar.getEpis() != null && !ar.getEpis().isEmpty()) {
            Paragraph t = new Paragraph("Équipements de Protection Individuelle (EPI) :", ftitle);
            t.setSpacingBefore(10);
            t.setSpacingAfter(5);
            doc.add(t);
            for (EPI epi : ar.getEpis()) {
                doc.add(new Paragraph("  • " + epi.getNomEPI()
                        + (epi.getDescriptionEPI() != null ? " - " + epi.getDescriptionEPI() : ""),
                        FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
            }
        }

        // Moyens d'accès
        if (ar.getMoyensAcces() != null && !ar.getMoyensAcces().isEmpty()) {
            Paragraph t = new Paragraph("Moyens d'accès :", ftitle);
            t.setSpacingBefore(10);
            t.setSpacingAfter(5);
            doc.add(t);
            for (MoyenAcces m : ar.getMoyensAcces()) {
                doc.add(new Paragraph("  • " + m.getNomMoyen()
                        + (m.getDescriptionMoyen() != null ? " - " + m.getDescriptionMoyen() : ""),
                        FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
            }
        }
    }

    // =========================================================================
    // PERMIS
    // =========================================================================

    private void ajouterSectionPermis(Document doc, AutorisationTravail at) throws DocumentException {
        ajouterTitreSection(doc, "5. PERMIS DE TRAVAIL");

        List<Permis> permis = at.getPermis();
        if (permis == null || permis.isEmpty()) {
            doc.add(new Paragraph("Aucun permis associé.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, COULEUR_GRIS)));
            return;
        }

        Font fl = FontFactory.getFont(FontFactory.HELVETICA, 10, COULEUR_GRIS);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font ftitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COULEUR_SECTION);

        for (Permis p : permis) {
            Paragraph tpermis = new Paragraph(
                    (p.getTypePermis() != null ? p.getTypePermis().getNom() : "INCONNU") +
                            (p.getNumero() != null ? " - N° " + p.getNumero() : ""), ftitle);
            tpermis.setSpacingBefore(10);
            tpermis.setSpacingAfter(5);
            doc.add(tpermis);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f});
            table.setSpacingBefore(5);
            table.setSpacingAfter(10);

            ajouterLigneCellule(table, "Statut vérification :",
                    p.getStatutVerification() != null ? p.getStatutVerification().name() : "-", fl, fv);
            ajouterLigneCellule(table, "Date émission :",
                    p.getDateEmission() != null ? p.getDateEmission().format(DATE_FMT) : "-", fl, fv);
            ajouterLigneCellule(table, "Date expiration :",
                    p.getDateExpiration() != null ? p.getDateExpiration().format(DATE_FMT) : "-", fl, fv);
            ajouterLigneCellule(table, "Obligatoire :",
                    Boolean.TRUE.equals(p.getEstObligatoire()) ? "Oui" : "Non", fl, fv);
            if (p.getCommentaire() != null) {
                ajouterLigneCellule(table, "Commentaire :", p.getCommentaire(), fl, fv);
            }

            // Résultat IA
            if (p.getAnalyseIA() != null) {
                AnalyseIA ia = p.getAnalyseIA();
                ajouterLigneCellule(table, "Résultat IA :", ia.getResultat() != null ? ia.getResultat() : "-", fl, fv);
                ajouterLigneCellule(table, "Taux confiance IA :",
                        ia.getTauxConfiance() != null ? String.format("%.1f%%", ia.getTauxConfiance() * 100) : "-", fl, fv);
                ajouterLigneCellule(table, "Modèle IA :", ia.getModeleUtilise() != null ? ia.getModeleUtilise() : "-", fl, fv);
            }

            doc.add(table);
        }
    }

    // =========================================================================
    // VISAS & SIGNATURES
    // =========================================================================

    private void ajouterSectionVisas(Document doc, AutorisationTravail at) throws DocumentException {
        ajouterTitreSection(doc, "6. VISAS & SIGNATURES ELECTRONIQUES");

        List<Visa> visas = at.getVisas();

        // Tableau synthétique HCEP, HCEE, HMEP, HMEE (S-HSE-SEC-31)
        Paragraph pTitleRoles = new Paragraph("Tableau de Synthèse des Validations Hiérarchiques :",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COULEUR_SECTION));
        pTitleRoles.setSpacingBefore(5);
        pTitleRoles.setSpacingAfter(10);
        doc.add(pTitleRoles);

        PdfPTable tableRoles = new PdfPTable(2);
        tableRoles.setWidthPercentage(100);
        tableRoles.setWidths(new float[]{1f, 1f});
        tableRoles.setSpacingAfter(15);

        Font fontLabelRole = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COULEUR_ENTETE);
        Font fontValueRole = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

        String[] rolesRequis = {"HCEP", "HCEE", "HMEP", "HMEE"};
        String[] labelsRoles = {
                "Hors Cadre Entité Propriétaire (HCEP)",
                "Hors Cadre Entité Exécutante (HCEE)",
                "Haute Maîtrise Entité Propriétaire (HMEP)",
                "Haute Maîtrise Entité Exécutante (HMEE)"
        };

        for (int i = 0; i < rolesRequis.length; i++) {
            String roleCode = rolesRequis[i];
            String roleLabel = labelsRoles[i];

            Visa vRole = findVisaForRole(visas, roleCode);

            PdfPCell cell = new PdfPCell();
            cell.setPadding(6);
            cell.setBackgroundColor(new Color(245, 247, 250));

            cell.addElement(new Paragraph(roleLabel, fontLabelRole));
            if (vRole != null && vRole.getUtilisateur() != null) {
                Utilisateur u = vRole.getUtilisateur();
                cell.addElement(new Paragraph("Signataire : " + u.getPrenom() + " " + u.getNom() + " (" + u.getMatricule() + ")", fontValueRole));
                cell.addElement(new Paragraph("Date : " + (vRole.getDateSignature() != null ? vRole.getDateSignature().format(DATETIME_FMT) : "-"), fontValueRole));
                if (vRole.getAdresseIP() != null) {
                    cell.addElement(new Paragraph("IP : " + vRole.getAdresseIP(), fontValueRole));
                }

                // Incruster l'image PNG si présente
                if (vRole.getSignaturePath() != null && !vRole.getSignaturePath().isBlank()) {
                    try {
                        Resource res = storageService.loadSignature(vRole.getSignaturePath());
                        if (res != null && res.exists()) {
                            byte[] imgBytes = res.getInputStream().readAllBytes();
                            Image img = Image.getInstance(imgBytes);
                            img.scaleToFit(100, 40);
                            cell.addElement(img);
                        }
                    } catch (Exception e) {
                        log.warn("Impossible de charger l'image de signature pour " + roleCode, e);
                    }
                }
            } else {
                cell.addElement(new Paragraph("Statut : Signature en attente", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.RED)));
            }
            tableRoles.addCell(cell);
        }

        doc.add(tableRoles);

        if (visas == null || visas.isEmpty()) {
            doc.add(new Paragraph("Aucun visa individuel enregistré.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, COULEUR_GRIS)));
            return;
        }

        Font fl = FontFactory.getFont(FontFactory.HELVETICA, 10, COULEUR_GRIS);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font ftitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COULEUR_SECTION);

        for (Visa v : visas) {
            Utilisateur u = v.getUtilisateur();
            String nomSignataire = u != null ? u.getPrenom() + " " + u.getNom() + " (" + u.getMatricule() + ")" : "Inconnu";

            Paragraph tvisa = new Paragraph("Visa #" + (v.getOrdre() != null ? v.getOrdre() : "?") + " - " + nomSignataire, ftitle);
            tvisa.setSpacingBefore(10);
            tvisa.setSpacingAfter(5);
            doc.add(tvisa);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f});
            table.setSpacingBefore(5);
            table.setSpacingAfter(10);

            ajouterLigneCellule(table, "Statut :", v.getStatut() != null ? v.getStatut().name() : "-", fl, fv);
            ajouterLigneCellule(table, "Date visa :", v.getDateVisa() != null ? v.getDateVisa().format(DATETIME_FMT) : "-", fl, fv);
            ajouterLigneCellule(table, "Date signature :", v.getDateSignature() != null ? v.getDateSignature().format(DATETIME_FMT) : "-", fl, fv);
            ajouterLigneCellule(table, "Commentaire :", v.getCommentaire() != null ? v.getCommentaire() : "-", fl, fv);
            ajouterLigneCellule(table, "Signataire :", nomSignataire, fl, fv);
            ajouterLigneCellule(table, "Adresse IP :", v.getAdresseIP() != null ? v.getAdresseIP() : "-", fl, fv);

            // Charger et ajouter la signature manuscrite PNG
            if (v.getSignaturePath() != null && !v.getSignaturePath().isBlank()) {
                try {
                    Resource res = storageService.loadSignature(v.getSignaturePath());
                    if (res != null && res.exists()) {
                        byte[] imgBytes = res.getInputStream().readAllBytes();
                        Image img = Image.getInstance(imgBytes);
                        img.scaleToFit(120, 50);

                        PdfPCell cellLbl = new PdfPCell(new Phrase("Signature manuscrite :", fl));
                        cellLbl.setPadding(5);
                        table.addCell(cellLbl);

                        PdfPCell cellImg = new PdfPCell(img);
                        cellImg.setPadding(5);
                        table.addCell(cellImg);
                    }
                } catch (Exception e) {
                    log.warn("Erreur chargement signature PNG visa {}", v.getId(), e);
                }
            }

            doc.add(table);
        }
    }

    private Visa findVisaForRole(List<Visa> visas, String roleCode) {
        if (visas == null) return null;
        return visas.stream().filter(v -> {
            if (v.getStatut() == null || v.getStatut() == com.ocp.at.entity.enums.StatutVisa.REFUS) return false;
            String comment = v.getCommentaire() != null ? v.getCommentaire().toUpperCase() : "";
            if (comment.contains(roleCode.toUpperCase())) return true;
            Utilisateur u = v.getUtilisateur();
            if (u != null && u.getRoles() != null) {
                return u.getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().equalsIgnoreCase(roleCode));
            }
            return false;
        }).findFirst().orElse(null);
    }

    // =========================================================================
    // HISTORIQUE WORKFLOW
    // =========================================================================

    private void ajouterSectionHistorique(Document doc, AutorisationTravail at) throws DocumentException {
        ajouterTitreSection(doc, "7. HISTORIQUE DU WORKFLOW");

        List<HistoriqueAT> historiques = at.getHistoriques();
        if (historiques == null || historiques.isEmpty()) {
            doc.add(new Paragraph("Aucun historique disponible.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, COULEUR_GRIS)));
            return;
        }

        // Tableau d'historique
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 1.5f, 1.5f, 3f});
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        // En-têtes
        Font fontEntete = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COULEUR_BLANC);
        String[] entetes = {"Date & Heure", "Action", "Statut", "Utilisateur / Commentaire"};
        for (String h : entetes) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontEntete));
            cell.setBackgroundColor(COULEUR_ENTETE);
            cell.setPadding(5);
            table.addCell(cell);
        }

        Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        boolean pair = false;
        for (HistoriqueAT h : historiques) {
            Color bg = pair ? new Color(240, 240, 240) : Color.WHITE;
            ajouterCelluleTableau(table, h.getDateAction() != null ? h.getDateAction().format(DATETIME_FMT) : "-", fontData, bg);
            ajouterCelluleTableau(table, h.getAction() != null ? h.getAction().name() : "-", fontData, bg);
            ajouterCelluleTableau(table, h.getNouveauStatut() != null ? h.getNouveauStatut().name() : "-", fontData, bg);
            String userCommentaire = "";
            if (h.getUtilisateur() != null) {
                userCommentaire = h.getUtilisateur().getPrenom() + " " + h.getUtilisateur().getNom();
            }
            if (h.getCommentaire() != null) {
                userCommentaire += (userCommentaire.isEmpty() ? "" : "\n") + h.getCommentaire();
            }
            ajouterCelluleTableau(table, userCommentaire.isEmpty() ? "-" : userCommentaire, fontData, bg);
            pair = !pair;
        }

        doc.add(table);
    }

    // =========================================================================
    // RÉCEPTION DES TRAVAUX
    // =========================================================================

    private void ajouterSectionReception(Document doc, ReceptionTravaux reception) throws DocumentException {
        ajouterTitreSection(doc, "8. RÉCEPTION DES TRAVAUX");

        Font fl = FontFactory.getFont(FontFactory.HELVETICA, 10, COULEUR_GRIS);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2f});
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        ajouterLigneCellule(table, "Date réception :",
                reception.getDateReception() != null ? reception.getDateReception().format(DATETIME_FMT) : "-", fl, fv);
        ajouterLigneCellule(table, "Responsable :",
                reception.getResponsable() != null ?
                        reception.getResponsable().getPrenom() + " " + reception.getResponsable().getNom() : "-", fl, fv);
        ajouterLigneCellule(table, "Début réel travaux :",
                reception.getDateDebutTravauxReelle() != null ? reception.getDateDebutTravauxReelle().format(DATETIME_FMT) : "-", fl, fv);
        ajouterLigneCellule(table, "Fin réelle travaux :",
                reception.getDateFinTravauxReelle() != null ? reception.getDateFinTravauxReelle().format(DATETIME_FMT) : "-", fl, fv);
        ajouterLigneCellule(table, "Travaux réalisés :", reception.getTravauxRealises() != null ? reception.getTravauxRealises() : "-", fl, fv);
        ajouterLigneCellule(table, "Travaux conformes :", Boolean.TRUE.equals(reception.getTravauxConformes()) ? "✓ Oui" : "✗ Non", fl, fv);
        ajouterLigneCellule(table, "Équipement remis en service :", Boolean.TRUE.equals(reception.getEquipementRemisEnService()) ? "✓ Oui" : "✗ Non", fl, fv);
        ajouterLigneCellule(table, "Zone nettoyée :", Boolean.TRUE.equals(reception.getZoneNettoyee()) ? "✓ Oui" : "✗ Non", fl, fv);
        ajouterLigneCellule(table, "Consignation retirée :", Boolean.TRUE.equals(reception.getConsignationRetiree()) ? "✓ Oui" : "✗ Non", fl, fv);
        ajouterLigneCellule(table, "Essais effectués :", Boolean.TRUE.equals(reception.getEssaisEffectues()) ? "✓ Oui" : "✗ Non", fl, fv);
        ajouterLigneCellule(table, "Résultat essais :", reception.getResultatEssais() != null ? reception.getResultatEssais() : "-", fl, fv);
        ajouterLigneCellule(table, "Observations :", reception.getObservations() != null ? reception.getObservations() : "-", fl, fv);
        ajouterLigneCellule(table, "Commentaire responsable :", reception.getCommentaireResponsable() != null ? reception.getCommentaireResponsable() : "-", fl, fv);
        ajouterLigneCellule(table, "Date signature :", reception.getSignatureDate() != null ? reception.getSignatureDate().format(DATETIME_FMT) : "-", fl, fv);
        ajouterLigneCellule(table, "Signé par :", reception.getSignatureBy() != null ? reception.getSignatureBy() : "-", fl, fv);

        doc.add(table);

        // Photos de réception
        if (reception.getPhotos() != null && !reception.getPhotos().isEmpty()) {
            Paragraph tp = new Paragraph("Photos de réception (" + reception.getPhotos().size() + " photo(s)) :",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COULEUR_SECTION));
            tp.setSpacingBefore(5);
            tp.setSpacingAfter(5);
            doc.add(tp);
            for (PhotoReception photo : reception.getPhotos()) {
                doc.add(new Paragraph("  • " + (photo.getNom() != null ? photo.getNom() : "Photo")
                        + (photo.getLegende() != null ? " - " + photo.getLegende() : ""),
                        FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
            }
        }
    }

    // =========================================================================
    // PIED DE DOSSIER
    // =========================================================================

    private void ajouterPiedDeDossier(Document doc, AutorisationTravail at) throws DocumentException {
        ajouterTitreSection(doc, "9. INFORMATIONS D'ARCHIVAGE");

        Font fl = FontFactory.getFont(FontFactory.HELVETICA, 10, COULEUR_GRIS);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2f});
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        ajouterLigneCellule(table, "Numéro AT :", at.getNumero() != null ? at.getNumero() : "-", fl, fv);
        ajouterLigneCellule(table, "Statut :", at.getStatut() != null ? at.getStatut().name() : "-", fl, fv);
        ajouterLigneCellule(table, "Version dossier :", at.getVersion() != null ? String.valueOf(at.getVersion()) : "1", fl, fv);
        ajouterLigneCellule(table, "Date d'export :", java.time.LocalDateTime.now().format(DATETIME_FMT), fl, fv);
        ajouterLigneCellule(table, "Système :", "OCP - Système Intelligent de Gestion des AT", fl, fv);

        doc.add(table);

        // Avertissement légal
        Paragraph avert = new Paragraph(
                "\nCe document est un export officiel généré automatiquement par le Système Intelligent de Gestion des Autorisations de Travail d'OCP S.A. "
                        + "Il constitue la preuve officielle de l'archivage de l'AT " + at.getNumero() + ". "
                        + "Toute modification ou altération de ce document est strictement interdite.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, COULEUR_GRIS));
        avert.setAlignment(Element.ALIGN_JUSTIFIED);
        doc.add(avert);

        Paragraph stamp = new Paragraph(
                "\nDocument généré le : " + java.time.LocalDateTime.now().format(DATETIME_FMT),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COULEUR_OCP_VERT));
        stamp.setAlignment(Element.ALIGN_CENTER);
        doc.add(stamp);
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES POUR NAVIGATION ENTITÉS
    // =========================================================================

    private VisitePrealable getVisitePrealable(AutorisationTravail at) {
        if (at.getDemandeIntervention() != null) return at.getDemandeIntervention().getVisitePrealable();
        if (at.getOrdreTravail() != null) return at.getOrdreTravail().getVisitePrealable();
        if (at.getBonTravail() != null) return at.getBonTravail().getVisitePrealable();
        return null;
    }

    private AnalyseRisque getAnalyseRisque(AutorisationTravail at) {
        VisitePrealable vp = getVisitePrealable(at);
        return vp != null ? vp.getAnalyseRisque() : null;
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES PDF
    // =========================================================================

    private void ajouterTitreSection(Document doc, String titre) throws DocumentException {
        Paragraph p = new Paragraph(titre, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COULEUR_BLANC));
        p.setAlignment(Element.ALIGN_LEFT);
        p.setSpacingBefore(15);
        p.setSpacingAfter(5);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COULEUR_ENTETE);
        cell.setPadding(8);
        cell.addElement(p);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        doc.add(table);
    }

    private void ajouterLigneCellule(PdfPTable table, String label, String valeur, Font fontLabel, Font fontValeur) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontLabel));
        cellLabel.setBackgroundColor(COULEUR_FOND_SECTION);
        cellLabel.setPadding(5);
        cellLabel.setBorderColor(new Color(200, 200, 200));
        table.addCell(cellLabel);

        PdfPCell cellValeur = new PdfPCell(new Phrase(valeur != null ? valeur : "-", fontValeur));
        cellValeur.setPadding(5);
        cellValeur.setBorderColor(new Color(200, 200, 200));
        table.addCell(cellValeur);
    }

    private void ajouterCelluleTableau(PdfPTable table, String valeur, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(valeur != null ? valeur : "-", font));
        cell.setPadding(4);
        cell.setBackgroundColor(bg);
        table.addCell(cell);
    }

    // =========================================================================
    // FORMULAIRE OFFICIEL F-HSE-SEC-31-04 (PAGES 1 & 2)
    // =========================================================================

    private void ajouterEnTeteFormulaire(Document doc, AutorisationTravail at, int pageNum) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{68f, 32f});
        table.setSpacingAfter(2f);

        // Cellule Gauche : Logo OCP + Titre
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorderColor(new Color(120, 120, 120));
        leftCell.setBorderWidth(0.7f);
        leftCell.setPadding(3f);

        PdfPTable innerLeft = new PdfPTable(2);
        innerLeft.setWidthPercentage(100);
        innerLeft.setWidths(new float[]{22f, 78f});

        // Logo OCP stylisé
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pLogo = new Paragraph("OCP", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, new Color(0, 135, 90)));
        pLogo.setAlignment(Element.ALIGN_CENTER);
        logoCell.addElement(pLogo);
        innerLeft.addCell(logoCell);

        // Titres
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pF = new Paragraph("FORMULAIRE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, Color.BLACK));
        pF.setAlignment(Element.ALIGN_CENTER);
        pF.setLeading(11.5f);
        titleCell.addElement(pF);

        Paragraph pT = new Paragraph("Autorisation de travail", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, Color.BLACK));
        pT.setAlignment(Element.ALIGN_CENTER);
        pT.setLeading(14f);
        titleCell.addElement(pT);

        innerLeft.addCell(titleCell);
        leftCell.addElement(innerLeft);
        table.addCell(leftCell);

        // Cellule Droite : Références documentaires
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorderColor(new Color(120, 120, 120));
        rightCell.setBorderWidth(0.7f);
        rightCell.setPadding(0f);

        PdfPTable innerRight = new PdfPTable(1);
        innerRight.setWidthPercentage(100);

        Font fRefBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, Color.BLACK);
        Font fRef = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.BLACK);

        innerRight.addCell(createRefRowCell("F-HSE-SEC-31-04", fRefBold));
        innerRight.addCell(createRefRowCell("Edition : 1.0", fRef));
        innerRight.addCell(createRefRowCell("Date d'émission : 01/07/2016", fRef));
        PdfPCell r4 = createRefRowCell("Page : " + pageNum + "/2", fRef);
        r4.setBorder(Rectangle.NO_BORDER);
        innerRight.addCell(r4);

        rightCell.addElement(innerRight);
        table.addCell(rightCell);

        doc.add(table);
    }

    private PdfPCell createRefRowCell(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setLeading(8.5f);
        PdfPCell cell = new PdfPCell(p);
        cell.setPaddingTop(1f);
        cell.setPaddingBottom(1.5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(new Color(160, 160, 160));
        cell.setBorderWidth(0.5f);
        cell.setBorder(Rectangle.BOTTOM);
        return cell;
    }

    private void ajouterBlocIdentification(Document doc, AutorisationTravail at) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35f, 30f, 35f});
        table.setSpacingAfter(2f);

        Font fl = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.2f, Color.BLACK);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA, 7.2f, Color.BLACK);

        String site = at.getZoneProprietaire() != null ? at.getZoneProprietaire().getNomZone() : "...............";
        // Entité = service du propriétaire. Ne pas dupliquer le site si le service est absent.
        String entite = (at.getProprietaireBrouillon() != null && at.getProprietaireBrouillon().getService() != null)
                ? at.getProprietaireBrouillon().getService().getNomService()
                : "...............";
        String numeroAT = at.getNumero() != null ? at.getNumero() : "N/A";

        // Row 1: Site + Entité (span 2) / Encadré AT n° (col 3, rowspan 2)
        Paragraph pSiteEntite = new Paragraph();
        pSiteEntite.setLeading(8f);
        pSiteEntite.add(new Chunk("Site : ", fl));
        pSiteEntite.add(new Chunk(site + "   ", fv));
        pSiteEntite.add(new Chunk("Entité : ", fl));
        pSiteEntite.add(new Chunk(entite, fv));

        PdfPCell c1 = createGridCell(pSiteEntite, 2, null);
        table.addCell(c1);

        // Encadré AT n° (rowspan 2)
        Paragraph pAtNum = new Paragraph();
        pAtNum.setLeading(8.5f);
        pAtNum.add(new Chunk("Autorisation de travail n° : ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, new Color(0, 80, 160))));
        pAtNum.add(new Chunk(numeroAT + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK)));
        pAtNum.add(new Chunk("(Document valable pendant 24 heures à instruire sur le terrain)", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 6.2f, new Color(90, 90, 90))));

        PdfPCell cAtBox = createGridCell(pAtNum, 1, new Color(245, 248, 255));
        cAtBox.setRowspan(2);
        cAtBox.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cAtBox);

        // Row 2: DI n° / OT n° / BT n° (span 2)
        TypeDocumentSource typeDoc = at.getTypeDocumentSource();
        String numDoc = at.getNumeroDocumentSource();
        if (numDoc == null || numDoc.isBlank()) {
            if (at.getDemandeIntervention() != null) numDoc = at.getDemandeIntervention().getNumero();
            else if (at.getOrdreTravail() != null) numDoc = at.getOrdreTravail().getNumero();
            else if (at.getBonTravail() != null) numDoc = at.getBonTravail().getNumero();
        }

        String diNum = (typeDoc == TypeDocumentSource.DI && numDoc != null && !numDoc.isBlank()) ? numDoc : "...............";
        String otNum = (typeDoc == TypeDocumentSource.OT && numDoc != null && !numDoc.isBlank()) ? numDoc : "...............";
        String btNum = (typeDoc == TypeDocumentSource.BT && numDoc != null && !numDoc.isBlank()) ? numDoc : "...............";

        Paragraph pDocSource = new Paragraph();
        pDocSource.setLeading(8f);
        pDocSource.add(new Chunk("DI n° : ", fl));
        pDocSource.add(new Chunk(diNum + "   ", fv));
        pDocSource.add(new Chunk("OT n° : ", fl));
        pDocSource.add(new Chunk(otNum + "   ", fv));
        pDocSource.add(new Chunk("BT n° : ", fl));
        pDocSource.add(new Chunk(btNum, fv));

        PdfPCell cDoc = createGridCell(pDocSource, 2, null);
        table.addCell(cDoc);

        // Row 3: Lieu / Services / Entreprises (span 3)
        String lieu = at.getZoneExecutante() != null ? at.getZoneExecutante().getNomZone()
                : (at.getServicesIntervenants() != null && !at.getServicesIntervenants().isBlank() ? at.getServicesIntervenants() : "...............");
        String services = at.getServicesIntervenants() != null ? at.getServicesIntervenants() : "...............";
        String entreprises = at.getEntreprisesIntervenantes() != null ? at.getEntreprisesIntervenantes() : "...............";

        Paragraph pLieu = new Paragraph();
        pLieu.setLeading(8f);
        pLieu.add(new Chunk("Lieu d'intervention : ", fl));
        pLieu.add(new Chunk(lieu + "   ", fv));
        pLieu.add(new Chunk("Services intervenants : ", fl));
        pLieu.add(new Chunk(services + "   ", fv));
        pLieu.add(new Chunk("Entreprises intervenantes : ", fl));
        pLieu.add(new Chunk(entreprises, fv));

        PdfPCell cLieu = createGridCell(pLieu, 3, null);
        table.addCell(cLieu);

        // Row 4: Description (span 2) + Date intervention (col 3)
        // Utiliser uniquement descriptionTravaux ; ne pas injecter l'objet qui est une donnée différente.
        String desc = (at.getDescriptionTravaux() != null && !at.getDescriptionTravaux().isBlank())
                ? at.getDescriptionTravaux()
                : "...............";
        String dateInt = at.getDateDebut() != null ? at.getDateDebut().format(DATE_FMT) : "...../...../..........";

        Paragraph pDesc = new Paragraph();
        pDesc.setLeading(8f);
        pDesc.add(new Chunk("Description de l'intervention : ", fl));
        pDesc.add(new Chunk(desc, fv));

        PdfPCell cDesc = createGridCell(pDesc, 2, null);
        table.addCell(cDesc);

        Paragraph pDate = new Paragraph();
        pDate.setLeading(8f);
        pDate.add(new Chunk("Date de l'intervention : ", fl));
        pDate.add(new Chunk(dateInt, fv));

        PdfPCell cDate = createGridCell(pDate, 1, null);
        table.addCell(cDate);

        // Row 5: Heure début (col 1 & 2) + Heure fin (col 3)
        String hDebut = at.getHeureDebut() != null ? at.getHeureDebut().toString() : ".....:.....";
        String hFin = at.getHeureFin() != null ? at.getHeureFin().toString() : ".....:.....";

        Paragraph pHDebut = new Paragraph();
        pHDebut.setLeading(8f);
        pHDebut.add(new Chunk("Heure début : ", fl));
        pHDebut.add(new Chunk(hDebut, fv));

        PdfPCell cHDebut = createGridCell(pHDebut, 2, null);
        table.addCell(cHDebut);

        Paragraph pHFin = new Paragraph();
        pHFin.setLeading(8f);
        pHFin.add(new Chunk("Heure fin (prévue) : ", fl));
        pHFin.add(new Chunk(hFin, fv));

        PdfPCell cHFin = createGridCell(pHFin, 1, null);
        table.addCell(cHFin);

        doc.add(table);
    }

    private void ajouterFormulaireSectionA(Document doc, AutorisationTravail at) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{33.3f, 33.3f, 33.4f});
        table.setSpacingAfter(2f);

        PdfPCell header = createHeaderSectionCell("A-  Risques évalués liés à / au");
        header.setColspan(3);
        table.addCell(header);

        // 8 lignes x 3 colonnes = 24 cases exactes
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "hauteur"), "Travail en hauteur"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "circulation personne", "personne"), "Circulation personnes"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "tournante", "machine"), "Machines tournantes"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "réseau", "reseau", "enterr"), "Proximité aux réseaux enterrés"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "chimique"), "Produits chimiques"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "chaud"), "Produits chauds"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "inflammable"), "Produits inflammables"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "eclairage", "éclairage"), "Eclairage insuffisant"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "pression"), "Equipement sous pression"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "manuelle"), "Manutention manuelle"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "intempérie", "intemperie"), "Intempéries"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "electri", "électri"), "Electricité"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "mécanique", "mecanique"), "Manutention mécanique"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "poussière", "poussiere", "ambiance"), "Ambiance poussiéreuse"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "confiné", "confine"), "Espaces confinés"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "outillage"), "Outillage"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "véhicule", "vehicule"), "Circulation véhicules"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "atex"), "Zone ATEX"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "bruit"), "Bruit (> 80 dB)"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "co-activité", "coactivité", "coactivite"), "Co-activité"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "noyade"), "Noyade"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRisqueCoche(at, "autre"), "Autres à préciser : .........."), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(false, "Autres à préciser : .........."), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(false, "Autres à préciser : .........."), 1, null));

        doc.add(table);
    }

    private void ajouterFormulaireSectionB(Document doc, AutorisationTravail at) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{33.3f, 33.3f, 33.4f});
        table.setSpacingAfter(2f);

        PdfPCell header = createHeaderSectionCell("B-  Mesures prises pour préparer l'intervention");
        header.setColspan(3);
        table.addCell(header);

        table.addCell(createGridCell(createCheckboxParagraph(isMesureCoche(at, "vidange", "circuit"), "Vidange de l'équipement et ses circuits"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isMesureCoche(at, "dépressurisation", "depressurisation", "pression"), "Dépressurisation"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isMesureCoche(at, "nettoyage"), "Nettoyage"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isMesureCoche(at, "consignation", "energie", "énergie"), "Consignation des Énergies"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isMesureCoche(at, "ventilation"), "Ventilation"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isMesureCoche(at, "balisage"), "Balisage"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isMesureCoche(at, "eclairage", "éclairage"), "Eclairage"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isMesureCoche(at, "autre"), "Autres à préciser : .........."), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(false, "Autres à préciser : .........."), 1, null));

        doc.add(table);
    }

    private void ajouterFormulaireSectionC(Document doc, AutorisationTravail at) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{33.3f, 33.3f, 33.4f});
        table.setSpacingAfter(2f);

        PdfPCell header = createHeaderSectionCell("C-  Moyens d'accès nécessaires");
        header.setColspan(3);
        table.addCell(header);

        table.addCell(createGridCell(createCheckboxParagraph(isMoyenCoche(at, "escabeau"), "Escabeau"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isMoyenCoche(at, "passerelle"), "Passerelle"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isMoyenCoche(at, "autre"), "Autres à préciser : .........."), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isMoyenCoche(at, "échafaudage", "echafaudage"), "Echafaudage"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isMoyenCoche(at, "nacelle", "pemp"), "Nacelle, PEMP"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(false, ".........................................."), 1, null));

        doc.add(table);
    }

    private void ajouterFormulaireSectionD(Document doc, AutorisationTravail at) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{33.3f, 33.3f, 33.4f});
        table.setSpacingAfter(2f);

        PdfPCell header = createHeaderSectionCell("D-  EPI spécifiques nécessaires");
        header.setColspan(3);
        table.addCell(header);

        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "casque soudure", "soudure"), "Casque soudure"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "ari", "appareil respiratoire"), "ARI"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "tenue", "antiacide"), "Tenue antiacide"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "masque à gaz", "gaz"), "Masque à gaz"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "stop bruit", "bruit", "bouchon"), "Stop bruit"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "tablier"), "Tablier soudure"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "panoramique"), "Masque panoramique"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "cagoule"), "Cagoule"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "guêtre", "guetre"), "Guêtres"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "poussière", "poussiere"), "Masque à poussières"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "gant", "antiacide"), "Gants antiacides"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "écran facial", "ecran facial", "visière", "visiere"), "Ecran facial"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "lunette", "étanche", "etanche"), "Lunettes étanches"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "manutention", "cuir"), "Gants de manutention"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "botte"), "Bottes de sécurité"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "harnais"), "Harnais de sécurité"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isEpiCoche(at, "autre"), "Autres à préciser : .........."), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(false, "Autres à préciser : .........."), 1, null));

        doc.add(table);
    }

    private void ajouterFormulaireSectionE(Document doc, AutorisationTravail at) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{33.3f, 33.3f, 33.4f});
        table.setSpacingAfter(2f);

        PdfPCell header = createHeaderSectionCell("E-  Permis nécessaires");
        header.setColspan(3);
        table.addCell(header);

        table.addCell(createGridCell(createCheckboxParagraph(isPermisCoche(at, "confiné", "confine"), "Permis pour espace confiné"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isPermisCoche(at, "feu"), "Permis de feu"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isPermisCoche(at, "consignation"), "Plan de consignation"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isPermisCoche(at, "hauteur"), "Permis pour travail en hauteur"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isPermisCoche(at, "fouille"), "Permis de fouille"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isPermisCoche(at, "autre"), "Autres à préciser : .........."), 1, null));

        doc.add(table);
    }

    private void ajouterFormulaireSectionF(Document doc, AutorisationTravail at) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(2f);

        PdfPCell header = createHeaderSectionCell("F-  Mesures de sécurité prises par l'exécutant (Référence du mode opératoire, ....)");
        table.addCell(header);

        // Section F : ne jamais injecter de texte générique. Laisser vide si non renseigné.
        String mesures = (at.getMesuresSecuriteExecutant() != null && !at.getMesuresSecuriteExecutant().isBlank())
                ? at.getMesuresSecuriteExecutant()
                : "...............";

        Paragraph p = new Paragraph(mesures, FontFactory.getFont(FontFactory.HELVETICA, 7.2f, Color.BLACK));
        p.setLeading(8.5f);

        PdfPCell cell = createGridCell(p, 1, null);
        cell.setMinimumHeight(18f);
        table.addCell(cell);

        doc.add(table);
    }

    private void ajouterFormulaireSectionG(Document doc, AutorisationTravail at) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{18f, 27.3f, 27.3f, 27.4f});
        table.setSpacingAfter(2f);

        PdfPCell header = createHeaderSectionCell("G-  Validation de l'autorisation de travail");
        header.setColspan(4);
        table.addCell(header);

        Font fh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.2f, Color.BLACK);
        Font fl = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.2f, Color.BLACK);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA, 7f, Color.BLACK);

        // Header Row: [Poste] | 1er poste | 2ème poste | 3ème poste
        table.addCell(createGridCell(new Paragraph("", fl), 1, new Color(245, 245, 245)));
        table.addCell(createGridCell(new Paragraph("1er poste", fh), 1, new Color(240, 244, 250)));
        table.addCell(createGridCell(new Paragraph("2ème poste", fh), 1, new Color(240, 244, 250)));
        table.addCell(createGridCell(new Paragraph("3ème poste", fh), 1, new Color(240, 244, 250)));

        List<Visa> visas = at.getVisas();
        PosteVisa p1Ceep = extractVisa(visas, true, 1, at);
        PosteVisa p2Ceep = extractVisa(visas, true, 2, at);
        PosteVisa p3Ceep = extractVisa(visas, true, 3, at);

        PosteVisa p1Ceee = extractVisa(visas, false, 1, at);
        PosteVisa p2Ceee = extractVisa(visas, false, 2, at);
        PosteVisa p3Ceee = extractVisa(visas, false, 3, at);

        // Row Nom CEEP
        table.addCell(createGridCell(new Paragraph("Nom CEEP", fl), 1, new Color(248, 249, 250)));
        table.addCell(createGridCell(new Paragraph(p1Ceep.nomSignataire != null ? p1Ceep.nomSignataire : ".........................", fv), 1, null));
        table.addCell(createGridCell(new Paragraph(p2Ceep.nomSignataire != null ? p2Ceep.nomSignataire : ".........................", fv), 1, null));
        table.addCell(createGridCell(new Paragraph(p3Ceep.nomSignataire != null ? p3Ceep.nomSignataire : ".........................", fv), 1, null));

        // Row Visa CEEP (with signature image)
        table.addCell(createGridCell(new Paragraph("Visa CEEP", fl), 1, new Color(248, 249, 250)));
        table.addCell(createSignatureCell(p1Ceep));
        table.addCell(createSignatureCell(p2Ceep));
        table.addCell(createSignatureCell(p3Ceep));

        // Row Nom CEEE
        table.addCell(createGridCell(new Paragraph("Nom CEEE", fl), 1, new Color(248, 249, 250)));
        table.addCell(createGridCell(new Paragraph(p1Ceee.nomSignataire != null ? p1Ceee.nomSignataire : ".........................", fv), 1, null));
        table.addCell(createGridCell(new Paragraph(p2Ceee.nomSignataire != null ? p2Ceee.nomSignataire : ".........................", fv), 1, null));
        table.addCell(createGridCell(new Paragraph(p3Ceee.nomSignataire != null ? p3Ceee.nomSignataire : ".........................", fv), 1, null));

        // Row Visa CEEE (with signature image)
        table.addCell(createGridCell(new Paragraph("Visa CEEE", fl), 1, new Color(248, 249, 250)));
        table.addCell(createSignatureCell(p1Ceee));
        table.addCell(createSignatureCell(p2Ceee));
        table.addCell(createSignatureCell(p3Ceee));

        doc.add(table);
    }

    private void ajouterFormulaireSectionReception(Document doc, AutorisationTravail at) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{33.3f, 33.3f, 33.4f});
        table.setSpacingAfter(1f);

        // Header Réception
        PdfPCell header = createHeaderSectionCell("Réception des travaux");
        header.setColspan(3);
        table.addCell(header);

        Font fl = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.2f, Color.BLACK);
        Font fv = FontFactory.getFont(FontFactory.HELVETICA, 7.2f, Color.BLACK);

        ReceptionTravaux rec = at.getReceptionTravaux();
        String dateRec = rec != null && rec.getDateReception() != null ? rec.getDateReception().format(DATE_FMT) : "...../...../..........";
        String heureRec = rec != null && rec.getDateReception() != null ? rec.getDateReception().format(DateTimeFormatter.ofPattern("HH:mm")) : ".....:.....";

        // Date & Heure
        Paragraph pDateHeure = new Paragraph();
        pDateHeure.setLeading(8f);
        pDateHeure.add(new Chunk("Date de réception : ", fl));
        pDateHeure.add(new Chunk(dateRec + "          ", fv));
        pDateHeure.add(new Chunk("Heure de réception : ", fl));
        pDateHeure.add(new Chunk(heureRec, fv));

        PdfPCell cDH = createGridCell(pDateHeure, 3, null);
        table.addCell(cDH);

        // Subtitle Cocher en cas de remise en place
        Paragraph pSub = new Paragraph("Cocher en cas de remise en place :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, new Color(40, 40, 40)));
        pSub.setLeading(7.8f);
        PdfPCell cSub = createGridCell(pSub, 3, new Color(245, 247, 250));
        table.addCell(cSub);

        // 5 lignes x 3 colonnes = 15 cases remise en place
        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "boulon"), "Boulonnerie"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "moteur"), "Cache moteur"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "compensateur"), "Cache compensateur"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "bride"), "Cache bride"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "caillebotis"), "Caillebotis"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "accouplement"), "Cache accouplement"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "support", "circuit"), "Support circuit"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "couvercle"), "Couvercle"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "trappe"), "Trappe"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "tambour"), "Cache tambour"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "arrêt", "arret", "urgence"), "Arrêt d'urgence"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "convoyeur", "capot"), "Capot convoyeur"), 1, null));

        table.addCell(createGridCell(createCheckboxParagraph(isRemiseCoche(at, "garde-corps", "gardecorps"), "Garde-corps"), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(false, "Autres à préciser : .........."), 1, null));
        table.addCell(createGridCell(createCheckboxParagraph(false, "Autres à préciser : .........."), 1, null));

        // Essai concluant : les deux conditions doivent être vraies (essais effectués ET conformes).
        // travauxConformes seul ne suffit pas à cocher "Oui".
        boolean essaiOui = rec != null
                && Boolean.TRUE.equals(rec.getEssaisEffectues())
                && Boolean.TRUE.equals(rec.getEssaisConformes());
        boolean essaiNon = rec != null
                && Boolean.TRUE.equals(rec.getEssaisEffectues())
                && Boolean.FALSE.equals(rec.getEssaisConformes());

        Paragraph pEssai = new Paragraph();
        pEssai.setLeading(8f);
        pEssai.add(new Chunk("Essai concluant\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.2f, Color.BLACK)));
        pEssai.add(new Chunk(essaiOui ? "[X] Oui   " : "[  ] Oui   ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, essaiOui ? new Color(0, 120, 0) : Color.DARK_GRAY)));
        pEssai.add(new Chunk(essaiNon ? "[X] Non" : "[  ] Non", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, essaiNon ? Color.RED : Color.DARK_GRAY)));

        PdfPCell cEssai = createGridCell(pEssai, 1, new Color(250, 250, 252));
        table.addCell(cEssai);

        Paragraph pWarning = new Paragraph();
        pWarning.setLeading(7.5f);
        pWarning.add(new Chunk("! Pour les modifications assujetties au Standard MOC, les exigences de ce standard, notamment celles du PSSR doivent être remplies avant d'entamer les essais\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.2f, new Color(150, 40, 0))));
        pWarning.add(new Chunk("La réception ne se fait que si les conditions de base sont assurées, que les mesures sécuritaires sont en place et que les essais sont concluants.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 6.2f, new Color(70, 70, 70))));

        PdfPCell cWarn = createGridCell(pWarning, 2, null);
        table.addCell(cWarn);

        // Header Validation de la réception
        PdfPCell hVal = createHeaderSectionCell("Validation de la réception");
        hVal.setColspan(3);
        table.addCell(hVal);

        // 3 colonnes signataires réception
        PosteVisa p1Ceep = extractVisa(at.getVisas(), true, 1, at);
        PosteVisa p1Ceee = extractVisa(at.getVisas(), false, 1, at);

        table.addCell(createReceptionSignCell("CEEP (Nom & Visa)", p1Ceep));
        table.addCell(createReceptionSignCell("CEEE (Nom & Visa)", p1Ceee));
        table.addCell(createSousTraitantSignCell("Sous-traitant (Nom & Visa)", at.getEntreprisesIntervenantes()));

        // N.B. note en bas
        Paragraph pNB = new Paragraph("N.B : Les 3 souches doivent être dûment instruites et visées", FontFactory.getFont(FontFactory.HELVETICA, 7f, Font.BOLD | Font.ITALIC, new Color(80, 80, 80)));
        pNB.setLeading(7.5f);
        PdfPCell cNB = createGridCell(pNB, 3, null);
        cNB.setBorder(Rectangle.NO_BORDER);
        cNB.setPaddingTop(2f);
        table.addCell(cNB);

        doc.add(table);
    }

    private void ajouterPageInstructions(Document doc) throws DocumentException {
        Paragraph title = new Paragraph("INSTRUCTION D'ÉTABLISSEMENT DE L'AUTORISATION DE TRAVAIL",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, new Color(20, 30, 50)));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(6f);
        title.setSpacingAfter(8f);
        doc.add(title);

        ajouterBlocInstruction(doc, "Visite préalable du chantier",
                "Une visite préalable et une évaluation des risques se fait conjointement par le propriétaire de l'installation, les responsables des travaux et les intervenants.\n" +
                "Lors de la visite le propriétaire de l'installation, les responsables des travaux et les intervenants procèdent à la revue et à la mise à jour de l'analyse des risques / Plan de Prévention de manière à s'assurer que l'ensemble des risques potentiels pour la sécurité et pour l'environnement sont bien identifiés et que les mesures de prévention prévues sont adaptées et suffisantes.\n" +
                "Le démarrage de l'intervention est conditionné par la mise en place de l'ensemble des mesures préconisées lors de la visite préalable au chantier où se déroulera l'intervention.\n" +
                "Lors de la visite de chantier, le point d'affichage, qui doit être à l'entrée de la zone d'intervention, de l'autorisation de travail et des permis est formellement fixé.");

        ajouterBlocInstruction(doc, "Rédaction de l'autorisation de Travail et établissement des permis",
                "La rédaction de l'autorisation de travail et des permis se fait sur le terrain. À cet effet, les trois souches de l'autorisation de travail sont dûment instruites et signées.\n" +
                "En particulier, toutes les dispositions retenues lors de la visite préalable doivent être consignées dans l'autorisation de travail.\n" +
                "L'instruction des cadrans B, C, D, E et F de l'autorisation de travail doit découler des informations du cadran A relatif à l'évaluation des risques.\n" +
                "La zone d'intervention doit être délimitée et balisée.\n" +
                "Afficher une copie de l'autorisation à l'entrée du balisage de la zone d'intervention.");

        ajouterBlocInstruction(doc, "Validité de l'autorisation de Travail",
                "Chaque début de poste, une mise à jour avec reconduction de l'autorisation de travail s'effectue sous la responsabilité du CEEP en collaboration avec le CEEE.\n" +
                "L'autorisation de travail est refaite pour les cas suivants :\n" +
                "  • Intervention dépassant 24 heures,\n" +
                "  • Changement d'un des points notifié dans l'autorisation de travail (nouveau risque, équipe d'intervention, périmètre, etc.),\n" +
                "  • Accident ou incident.");

        ajouterBlocInstruction(doc, "Fin de l'intervention & Réception",
                "Avant la déclaration de la fin de l'intervention, le CEEE doit s'assurer qu'au minimum les dispositions exigées dans l'autorisation relatives à la remise en état de l'équipement sont respectées et mise en place. Une attention particulière est donnée au rangement et à la propreté, à la remise des EPC, de la boulonnerie, etc.\n" +
                "En cas de modifications au niveau de l'équipement/installation assujetti au Standard MOC, les dispositions du standard doivent être respectées, notamment la revue sécurité avant démarrage (PSSR).\n" +
                "Une visite de fin de chantier est alors effectuée entre le CEEP et le CEEE. Une vérification de l'état de la zone d'intervention est effectuée et des essais sont réalisés avant de signer les 3 souches des coupons de fin des travaux.");

        ajouterBlocInstruction(doc, "Gestion documentaire & archivage",
                "Les dispositions suivantes doivent être observées en matière de gestion documentaire et d'archivage :\n" +
                "1) L'autorisation de travail comprend 3 souches : Une copie pour le propriétaire (rose) à afficher pendant la durée de l'intervention au niveau d'un point identifié à l'entrée de la zone d'intervention, une copie pour l'exécutant (blanche) et une 3ème copie pour le sous-traitant (vert).\n" +
                "2) L'archivage de tous les documents ayant constitués l'intervention (autorisation de travail (souche rose), permis y afférents, analyse des risques, etc.) sera assuré par l'entité propriétaire de l'installation.\n" +
                "3) L'archivage se fait pour une durée d'au moins un an après la réception des travaux. Ils peuvent être détruits par la suite par l'entité propriétaire.");
    }

    private void ajouterBlocInstruction(Document doc, String titre, String contenu) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(6f);

        // Header bloc
        Paragraph pTitre = new Paragraph(titre, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.2f, Color.BLACK));
        pTitre.setLeading(9.5f);
        PdfPCell cTitre = new PdfPCell(pTitre);
        cTitre.setBackgroundColor(new Color(235, 238, 242));
        cTitre.setPaddingTop(2.5f);
        cTitre.setPaddingBottom(3f);
        cTitre.setPaddingLeft(4f);
        cTitre.setBorderColor(new Color(160, 160, 160));
        cTitre.setBorderWidth(0.5f);
        table.addCell(cTitre);

        // Contenu
        Paragraph pContenu = new Paragraph(contenu, FontFactory.getFont(FontFactory.HELVETICA, 7.4f, Color.BLACK));
        pContenu.setLeading(9f);
        pContenu.setAlignment(Element.ALIGN_JUSTIFIED);
        PdfPCell cContenu = new PdfPCell(pContenu);
        cContenu.setPadding(4f);
        cContenu.setBorderColor(new Color(160, 160, 160));
        cContenu.setBorderWidth(0.5f);
        table.addCell(cContenu);

        doc.add(table);
    }

    private PdfPCell createGridCell(Paragraph content, int colspan, Color bgColor) {
        PdfPCell cell = new PdfPCell();
        if (content != null) {
            cell.addElement(content);
        }
        if (colspan > 1) {
            cell.setColspan(colspan);
        }
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        cell.setPaddingTop(1.5f);
        cell.setPaddingBottom(1.5f);
        cell.setPaddingLeft(3f);
        cell.setPaddingRight(3f);
        cell.setBorderColor(new Color(160, 160, 160));
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell createHeaderSectionCell(String title) {
        Paragraph p = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.8f, Color.WHITE));
        p.setLeading(8.5f);
        PdfPCell cell = new PdfPCell(p);
        cell.setBackgroundColor(new Color(35, 45, 60));
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(2.5f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setBorderColor(new Color(120, 120, 120));
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private Paragraph createCheckboxParagraph(boolean checked, String label) {
        Paragraph p = new Paragraph();
        p.setLeading(7.8f);
        if (checked) {
            Chunk chk = new Chunk("[X] ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.2f, new Color(0, 120, 0)));
            Chunk lbl = new Chunk(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.8f, Color.BLACK));
            p.add(chk);
            p.add(lbl);
        } else {
            Chunk chk = new Chunk("[  ] ", FontFactory.getFont(FontFactory.HELVETICA, 7.2f, new Color(130, 130, 130)));
            Chunk lbl = new Chunk(label, FontFactory.getFont(FontFactory.HELVETICA, 6.8f, new Color(40, 40, 40)));
            p.add(chk);
            p.add(lbl);
        }
        return p;
    }

    private PdfPCell createSignatureCell(PosteVisa pv) {
        PdfPCell cell = new PdfPCell();
        cell.setPaddingTop(1f);
        cell.setPaddingBottom(1.5f);
        cell.setPaddingLeft(3f);
        cell.setPaddingRight(3f);
        cell.setBorderColor(new Color(160, 160, 160));
        cell.setBorderWidth(0.5f);
        cell.setMinimumHeight(24f);

        if (pv != null && pv.signatureImage != null) {
            cell.addElement(pv.signatureImage);
            if (pv.dateHeure != null) {
                Paragraph pDate = new Paragraph(pv.dateHeure, FontFactory.getFont(FontFactory.HELVETICA, 6.2f, new Color(80, 80, 80)));
                pDate.setLeading(6.8f);
                cell.addElement(pDate);
            }
        } else if (pv != null && pv.dateHeure != null) {
            Paragraph pSign = new Paragraph("Signé le " + pv.dateHeure, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, new Color(0, 100, 50)));
            pSign.setLeading(7f);
            cell.addElement(pSign);
        } else {
            Paragraph pEmpty = new Paragraph(".........................", FontFactory.getFont(FontFactory.HELVETICA, 6.5f, new Color(160, 160, 160)));
            pEmpty.setLeading(7f);
            cell.addElement(pEmpty);
        }
        return cell;
    }

    private PdfPCell createReceptionSignCell(String roleTitle, PosteVisa pv) {
        PdfPCell cell = new PdfPCell();
        cell.setPaddingTop(1.5f);
        cell.setPaddingBottom(2f);
        cell.setPaddingLeft(3f);
        cell.setPaddingRight(3f);
        cell.setBorderColor(new Color(160, 160, 160));
        cell.setBorderWidth(0.5f);
        cell.setMinimumHeight(28f);

        Paragraph pTitle = new Paragraph(roleTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, Color.BLACK));
        pTitle.setLeading(7.8f);
        cell.addElement(pTitle);

        if (pv != null && pv.nomSignataire != null) {
            Paragraph pNom = new Paragraph(pv.nomSignataire, FontFactory.getFont(FontFactory.HELVETICA, 6.5f, Color.BLACK));
            pNom.setLeading(7.2f);
            cell.addElement(pNom);
        }
        if (pv != null && pv.signatureImage != null) {
            cell.addElement(pv.signatureImage);
        }
        if (pv != null && pv.dateHeure != null) {
            Paragraph pDate = new Paragraph(pv.dateHeure, FontFactory.getFont(FontFactory.HELVETICA, 6.2f, new Color(80, 80, 80)));
            pDate.setLeading(6.8f);
            cell.addElement(pDate);
        } else {
            Paragraph pDotted = new Paragraph("........................................", FontFactory.getFont(FontFactory.HELVETICA, 6.5f, new Color(160, 160, 160)));
            pDotted.setLeading(7f);
            cell.addElement(pDotted);
        }
        return cell;
    }

    private PdfPCell createSousTraitantSignCell(String roleTitle, String entrepriseName) {
        PdfPCell cell = new PdfPCell();
        cell.setPaddingTop(1.5f);
        cell.setPaddingBottom(2f);
        cell.setPaddingLeft(3f);
        cell.setPaddingRight(3f);
        cell.setBorderColor(new Color(160, 160, 160));
        cell.setBorderWidth(0.5f);
        cell.setMinimumHeight(28f);

        Paragraph pTitle = new Paragraph(roleTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, Color.BLACK));
        pTitle.setLeading(7.8f);
        cell.addElement(pTitle);

        if (entrepriseName != null && !entrepriseName.isBlank()) {
            Paragraph pEnt = new Paragraph(entrepriseName, FontFactory.getFont(FontFactory.HELVETICA, 6.5f, Color.BLACK));
            pEnt.setLeading(7.2f);
            cell.addElement(pEnt);
        }
        Paragraph pDotted = new Paragraph("........................................", FontFactory.getFont(FontFactory.HELVETICA, 6.5f, new Color(160, 160, 160)));
        pDotted.setLeading(7f);
        cell.addElement(pDotted);

        return cell;
    }

    // =========================================================================
    // RÉSOLUTION DES IDS EN NOMS (Option A - fix bug cases cochées PDF)
    // Les formXxxIds stockent des UUIDs : on les résout via les repositories
    // pour retrouver les noms et faire le matching par mot-clé.
    // =========================================================================

    private List<String> resolveNoms(String jsonIds,
                                     java.util.function.Function<List<String>, List<String>> fetcher) {
        if (jsonIds == null || jsonIds.isBlank()) return Collections.emptyList();
        try {
            List<String> ids = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(jsonIds, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            if (ids.isEmpty()) return Collections.emptyList();
            return fetcher.apply(ids);
        } catch (Exception e) {
            log.warn("resolveNoms: impossible de parser les IDs JSON '{}': {}", jsonIds, e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean matchesKeywords(String nom, String... keywords) {
        if (nom == null) return false;
        String nomLower = nom.toLowerCase();
        for (String kw : keywords) {
            if (nomLower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private boolean isRisqueCoche(AutorisationTravail at, String... keywords) {
        if (at == null) return false;

        // Source 1 : collection ManyToMany déjà chargée (cas nominal)
        if (at.getRisques() != null && !at.getRisques().isEmpty()) {
            for (Risque r : at.getRisques()) {
                if (matchesKeywords(r.getNomRisque(), keywords)) return true;
            }
        }

        // Source 2 : formRisquesIds (JSON d'UUIDs) → résolution via repository
        if (at.getFormRisquesIds() != null && !at.getFormRisquesIds().isBlank()) {
            List<String> noms = resolveNoms(at.getFormRisquesIds(), ids -> {
                List<String> result = new ArrayList<>();
                risqueRepository.findAllById(ids).forEach(r -> {
                    if (r.getNomRisque() != null) result.add(r.getNomRisque());
                });
                return result;
            });
            for (String nom : noms) {
                if (matchesKeywords(nom, keywords)) return true;
            }
        }

        // Source 3 : fallback sur descriptionTravaux (AT sans données de formulaire structurées)
        if (at.getDescriptionTravaux() != null && !at.getDescriptionTravaux().isBlank()) {
            if (matchesKeywords(at.getDescriptionTravaux(), keywords)) return true;
        }
        return false;
    }

    private boolean isMesureCoche(AutorisationTravail at, String... keywords) {
        if (at == null) return false;

        // Source 1 : collection ManyToMany déjà chargée
        if (at.getMesures() != null && !at.getMesures().isEmpty()) {
            for (MesurePreparation m : at.getMesures()) {
                if (matchesKeywords(m.getNomMesure(), keywords)) return true;
            }
        }

        // Source 2 : formMesuresIds → résolution via repository
        if (at.getFormMesuresIds() != null && !at.getFormMesuresIds().isBlank()) {
            List<String> noms = resolveNoms(at.getFormMesuresIds(), ids -> {
                List<String> result = new ArrayList<>();
                mesureRepository.findAllById(ids).forEach(m -> {
                    if (m.getNomMesure() != null) result.add(m.getNomMesure());
                });
                return result;
            });
            for (String nom : noms) {
                if (matchesKeywords(nom, keywords)) return true;
            }
        }

        // Source 3 : fallback sur descriptionTravaux
        if (at.getDescriptionTravaux() != null && !at.getDescriptionTravaux().isBlank()) {
            if (matchesKeywords(at.getDescriptionTravaux(), keywords)) return true;
        }
        return false;
    }

    private boolean isMoyenCoche(AutorisationTravail at, String... keywords) {
        if (at == null) return false;

        // Source 1 : collection ManyToMany déjà chargée
        if (at.getMoyensAcces() != null && !at.getMoyensAcces().isEmpty()) {
            for (MoyenAcces m : at.getMoyensAcces()) {
                if (matchesKeywords(m.getNomMoyen(), keywords)) return true;
            }
        }

        // Source 2 : formMoyensIds → résolution via repository
        if (at.getFormMoyensIds() != null && !at.getFormMoyensIds().isBlank()) {
            List<String> noms = resolveNoms(at.getFormMoyensIds(), ids -> {
                List<String> result = new ArrayList<>();
                moyenAccesRepository.findAllById(ids).forEach(m -> {
                    if (m.getNomMoyen() != null) result.add(m.getNomMoyen());
                });
                return result;
            });
            for (String nom : noms) {
                if (matchesKeywords(nom, keywords)) return true;
            }
        }

        // Source 3 : fallback sur descriptionTravaux
        if (at.getDescriptionTravaux() != null && !at.getDescriptionTravaux().isBlank()) {
            if (matchesKeywords(at.getDescriptionTravaux(), keywords)) return true;
        }
        return false;
    }

    private boolean isEpiCoche(AutorisationTravail at, String... keywords) {
        if (at == null) return false;

        // Source 1 : collection ManyToMany déjà chargée
        if (at.getEpis() != null && !at.getEpis().isEmpty()) {
            for (EPI e : at.getEpis()) {
                if (matchesKeywords(e.getNomEPI(), keywords)) return true;
            }
        }

        // Source 2 : formEpisIds → résolution via repository
        if (at.getFormEpisIds() != null && !at.getFormEpisIds().isBlank()) {
            List<String> noms = resolveNoms(at.getFormEpisIds(), ids -> {
                List<String> result = new ArrayList<>();
                epiRepository.findAllById(ids).forEach(e -> {
                    if (e.getNomEPI() != null) result.add(e.getNomEPI());
                });
                return result;
            });
            for (String nom : noms) {
                if (matchesKeywords(nom, keywords)) return true;
            }
        }

        // Source 3 : fallback sur descriptionTravaux
        if (at.getDescriptionTravaux() != null && !at.getDescriptionTravaux().isBlank()) {
            if (matchesKeywords(at.getDescriptionTravaux(), keywords)) return true;
        }
        return false;
    }

    private boolean isPermisCoche(AutorisationTravail at, String... keywords) {
        if (at == null) return false;

        // Source 1 : collection Permis déjà chargée (typePermis.nom)
        if (at.getPermis() != null && !at.getPermis().isEmpty()) {
            for (Permis p : at.getPermis()) {
                String nom = p.getTypePermis() != null ? p.getTypePermis().getNom() : null;
                if (matchesKeywords(nom, keywords)) return true;
            }
        }

        // Source 2 : formPermisIds → résolution des TypePermis via repository
        if (at.getFormPermisIds() != null && !at.getFormPermisIds().isBlank()) {
            List<String> noms = resolveNoms(at.getFormPermisIds(), ids -> {
                List<String> result = new ArrayList<>();
                typePermisRepository.findAllById(ids).forEach(tp -> {
                    if (tp.getNom() != null) result.add(tp.getNom());
                });
                return result;
            });
            for (String nom : noms) {
                if (matchesKeywords(nom, keywords)) return true;
            }
        }
        return false;
    }

    private boolean isRemiseCoche(AutorisationTravail at, String... keywords) {
        if (at == null || at.getReceptionTravaux() == null) return false;
        ReceptionTravaux rec = at.getReceptionTravaux();
        String txt = (rec.getTravauxRealises() != null ? rec.getTravauxRealises() : "") + " "
                + (rec.getObservations() != null ? rec.getObservations() : "") + " "
                + (rec.getCommentaireResponsable() != null ? rec.getCommentaireResponsable() : "");
        txt = txt.toLowerCase();
        for (String kw : keywords) {
            if (txt.contains(kw.toLowerCase())) return true;
        }
        if (Boolean.TRUE.equals(rec.getInstallationRemiseEnEtat()) && (keywords[0].equals("boulon") || keywords[0].equals("caillebotis"))) {
            return true;
        }
        return false;
    }

    private static class PosteVisa {
        String nomSignataire;
        Image signatureImage;
        String dateHeure;
    }

    private PosteVisa extractVisa(List<Visa> visas, boolean isCeep, int posteNum, AutorisationTravail at) {
        PosteVisa pv = new PosteVisa();
        if (visas != null) {
            for (Visa v : visas) {
                if (v == null || v.getStatut() == com.ocp.at.entity.enums.StatutVisa.REFUS || v.getStatut() == com.ocp.at.entity.enums.StatutVisa.REFUSE) continue;
                String comment = v.getCommentaire() != null ? v.getCommentaire().toLowerCase() : "";

                // Le visa créé par le formulaire CEEP porte explicitement le marqueur g1visaceep ou ceep.
                if (isCeep && posteNum == 1 && (comment.contains("g1visaceep") || comment.contains("ceep"))) {
                    PosteVisa exact = new PosteVisa();
                    Utilisateur u = v.getUtilisateur();
                    if (u != null) {
                        exact.nomSignataire = (u.getPrenom() != null ? u.getPrenom() : "") + " "
                                + (u.getNom() != null ? u.getNom() : "")
                                + (u.getMatricule() != null ? " (" + u.getMatricule() + ")" : "");
                    }
                    if (v.getSignaturePath() != null && !v.getSignaturePath().isBlank()) {
                        exact.signatureImage = loadSignatureImage(v.getSignaturePath());
                    }
                    if (v.getDateSignature() != null) {
                        exact.dateHeure = v.getDateSignature().format(DATETIME_FMT);
                    } else if (v.getDateVisa() != null) {
                        exact.dateHeure = v.getDateVisa().format(DATETIME_FMT);
                    }
                    if (exact.signatureImage != null || exact.nomSignataire != null) {
                        return exact;
                    }
                }

                // Identification rôle CEEP vs CEEE
                boolean matchesRole;
                if (isCeep) {
                    matchesRole = comment.contains("ceep") || comment.contains("g1visaceep")
                            || (!comment.contains("ceee") && (comment.contains("p1") || comment.contains("poste") || comment.contains("création") || comment.contains("creation")));
                    if (!matchesRole && v.getUtilisateur() != null) {
                        if (at != null && at.getProprietaireBrouillon() != null && at.getProprietaireBrouillon().getId().equals(v.getUtilisateur().getId())) {
                            matchesRole = true;
                        } else if (v.getUtilisateur().getRoles() != null) {
                            matchesRole = v.getUtilisateur().getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("CEEP"));
                        }
                    }
                } else {
                    matchesRole = comment.contains("ceee") || comment.contains("g1visaceee");
                    if (!matchesRole && v.getUtilisateur() != null && v.getUtilisateur().getRoles() != null) {
                        matchesRole = v.getUtilisateur().getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("CEEE"));
                    }
                }

                if (!matchesRole) continue;

                // Identification poste (1er, 2ème, 3ème)
                boolean matchesPoste;
                if (posteNum == 3) {
                    matchesPoste = comment.contains("g3") || comment.contains("p3") || comment.contains("3ème") || comment.contains("3eme") || comment.contains("3e");
                } else if (posteNum == 2) {
                    matchesPoste = comment.contains("g2") || comment.contains("p2") || comment.contains("2ème") || comment.contains("2eme") || comment.contains("2e");
                } else {
                    matchesPoste = !comment.contains("g2") && !comment.contains("g3") && !comment.contains("p2") && !comment.contains("p3") && !comment.contains("2ème") && !comment.contains("3ème");
                }

                if (matchesPoste) {
                    Utilisateur u = v.getUtilisateur();
                    if (u != null) {
                        pv.nomSignataire = (u.getPrenom() != null ? u.getPrenom() : "") + " " + (u.getNom() != null ? u.getNom() : "") + (u.getMatricule() != null ? " (" + u.getMatricule() + ")" : "");
                    }
                    if (v.getSignaturePath() != null && !v.getSignaturePath().isBlank()) {
                        pv.signatureImage = loadSignatureImage(v.getSignaturePath());
                    }
                    if (v.getDateSignature() != null) {
                        pv.dateHeure = v.getDateSignature().format(DATETIME_FMT);
                    } else if (v.getDateVisa() != null) {
                        pv.dateHeure = v.getDateVisa().format(DATETIME_FMT);
                    }
                    return pv;
                }
            }
        }

        // Pas de fallback automatique sur proprietaireBrouillon : seuls les visas réels signés
        // par le bon rôle doivent apparaître dans le formulaire officiel. Laisser vide sinon.
        return pv;
    }

    private Image loadSignatureImage(String signaturePath) {
        if (signaturePath == null || signaturePath.isBlank()) return null;
        try {
            Resource res = storageService.loadSignature(signaturePath);
            if (res != null && res.exists()) {
                byte[] imgBytes = res.getInputStream().readAllBytes();
                Image img = Image.getInstance(imgBytes);
                img.scaleToFit(80f, 22f);
                return img;
            }
        } catch (Exception e) {
            log.warn("Impossible de charger la signature: " + signaturePath, e);
        }
        return null;
    }

    // =========================================================================
    // GESTIONNAIRE DE PIED DE PAGE
    // =========================================================================

    private static class PiedDePageHandler extends com.lowagie.text.pdf.PdfPageEventHelper {
        private final String titre;

        PiedDePageHandler(String titre) {
            this.titre = titre;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // En-tête
            Phrase header = new Phrase(titre,
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(100, 100, 100)));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, header,
                    document.left(), document.top() + 10, 0);

            // Pied de page - date
            Phrase footer = new Phrase("OCP S.A. - Document Confidentiel",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(100, 100, 100)));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, footer,
                    document.left(), document.bottom() - 10, 0);

            // Numéro de page
            Phrase page = new Phrase("Page " + writer.getPageNumber(),
                    FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 100, 100)));
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, page,
                    document.right(), document.bottom() - 10, 0);
        }
    }

    // =========================================================================
    // CLASSE INTERNE LEGACY (conservée pour compatibilité)
    // =========================================================================

    private static class FooterPageEventHandler extends com.lowagie.text.pdf.PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase("OCP S.A. - Autorisation de Travail",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(100, 100, 100)));
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                    (document.left() + document.right()) / 2, document.bottom() - 10, 0);
        }
    }
}