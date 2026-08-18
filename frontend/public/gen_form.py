
# gen_form.py - génère formulaire-at-ocp.html
import os

OUT = os.path.join(os.path.dirname(__file__), "formulaire-at-ocp.html")

HTML = r"""<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8"/>
<meta name="viewport" content="width=device-width,initial-scale=1.0"/>
<title>F-HSE-SEC-31-04 - Autorisation de Travail OCP</title>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"></script>
<script src="https://unpkg.com/docx@8.2.2/build/index.js"></script>
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:Arial,sans-serif;font-size:8.5pt;background:#d0d0d0;padding:20px;color:#000}
#toolbar{max-width:780px;margin:0 auto 12px;display:flex;gap:10px;align-items:center}
.btn-export{padding:8px 18px;font-size:10pt;font-weight:700;border:none;border-radius:4px;cursor:pointer;transition:opacity .2s}
.btn-export:hover{opacity:.85}
.btn-pdf{background:#dc2626;color:#fff}
.btn-word{background:#1d4ed8;color:#fff}
#form-container{max-width:780px;margin:0 auto;background:#fff;border:2px solid #000}
.form-header{display:grid;grid-template-columns:90px 1fr 155px;border-bottom:2px solid #000}
.header-logo{border-right:2px solid #000;padding:6px;display:flex;flex-direction:column;align-items:center;justify-content:center}
.ocp-text{font-size:14pt;font-weight:900;color:#00875A;line-height:1}
.header-title{display:flex;flex-direction:column;align-items:center;justify-content:center;border-right:2px solid #000;padding:6px}
.header-title h1{font-size:11pt;font-weight:900;text-transform:uppercase}
.header-title h2{font-size:13pt;font-weight:900}
.header-ref{padding:5px 6px;font-size:7.5pt;line-height:1.8}
.header-ref .ref-num{font-weight:900;font-size:8.5pt}
.form-table{width:100%;border-collapse:collapse}
.form-table td{border:1px solid #000;padding:2px 4px;vertical-align:top}
input[type=text],input[type=date],input[type=time],textarea{border:none;border-bottom:1px solid #555;background:transparent;font-family:Arial,sans-serif;font-size:8pt;width:100%;outline:none;padding:0 1px}
textarea{resize:vertical}
.sec-header{background:#c8c8c8;font-weight:900;font-size:8.5pt;padding:2px 5px}
.sec-header-red{background:#dc2626;color:#fff;font-weight:900;font-size:8.5pt;padding:2px 5px}
.cb-row{display:flex;align-items:flex-start;gap:4px;padding:1px 0}
.cb-row input[type=checkbox]{margin-top:1px;flex-shrink:0;width:11px;height:11px;cursor:pointer}
.cb-row label{font-size:8pt;line-height:1.25;cursor:pointer}
.g-table{width:100%;border-collapse:collapse}
.g-table td{border:1px solid #000;padding:2px 4px;font-size:8pt;vertical-align:middle}
.g-poste-header{background:#e0e0e0;font-weight:900;text-align:center}
.g-field-label{font-weight:700;background:#f5f5f5;width:42%}
.radio-row{display:flex;align-items:center;gap:14px;margin-top:4px}
.radio-row label{display:flex;align-items:center;gap:4px;cursor:pointer;font-weight:700}
@media print{body{background:#fff;padding:0}#toolbar{display:none}}
</style>
</head>
<body>
<div id="toolbar">
  <button class="btn-export btn-pdf" onclick="exportPDF()">&#128196; Exporter en PDF</button>
  <button class="btn-export btn-word" onclick="exportWord()">&#128196; Exporter en Word (.docx)</button>
  <span style="font-size:8pt;color:#444;">Formulaire F-HSE-SEC-31-04 &mdash; Autorisation de Travail OCP S-HSE-SEC-31</span>
</div>

<div id="form-container">
  <!-- HEADER -->
  <div class="form-header">
    <div class="header-logo">
      <svg width="52" height="52" viewBox="0 0 100 100">
        <polygon points="50,5 61,38 96,38 68,58 79,91 50,71 21,91 32,58 4,38 39,38" fill="#00875A"/>
        <circle cx="50" cy="55" r="14" fill="white"/>
        <circle cx="50" cy="55" r="9" fill="#00875A"/>
      </svg>
      <div class="ocp-text">OCP</div>
    </div>
    <div class="header-title">
      <h1>FORMULAIRE</h1>
      <h2>Autorisation de travail</h2>
    </div>
    <div class="header-ref">
      <div class="ref-num">F-HSE-SEC-31-04</div>
      <div>Edition : 1.0</div>
      <div>Date : 01/07/2016</div>
      <div>Page : 1/2</div>
    </div>
  </div>

  <!-- FORM BODY -->
  <table class="form-table">

    <!-- Identification row 1 -->
    <tr>
      <td colspan="3" style="padding:0;border:1px solid #000;">
        <table style="width:100%;border-collapse:collapse;">
          <tr>
            <td style="width:50%;border:none;border-right:1px solid #000;padding:3px 5px;">
              <strong>Site :</strong>&nbsp;<input type="text" id="site" style="width:90px;" placeholder="..........">
              &nbsp;&nbsp;<strong>Entit&eacute; :</strong>&nbsp;<input type="text" id="entite" style="width:90px;" placeholder="..........">
            </td>
            <td style="border:none;padding:3px 5px;">
              <strong>Autorisation de travail n&deg; :</strong>&nbsp;
              <input type="text" id="at_numero" style="width:140px;" placeholder="AT-2026-XXXX">
              <br><em style="font-size:7pt;">(Document valable pendant 24 heures &agrave; instruire sur le terrain)</em>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- Identification row 2 -->
    <tr>
      <td colspan="3" style="padding:2px 5px;">
        <strong>DI n&deg; :</strong>&nbsp;<input type="text" id="di" style="width:90px;">
        &nbsp;&nbsp;&nbsp;
        <strong>OT n&deg; :</strong>&nbsp;<input type="text" id="ot" style="width:90px;">
        &nbsp;&nbsp;&nbsp;
        <strong>BT n&deg; :</strong>&nbsp;<input type="text" id="bt" style="width:90px;">
      </td>
    </tr>

    <!-- Identification row 3 -->
    <tr>
      <td colspan="3" style="padding:2px 5px;">
        <strong>Lieu d&apos;intervention :</strong>&nbsp;<input type="text" id="lieu" style="width:120px;">
        &nbsp;&nbsp;
        <strong>Services intervenants :</strong>&nbsp;<input type="text" id="services" style="width:120px;">
        &nbsp;&nbsp;
        <strong>Entreprises intervenantes :</strong>&nbsp;<input type="text" id="entreprises" style="width:110px;">
      </td>
    </tr>

    <!-- Identification row 4 -->
    <tr>
      <td colspan="3" style="padding:0;">
        <table style="width:100%;border-collapse:collapse;">
          <tr>
            <td style="width:50%;border:none;border-right:1px solid #000;padding:2px 5px;">
              <strong>Description de l&apos;intervention :</strong>
              <input type="text" id="description" placeholder=".....................................">
            </td>
            <td style="border:none;padding:2px 5px;">
              <strong>Date :</strong>&nbsp;<input type="date" id="date_intervention" style="width:115px;">
              &nbsp;
              <strong>H.d&eacute;but :</strong>&nbsp;<input type="time" id="heure_debut" style="width:70px;">
              &nbsp;
              <strong>H.fin :</strong>&nbsp;<input type="time" id="heure_fin" style="width:70px;">
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- ===== SECTION A ===== -->
    <tr><td colspan="3" class="sec-header">A- Risques &eacute;valu&eacute;s li&eacute;s &agrave; / au</td></tr>
    <tr>
      <td style="padding:3px 5px;border-right:1px solid #000;width:34%">
        <div class="cb-row"><input type="checkbox" id="r_hauteur"><label for="r_hauteur">Travail en hauteur</label></div>
        <div class="cb-row"><input type="checkbox" id="r_reseaux"><label for="r_reseaux">Proximit&eacute; aux r&eacute;seaux enterr&eacute;s</label></div>
        <div class="cb-row"><input type="checkbox" id="r_inflammable"><label for="r_inflammable">Produits inflammables</label></div>
        <div class="cb-row"><input type="checkbox" id="r_man_manuel"><label for="r_man_manuel">Manutention manuelle</label></div>
        <div class="cb-row"><input type="checkbox" id="r_man_meca"><label for="r_man_meca">Manutention m&eacute;canique</label></div>
        <div class="cb-row"><input type="checkbox" id="r_outillage"><label for="r_outillage">Outillage</label></div>
        <div class="cb-row"><input type="checkbox" id="r_bruit"><label for="r_bruit">Bruit (&gt; 80 dB)</label></div>
        <div class="cb-row"><input type="checkbox" id="r_autre1"><label for="r_autre1">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:60px;border-bottom:1px solid #888;"></div>
      </td>
      <td style="padding:3px 5px;border-right:1px solid #000;width:33%">
        <div class="cb-row"><input type="checkbox" id="r_circ_pers"><label for="r_circ_pers">Circulation personnes</label></div>
        <div class="cb-row"><input type="checkbox" id="r_chimique"><label for="r_chimique">Produits chimiques</label></div>
        <div class="cb-row"><input type="checkbox" id="r_eclairage"><label for="r_eclairage">Eclairage insuffisant</label></div>
        <div class="cb-row"><input type="checkbox" id="r_intemperies"><label for="r_intemperies">Intemp&eacute;ries</label></div>
        <div class="cb-row"><input type="checkbox" id="r_poussiere"><label for="r_poussiere">Ambiance poussi&eacute;reuse</label></div>
        <div class="cb-row"><input type="checkbox" id="r_vehicule"><label for="r_vehicule">Circulation v&eacute;hicules</label></div>
        <div class="cb-row"><input type="checkbox" id="r_coactivite"><label for="r_coactivite">Co-activit&eacute;</label></div>
        <div class="cb-row"><input type="checkbox" id="r_autre2"><label for="r_autre2">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:60px;border-bottom:1px solid #888;"></div>
      </td>
      <td style="padding:3px 5px;width:33%">
        <div class="cb-row"><input type="checkbox" id="r_machines"><label for="r_machines">Machines tournantes</label></div>
        <div class="cb-row"><input type="checkbox" id="r_chauds"><label for="r_chauds">Produits chauds</label></div>
        <div class="cb-row"><input type="checkbox" id="r_pression"><label for="r_pression">Equipement sous pression</label></div>
        <div class="cb-row"><input type="checkbox" id="r_electricite"><label for="r_electricite">Electricit&eacute;</label></div>
        <div class="cb-row"><input type="checkbox" id="r_confine"><label for="r_confine">Espaces confin&eacute;s</label></div>
        <div class="cb-row"><input type="checkbox" id="r_atex"><label for="r_atex">Zone ATEX</label></div>
        <div class="cb-row"><input type="checkbox" id="r_noyade"><label for="r_noyade">Noyade</label></div>
        <div class="cb-row"><input type="checkbox" id="r_autre3"><label for="r_autre3">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:60px;border-bottom:1px solid #888;"></div>
      </td>
    </tr>

    <!-- ===== SECTION B ===== -->
    <tr><td colspan="3" class="sec-header">B- Mesures prises pour pr&eacute;parer l&apos;intervention</td></tr>
    <tr>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="m_vidange"><label for="m_vidange">Vidange de l&apos;&eacute;quipement et ses circuits</label></div>
        <div class="cb-row"><input type="checkbox" id="m_consignation"><label for="m_consignation">Consignation des &Eacute;nergies</label></div>
        <div class="cb-row"><input type="checkbox" id="m_eclairage"><label for="m_eclairage">Eclairage</label></div>
      </td>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="m_depressu"><label for="m_depressu">D&eacute;pressurisation</label></div>
        <div class="cb-row"><input type="checkbox" id="m_ventilation"><label for="m_ventilation">Ventilation</label></div>
        <div class="cb-row"><input type="checkbox" id="m_autre1"><label for="m_autre1">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:70px;border-bottom:1px solid #888;"></div>
      </td>
      <td style="padding:3px 5px;">
        <div class="cb-row"><input type="checkbox" id="m_nettoyage"><label for="m_nettoyage">Nettoyage</label></div>
        <div class="cb-row"><input type="checkbox" id="m_balisage"><label for="m_balisage">Balisage</label></div>
        <div class="cb-row"><input type="checkbox" id="m_autre2"><label for="m_autre2">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:70px;border-bottom:1px solid #888;"></div>
      </td>
    </tr>

    <!-- ===== SECTION C ===== -->
    <tr><td colspan="3" class="sec-header">C- Moyens d&apos;acc&egrave;s n&eacute;cessaires</td></tr>
    <tr>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="c_escabeau"><label for="c_escabeau">Escabeau</label></div>
        <div class="cb-row"><input type="checkbox" id="c_echafaudage"><label for="c_echafaudage">Echafaudage</label></div>
      </td>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="c_passerelle"><label for="c_passerelle">Passerelle</label></div>
        <div class="cb-row"><input type="checkbox" id="c_nacelle"><label for="c_nacelle">Nacelle, PEMP</label></div>
      </td>
      <td style="padding:3px 5px;">
        <div class="cb-row"><input type="checkbox" id="c_autre"><label for="c_autre">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:90px;border-bottom:1px solid #888;"></div>
      </td>
    </tr>

    <!-- ===== SECTION D ===== -->
    <tr><td colspan="3" class="sec-header">D- EPI sp&eacute;cifiques n&eacute;cessaires</td></tr>
    <tr>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="d_casque_soudure"><label for="d_casque_soudure">Casque soudure</label></div>
        <div class="cb-row"><input type="checkbox" id="d_masque_gaz"><label for="d_masque_gaz">Masque &agrave; gaz</label></div>
        <div class="cb-row"><input type="checkbox" id="d_masque_pano"><label for="d_masque_pano">Masque panoramique</label></div>
        <div class="cb-row"><input type="checkbox" id="d_masque_pouss"><label for="d_masque_pouss">Masque &agrave; poussi&egrave;res</label></div>
        <div class="cb-row"><input type="checkbox" id="d_lunettes"><label for="d_lunettes">Lunettes &eacute;tanches</label></div>
        <div class="cb-row"><input type="checkbox" id="d_harnais"><label for="d_harnais">Harnais de s&eacute;curit&eacute;</label></div>
      </td>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="d_ari"><label for="d_ari">ARI</label></div>
        <div class="cb-row"><input type="checkbox" id="d_stop_bruit"><label for="d_stop_bruit">Stop bruit</label></div>
        <div class="cb-row"><input type="checkbox" id="d_cagoule"><label for="d_cagoule">Cagoule</label></div>
        <div class="cb-row"><input type="checkbox" id="d_gants_antiacide"><label for="d_gants_antiacide">Gants antiacides</label></div>
        <div class="cb-row"><input type="checkbox" id="d_gants_man"><label for="d_gants_man">Gants de manutention</label></div>
        <div class="cb-row"><input type="checkbox" id="d_autre_d1"><label for="d_autre_d1">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:55px;border-bottom:1px solid #888;"></div>
      </td>
      <td style="padding:3px 5px;">
        <div class="cb-row"><input type="checkbox" id="d_tenue"><label for="d_tenue">Tenue antiacide</label></div>
        <div class="cb-row"><input type="checkbox" id="d_tablier"><label for="d_tablier">Tablier soudure</label></div>
        <div class="cb-row"><input type="checkbox" id="d_guetres"><label for="d_guetres">Gu&ecirc;tres</label></div>
        <div class="cb-row"><input type="checkbox" id="d_ecran"><label for="d_ecran">Ecran facial</label></div>
        <div class="cb-row"><input type="checkbox" id="d_bottes"><label for="d_bottes">Bottes de s&eacute;curit&eacute;</label></div>
        <div class="cb-row"><input type="checkbox" id="d_autre_d2"><label for="d_autre_d2">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:55px;border-bottom:1px solid #888;"></div>
      </td>
    </tr>

    <!-- ===== SECTION E ===== -->
    <tr><td colspan="3" class="sec-header">E- Permis n&eacute;cessaires</td></tr>
    <tr>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="e_confine"><label for="e_confine">Permis pour espace confin&eacute;</label></div>
        <div class="cb-row"><input type="checkbox" id="e_hauteur"><label for="e_hauteur">Permis pour travail en hauteur</label></div>
      </td>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="e_feu"><label for="e_feu">Permis de feu</label></div>
        <div class="cb-row"><input type="checkbox" id="e_fouille"><label for="e_fouille">Permis de fouille</label></div>
      </td>
      <td style="padding:3px 5px;">
        <div class="cb-row"><input type="checkbox" id="e_consignation"><label for="e_consignation">Plan de consignation</label></div>
        <div class="cb-row"><input type="checkbox" id="e_autre"><label for="e_autre">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:80px;border-bottom:1px solid #888;"></div>
      </td>
    </tr>

    <!-- ===== SECTION F ===== -->
    <tr><td colspan="3" class="sec-header">F- Mesures de s&eacute;curit&eacute; prises par l&apos;ex&eacute;cutant (R&eacute;f&eacute;rence du mode op&eacute;ratoire, ....)</td></tr>
    <tr>
      <td colspan="3" style="padding:3px 5px;">
        <textarea id="section_f" rows="2" style="width:100%;font-size:8pt;" placeholder="D&eacute;crire les mesures de s&eacute;curit&eacute; prises par l&apos;ex&eacute;cutant et les r&eacute;f&eacute;rences du mode op&eacute;ratoire..."></textarea>
      </td>
    </tr>

    <!-- ===== SECTION G ===== -->
    <tr><td colspan="3" class="sec-header">G- Validation de l&apos;autorisation de travail</td></tr>
    <tr>
      <td colspan="3" style="padding:4px 5px;">
        <table class="g-table">
          <tr>
            <td colspan="2" class="g-poste-header">1er poste</td>
            <td style="width:4%;border:none;background:#fff;"></td>
            <td colspan="2" class="g-poste-header">2&egrave;me poste</td>
            <td style="width:4%;border:none;background:#fff;"></td>
            <td colspan="2" class="g-poste-header">3&egrave;me poste</td>
          </tr>
          <tr>
            <td class="g-field-label">Nom CEEP</td><td><input type="text" id="g1_nom_ceep" placeholder="Nom &amp; Pr&eacute;nom"></td>
            <td style="border:none;background:#fff;"></td>
            <td class="g-field-label">Nom CEEP</td><td><input type="text" id="g2_nom_ceep" placeholder="Nom &amp; Pr&eacute;nom"></td>
            <td style="border:none;background:#fff;"></td>
            <td class="g-field-label">Nom CEEP</td><td><input type="text" id="g3_nom_ceep" placeholder="Nom &amp; Pr&eacute;nom"></td>
          </tr>
          <tr>
            <td class="g-field-label">Visa CEEP</td><td><input type="text" id="g1_visa_ceep" placeholder="Visa"></td>
            <td style="border:none;background:#fff;"></td>
            <td class="g-field-label">Visa CEEP</td><td><input type="text" id="g2_visa_ceep" placeholder="Visa"></td>
            <td style="border:none;background:#fff;"></td>
            <td class="g-field-label">Visa CEEP</td><td><input type="text" id="g3_visa_ceep" placeholder="Visa"></td>
          </tr>
          <tr>
            <td class="g-field-label">Nom CEEE</td><td><input type="text" id="g1_nom_ceee" placeholder="Nom &amp; Pr&eacute;nom"></td>
            <td style="border:none;background:#fff;"></td>
            <td class="g-field-label">Nom CEEE</td><td><input type="text" id="g2_nom_ceee" placeholder="Nom &amp; Pr&eacute;nom"></td>
            <td style="border:none;background:#fff;"></td>
            <td class="g-field-label">Nom CEEE</td><td><input type="text" id="g3_nom_ceee" placeholder="Nom &amp; Pr&eacute;nom"></td>
          </tr>
          <tr>
            <td class="g-field-label">Visa CEEE</td><td><input type="text" id="g1_visa_ceee" placeholder="Visa"></td>
            <td style="border:none;background:#fff;"></td>
            <td class="g-field-label">Visa CEEE</td><td><input type="text" id="g2_visa_ceee" placeholder="Visa"></td>
            <td style="border:none;background:#fff;"></td>
            <td class="g-field-label">Visa CEEE</td><td><input type="text" id="g3_visa_ceee" placeholder="Visa"></td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- ===== RECEPTION DES TRAVAUX ===== -->
    <tr><td colspan="3" class="sec-header-red">R&eacute;ception des travaux</td></tr>
    <tr>
      <td colspan="3" style="padding:3px 5px;">
        <strong>Date de r&eacute;ception :</strong>&nbsp;<input type="date" id="date_reception" style="width:120px;">
        &nbsp;&nbsp;&nbsp;&nbsp;
        <strong>Heure de r&eacute;ception :</strong>&nbsp;<input type="time" id="heure_reception" style="width:75px;">
      </td>
    </tr>
    <tr>
      <td colspan="3" style="padding:2px 5px;"><strong>Cocher en cas de remise en place :</strong></td>
    </tr>
    <tr>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="rec_boulonnerie"><label for="rec_boulonnerie">Boulonnerie</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_cache_bride"><label for="rec_cache_bride">Cache bride</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_support"><label for="rec_support">Support circuit</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_cache_tambour"><label for="rec_cache_tambour">Cache tambour</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_garde_corps"><label for="rec_garde_corps">Garde-corps</label></div>
      </td>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <div class="cb-row"><input type="checkbox" id="rec_cache_moteur"><label for="rec_cache_moteur">Cache moteur</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_caillebotis"><label for="rec_caillebotis">Caillebotis</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_couvercle"><label for="rec_couvercle">Couvercle</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_arret_urgence"><label for="rec_arret_urgence">Arr&ecirc;t d&apos;urgence</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_autre1"><label for="rec_autre1">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:65px;border-bottom:1px solid #888;"></div>
      </td>
      <td style="padding:3px 5px;">
        <div class="cb-row"><input type="checkbox" id="rec_cache_comp"><label for="rec_cache_comp">Cache compensateur</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_cache_acc"><label for="rec_cache_acc">Cache accouplement</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_trappe"><label for="rec_trappe">Trappe</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_capot_conv"><label for="rec_capot_conv">Capot convoyeur</label></div>
        <div class="cb-row"><input type="checkbox" id="rec_autre2"><label for="rec_autre2">Autres &agrave; pr&eacute;ciser :</label>&nbsp;<input type="text" style="width:65px;border-bottom:1px solid #888;"></div>
      </td>
    </tr>

    <!-- Essai concluant -->
    <tr>
      <td colspan="3" style="padding:3px 5px;">
        <table style="width:100%;border-collapse:collapse;">
          <tr>
            <td style="width:22%;border:1px solid #000;padding:4px;vertical-align:middle;">
              <strong>Essai concluant</strong>
              <div class="radio-row">
                <label><input type="radio" name="essai" value="oui" id="essai_oui"> Oui</label>
                <label><input type="radio" name="essai" value="non" id="essai_non"> Non</label>
              </div>
            </td>
            <td style="border:1px solid #000;padding:4px;font-size:7.5pt;font-style:italic;">
              ! Pour les modifications assujetties au Standard MOC, les exigences de ce standard, notamment celles du PSSR doivent &ecirc;tre remplies avant d&apos;entamer les essais.<br>
              La r&eacute;ception ne se fait que si les conditions de base sont assur&eacute;es, que les mesures s&eacute;curitaires sont en place et que les essais sont concluants.
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- ===== VALIDATION RECEPTION ===== -->
    <tr><td colspan="3" class="sec-header-red">Validation de la r&eacute;ception</td></tr>
    <tr>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <strong>CEEP (Nom &amp; Visa)</strong><br>
        <input type="text" id="val_ceep" placeholder="Nom &amp; Visa CEEP">
      </td>
      <td style="padding:3px 5px;border-right:1px solid #000;">
        <strong>CEEE (Nom &amp; Visa)</strong><br>
        <input type="text" id="val_ceee" placeholder="Nom &amp; Visa CEEE">
      </td>
      <td style="padding:3px 5px;">
        <strong>Sous-traitant (Nom &amp; Visa)</strong><br>
        <input type="text" id="val_st" placeholder="Nom &amp; Visa Sous-traitant">
      </td>
    </tr>
    <tr>
      <td colspan="3" style="padding:2px 5px;font-size:7.5pt;font-style:italic;">
        <em>N.B : Les 3 souches doivent &ecirc;tre d&ucirc;ment instruites et vis&eacute;es</em>
      </td>
    </tr>

  </table>
</div>

<script>
// ==================== PDF EXPORT ====================
async function exportPDF() {
  if (!window.jspdf || !window.html2canvas) { alert('Librairies PDF non chargees'); return; }
  const btns = document.querySelectorAll('.btn-export');
  btns.forEach(b => b.disabled = true);
  try {
    const canvas = await html2canvas(document.getElementById('form-container'), {
      scale: 2, useCORS: true, backgroundColor: '#ffffff', logging: false
    });
    const { jsPDF } = window.jspdf;
    const pdf = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
    const pw = pdf.internal.pageSize.getWidth();
    const ph = pdf.internal.pageSize.getHeight();
    const iw = pw - 10;
    const pageH_px = Math.floor((ph - 10) / iw * canvas.width);
    let y = 0, page = 0;
    while (y < canvas.height) {
      if (page > 0) pdf.addPage();
      const sliceH = Math.min(pageH_px, canvas.height - y);
      const sl = document.createElement('canvas');
      sl.width = canvas.width; sl.height = sliceH;
      sl.getContext('2d').drawImage(canvas, 0, y, canvas.width, sliceH, 0, 0, canvas.width, sliceH);
      const sliceIH = sliceH / canvas.width * iw;
      pdf.addImage(sl.toDataURL('image/png'), 'PNG', 5, 5, iw, sliceIH);
      y += sliceH; page++;
    }
    const num = (document.getElementById('at_numero').value || 'AT').replace(/[^a-zA-Z0-9-]/g, '_');
    pdf.save('AT_OCP_' + num + '_F-HSE-SEC-31-04.pdf');
  } catch(e) { alert('Erreur PDF : ' + e.message); }
  finally { btns.forEach(b => b.disabled = false); }
}

// ==================== WORD EXPORT ====================
async function exportWord() {
  if (!window.docx) { alert('Librairie docx non chargee'); return; }
  const { Document, Packer, Paragraph, TextRun, AlignmentType } = window.docx;
  const v = id => document.getElementById(id)?.value || '';
  const c = id => document.getElementById(id)?.checked ? '[X]' : '[ ]';
  const bold = txt => new TextRun({ text: txt, bold: true, size: 16 });
  const normal = txt => new TextRun({ text: txt, size: 16 });
  const cbLine = (id, lbl) => new Paragraph({ children: [new TextRun({ text: c(id) + ' ' + lbl, size: 15 })] });
  const fieldPara = (lbl, val) => new Paragraph({ children: [bold(lbl + ' : '), normal(val || '.......................')] });
  const sectionHdr = txt => new Paragraph({ children: [new TextRun({ text: txt, bold: true, size: 18, underline: {} })] });
  const sp = () => new Paragraph({ text: '' });

  const risques = [['r_hauteur','Travail en hauteur'],['r_reseaux','Proximite aux reseaux enterres'],['r_inflammable','Produits inflammables'],['r_man_manuel','Manutention manuelle'],['r_man_meca','Manutention mecanique'],['r_outillage','Outillage'],['r_bruit','Bruit > 80 dB'],['r_circ_pers','Circulation personnes'],['r_chimique','Produits chimiques'],['r_eclairage','Eclairage insuffisant'],['r_intemperies','Intemperies'],['r_poussiere','Ambiance poussiereuse'],['r_vehicule','Circulation vehicules'],['r_coactivite','Co-activite'],['r_machines','Machines tournantes'],['r_chauds','Produits chauds'],['r_pression','Equipement sous pression'],['r_electricite','Electricite'],['r_confine','Espaces confines'],['r_atex','Zone ATEX'],['r_noyade','Noyade']];
  const mesures = [['m_vidange',"Vidange de l'equipement"],['m_consignation','Consignation des Energies'],['m_eclairage','Eclairage'],['m_depressu','Depressurisation'],['m_ventilation','Ventilation'],['m_nettoyage','Nettoyage'],['m_balisage','Balisage']];
  const acces = [['c_escabeau','Escabeau'],['c_echafaudage','Echafaudage'],['c_passerelle','Passerelle'],['c_nacelle','Nacelle, PEMP']];
  const epi = [['d_casque_soudure','Casque soudure'],['d_masque_gaz','Masque a gaz'],['d_masque_pano','Masque panoramique'],['d_masque_pouss','Masque a poussieres'],['d_lunettes','Lunettes etanches'],['d_harnais','Harnais de securite'],['d_ari','ARI'],['d_stop_bruit','Stop bruit'],['d_cagoule','Cagoule'],['d_gants_antiacide','Gants antiacides'],['d_gants_man','Gants de manutention'],['d_tenue','Tenue antiacide'],['d_tablier','Tablier soudure'],['d_guetres','Guetres'],['d_ecran','Ecran facial'],['d_bottes','Bottes de securite']];
  const permis = [['e_confine','Permis espace confine'],['e_hauteur','Permis travail en hauteur'],['e_feu','Permis de feu'],['e_fouille','Permis de fouille'],['e_consignation','Plan de consignation']];
  const reception_items = [['rec_boulonnerie','Boulonnerie'],['rec_cache_bride','Cache bride'],['rec_support','Support circuit'],['rec_cache_tambour','Cache tambour'],['rec_garde_corps','Garde-corps'],['rec_cache_moteur','Cache moteur'],['rec_caillebotis','Caillebotis'],['rec_couvercle','Couvercle'],['rec_arret_urgence',"Arret d'urgence"],['rec_cache_comp','Cache compensateur'],['rec_cache_acc','Cache accouplement'],['rec_trappe','Trappe'],['rec_capot_conv','Capot convoyeur']];

  const doc = new Document({ sections: [{ children: [
    new Paragraph({ children: [new TextRun({ text: 'F-HSE-SEC-31-04  |  Edition 1.0  |  01/07/2016', size: 14, color: '888888' })], alignment: AlignmentType.RIGHT }),
    new Paragraph({ children: [new TextRun({ text: 'FORMULAIRE - Autorisation de travail', bold: true, size: 28 })], alignment: AlignmentType.CENTER }),
    sp(),
    fieldPara('AT n\u00b0', v('at_numero')),
    fieldPara('Site', v('site') + '   Entite : ' + v('entite')),
    fieldPara('DI n\u00b0', v('di') + '   OT n\u00b0 : ' + v('ot') + '   BT n\u00b0 : ' + v('bt')),
    fieldPara("Lieu d'intervention", v('lieu')),
    fieldPara('Services intervenants', v('services')),
    fieldPara('Entreprises intervenantes', v('entreprises')),
    fieldPara("Description", v('description')),
    fieldPara('Date', v('date_intervention') + '  Debut : ' + v('heure_debut') + '  Fin : ' + v('heure_fin')),
    sp(),
    sectionHdr('A- RISQUES EVALUES'),
    ...risques.map(([id,lbl]) => cbLine(id, lbl)),
    sp(),
    sectionHdr('B- MESURES PREPARATOIRES'),
    ...mesures.map(([id,lbl]) => cbLine(id, lbl)),
    sp(),
    sectionHdr('C- MOYENS D\'ACCES'),
    ...acces.map(([id,lbl]) => cbLine(id, lbl)),
    sp(),
    sectionHdr('D- EPI SPECIFIQUES'),
    ...epi.map(([id,lbl]) => cbLine(id, lbl)),
    sp(),
    sectionHdr('E- PERMIS NECESSAIRES'),
    ...permis.map(([id,lbl]) => cbLine(id, lbl)),
    sp(),
    sectionHdr('F- MESURES DE SECURITE EXECUTANT'),
    new Paragraph({ children: [normal(v('section_f') || '...')] }),
    sp(),
    sectionHdr('G- VALIDATION DE L\'AUTORISATION'),
    new Paragraph({ children: [bold('1er poste -- '), normal('CEEP: ' + v('g1_nom_ceep') + ' / Visa: ' + v('g1_visa_ceep') + '   CEEE: ' + v('g1_nom_ceee') + ' / Visa: ' + v('g1_visa_ceee'))] }),
    new Paragraph({ children: [bold('2eme poste -- '), normal('CEEP: ' + v('g2_nom_ceep') + ' / Visa: ' + v('g2_visa_ceep') + '   CEEE: ' + v('g2_nom_ceee') + ' / Visa: ' + v('g2_visa_ceee'))] }),
    new Paragraph({ children: [bold('3eme poste -- '), normal('CEEP: ' + v('g3_nom_ceep') + ' / Visa: ' + v('g3_visa_ceep') + '   CEEE: ' + v('g3_nom_ceee') + ' / Visa: ' + v('g3_visa_ceee'))] }),
    sp(),
    sectionHdr('RECEPTION DES TRAVAUX'),
    fieldPara('Date reception', v('date_reception') + '  Heure : ' + v('heure_reception')),
    ...reception_items.map(([id,lbl]) => cbLine(id, lbl)),
    new Paragraph({ children: [bold('Essai concluant : '), normal(document.querySelector('input[name=\"essai\"]:checked')?.value || 'non coche')] }),
    sp(),
    sectionHdr('VALIDATION DE LA RECEPTION'),
    fieldPara('CEEP', v('val_ceep')),
    fieldPara('CEEE', v('val_ceee')),
    fieldPara('Sous-traitant', v('val_st')),
    new Paragraph({ children: [new TextRun({ text: 'N.B : Les 3 souches doivent etre dument instruites et visees', italics: true, size: 14 })] }),
  ]}]});

  const num = (v('at_numero') || 'AT').replace(/[^a-zA-Z0-9-]/g,'_');
  const blob = await Packer.toBlob(doc);
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = 'AT_OCP_' + num + '_F-HSE-SEC-31-04.docx';
  document.body.appendChild(a); a.click(); document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
</script>
</body>
</html>"""

with open(OUT, "w", encoding="utf-8") as f:
    f.write(HTML)

import os
print(f"Done! Size: {os.path.getsize(OUT)} bytes -> {OUT}")
