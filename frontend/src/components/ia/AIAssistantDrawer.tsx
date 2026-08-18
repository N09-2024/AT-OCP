import React, { useState } from 'react';
import {
  Drawer,
  Box,
  Typography,
  IconButton,
  TextField,
  Button,
  Paper,
  Chip,
  CircularProgress,
  Divider,
  Stack,
  Avatar,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SendIcon from '@mui/icons-material/Send';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import PersonIcon from '@mui/icons-material/Person';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import { iaApi, type ChatResult } from '../../services/iaApi';

interface Message {
  id: string;
  sender: 'user' | 'ai';
  text: string;
  sources?: string[];
  suggestedQuestions?: string[];
}

interface AIAssistantDrawerProps {
  open: boolean;
  onClose: () => void;
  atContext?: Record<string, any>;
}

export const AIAssistantDrawer: React.FC<AIAssistantDrawerProps> = ({
  open,
  onClose,
  atContext,
}) => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 'welcome',
      sender: 'ai',
      text: "Bonjour ! Je suis votre Assistant IA HSE OCP. Je peux vous renseigner sur le Standard S-HSE-SEC-31, les étapes du formulaire F-HSE-SEC-31-04, les risques, les EPI et les permis nécessaires.",
      sources: ["Standard OCP S-HSE-SEC-31"],
      suggestedQuestions: [
        "Quels sont les prérequis pour un permis de feu ?",
        "Quels sont les rôles respectifs du CEEP et du CEEE ?",
        "Quels sont les EPI obligatoires pour un travail en hauteur ?"
      ]
    }
  ]);
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSendMessage = async (textToSend?: string) => {
    const messageContent = textToSend || inputText;
    if (!messageContent.trim() || loading) return;

    const userMsg: Message = {
      id: Date.now().toString(),
      sender: 'user',
      text: messageContent.trim(),
    };

    setMessages((prev) => [...prev, userMsg]);
    if (!textToSend) setInputText('');
    setLoading(true);

    try {
      const response: ChatResult = await iaApi.chat({
        message: messageContent,
        atContext: atContext || undefined,
      });

      const aiMsg: Message = {
        id: (Date.now() + 1).toString(),
        sender: 'ai',
        text: response.answer,
        sources: response.sources,
        suggestedQuestions: response.suggestedQuestions,
      };
      setMessages((prev) => [...prev, aiMsg]);
    } catch {
      const errorMsg: Message = {
        id: (Date.now() + 1).toString(),
        sender: 'ai',
        text: "Désolé, une erreur s'est produite lors de la consultation de l'assistant. Veuillez réessayer.",
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      sx={{
        '& .MuiDrawer-paper': {
          width: { xs: '100%', sm: 440 },
          display: 'flex',
          flexDirection: 'column',
          bgcolor: 'background.paper',
        },
      }}
    >
      {/* Header */}
      <Box
        sx={{
          p: 2,
          bgcolor: 'primary.main',
          color: 'primary.contrastText',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
          <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)' }}>
            <AutoAwesomeIcon />
          </Avatar>
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
              Assistant AT & HSE OCP
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.85 }}>
              Standard S-HSE-SEC-31 & RAG officiel
            </Typography>
          </Box>
        </Stack>
        <IconButton onClick={onClose} sx={{ color: 'inherit' }}>
          <CloseIcon />
        </IconButton>
      </Box>

      {/* Messages List */}
      <Box
        sx={{
          flex: 1,
          p: 2,
          overflowY: 'auto',
          display: 'flex',
          flexDirection: 'column',
          gap: 2,
          bgcolor: '#f8fafc',
        }}
      >
        {messages.map((msg) => (
          <Box
            key={msg.id}
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: msg.sender === 'user' ? 'flex-end' : 'flex-start',
            }}
          >
            <Stack
              direction="row"
              spacing={1}
              sx={{
                maxWidth: '90%',
                flexDirection: msg.sender === 'user' ? 'row-reverse' : 'row',
              }}
            >
              <Avatar
                sx={{
                  width: 32,
                  height: 32,
                  bgcolor: msg.sender === 'user' ? 'secondary.main' : 'primary.main',
                  fontSize: 14,
                }}
              >
                {msg.sender === 'user' ? <PersonIcon fontSize="small" /> : <SmartToyIcon fontSize="small" />}
              </Avatar>
              <Paper
                elevation={1}
                sx={{
                  p: 1.5,
                  borderRadius: 2,
                  bgcolor: msg.sender === 'user' ? 'primary.light' : '#ffffff',
                  color: msg.sender === 'user' ? '#ffffff' : 'text.primary',
                }}
              >
                <Typography variant="body2" sx={{ whiteSpace: 'pre-line' }}>
                  {msg.text}
                </Typography>

                {/* Sources RAG citées */}
                {msg.sources && msg.sources.length > 0 && (
                  <Box sx={{ mt: 1.5, pt: 1, borderTop: '1px solid rgba(0,0,0,0.08)' }}>
                    <Stack direction="row" spacing={0.5} sx={{ mb: 0.5, alignItems: 'center' }}>
                      <MenuBookIcon sx={{ fontSize: 13, color: 'text.secondary' }} />
                      <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                        Sources officielles :
                      </Typography>
                    </Stack>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {msg.sources.map((src, i) => (
                        <Chip
                          key={i}
                          label={src}
                          size="small"
                          variant="outlined"
                          sx={{ fontSize: '0.68rem', height: 20 }}
                        />
                      ))}
                    </Box>
                  </Box>
                )}
              </Paper>
            </Stack>

            {/* Questions suggérées */}
            {msg.suggestedQuestions && msg.suggestedQuestions.length > 0 && (
              <Box sx={{ mt: 1, pl: 5, display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                  Suggestions :
                </Typography>
                {msg.suggestedQuestions.map((q, idx) => (
                  <Button
                    key={idx}
                    variant="text"
                    size="small"
                    onClick={() => handleSendMessage(q)}
                    sx={{
                      textTransform: 'none',
                      justifyContent: 'flex-start',
                      fontSize: '0.75rem',
                      py: 0.2,
                      px: 1,
                      bgcolor: 'rgba(0, 135, 81, 0.05)',
                      '&:hover': { bgcolor: 'rgba(0, 135, 81, 0.12)' },
                      borderRadius: 1,
                    }}
                  >
                    💡 {q}
                  </Button>
                ))}
              </Box>
            )}
          </Box>
        ))}

        {loading && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, pl: 5 }}>
            <CircularProgress size={18} />
            <Typography variant="caption" color="text.secondary">
              L'assistant analyse les référentiels OCP...
            </Typography>
          </Box>
        )}
      </Box>

      <Divider />

      {/* Input Form */}
      <Box sx={{ p: 2, bgcolor: 'background.paper' }}>
        <Stack direction="row" spacing={1}>
          <TextField
            fullWidth
            size="small"
            placeholder="Posez une question sur l'AT ou la sécurité..."
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSendMessage();
              }
            }}
            disabled={loading}
          />
          <Button
            variant="contained"
            color="primary"
            onClick={() => handleSendMessage()}
            disabled={!inputText.trim() || loading}
            sx={{ minWidth: 48, px: 2 }}
          >
            <SendIcon fontSize="small" />
          </Button>
        </Stack>
      </Box>
    </Drawer>
  );
};
