/// Contrôleur Riverpod du Chat Assistant IA HSE OCP.
library;

import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/network/api_providers.dart';
import '../data/assistant_api.dart';

final assistantApiProvider =
    Provider<AssistantApi>((ref) => AssistantApi(ref.watch(apiClientProvider)));

@immutable
class ChatMessage {
  final String text;
  final bool isUser;
  final DateTime timestamp;
  final List<String> sources;
  final String? confidence;
  final List<String> suggestedQuestions;

  const ChatMessage({
    required this.text,
    required this.isUser,
    required this.timestamp,
    this.sources = const [],
    this.confidence,
    this.suggestedQuestions = const [],
  });
}

@immutable
class AssistantChatState {
  final List<ChatMessage> messages;
  final bool isLoading;
  final String? error;
  final String conversationId;
  final List<String> suggestions;

  const AssistantChatState({
    this.messages = const [],
    this.isLoading = false,
    this.error,
    required this.conversationId,
    this.suggestions = const [
      'Quels sont les EPI obligatoires pour un travail en hauteur ?',
      'Quelle est la durée maximale de validité d\'une AT ?',
      'Comment fonctionne la reconduction au-delà de 24h ?',
      'Qui doit signer le visa en premier selon le logigramme ?',
    ],
  });

  AssistantChatState copyWith({
    List<ChatMessage>? messages,
    bool? isLoading,
    String? error,
    bool clearError = false,
    String? conversationId,
    List<String>? suggestions,
  }) {
    return AssistantChatState(
      messages: messages ?? this.messages,
      isLoading: isLoading ?? this.isLoading,
      error: clearError ? null : (error ?? this.error),
      conversationId: conversationId ?? this.conversationId,
      suggestions: suggestions ?? this.suggestions,
    );
  }
}

class AssistantChatNotifier extends StateNotifier<AssistantChatState> {
  final AssistantApi _api;
  final Map<String, dynamic>? _initialContext;

  AssistantChatNotifier(this._api, {Map<String, dynamic>? initialContext})
      : _initialContext = initialContext,
        super(AssistantChatState(
          conversationId: 'mobile_${DateTime.now().millisecondsSinceEpoch}',
          messages: [
            ChatMessage(
              text: initialContext != null && initialContext['objet'] != null
                  ? 'Bonjour ! Je suis votre Assistant IA HSE OCP. Je peux vous aider spécifiquement sur l\'Autorisation de Travail : "${initialContext['objet']}" ou sur toute règle du standard S-HSE-SEC-31.'
                  : 'Bonjour ! Je suis votre Assistant IA HSE OCP. Je peux vous renseigner sur le Standard S-HSE-SEC-31, les étapes du formulaire F-HSE-SEC-31-04, les risques, les EPI et les permis nécessaires.',
              isUser: false,
              timestamp: DateTime.now(),
              suggestedQuestions: const [
                'Quels sont les EPI requis pour cette intervention ?',
                'Quels permis complémentaires sont obligatoires ?',
                'Quelles sont les étapes du logigramme S-HSE-SEC-31 ?',
              ],
            ),
          ],
        ));

  Future<void> sendMessage(String text) async {
    final cleanText = text.trim();
    if (cleanText.isEmpty || state.isLoading) return;

    final userMsg = ChatMessage(
      text: cleanText,
      isUser: true,
      timestamp: DateTime.now(),
    );

    state = state.copyWith(
      messages: [...state.messages, userMsg],
      isLoading: true,
      clearError: true,
    );

    try {
      final response = await _api.chat(
        message: cleanText,
        conversationId: state.conversationId,
        atContext: _initialContext,
      );

      final aiMsg = ChatMessage(
        text: response.answer,
        isUser: false,
        timestamp: DateTime.now(),
        sources: response.sources,
        confidence: response.confidence,
        suggestedQuestions: response.suggestedQuestions,
      );

      state = state.copyWith(
        messages: [...state.messages, aiMsg],
        isLoading: false,
        suggestions: response.suggestedQuestions.isNotEmpty
            ? response.suggestedQuestions
            : state.suggestions,
      );
    } catch (e) {
      final fail = mapDioError(e);
      state = state.copyWith(
        isLoading: false,
        error: fail.message,
      );
    }
  }

  void clearConversation() {
    state = AssistantChatState(
      conversationId: 'mobile_${DateTime.now().millisecondsSinceEpoch}',
      messages: [
        ChatMessage(
          text: 'Nouvelle conversation démarrée. Posez votre question sur les règles HSE ou vos autorisations de travail.',
          isUser: false,
          timestamp: DateTime.now(),
        ),
      ],
    );
  }
}

final assistantChatProvider = StateNotifierProvider.autoDispose
    .family<AssistantChatNotifier, AssistantChatState, Map<String, dynamic>?>((ref, context) {
  final api = ref.watch(assistantApiProvider);
  return AssistantChatNotifier(api, initialContext: context);
});
