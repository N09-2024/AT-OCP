/// Providers des référentiels - toutes les données proviennent du backend
/// (aucune liste codée en dur). autoDispose + cache pendant le watch.
library;

import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/api_providers.dart';
import '../referentiel_api.dart';
import '../data/referentiel_models.dart';

final referentielApiProvider =
    Provider<ReferentielApi>((ref) => ReferentielApi(ref.watch(apiClientProvider)));

final zonesProvider = FutureProvider.autoDispose<List<Zone>>((ref) async {
  try {
    return await ref.watch(referentielApiProvider).zones();
  } catch (_) {
    throw Exception('Référentiels indisponibles');
  }
});

/// Tous les services OCP (GET /services).
final servicesProvider = FutureProvider.autoDispose<List<ServiceOcp>>((ref) async {
  try {
    return await ref.watch(referentielApiProvider).services();
  } catch (_) {
    throw Exception('Services indisponibles');
  }
});

/// Entreprises extérieures (GET /entreprises-externes).
final entreprisesExternesProvider =
    FutureProvider.autoDispose<List<EntrepriseExterne>>((ref) async {
  try {
    return await ref.watch(referentielApiProvider).entreprisesExternes();
  } catch (_) {
    throw Exception('Entreprises externes indisponibles');
  }
});

/// Services filtrés par zone (GET /zones/{id}/services).
final servicesByZoneProvider = FutureProvider.autoDispose
    .family<List<ServiceOcp>, String>((ref, zoneId) async {
  return ref.watch(referentielApiProvider).services(zoneId: zoneId);
});

final risquesProvider = FutureProvider.autoDispose<List<ReferentielItem>>(
    (ref) => ref.watch(referentielApiProvider).risques(),);

final mesuresProvider = FutureProvider.autoDispose<List<ReferentielItem>>(
    (ref) => ref.watch(referentielApiProvider).mesures(),);

final episProvider = FutureProvider.autoDispose<List<ReferentielItem>>(
    (ref) => ref.watch(referentielApiProvider).epis(),);

final moyensAccesProvider = FutureProvider.autoDispose<List<ReferentielItem>>(
    (ref) => ref.watch(referentielApiProvider).moyensAcces(),);

final typesPermisProvider = FutureProvider.autoDispose<List<TypePermis>>(
    (ref) => ref.watch(referentielApiProvider).typesPermis(),);
