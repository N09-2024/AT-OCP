const fs = require('fs');
const path = require('path');

function walkDir(dir, callback) {
  fs.readdirSync(dir).forEach(f => {
    let dirPath = path.join(dir, f);
    let isDirectory = fs.statSync(dirPath).isDirectory();
    isDirectory ? walkDir(dirPath, callback) : callback(path.join(dir, f));
  });
}

walkDir('./frontend/src', function(filePath) {
  if (filePath.endsWith('.tsx')) {
    let content = fs.readFileSync(filePath, 'utf8');
    let original = content;

    // Replace <Stack ... alignItems="center"> with <Stack ... sx={{ alignItems: 'center' }}>
    content = content.replace(/<Stack([^>]*?) alignItems="center"([^>]*?)>/g, '<Stack$1 sx={{ alignItems: \'center\' }}$2>');
    // Handle cases where sx already exists: <Stack ... sx={{ ... }} alignItems="center">
    // Actually, simple regex might fail if sx already exists. Let's just fix the basic ones first.
    // If we have `<Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems="center">` -> `<Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: 'center' }}>`

    // Replace InputProps for MagnifyingGlassIcon
    content = content.replace(/InputProps=\{\{\s*startAdornment:\s*\(\s*<InputAdornment position="start">\s*<MagnifyingGlassIcon([^>]*?)\/>\s*<\/InputAdornment>\s*\),\s*\}\}/g, 
`slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <MagnifyingGlassIcon$1/>
                    </InputAdornment>
                  ),
                }
              }}`);

    // Replace InputLabelProps={{ shrink: true }}
    content = content.replace(/InputLabelProps=\{\{\s*shrink:\s*true\s*\}\}/g, "slotProps={{ inputLabel: { shrink: true } }}");

    if (content !== original) {
      console.log(`Updated ${filePath}`);
      fs.writeFileSync(filePath, content, 'utf8');
    }
  }
});
