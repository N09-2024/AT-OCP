/// OCP design tokens extraits du frontend React.
/// Source : frontend/src/theme/tokens.ts
///
/// NE PAS modifier ces valeurs sans verifier la coherence avec le web.
library;

import 'dart:ui';

abstract final class OcpColors {
  // --- Primaires vert foret ---
  static const Color deep = Color(0xFF0E2A21);
  static const Color forest = Color(0xFF1F4D3E);
  static const Color forestDark = Color(0xFF163C30);
  static const Color forestSoft = Color(0xFFDCEBE3);

  // --- Moss ---
  static const Color moss = Color(0xFF3C7A5C);
  static const Color mossDark = Color(0xFF2E624A);

  // --- Mint ---
  static const Color mint = Color(0xFF7FC8A9);
  static const Color mintSoft = Color(0xFFE2F0E8);

  // --- Sage / Fonds ---
  static const Color sage = Color(0xFFEDF2EE);
  static const Color surfaceSoft = Color(0xFFF7FAF8);

  // --- Texte ---
  static const Color ink = Color(0xFF16241E);
  static const Color slate = Color(0xFF5C6E67);

  // --- Bordures ---
  static const Color border = Color(0xFFD6E3DC);
  static const Color borderSoft = Color(0xFFE3ECE7);

  // --- Neutres ---
  static const Color white = Color(0xFFFFFFFF);
  static const Color black = Color(0xFF000000);

  // --- Semantiques ---
  static const Color warning = Color(0xFFA87532);
  static const Color warningSoft = Color(0xFFF6EEDC);
  static const Color error = Color(0xFF9A3D2F);
  static const Color errorSoft = Color(0xFFFBEAE3);
  static const Color success = Color(0xFF3C7A5C);
  static const Color successDark = Color(0xFF163C30);
  static const Color successLight = Color(0xFFE2F0E8);

  // NB : les couleurs de statut AT vivent dans core/widgets/statut_chip.dart -
  // réplique exacte de STATUT_CONFIG du frontend React (AtTable.tsx).
}
