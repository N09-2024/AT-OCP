# AT-OCP — Frontend palette harmonisation

The frontend has been harmonised around the exact green palette used by the Login page:

- Deep: #0E2A21
- Forest: #1F4D3E
- Moss: #3C7A5C
- Mint: #7FC8A9
- Sage: #EDF2EE
- Ink: #16241E
- Slate: #5C6E67

Additional neutral/surface tokens are derived from the same palette.

## Central tokens
`src/theme/tokens.ts`

## Global MUI theme
`src/theme/index.ts`

The global theme now controls primary/secondary/success colors, typography, buttons, cards, inputs, tables, links and surfaces.

Inline hard-coded blue, purple, cyan, pink and unrelated green accents in `src` were replaced with the Login palette. Red/orange tones are retained only where they are semantic error/warning states.

The Login page imports the same tokens, so Login and the rest of the frontend share one source of truth.
