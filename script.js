const fs = require('fs');
const file = 'frontend/src/modules/autorisations/pages/AutorisationFormPage.tsx';
let content = fs.readFileSync(file, 'utf8');

// Fix Grid item xs={12} sm={6} md={4} -> size={{ xs: 12, sm: 6, md: 4 }}
content = content.replace(/<Grid\s+item\s+xs={(\d+)}\s*>/g, '<Grid size={{ xs:  }}>');
content = content.replace(/<Grid\s+item\s+xs={(\d+)}\s+sm={(\d+)}\s*>/g, '<Grid size={{ xs: , sm:  }}>');
content = content.replace(/<Grid\s+item\s+xs={(\d+)}\s+sm={(\d+)}\s+md={(\d+)}\s+key={([^}]+)}>/g, '<Grid size={{ xs: , sm: , md:  }} key={}>');

// Fix Typography mb={...} -> sx={{ mb: ... }}
content = content.replace(/<Typography([^>]*)\smb={([^}]+)}([^>]*)>/g, '<Typography sx={{ mb:  }}>');

fs.writeFileSync(file, content, 'utf8');
