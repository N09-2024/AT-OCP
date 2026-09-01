/// Écran de connexion — POST /api/auth/login {email, motDePasse}.
/// Validation des champs, affichage des erreurs métier, redirection après login.
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import 'auth_controller.dart';

class LoginPage extends ConsumerStatefulWidget {
  const LoginPage({super.key});

  @override
  ConsumerState<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends ConsumerState<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    FocusScope.of(context).unfocus();
    if (!_formKey.currentState!.validate()) return;

    final ok = await ref.read(authControllerProvider.notifier).login(
          _emailController.text.trim(),
          _passwordController.text,
        );
    if (!ok && mounted) {
      final state = ref.read(authControllerProvider);
      if (state is AuthError) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(state.failure.message)),
        );
      }
    }
    // Succès → le routeur redirige automatiquement (authState listener).
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authControllerProvider);
    final loading = authState is AuthLoading;

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // --- Identité OCP (logo officiel transparent, comme le web) ---
                    Center(
                      child: Image.asset(
                        'assets/images/logo-ocp.png',
                        height: 130,
                        fit: BoxFit.contain,
                        errorBuilder: (_, __, ___) => Container(
                          width: 84,
                          height: 84,
                          decoration: BoxDecoration(
                            gradient: const LinearGradient(
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight,
                              colors: [
                                OcpColors.deep,
                                OcpColors.forest,
                                OcpColors.moss,
                              ],
                            ),
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: const Icon(Icons.security_rounded,
                              size: 42, color: OcpColors.white),
                        ),
                      ),
                    ),
                    const SizedBox(height: 20),
                    const Text(
                      'Autorisations de Travail',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontFamily: 'SpaceGrotesk',
                        fontSize: 24,
                        fontWeight: FontWeight.w700,
                        color: OcpColors.ink,
                      ),
                    ),
                    const SizedBox(height: 6),
                    const Text(
                      'OCP — Sécurité & HSE',
                      textAlign: TextAlign.center,
                      style: TextStyle(fontSize: 14, color: OcpColors.slate),
                    ),
                    const SizedBox(height: 32),

                    // --- Email ---
                    TextFormField(
                      controller: _emailController,
                      keyboardType: TextInputType.emailAddress,
                      autofillHints: const [AutofillHints.email],
                      textInputAction: TextInputAction.next,
                      decoration: const InputDecoration(
                        labelText: 'Adresse e-mail',
                        prefixIcon: Icon(Icons.mail_outline_rounded),
                      ),
                      validator: (v) {
                        if (v == null || v.trim().isEmpty) {
                          return 'L\'e-mail est obligatoire';
                        }
                        if (!RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$').hasMatch(v.trim())) {
                          return 'Format d\'e-mail invalide';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // --- Mot de passe ---
                    TextFormField(
                      controller: _passwordController,
                      obscureText: _obscurePassword,
                      autofillHints: const [AutofillHints.password],
                      textInputAction: TextInputAction.done,
                      onFieldSubmitted: (_) => _submit(),
                      decoration: InputDecoration(
                        labelText: 'Mot de passe',
                        prefixIcon: const Icon(Icons.lock_outline_rounded),
                        suffixIcon: IconButton(
                          icon: Icon(_obscurePassword
                              ? Icons.visibility_off_outlined
                              : Icons.visibility_outlined,),
                          onPressed: () =>
                              setState(() => _obscurePassword = !_obscurePassword),
                        ),
                      ),
                      validator: (v) =>
                          (v == null || v.isEmpty) ? 'Le mot de passe est obligatoire' : null,
                    ),
                    const SizedBox(height: 24),

                    // --- Connexion ---
                    ElevatedButton(
                      onPressed: loading ? null : _submit,
                      child: loading
                          ? const SizedBox(
                              width: 22,
                              height: 22,
                              child: CircularProgressIndicator(
                                strokeWidth: 2.4,
                                color: OcpColors.white,
                              ),
                            )
                          : const Text('Se connecter'),
                    ),
                    const SizedBox(height: 20),

                    Text(
                      'Accès réservé au personnel habilité OCP.',
                      textAlign: TextAlign.center,
                      style: TextStyle(fontSize: 12, color: OcpColors.slate.withValues(alpha: 0.8)),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
