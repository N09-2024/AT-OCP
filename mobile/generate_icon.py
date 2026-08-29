# Génère l'icône placeholder de l'application (1024x1024, PNG RGB).
# Usage : py generate_icon.py
# Version définitive OCP : remplacer ce fichier par le visuel officiel.

import struct
import zlib

W = H = 1024
BG = bytes((0x1F, 0x4D, 0x3E))     # forêt OCP
MINT = bytes((0x7F, 0xC8, 0xA9))   # menthe OCP
WHITE = bytes((0xFF, 0xFF, 0xFF))

CX, CY, R = W // 2, H // 2, 300

# Colonnes du cercle : dx au carré par colonne (précalculé)
dx2 = [(x - CX) ** 2 for x in range(W)]

rows = []
for y in range(H):
    row = bytearray()
    row += b'\x00'  # filtre None par ligne
    dy = y - CY
    dy2 = dy * dy
    in_circle_rows = abs(dy) <= R
    for x in range(W):
        if not in_circle_rows or dx2[x] + dy2 > R * R:
            row += BG
            continue
        adx = abs(x - CX)
        # Bouclier blanc simplifié
        if adx < 150 and -180 < dy < 220 and (dy < 120 or adx < 150 - (dy - 120)):
            row += WHITE
        else:
            row += MINT
    rows.append(bytes(row))

def chunk(tag, data):
    c = tag + data
    return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xFFFFFFFF)

png = b'\x89PNG\r\n\x1a\n'
png += chunk(b'IHDR', struct.pack('>IIBBBBB', W, H, 8, 2, 0, 0, 0))
png += chunk(b'IDAT', zlib.compress(b''.join(rows), 6))
png += chunk(b'IEND', b'')

with open('assets/images/app_icon.png', 'wb') as f:
    f.write(png)
print(f'OK - {len(png)} octets')
