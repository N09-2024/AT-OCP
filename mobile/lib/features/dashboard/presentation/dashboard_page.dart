/// Dashboards par rôle applicatif — INTERFACES_PAR_ROLE.md (Standard S-HSE-SEC-31 v1.0).
///
/// 5 espaces distincts, résolus par priorité ADMIN > HC > HM > CE > RESPONSABLE_EXTERIEUR :
/// - CE   : cartes P (rédiger, réceptionner) + cartes E (viser, démarrer, en cours, reconduire).
/// - HM   : visites à garantir, démarrages à cautionner, consultation périmètre.
///          Règle fail-closed : position E (HMEE) = lecture seule, aucune écriture.
/// - HC   : classifier, garantir/valider (E), archivage (+ modules web habilitations/registre).
/// - ADMIN: KPIs globaux + accès modules + administration (web).
/// - RESPONSABLE_EXTERIEUR : consultation seule — création/rédaction/validation interdites.
///
/// Rôles métier multiples (ex. CE + HC) → sélecteur de contexte (§1.3).
/// Design system : tokens OcpColors, SpaceGrotesk, coins 8–12, textes clippés.
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/errors/failures.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/role_applicatif.dart';
import '../../auth/presentation/auth_controller.dart';
import 'dashboard_providers.dart';

/// Contexte applicatif choisi manuellement (sélecteur multi-rôles §1.3).
final dashboardContextProvider =
    StateProvider.autoDispose<RoleApplicatif?>((ref) => null);

class _RoleUi {
  final String title;
  final String subtitle;
  final Color color;
  final IconData icon;
  const _RoleUi({
    required this.title,
    required this.subtitle,
    required this.color,
    required this.icon,
  });

  static const Map<RoleApplicatif, _RoleUi> configs = {
    RoleApplicatif.admin: _RoleUi(
      title: 'Administration HSE',
      subtitle: 'KPIs globaux · accès complet aux modules · audit',
      color: Color(0xFF6B2D5C),
      icon: Icons.admin_panel_settings_rounded,
    ),
    RoleApplicatif.hc: _RoleUi(
      title: 'Espace Hors Cadre',
      subtitle: 'Classifier · garantir sécurité · archivage (HCEP / HCEE)',
      color: Color(0xFF1E3A8A),
      icon: Icons.shield_rounded,
    ),
    RoleApplicatif.hm: _RoleUi(
      title: 'Espace Haute Maîtrise',
      subtitle: 'Garantie HMEP · consultation périmètre · HMEE lecture seule',
      color: Color(0xFF854D0E),
      icon: Icons.verified_user_rounded,
    ),
    RoleApplicatif.ce: _RoleUi(
      title: 'Espace Chef d\'Équipe',
      subtitle: 'Position P : rédiger · réceptionner · Position E : viser · démarrer',
      color: OcpColors.forest,
      icon: Icons.engineering_rounded,
    ),
    RoleApplicatif.externe: _RoleUi(
      title: 'Espace Entreprise Extérieure',
      subtitle: 'Consultation seule : BT, permis, AT liées',
      color: Color(0xFF0D9488),
      icon: Icons.business_center_rounded,
    ),
  };
}

class DashboardPage extends ConsumerWidget {
  const DashboardPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final session = ref.watch(sessionProvider);
    final unreadCount = ref.watch(unreadCountProvider);

    final roles = (session?.roles ?? const <String>[])
        .map((r) => r.toUpperCase())
        .toSet();
    final applicatifs = roleApplicatifs(roles);
    final selection = ref.watch(dashboardContextProvider);
    final actif = (selection != null && applicatifs.contains(selection))
        ? selection
        : roleApplicatifPrincipal(roles);
    final ui = _RoleUi.configs[actif]!;

    return Scaffold(
      backgroundColor: OcpColors.sage,
      appBar: AppBar(
        backgroundColor: OcpColors.forest,
        foregroundColor: OcpColors.white,
        elevation: 0,
        titleTextStyle: const TextStyle(
          fontFamily: 'SpaceGrotesk',
          fontWeight: FontWeight.w700,
          fontSize: 16,
          color: OcpColors.white,
        ),
        title: Text(ui.title, overflow: TextOverflow.ellipsis, maxLines: 1),
        actions: [
          IconButton(
            tooltip: 'Assistant IA',
            onPressed: () => context.push('/assistant'),
            icon: const Icon(Icons.psychology_alt_rounded, color: OcpColors.mint),
          ),
          IconButton(
            onPressed: () => context.push('/notifications'),
            icon: Badge(
              isLabelVisible: unreadCount > 0,
              label: Text('$unreadCount', style: const TextStyle(fontSize: 10)),
              child: const Icon(Icons.notifications_outlined, color: OcpColors.white),
            ),
          ),
          IconButton(
            onPressed: () => context.push('/profile'),
            icon: const Icon(Icons.person_outline_rounded, color: OcpColors.white),
          ),
        ],
      ),
      body: RefreshIndicator(
        color: OcpColors.forest,
        onRefresh: () async {
          ref.invalidate(dashboardProvider);
          ref.read(unreadCountProvider.notifier).refresh();
          await ref.read(dashboardProvider.future);
        },
        child: ListView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.only(bottom: 32),
          children: [
            _RoleHeader(
              nom: session?.utilisateur.nomComplet ?? 'Utilisateur OCP',
              subtitle: ui.subtitle,
              color: ui.color,
              icon: ui.icon,
            ),

            // ── Sélecteur de contexte (rôles métier multiples, §1.3) ──────
            if (applicatifs.length > 1) ...[
              const SizedBox(height: 12),
              _ContextSelector(
                disponibles: applicatifs,
                actif: actif,
                onSelect: (role) =>
                    ref.read(dashboardContextProvider.notifier).state = role,
              ),
            ],

            switch (actif) {
              RoleApplicatif.ce => const _CeDashboard(),
              RoleApplicatif.hm => const _HmDashboard(),
              RoleApplicatif.hc => const _HcDashboard(),
              RoleApplicatif.admin => const _AdminDashboard(),
              RoleApplicatif.externe => const _ExterneDashboard(),
            },

            // ── Section transversale ────────────────────────────────────────
            const _SectionLabel(label: 'Accès rapides'),
            _ActionTile(
              icon: Icons.format_list_bulleted_rounded,
              title: 'Toutes les autorisations',
              subtitle: actif == RoleApplicatif.externe
                  ? 'Consultation des AT liées à vos BT'
                  : 'Rechercher et filtrer l\'ensemble des dossiers',
              accentColor: OcpColors.forest,
              onTap: () => context.push('/at'),
            ),
            _ActionTile(
              icon: Icons.psychology_alt_rounded,
              title: 'Assistant IA HSE',
              subtitle: 'Analyse des risques, aide à la rédaction, réglementation',
              accentColor: const Color(0xFF0D9488),
              onTap: () => context.push('/assistant'),
            ),
          ],
        ),
      ),
    );
  }
}

// ─── Sélecteur de contexte multi-rôles ───────────────────────────────────────

class _ContextSelector extends StatelessWidget {
  final Set<RoleApplicatif> disponibles;
  final RoleApplicatif actif;
  final ValueChanged<RoleApplicatif> onSelect;

  const _ContextSelector({
    required this.disponibles,
    required this.actif,
    required this.onSelect,
  });

  static const Map<RoleApplicatif, String> _labels = {
    RoleApplicatif.admin: 'ADMIN',
    RoleApplicatif.hc: 'HC',
    RoleApplicatif.hm: 'HM',
    RoleApplicatif.ce: 'CE',
    RoleApplicatif.externe: 'EXTERNE',
  };

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'VOTRE CONTEXTE',
            style: TextStyle(
              fontFamily: 'SpaceGrotesk',
              fontWeight: FontWeight.w800,
              fontSize: 11,
              letterSpacing: 1.2,
              color: OcpColors.slate,
            ),
          ),
          const SizedBox(height: 6),
          Wrap(
            spacing: 8,
            children: [
              for (final role in disponibles)
                ChoiceChip(
                  label: Text(_labels[role] ?? '?'),
                  selected: role == actif,
                  onSelected: (_) => onSelect(role),
                  labelStyle: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w700,
                    color: role == actif ? OcpColors.white : OcpColors.ink,
                  ),
                  selectedColor: OcpColors.forest,
                  backgroundColor: OcpColors.white,
                  side: const BorderSide(color: OcpColors.border),
                ),
            ],
          ),
        ],
      ),
    );
  }
}

// ─── Dashboard CE (§2) ───────────────────────────────────────────────────────

class _CeDashboard extends StatelessWidget {
  const _CeDashboard();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const _SectionLabel(label: 'Position Propriétaire (P)'),
        _ActionTile(
          icon: Icons.edit_note_rounded,
          title: 'AT propriétaire à rédiger',
          subtitle: 'DI / OT / BT · brouillons et visites en préparation',
          accentColor: OcpColors.warning,
          onTap: () => context.push('/at?filter=mine&statut=BROUILLON'),
        ),
        _ActionTile(
          icon: Icons.verified_rounded,
          title: 'AT propriétaire à réceptionner',
          subtitle: 'Travaux déclarés finis — réception conjointe CEEP + CEEE',
          accentColor: const Color(0xFF4F46E5),
          onTap: () =>
              context.push('/at?filter=mine&statut=FIN_TRAVAUX_DECLAREE'),
        ),
        const _SectionLabel(label: 'Position Exécutant (E)'),
        _ActionTile(
          icon: Icons.draw_rounded,
          title: 'AT exécutant à viser',
          subtitle: 'Apposer votre visa CEEE (SOUMISE / AT_REDIGEE)',
          accentColor: const Color(0xFF2563EB),
          onTap: () => context.push('/at?filter=aValider'),
        ),
        _ActionTile(
          icon: Icons.play_arrow_rounded,
          title: 'AT exécutant à démarrer',
          subtitle: 'Visa HMEE apposé — readiness check puis démarrage',
          accentColor: const Color(0xFF059669),
          onTap: () => context.push('/at?filter=mine&statut=AT_VALIDEE'),
        ),
        _ActionTile(
          icon: Icons.engineering_rounded,
          title: 'Interventions en cours (E)',
          subtitle: 'Déclarer la fin des travaux · signaler un incident',
          accentColor: OcpColors.forest,
          onTap: () =>
              context.push('/at?filter=mine&statut=INTERVENTION_EN_COURS'),
        ),
        _ActionTile(
          icon: Icons.update_rounded,
          title: 'AT à reconduire',
          subtitle: 'Prolongation de poste (P ou E) — dépassement planifié',
          accentColor: const Color(0xFF854D0E),
          onTap: () => context.push('/at?filter=mine&statut=AT_RECONDUITE'),
        ),
        const _SectionLabel(label: 'Actions globales'),
        _ActionTile(
          icon: Icons.add_circle_outline_rounded,
          title: 'Nouvelle demande d\'intervention',
          subtitle: 'Créer DI / OT / BT ou AT directe (position P)',
          accentColor: OcpColors.forest,
          onTap: () => context.push('/at/nouvelle'),
        ),
        _ActionTile(
          icon: Icons.format_list_bulleted_rounded,
          title: 'Voir toutes mes AT (P + E)',
          subtitle: 'Mes dossiers propriétaires et exécutés',
          accentColor: OcpColors.moss,
          onTap: () => context.push('/at?filter=mine'),
        ),
      ],
    );
  }
}

// ─── Dashboard HM (§3) ───────────────────────────────────────────────────────

class _HmDashboard extends StatelessWidget {
  const _HmDashboard();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const _SectionLabel(label: 'Garantie Haute Maîtrise (P)'),
        _ActionTile(
          icon: Icons.fact_check_rounded,
          title: 'Visites à garantir',
          subtitle: 'Visites réalisées en attente de garantie HMEP',
          accentColor: const Color(0xFF2563EB),
          onTap: () => context.push('/at?filter=mine&statut=VISITE_REALISEE'),
        ),
        _ActionTile(
          icon: Icons.gavel_rounded,
          title: 'Démarrages à cautionner',
          subtitle: 'AT rédigées en attente de garantie avant travaux',
          accentColor: const Color(0xFF854D0E),
          onTap: () => context.push('/at?filter=mine&statut=AT_REDIGEE'),
        ),
        const _SectionLabel(label: 'Consultation'),
        _ActionTile(
          icon: Icons.travel_explore_rounded,
          title: 'Consultation du périmètre',
          subtitle: 'Toutes les AT de ma zone — lecture seule',
          accentColor: OcpColors.forest,
          onTap: () => context.push('/at'),
        ),
        _banner(
          icon: Icons.lock_outline_rounded,
          message:
              'Règle fail-closed : en position E (HMEE), aucune action d\'écriture — consultation seule.',
        ),
      ],
    );
  }
}

// ─── Dashboard HC (§4) ───────────────────────────────────────────────────────

class _HcDashboard extends StatelessWidget {
  const _HcDashboard();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const _SectionLabel(label: 'Pilotage sécurité'),
        _ActionTile(
          icon: Icons.category_rounded,
          title: 'Interventions à classifier',
          subtitle: 'Nouvelles demandes — classifier Niveau 1 / Niveau 2',
          accentColor: const Color(0xFF1E3A8A),
          onTap: () => context.push('/at?statut=DEMANDE_CREEE'),
        ),
        _ActionTile(
          icon: Icons.shield_rounded,
          title: 'AT en attente de garantie (E)',
          subtitle: 'Visite · rédaction · démarrage · visa — HCEE garantir/valider',
          accentColor: const Color(0xFF2563EB),
          onTap: () => context.push('/at?filter=aValider'),
        ),
        _ActionTile(
          icon: Icons.inventory_2_rounded,
          title: 'AT à archiver',
          subtitle: 'Réception effectuée — archivage réglementaire (≥ 1 an)',
          accentColor: const Color(0xFF059669),
          onTap: () => context.push('/at?statut=TRAVAUX_RECEPTIONES'),
        ),
        const _SectionLabel(label: 'Référentiels HC'),
        _DisabledTile(
          icon: Icons.badge_outlined,
          title: 'Agents habilités',
          subtitle: 'Gestion de la liste F-HSE-SEC-31-02 (module web)',
        ),
        _DisabledTile(
          icon: Icons.menu_book_rounded,
          title: 'Registre Niveau 1',
          subtitle: 'Registre F-HSE-SEC-31-01 (module web)',
        ),
      ],
    );
  }
}

// ─── Dashboard ADMIN (§5) ────────────────────────────────────────────────────

class _AdminDashboard extends ConsumerWidget {
  const _AdminDashboard();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stats = ref.watch(dashboardProvider);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const _SectionLabel(label: 'KPIs globaux'),
        stats.when(
          loading: () => const _KpiSkeleton(),
          error: (e, _) => _KpiError(
            message: e is Failure ? e.message : 'Statistiques indisponibles.',
          ),
          data: (data) => _KpiGrid(
            kpis: [
              _KpiItem(
                icon: Icons.engineering_rounded,
                label: 'AT en cours',
                value: data.kpis.autorisationsEnCours,
                color: OcpColors.forest,
                route: '/at?statut=EN_COURS',
              ),
              _KpiItem(
                icon: Icons.draw_rounded,
                label: 'Visas en attente',
                value: data.kpis.visasEnAttente,
                color: const Color(0xFFD97706),
                route: '/at?statut=SOUMISE',
              ),
              _KpiItem(
                icon: Icons.badge_outlined,
                label: 'Permis actifs',
                value: data.kpis.permisActifs,
                color: const Color(0xFF2563EB),
                route: '/at',
              ),
              _KpiItem(
                icon: Icons.assignment_turned_in_outlined,
                label: 'À réceptionner',
                value: data.kpis.receptionsEnAttente,
                color: const Color(0xFF059669),
                route: '/at?statut=FIN_TRAVAUX_DECLAREE',
              ),
            ],
          ),
        ),
        const _SectionLabel(label: 'Modules'),
        _ActionTile(
          icon: Icons.format_list_bulleted_rounded,
          title: 'Toutes les autorisations',
          subtitle: 'Supervision de l\'ensemble des dossiers AT',
          accentColor: OcpColors.forest,
          onTap: () => context.push('/at'),
        ),
        _DisabledTile(
          icon: Icons.manage_accounts_rounded,
          title: 'Administration',
          subtitle: 'Utilisateurs, rôles, référentiels, audit (module web)',
        ),
      ],
    );
  }
}

// ─── Dashboard RESPONSABLE_EXTERIEUR (§6) ────────────────────────────────────

class _ExterneDashboard extends StatelessWidget {
  const _ExterneDashboard();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const _SectionLabel(label: 'Mon périmètre'),
        _DisabledTile(
          icon: Icons.description_outlined,
          title: 'Mes Bons de Travaux',
          subtitle: 'Créer / consulter les BT (module web)',
        ),
        _DisabledTile(
          icon: Icons.upload_file_rounded,
          title: 'Permis à uploader',
          subtitle: 'Upload des permis liés aux BT (module web)',
        ),
        _ActionTile(
          icon: Icons.visibility_rounded,
          title: 'AT liées à mes BT',
          subtitle: 'Consultation seule — aucune action d\'écriture',
          accentColor: OcpColors.forest,
          onTap: () => context.push('/at'),
        ),
        _banner(
          icon: Icons.block_rounded,
          message:
              'Interdits (§6) : création, rédaction, validation d\'AT, visite, démarrage, réception, archivage.',
        ),
      ],
    );
  }
}

// ─── Sous-widgets ─────────────────────────────────────────────────────────────

Widget _banner({required IconData icon, required String message}) {
  return Padding(
    padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
    child: Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: OcpColors.warningSoft,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: OcpColors.warning.withValues(alpha: 0.3)),
      ),
      child: Row(
        children: [
          Icon(icon, color: OcpColors.warning, size: 18),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              message,
              style: const TextStyle(fontSize: 11, color: OcpColors.ink, height: 1.4),
            ),
          ),
        ],
      ),
    ),
  );
}

class _RoleHeader extends StatelessWidget {
  final String nom;
  final String subtitle;
  final Color color;
  final IconData icon;
  const _RoleHeader({
    required this.nom,
    required this.subtitle,
    required this.color,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      color: OcpColors.forest,
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 20),
      child: Container(
        decoration: BoxDecoration(
          color: OcpColors.white,
          borderRadius: BorderRadius.circular(14),
          boxShadow: [
            BoxShadow(
              color: OcpColors.deep.withValues(alpha: 0.08),
              blurRadius: 12,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Container(
              width: 52,
              height: 52,
              decoration: BoxDecoration(
                color: color.withValues(alpha: 0.10),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: color, size: 28),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    nom,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontFamily: 'SpaceGrotesk',
                      fontWeight: FontWeight.w700,
                      fontSize: 15,
                      color: OcpColors.ink,
                    ),
                  ),
                  const SizedBox(height: 3),
                  Text(
                    subtitle,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 11,
                      color: OcpColors.slate,
                      height: 1.4,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _SectionLabel extends StatelessWidget {
  final String label;
  const _SectionLabel({required this.label});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 20, 16, 8),
      child: Text(
        label.toUpperCase(),
        style: const TextStyle(
          fontFamily: 'SpaceGrotesk',
          fontWeight: FontWeight.w800,
          fontSize: 11,
          letterSpacing: 1.2,
          color: OcpColors.slate,
        ),
      ),
    );
  }
}

class _KpiItem {
  final IconData icon;
  final String label;
  final int value;
  final Color color;
  final String route;
  const _KpiItem({
    required this.icon,
    required this.label,
    required this.value,
    required this.color,
    required this.route,
  });
}

class _KpiGrid extends StatelessWidget {
  final List<_KpiItem> kpis;
  const _KpiGrid({required this.kpis});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12),
      child: GridView.count(
        crossAxisCount: 2,
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
        childAspectRatio: 1.35,
        children: kpis.map((kpi) => _KpiCell(kpi: kpi)).toList(),
      ),
    );
  }
}

class _KpiCell extends StatelessWidget {
  final _KpiItem kpi;
  const _KpiCell({required this.kpi});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: OcpColors.white,
      borderRadius: BorderRadius.circular(12),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: () => context.push(kpi.route),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(kpi.icon, size: 16, color: kpi.color),
              const SizedBox(height: 2),
              FittedBox(
                fit: BoxFit.scaleDown,
                alignment: Alignment.centerLeft,
                child: Text(
                  '${kpi.value}',
                  style: TextStyle(
                    fontFamily: 'SpaceGrotesk',
                    fontWeight: FontWeight.w800,
                    fontSize: 20,
                    color: kpi.color,
                    height: 1.1,
                  ),
                ),
              ),
              const SizedBox(height: 2),
              Text(
                kpi.label,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  fontSize: 10,
                  color: OcpColors.slate,
                  fontWeight: FontWeight.w500,
                  height: 1.2,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _KpiSkeleton extends StatelessWidget {
  const _KpiSkeleton();

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.symmetric(vertical: 24),
      child: Center(
        child: CircularProgressIndicator(color: OcpColors.forest),
      ),
    );
  }
}

class _KpiError extends StatelessWidget {
  final String message;
  const _KpiError({required this.message});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Container(
        decoration: BoxDecoration(
          color: OcpColors.warningSoft,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: OcpColors.warning.withValues(alpha: 0.3)),
        ),
        padding: const EdgeInsets.all(14),
        child: Text(
          message,
          style: const TextStyle(fontSize: 12, color: OcpColors.warning),
        ),
      ),
    );
  }
}

class _ActionTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final Color accentColor;
  final VoidCallback onTap;
  const _ActionTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.accentColor,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: Material(
        color: OcpColors.white,
        borderRadius: BorderRadius.circular(12),
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
            child: Row(
              children: [
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: accentColor.withValues(alpha: 0.10),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Icon(icon, color: accentColor, size: 20),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 13,
                          color: OcpColors.ink,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        subtitle,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontSize: 11,
                          color: OcpColors.slate,
                          height: 1.4,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                Icon(Icons.chevron_right_rounded,
                    size: 18, color: OcpColors.slate.withValues(alpha: 0.5)),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// Tile non disponible sur mobile (module web uniquement) — explicite, non cliquable.
class _DisabledTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  const _DisabledTile({
    required this.icon,
    required this.title,
    required this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: Material(
        color: OcpColors.surfaceSoft,
        borderRadius: BorderRadius.circular(12),
        clipBehavior: Clip.antiAlias,
        child: Opacity(
          opacity: 0.75,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
            child: Row(
              children: [
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: OcpColors.slate.withValues(alpha: 0.08),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Icon(icon, color: OcpColors.slate, size: 20),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 13,
                          color: OcpColors.slate,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        subtitle,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontSize: 11,
                          color: OcpColors.slate,
                          height: 1.4,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                const Icon(Icons.lock_outline_rounded, size: 16, color: OcpColors.slate),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
