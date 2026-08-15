import React, { useRef } from "react";
import {
  Box, Card, CardContent, Typography, Button, Chip,
  LinearProgress, Tooltip, Link, Stack, Divider
} from "@mui/material";
import {
  CheckCircle, Cancel, HourglassTop, CloudUpload,
  PhotoCamera, Refresh, OpenInNew
} from "@mui/icons-material";
import type { PermisDocumentResponse } from "../../services/permisDocumentApi";

const TYPE_ICONS: Record<string, string> = {
  PERMIS_FEU: "🔥",
  ESPACE_CONFINE: "🚧",
  TRAVAIL_HAUTEUR: "🪜",
  FOUILLE: "⛏️",
  CONSIGNATION_ENERGIES: "⚡",
  PLAN_CONSIGNATION: "📋",
};

const TYPE_LABELS: Record<string, string> = {
  PERMIS_FEU: "Permis de feu",
  ESPACE_CONFINE: "Espace confiné",
  TRAVAIL_HAUTEUR: "Travail en hauteur",
  FOUILLE: "Permis de fouille",
  CONSIGNATION_ENERGIES: "Consignation des énergies",
  PLAN_CONSIGNATION: "Plan de consignation",
};

interface Props {
  doc: PermisDocumentResponse;
  onUpload: (typePermis: string, file: File) => Promise<void>;
  onRelancer: (id: string) => Promise<void>;
  uploading?: boolean;
}

const PermisUploadCard: React.FC<Props> = ({ doc, onUpload, onRelancer, uploading }) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>, isCamera: boolean) => {
    const file = e.target.files?.[0];
    if (!file) return;
    await onUpload(doc.typePermisAttendu, file);
    // Reset input
    if (isCamera && cameraInputRef.current) cameraInputRef.current.value = "";
    if (!isCamera && fileInputRef.current) fileInputRef.current.value = "";
  };

  const icon = TYPE_ICONS[doc.typePermisAttendu] ?? "📄";
  const label = TYPE_LABELS[doc.typePermisAttendu] ?? doc.typePermisAttendu;

  return (
    <Card
      variant="outlined"
      sx={{
        mb: 1.5,
        borderRadius: 2,
        borderColor:
          doc.statut === "VALIDE"
            ? "success.main"
            : doc.statut === "REJETE"
            ? "error.main"
            : doc.statut === "EN_ATTENTE_ANALYSE"
            ? "warning.main"
            : "divider",
        background:
          doc.statut === "VALIDE"
            ? "rgba(76,175,80,0.04)"
            : doc.statut === "REJETE"
            ? "rgba(244,67,54,0.04)"
            : undefined,
      }}
    >
      <CardContent sx={{ p: 2, "&:last-child": { pb: 2 } }}>
        {/* En-tête */}
        <Box sx={{ display: "flex", flexDirection: "row", alignItems: "center", gap: 1, mb: 1 }}>
          <Typography sx={{ fontSize: 20 }}>{icon}</Typography>
          <Typography sx={{ fontWeight: 600, fontSize: 14, flex: 1 }}>{label}</Typography>
          {doc.statut === "VALIDE" && (
            <Chip icon={<CheckCircle />} label="Validé IA" color="success" size="small" />
          )}
          {doc.statut === "REJETE" && (
            <Chip icon={<Cancel />} label="Rejeté" color="error" size="small" />
          )}
          {doc.statut === "EN_ATTENTE_ANALYSE" && (
            <Chip icon={<HourglassTop />} label="Analyse en cours…" color="warning" size="small" />
          )}
          {doc.statut === "EN_ATTENTE_UPLOAD" && (
            <Chip label="En attente" color="default" size="small" />
          )}
        </Box>

        {/* Contenu selon statut */}
        {(doc.statut === "EN_ATTENTE_UPLOAD" || doc.statut === "REJETE") && (
          <Box>
            {doc.statut === "REJETE" && doc.motifRejet && (
              <Typography sx={{ color: "error.main", fontSize: 12, mb: 1, fontStyle: "italic" }}>
                ❌ {doc.motifRejet}
              </Typography>
            )}
            <Box sx={{ display: "flex", flexDirection: "row", gap: 1, flexWrap: "wrap" }}>
              {/* Bouton Photographier */}
              <Button
                size="small"
                variant="outlined"
                startIcon={<PhotoCamera />}
                onClick={() => cameraInputRef.current?.click()}
                disabled={uploading}
                sx={{ fontSize: 12 }}
              >
                Photographier
              </Button>
              <input
                ref={cameraInputRef}
                type="file"
                accept="image/*"
                capture="environment"
                hidden
                onChange={(e) => handleFileChange(e, true)}
              />
              {/* Bouton Importer */}
              <Button
                size="small"
                variant="outlined"
                color="secondary"
                startIcon={<CloudUpload />}
                onClick={() => fileInputRef.current?.click()}
                disabled={uploading}
                sx={{ fontSize: 12 }}
              >
                {doc.statut === "REJETE" ? "Re-importer" : "Importer"}
              </Button>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,application/pdf"
                hidden
                onChange={(e) => handleFileChange(e, false)}
              />
            </Box>
            <Typography sx={{ fontSize: 10, color: "text.secondary", mt: 0.5 }}>
              Formats : JPG, PNG, PDF — max 10 Mo
            </Typography>
          </Box>
        )}

        {doc.statut === "EN_ATTENTE_ANALYSE" && (
          <Box sx={{ mt: 1 }}>
            <Typography sx={{ fontSize: 12, color: "warning.dark", mb: 0.5 }}>
              ⏳ Analyse IA en cours…
            </Typography>
            <LinearProgress color="warning" sx={{ borderRadius: 1 }} />
          </Box>
        )}

        {doc.statut === "VALIDE" && (
          <Box sx={{ mt: 1 }}>
            <Divider sx={{ mb: 1 }} />
            <Stack spacing={0.3}>
              {doc.typeExtrait && (
                <Typography sx={{ fontSize: 12 }}>
                  <strong>Type détecté :</strong> {doc.typeExtrait}
                </Typography>
              )}
              {(doc.dateDebutExtrait || doc.dateFinExtrait) && (
                <Typography sx={{ fontSize: 12 }}>
                  <strong>Validité :</strong> {doc.dateDebutExtrait ?? "?"} → {doc.dateFinExtrait ?? "?"}
                </Typography>
              )}
              {doc.responsablesExtraits && (
                <Typography sx={{ fontSize: 12 }}>
                  <strong>Signataires :</strong> {doc.responsablesExtraits}
                </Typography>
              )}
              {doc.scoreConfiance !== null && doc.scoreConfiance !== undefined && (
                <Typography sx={{ fontSize: 12 }}>
                  <strong>Confiance IA :</strong> {Math.round(doc.scoreConfiance * 100)}%
                </Typography>
              )}
              {doc.commentaireIA && (
                <Typography sx={{ fontSize: 11, color: "text.secondary", fontStyle: "italic" }}>
                  {doc.commentaireIA}
                </Typography>
              )}
            </Stack>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default PermisUploadCard;
