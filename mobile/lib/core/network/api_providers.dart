/// Fournisseurs globaux (Riverpod) : stockage sécurisé et client HTTP.
library;

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'api_client.dart';
import '../storage/secure_token_storage.dart';

final secureTokenStorageProvider = Provider<SecureTokenStorage>((ref) => SecureTokenStorage());

final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient(storage: ref.watch(secureTokenStorageProvider)),
);
