import re
import sys

def extract_table_name(statement):
    # Extract table name from a CREATE TABLE statement
    # Assuming format: CREATE TABLE table_name ( ... );
    match = re.search(r'create\s+table\s+(\w+)', statement, re.IGNORECASE)
    if match:
        return match.group(1).lower()  # normalize to lower case
    return None

def main():
    if len(sys.argv) != 3:
        print("Usage: python fix_flyway.py <input_file> <output_file>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find all') as f:
        content = f.read()

    # Find all CREATE TABLE statements
    # We use a regex that matches from CREATE TABLE to the next semicolon (non-greedy) across lines
    pattern = re.compile(r'create\s+table\s+.*?;', re.DOTALL | re.IGNORECASE)
    matches = list(pattern.finditer(content))

    # We'll store the last occurrence (by start index) for each table name
    last_occurrence = {}  # table_name -> (start, end)

    for match in matches:
        stmt = match.group(0)
        table_name = extract_table_name(stmt)
        if table_name:
            # We update the last occurrence for this table to be this match
            last_occurrence[table_name] = (match.start(), match.end())

    # Now, we want to remove all CREATE TABLE statements except the last one for each table.
    # We'll create a list of intervals to remove.
    intervals_to_remove = []
    for match in matches:
        stmt = match.group(0)
        table_name = extract_table_name(stmt)
        if table_name and table_name in last_occurrence:
            # If this match is not the last occurrence for this table, mark it for removal
            start, end = match.start(), match.end()
            last_start, last_end = last_occurrence[table_name]
            if not (start == last_start and end == last_end):
                intervals_to_remove.append((start, end))

    # Sort intervals by start index
    intervals_to_remove.sort(key=lambda x: x[0])

    # Now, build the new string by skipping the intervals to remove
    if not intervals_to_remove:
        new_content = content
    else:
        parts = []
        prev_end = 0
        for start, end in intervals_to_remove:
            parts.append(content[prev_end:start])
            prev_end = end
        parts.append(content[prev_end:])
        new_content = ''.join(parts)

    # Write the new content
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(new_content)

    print(f"Processed {len(matches)} CREATE TABLE statements.")
    print(f"Kept {len(last_occurrence)} unique tables.")
    print(f"Removed {len(intervals_to_remove)} duplicate table definitions.")
    print(f"Output written to {output_file}")

if __name__ == '__main__':
    main()