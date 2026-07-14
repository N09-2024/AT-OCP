import sys
import re

file_path = r'c:\Users\pc\.gemini\antigravity-ide\scratch\ocp-at-system\backend\src\main\resources\db\migration\V1__init_schema.sql'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

tables = {}
constraints = {}

for line in lines:
    line = line.strip()
    if not line:
        continue
    
    # Check for CREATE TABLE
    match_create = re.match(r'create table (\w+)', line, re.IGNORECASE)
    if match_create:
        table_name = match_create.group(1).lower()
        tables[table_name] = line
        continue
        
    # Check for ALTER TABLE ADD CONSTRAINT
    match_alter = re.match(r'alter table if exists (\w+) add constraint (\w+)', line, re.IGNORECASE)
    if match_alter:
        table_name = match_alter.group(1).lower()
        constraint_name = match_alter.group(2).lower()
        constraints[f"{table_name}_{constraint_name}"] = line
        continue

with open(file_path, 'w', encoding='utf-8') as f:
    for table_name in sorted(tables.keys()):
        f.write(tables[table_name] + '\n')
    for constraint_key in sorted(constraints.keys()):
        f.write(constraints[constraint_key] + '\n')

print(f"Schema cleaned! Tables: {len(tables)}, Constraints: {len(constraints)}")
