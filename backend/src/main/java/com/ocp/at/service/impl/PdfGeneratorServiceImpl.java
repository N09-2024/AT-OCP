package com.ocp.at.service.impl;

import com.ocp.at.entity.*;

import com.ocp.at.service.PdfGeneratorService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implémentation du service de génération PDF utilisant OpenPDF (com.lowagie).
 * Génère des PDFs complets pour les Autorisations de Travail OCP.
 * Basé uniquement sur les champs réels des entités du projet.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

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
        log.info("Génération du PDF AT pour : {}", at.getNumero());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = creerDocument();
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new PiedDePageHandler("OCP - Autorisation de Travail " + at.getNumero()));
            doc.open();
            ajouterPageDeGarde(doc, at);
            doc.newPage();
            ajouterInfosGenerales(doc, at);
            ajouterInfosDocumentSource(doc, at);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur génération PDF AT {}", at.getNumero(), e);
            throw new RuntimeException("Erreur génération PDF AT", e);
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
            if (di.getInstallation() != null) {
                ajouterLigneCellule(table, "Installation :", di.getInstallation().getNomInstallation() + " (" + di.getInstallation().getCodeInstallation() + ")", fl, fv);
                if (di.getInstallation().getLocalisation() != null) {
                    ajouterLigneCellule(table, "Localisation :", di.getInstallation().getLocalisation(), fl, fv);
                }
                if (di.getInstallation().getAtelier() != null) {
                    ajouterLigneCellule(table, "Atelier :", di.getInstallation().getAtelier(), fl, fv);
                }
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
            if (ot.getInstallation() != null) {
                ajouterLigneCellule(table, "Installation :", ot.getInstallation().getNomInstallation() + " (" + ot.getInstallation().getCodeInstallation() + ")", fl, fv);
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
            if (bt.getInstallation() != null) {
                ajouterLigneCellule(table, "Installation :", bt.getInstallation().getNomInstallation() + " (" + bt.getInstallation().getCodeInstallation() + ")", fl, fv);
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
        ajouterTitreSection(doc, "6. VISAS & SIGNATURES");

        List<Visa> visas = at.getVisas();
        if (visas == null || visas.isEmpty()) {
            doc.add(new Paragraph("Aucun visa enregistré.",
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

            doc.add(table);
        }
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
