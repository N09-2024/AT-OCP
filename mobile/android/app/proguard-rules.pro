# Règles R8/ProGuard spécifiques à l'application OCP AT Mobile.
# Les règles Flutter/Dio/Riverpod nécessaires sont déjà fournies par les
# bibliothèques (consumer rules). Les entités Dart ne sont pas concernées
# par la minification Java.

# Conserve les stacktraces exploitables en release (crashs à remonter).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
