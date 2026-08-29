"""
Générateur d'assets officiels OCP pour l'application mobile Flutter:
- App icon 1024x1024 (assets/images/app_icon.png)
- Splash image 1024x1024 (assets/images/splash.png)
- Logo OCP transparent (assets/images/ocp_logo.png)
- Adaptive foreground 1024x1024 (assets/images/adaptive_foreground.png)
- Mipmap icons pour Android (mdpi 48, hdpi 72, xhdpi 96, xxhdpi 144, xxxhdpi 192)
"""

import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

BG_FOREST = (31, 77, 62)       # #1F4D3E
BG_FOREST_DARK = (14, 42, 33)  # #0E2A21
MINT = (127, 200, 169)         # #7FC8A9
MINT_LIGHT = (226, 240, 232)   # #E2F0E8
WHITE = (255, 255, 255)
GOLD = (200, 160, 60)

os.makedirs('mobile/assets/images', exist_ok=True)
os.makedirs('mobile/assets/icons', exist_ok=True)

# Load official OCP logo if present
ocp_raw = None
if os.path.exists('frontend/public/OCP_Group.svg.webp'):
    ocp_raw = Image.open('frontend/public/OCP_Group.svg.webp').convert('RGBA')
elif os.path.exists('frontend/public/logo-ocp.png'):
    ocp_raw = Image.open('frontend/public/logo-ocp.png').convert('RGBA')

# 1. Generate 1024x1024 App Icon
icon_size = 1024
app_icon = Image.new('RGBA', (icon_size, icon_size), BG_FOREST + (255,))
draw = ImageDraw.Draw(app_icon)

# Draw elegant background radial/gradient subtle styling
for r in range(480, 0, -2):
    alpha = int(18 * (1 - r / 480))
    draw.ellipse((512 - r, 512 - r, 512 + r, 512 + r), fill=(36, 92, 74, alpha))

# Inner circular container with border
container_r = 380
draw.ellipse((512 - container_r, 512 - container_r, 512 + container_r, 512 + container_r),
             fill=BG_FOREST_DARK, outline=MINT, width=6)

# Overlay official OCP logo in center
if ocp_raw:
    # Scale to fit nicely inside circle
    target_w = 460
    aspect = ocp_raw.height / ocp_raw.width
    target_h = int(target_w * aspect)
    logo_resized = ocp_raw.resize((target_w, target_h), Image.Resampling.LANCZOS)
    
    # Position
    pos_x = (icon_size - target_w) // 2
    pos_y = (icon_size - target_h) // 2 - 30
    app_icon.paste(logo_resized, (pos_x, pos_y), logo_resized)

# Add "AT - HSE" badge at bottom
try:
    font = ImageFont.truetype('mobile/assets/fonts/SpaceGrotesk-Bold.ttf', 54)
    text = "AUTORISATIONS DE TRAVAIL"
    bbox = draw.textbbox((0, 0), text, font=font)
    tw = bbox[2] - bbox[0]
    draw.text(((icon_size - tw) // 2, 780), text, fill=MINT, font=font)
except Exception:
    pass

# Save app_icon.png as RGB
app_icon_rgb = Image.new('RGB', (icon_size, icon_size), BG_FOREST)
app_icon_rgb.paste(app_icon, (0, 0), app_icon)
app_icon_rgb.save('mobile/assets/images/app_icon.png', quality=95)
print("Saved mobile/assets/images/app_icon.png")

# 2. Adaptive Foreground (Centered logo on transparent background)
adaptive_fg = Image.new('RGBA', (icon_size, icon_size), (0, 0, 0, 0))
if ocp_raw:
    target_w = 400
    target_h = int(target_w * (ocp_raw.height / ocp_raw.width))
    logo_resized = ocp_raw.resize((target_w, target_h), Image.Resampling.LANCZOS)
    adaptive_fg.paste(logo_resized, ((icon_size - target_w) // 2, (icon_size - target_h) // 2 - 20), logo_resized)
adaptive_fg.save('mobile/assets/images/adaptive_foreground.png')
print("Saved mobile/assets/images/adaptive_foreground.png")

# 3. Splash Screen Image (1024x1024)
splash = Image.new('RGB', (icon_size, icon_size), BG_FOREST)
if ocp_raw:
    target_w = 420
    target_h = int(target_w * (ocp_raw.height / ocp_raw.width))
    logo_resized = ocp_raw.resize((target_w, target_h), Image.Resampling.LANCZOS)
    splash.paste(logo_resized, ((icon_size - target_w) // 2, (icon_size - target_h) // 2 - 50), logo_resized)

draw_splash = ImageDraw.Draw(splash)
try:
    font_sub = ImageFont.truetype('mobile/assets/fonts/SpaceGrotesk-SemiBold.ttf', 38)
    sub = "Système HSE des Autorisations de Travail"
    bbox = draw_splash.textbbox((0, 0), sub, font=font_sub)
    tw = bbox[2] - bbox[0]
    draw_splash.text(((icon_size - tw) // 2, 750), sub, fill=MINT, font=font_sub)
except Exception:
    pass
splash.save('mobile/assets/images/splash.png', quality=95)
print("Saved mobile/assets/images/splash.png")

# 4. Transparent OCP Logo
if ocp_raw:
    ocp_raw.save('mobile/assets/images/ocp_logo.png')
    print("Saved mobile/assets/images/ocp_logo.png")

# 5. Generate Android Mipmap Icons
mipmap_sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192,
}

for folder, sz in mipmap_sizes.items():
    res_dir = os.path.join('mobile/android/app/src/main/res', folder)
    os.makedirs(res_dir, exist_ok=True)
    
    # Standard launcher icon
    scaled_icon = app_icon_rgb.resize((sz, sz), Image.Resampling.LANCZOS)
    scaled_icon.save(os.path.join(res_dir, 'ic_launcher.png'))
    
    # Round icon
    mask = Image.new('L', (sz, sz), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.ellipse((0, 0, sz, sz), fill=255)
    round_icon = Image.new('RGBA', (sz, sz), (0, 0, 0, 0))
    round_icon.paste(scaled_icon, (0, 0))
    round_icon.putalpha(mask)
    round_icon.save(os.path.join(res_dir, 'ic_launcher_round.png'))
    
    # Adaptive foreground & background (108dp base: mdpi=108, hdpi=162, xhdpi=216, xxhdpi=324, xxxhdpi=432)
    adaptive_sz = int(sz * (108.0 / 48.0))
    scaled_fg = adaptive_fg.resize((adaptive_sz, adaptive_sz), Image.Resampling.LANCZOS)
    scaled_fg.save(os.path.join(res_dir, 'ic_launcher_foreground.png'))
    
    bg_img = Image.new('RGB', (adaptive_sz, adaptive_sz), BG_FOREST)
    bg_img.save(os.path.join(res_dir, 'ic_launcher_background.png'))

print("All Android mipmap icons generated successfully.")
