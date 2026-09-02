/// Theme Material 3 de l'application mobile OCP AT.
/// Derive de la palette OcpColors (tokens du frontend React).
library;

import 'package:flutter/material.dart';
import 'app_colors.dart';

abstract final class AppTheme {
  static const String fontBody = 'Inter';
  static const String fontDisplay = 'SpaceGrotesk';

  static ThemeData light() {
    final colorScheme = ColorScheme.light(
      primary: OcpColors.forest,
      onPrimary: OcpColors.white,
      primaryContainer: OcpColors.forestSoft,
      onPrimaryContainer: OcpColors.forestDark,
      secondary: OcpColors.moss,
      onSecondary: OcpColors.white,
      secondaryContainer: OcpColors.mintSoft,
      onSecondaryContainer: OcpColors.mossDark,
      tertiary: OcpColors.mint,
      onTertiary: OcpColors.ink,
      error: OcpColors.error,
      onError: OcpColors.white,
      errorContainer: OcpColors.errorSoft,
      onErrorContainer: OcpColors.error,
      surface: OcpColors.white,
      onSurface: OcpColors.ink,
      surfaceContainerHighest: OcpColors.sage,
      onSurfaceVariant: OcpColors.slate,
      outline: OcpColors.border,
      outlineVariant: OcpColors.borderSoft,
    );

    final base = ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      scaffoldBackgroundColor: OcpColors.surfaceSoft,
      fontFamily: fontBody,
      splashFactory: InkSparkle.splashFactory,
    );

    return base.copyWith(
      textTheme: base.textTheme.apply(
        bodyColor: OcpColors.ink,
        displayColor: OcpColors.ink,
        fontFamily: fontBody,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: OcpColors.forest,
        foregroundColor: OcpColors.white,
        elevation: 0,
        scrolledUnderElevation: 1,
        centerTitle: true,
        titleTextStyle: TextStyle(
          fontFamily: fontDisplay,
          fontSize: 18,
          fontWeight: FontWeight.w700,
          color: OcpColors.white,
        ),
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: OcpColors.white,
        surfaceTintColor: OcpColors.white,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
          side: const BorderSide(color: OcpColors.border),
        ),
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: OcpColors.forest,
          foregroundColor: OcpColors.white,
          minimumSize: const Size(64, 48),
          elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: OcpColors.forest,
          side: const BorderSide(color: OcpColors.forest),
          minimumSize: const Size(64, 48),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: OcpColors.forest,
          foregroundColor: OcpColors.white,
          minimumSize: const Size(64, 48),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: OcpColors.moss,
          textStyle: const TextStyle(fontWeight: FontWeight.w600),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: OcpColors.white,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: OcpColors.border),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: OcpColors.border),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: OcpColors.moss, width: 1.5),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: OcpColors.error),
        ),
        labelStyle: const TextStyle(color: OcpColors.slate),
        hintStyle: const TextStyle(color: OcpColors.slate),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: OcpColors.sage,
        selectedColor: OcpColors.forestSoft,
        labelStyle: const TextStyle(fontSize: 13, color: OcpColors.ink),
        side: const BorderSide(color: OcpColors.border),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: OcpColors.white,
        selectedItemColor: OcpColors.forest,
        unselectedItemColor: OcpColors.slate,
        type: BottomNavigationBarType.fixed,
        elevation: 2,
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: OcpColors.white,
        indicatorColor: OcpColors.forestSoft,
        labelTextStyle: WidgetStateProperty.resolveWith((states) {
          final selected = states.contains(WidgetState.selected);
          return TextStyle(
            fontSize: 12,
            fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
            color: selected ? OcpColors.forest : OcpColors.slate,
          );
        }),
      ),
      snackBarTheme: SnackBarThemeData(
        backgroundColor: OcpColors.deep,
        contentTextStyle: const TextStyle(color: OcpColors.white),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      dividerTheme: const DividerThemeData(color: OcpColors.borderSoft, thickness: 1),
      progressIndicatorTheme: const ProgressIndicatorThemeData(color: OcpColors.forest),
    );
  }
}
