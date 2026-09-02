/// Client API et modèles pour l'Assistant IA HSE OCP (RAG + CrewAI + Gemini).
library;

import '../../../core/network/api_client.dart';

class AiChatResponse {
  final String answer;
  final List<String> sources;
  final String? confidence;
  final List<String> suggestedQuestions;

  const AiChatResponse({
    required this.answer,
    this.sources = const [],
    this.confidence,
    this.suggestedQuestions = const [],
  });

  factory AiChatResponse.fromJson(Map<String, dynamic> json) {
    List<String> parseList(dynamic v) {
      if (v == null) return [];
      if (v is List) return v.map((e) => e.toString()).toList();
      return [];
    }

    return AiChatResponse(
      answer: json['answer'] as String? ?? 'Désolé, aucune réponse générée.',
      sources: parseList(json['sources']),
      confidence: json['confidence'] as String?,
      suggestedQuestions: parseList(json['suggestedQuestions']),
    );
  }
}

class AssistantApi {
  final ApiClient _client;
  const AssistantApi(this._client);

  Future<AiChatResponse> chat({
    required String message,
    String? conversationId,
    Map<String, dynamic>? atContext,
  }) async {
    final payload = <String, dynamic>{
      'message': message,
    };
    if (conversationId != null) payload['conversationId'] = conversationId;
    if (atContext != null) payload['atContext'] = atContext;

    try {
      final res = await _client.post<Map<String, dynamic>>('/ai/chat', body: payload);
      return AiChatResponse.fromJson(res.data!);
    } catch (_) {
      // Repli sur route /ia/chat si nécessaire
      final res = await _client.post<Map<String, dynamic>>('/ia/chat', body: payload);
      return AiChatResponse.fromJson(res.data!);
    }
  }

  /// Contrôle IA de complétude avant soumission CEEP - identique au web
  /// (iaApi.controlerDossier → POST /ia/controler-dossier).
  /// Réponse : {complet: bool, alertes: [String], rapport?: String, ...}.
  Future<Map<String, dynamic>> controlerDossier({
    String? description,
    required bool visiteFaite,
    required int nbRisques,
    required int nbMesures,
    required int nbEpis,
    required int nbPermis,
    required bool sectionFRenseignee,
  }) async {
    final res = await _client.post<Map<String, dynamic>>(
      '/ia/controler-dossier',
      body: {
        'description': ?description,
        'visiteFaite': visiteFaite,
        'nbRisques': nbRisques,
        'nbMesures': nbMesures,
        'nbEpis': nbEpis,
        'nbPermis': nbPermis,
        'sectionFRenseignee': sectionFRenseignee,
      },
    );
    return res.data ?? const {};
  }
}
