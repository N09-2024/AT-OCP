/// Écran de conversation avec l'Assistant IA HSE OCP (RAG + Gemini).
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/app_date.dart';
import 'assistant_controller.dart';

class AssistantPage extends ConsumerStatefulWidget {
  final Map<String, dynamic>? atContext;
  const AssistantPage({super.key, this.atContext});

  @override
  ConsumerState<AssistantPage> createState() => _AssistantPageState();
}

class _AssistantPageState extends ConsumerState<AssistantPage> {
  final _textController = TextEditingController();
  final _scrollController = ScrollController();

  @override
  void dispose() {
    _textController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  void _sendMessage(String text) {
    if (text.trim().isEmpty) return;
    _textController.clear();
    ref.read(assistantChatProvider(widget.atContext).notifier).sendMessage(text);
    _scrollToBottom();
  }

  @override
  Widget build(BuildContext context) {
    final chatState = ref.watch(assistantChatProvider(widget.atContext));

    ref.listen(assistantChatProvider(widget.atContext), (_, next) {
      _scrollToBottom();
    });

    final contextTitle = widget.atContext?['numero'] != null
        ? '${widget.atContext!['numero']} - ${widget.atContext!['objet'] ?? ''}'
        : widget.atContext?['objet']?.toString();

    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.smart_toy_rounded, size: 20, color: OcpColors.mint),
                SizedBox(width: 8),
                Text('Assistant IA HSE', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700)),
              ],
            ),
            Text(
              'Standard S-HSE-SEC-31 · RAG OCP',
              style: TextStyle(fontSize: 11, color: Theme.of(context).colorScheme.onSurfaceVariant),
            ),
          ],
        ),
        actions: [
          IconButton(
            tooltip: 'Nouvelle conversation',
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () =>
                ref.read(assistantChatProvider(widget.atContext).notifier).clearConversation(),
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Bandeau de contexte si présent
            if (contextTitle != null && contextTitle.isNotEmpty)
              Container(
                width: double.infinity,
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                color: OcpColors.forestSoft,
                child: Row(
                  children: [
                    const Icon(Icons.assignment_outlined, size: 16, color: OcpColors.forest),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Contexte : $contextTitle',
                        style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: OcpColors.forestDark),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              ),

            // Messages
            Expanded(
              child: ListView.builder(
                controller: _scrollController,
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                itemCount: chatState.messages.length + (chatState.isLoading ? 1 : 0),
                itemBuilder: (context, index) {
                  if (index == chatState.messages.length && chatState.isLoading) {
                    return const _TypingIndicator();
                  }
                  final msg = chatState.messages[index];
                  return _MessageBubble(
                    message: msg,
                    onSuggestionTap: _sendMessage,
                  );
                },
              ),
            ),

            // Message d'erreur éventuel
            if (chatState.error != null)
              Container(
                margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: OcpColors.errorSoft,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.error_outline_rounded, size: 16, color: OcpColors.error),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        chatState.error!,
                        style: const TextStyle(fontSize: 12, color: OcpColors.error),
                      ),
                    ),
                  ],
                ),
              ),

            // Suggestions de questions
            if (chatState.suggestions.isNotEmpty && !chatState.isLoading)
              SizedBox(
                height: 38,
                child: ListView.separated(
                  scrollDirection: Axis.horizontal,
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: chatState.suggestions.length,
                  separatorBuilder: (_, _) => const SizedBox(width: 8),
                  itemBuilder: (context, i) {
                    final suggestion = chatState.suggestions[i];
                    return ActionChip(
                      backgroundColor: OcpColors.surfaceSoft,
                      side: const BorderSide(color: OcpColors.borderSoft),
                      label: Text(
                        suggestion,
                        style: const TextStyle(fontSize: 11, color: OcpColors.deep),
                      ),
                      onPressed: () => _sendMessage(suggestion),
                    );
                  },
                ),
              ),

            const SizedBox(height: 6),

            // Zone de saisie
            Container(
              padding: const EdgeInsets.fromLTRB(16, 6, 16, 10),
              decoration: const BoxDecoration(
                color: Colors.white,
                border: Border(top: BorderSide(color: OcpColors.borderSoft)),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _textController,
                      minLines: 1,
                      maxLines: 3,
                      textInputAction: TextInputAction.send,
                      onSubmitted: _sendMessage,
                      decoration: InputDecoration(
                        hintText: 'Posez votre question HSE / S-HSE-SEC-31...',
                        hintStyle: const TextStyle(fontSize: 13, color: OcpColors.slate),
                        filled: true,
                        fillColor: OcpColors.surfaceSoft,
                        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(24),
                          borderSide: BorderSide.none,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton.filled(
                    style: IconButton.styleFrom(
                      backgroundColor: OcpColors.forest,
                      foregroundColor: Colors.white,
                    ),
                    icon: chatState.isLoading
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Icon(Icons.send_rounded, size: 18),
                    onPressed: chatState.isLoading
                        ? null
                        : () => _sendMessage(_textController.text),
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

class _MessageBubble extends StatelessWidget {
  final ChatMessage message;
  final ValueChanged<String> onSuggestionTap;

  const _MessageBubble({
    required this.message,
    required this.onSuggestionTap,
  });

  @override
  Widget build(BuildContext context) {
    final isUser = message.isUser;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        mainAxisAlignment: isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (!isUser) ...[
            const CircleAvatar(
              radius: 16,
              backgroundColor: OcpColors.forest,
              child: Icon(Icons.psychology_alt_rounded, size: 18, color: Colors.white),
            ),
            const SizedBox(width: 8),
          ],
          Flexible(
            child: Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: isUser ? OcpColors.forest : Colors.white,
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(16),
                  topRight: const Radius.circular(16),
                  bottomLeft: Radius.circular(isUser ? 16 : 4),
                  bottomRight: Radius.circular(isUser ? 4 : 16),
                ),
                border: isUser ? null : Border.all(color: OcpColors.borderSoft),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.04),
                    blurRadius: 4,
                    offset: const Offset(0, 2),
                  ),
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Texte du message
                  SelectableText(
                    message.text,
                    style: TextStyle(
                      fontSize: 13,
                      height: 1.4,
                      color: isUser ? Colors.white : OcpColors.ink,
                    ),
                  ),

                  // Sources et confiance pour l'IA
                  if (!isUser && message.sources.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 4,
                      runSpacing: 4,
                      children: message.sources.map((s) => Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: OcpColors.forestSoft,
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          '📖 $s',
                          style: const TextStyle(fontSize: 10, color: OcpColors.forestDark, fontWeight: FontWeight.w600),
                        ),
                      )).toList(),
                    ),
                  ],

                  if (!isUser && message.confidence != null) ...[
                    const SizedBox(height: 4),
                    Text(
                      'Confiance RAG : ${message.confidence}',
                      style: const TextStyle(fontSize: 10, color: OcpColors.slate),
                    ),
                  ],

                  // Horodatage
                  const SizedBox(height: 4),
                  Align(
                    alignment: Alignment.bottomRight,
                    child: Text(
                      AppDate.heureSimple('${message.timestamp.hour.toString().padLeft(2, '0')}:${message.timestamp.minute.toString().padLeft(2, '0')}:00'),
                      style: TextStyle(
                        fontSize: 10,
                        color: isUser ? Colors.white.withValues(alpha: 0.7) : OcpColors.slate,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          if (isUser) const SizedBox(width: 8),
        ],
      ),
    );
  }
}

class _TypingIndicator extends StatelessWidget {
  const _TypingIndicator();

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          const CircleAvatar(
            radius: 16,
            backgroundColor: OcpColors.forest,
            child: Icon(Icons.psychology_alt_rounded, size: 18, color: Colors.white),
          ),
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: OcpColors.borderSoft),
            ),
            child: const Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                SizedBox(
                  width: 14,
                  height: 14,
                  child: CircularProgressIndicator(strokeWidth: 2, color: OcpColors.forest),
                ),
                SizedBox(width: 8),
                Text(
                  'L\'assistant analyse les référentiels OCP...',
                  style: TextStyle(fontSize: 12, color: OcpColors.slate, fontStyle: FontStyle.italic),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
