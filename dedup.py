import sys

file_path = r'c:\Users\pc\.gemini\antigravity-ide\scratch\ocp-at-system\backend\src\main\resources\db\migration\V1__init_schema.sql'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

unique_lines = []
seen = set()

for line in lines:
    clean_line = line.strip()
    if clean_line and clean_line not in seen:
        seen.add(clean_line)
        unique_lines.append(line)

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(unique_lines)

print(f"Deduplicated. Original lines: {len(lines)}, New lines: {len(unique_lines)}")
