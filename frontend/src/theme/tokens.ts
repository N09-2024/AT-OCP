/**
 * AT-OCP design tokens.
 * Base palette taken from the Login page and reused across the frontend.
 */
export const OCP = {
  deep: '#0E2A21',
  forest: '#1F4D3E',
  moss: '#3C7A5C',
  mint: '#7FC8A9',
  sage: '#EDF2EE',
  ink: '#16241E',
  slate: '#5C6E67',
  white: '#FFFFFF',
  border: '#D6E3DC',
  borderSoft: '#E3ECE7',
  surfaceSoft: '#F7FAF8',
  mintSoft: '#E2F0E8',
  forestSoft: '#DCEBE3',
  mossDark: '#2E624A',
  forestDark: '#163C30',
  warning: '#A87532',
  warningSoft: '#F6EEDC',
  error: '#9A3D2F',
  errorSoft: '#FBEAE3',
} as const;

export const OCP_GRADIENTS = {
  hero: `linear-gradient(160deg, ${OCP.deep} 0%, ${OCP.forest} 65%, ${OCP.moss} 130%)`,
  cta: `linear-gradient(135deg, ${OCP.moss} 0%, ${OCP.forest} 100%)`,
  ctaHover: `linear-gradient(135deg, #34694E 0%, ${OCP.forestDark} 100%)`,
  soft: `linear-gradient(135deg, ${OCP.surfaceSoft} 0%, ${OCP.sage} 100%)`,
} as const;

export const OCP_FONTS = {
  display: "'Space Grotesk', 'Inter', sans-serif",
  body: "'Inter', 'Roboto', 'Helvetica', 'Arial', sans-serif",
  mono: "'IBM Plex Mono', monospace",
} as const;
