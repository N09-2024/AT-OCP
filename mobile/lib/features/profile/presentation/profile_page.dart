/// Profil utilisateur — GET /api/auth/me (données fraîches).
/// Affiche nom, rôle, service, et bouton de déconnexion.
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../auth/data/models/utilisateur.dart';
import '../../auth/presentation/auth_controller.dart';

class ProfilePage extends ConsumerWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final session = ref.watch(sessionProvider);
    final utilisateur = session?.utilisateur;

    return Scaffold(
      appBar: AppBar(title: const Text('Profil')),
      body: utilisateur == null
          ? const Center(child: Text('Aucune session active.'))
          : ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.only(bottom: 32),
              children: [
                const SizedBox(height: 16),
                Center(
                  child: CircleAvatar(
                    radius: 44,
                    backgroundColor: OcpColors.forestSoft,
                    child: Text(
                      _initials(utilisateur),
                      style: const TextStyle(
                        fontFamily: 'SpaceGrotesk',
                        fontWeight: FontWeight.w700,
                        fontSize: 24,
                        color: OcpColors.forest,
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                Center(
                  child: Text(
                    utilisateur.nomComplet,
                    style: const TextStyle(
                      fontFamily: 'SpaceGrotesk',
                      fontWeight: FontWeight.w700,
                      fontSize: 18,
                    ),
                  ),
                ),
                Center(
                  child: Text(
                    utilisateur.email,
                    style: const TextStyle(fontSize: 14, color: OcpColors.slate),
                  ),
                ),
                const SizedBox(height: 20),
                _infoSection('Informations', [
                  _infoRow('Matricule', utilisateur.matricule),
                  _infoRow('Téléphone', utilisateur.telephone),
                  _infoRow('Service', utilisateur.service?.nomService),
                ]),
                _infoSection('Rôles & permissions', [
                  _infoRow('Rôles', (session?.roles ?? []).join(', ')),
                  const Padding(
                    padding: EdgeInsets.only(top: 6),
                    child: Text(
                      'Les permissions sont gérées côté serveur (backend OCP).',
                      style: TextStyle(fontSize: 12, color: OcpColors.slate),
                    ),
                  ),
                ]),
                const SizedBox(height: 20),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: OutlinedButton.icon(
                    onPressed: () {
                      ref.read(authControllerProvider.notifier).logout();
                      context.go('/login');
                    },
                    icon: const Icon(Icons.logout_rounded),
                    label: const Text('Se déconnecter'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: OcpColors.error,
                      side: const BorderSide(color: OcpColors.error),
                    ),
                  ),
                ),
              ],
            ),
    );
  }

  String _initials(Utilisateur u) {
    final p = u.prenom?.trim() ?? '';
    final n = u.nom?.trim() ?? '';
    if (p.isEmpty && n.isEmpty) return '?';
    if (p.isEmpty) return n.substring(0, 1).toUpperCase();
    return '${p.substring(0, 1)}${n.substring(0, 1)}'.toUpperCase();
  }

  Widget _infoSection(String title, List<Widget> children) => Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
        child: Card(
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title,
                    style: const TextStyle(
                      fontFamily: 'SpaceGrotesk',
                      fontWeight: FontWeight.w700,
                      fontSize: 15,
                      color: OcpColors.forest,
                    ),),
                const SizedBox(height: 10),
                ...children,
              ],
            ),
          ),
        ),
      );

  Widget _infoRow(String label, String? value) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 3),
        child: Row(
          children: [
            SizedBox(width: 120, child: Text(label, style: const TextStyle(fontSize: 13, color: OcpColors.slate))),
            Expanded(child: Text(value ?? '—', style: const TextStyle(fontSize: 13))),
          ],
        ),
      );
}