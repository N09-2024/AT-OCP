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
        print("Usage: python fix_flyway2.py <input_file> <output_file>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # We'll split the content by 'CREATE TABLE' (case-insensitive) but keep the delimiter
    # Using a regex to split and keep the delimiter
    parts = re.split(r'(create\s+table)', content, flags=re.IGNORECASE)
    # The first part is everything before the first CREATE TABLE
    # Then we have pairs: delimiter, statement_body (until next delimiter or end)
    # We need to group them: for i in range(1, len(parts), 2): delimiter = parts[i], statement_body = parts[i+1] (if exists)
    # Actually, after split, we have: [text_before, 'CREATE TABLE', rest1, 'CREATE TABLE', rest2, ...]
    # So we can iterate over the parts in steps of 2 starting at index 1.

    # We'll collect all CREATE TABLE statements with their start and end positions? 
    # Instead, let's reconstruct by iterating and keeping track of the last statement for each table.

    # We'll build a new list of parts for the output.
    output_parts = []
    # Add the text before the first CREATE TABLE
    output_parts.append(parts[0])  # everything before the first match

    # Dictionary to store the last statement for each table
    last_statement = {}  # table_name -> (delimiter, statement_body)
    # We'll also store the index of the delimiter in the parts list for each table? Not needed.

    # Iterate over the parts in pairs: delimiter and statement_body
    i = 1
    while i < len(parts):
        delimiter = parts[i]  # this is 'CREATE TABLE' (with original case)
        statement_body = parts[i+1] if i+1 < len(parts) else ''
        statement = delimiter + statement_body
        table_name = extract_table_name(statement)
        if table_name:
            # Store this statement as the last one for this table
            last_statement[table_name] = (delimiter, statement_body)
        # We don't add anything to output yet; we'll add only the last ones at the end.
        i += 2

    # Now, we need to output the parts up to the first CREATE TABLE, then for each table in the order of last appearance?
    # But we lost the order. To preserve order, we can go through the parts again and output only the last occurrence.
    # Let's do a second pass: we'll go through the parts again and output a statement only if it is the last one for its table.

    # Reset i to 1
    i = 1
    while i < len(parts):
        delimiter = parts[i]
        statement_body = parts[i+1] if i+1 < len(parts) else ''
        statement = delimiter + statement_body
        table_name = extract_table_name(statement)
        if table_name:
            # Check if this is the last statement for this table
            if table_name in last_statement:
                last_delim, last_body = last_statement[table_name]
                # Compare the current delimiter and body with the stored one (they should be identical if it's the last occurrence)
                # Since we stored the last one we saw, we can just check if the current position is the one we stored?
                # Actually, we stored the last occurrence as we iterated, so the current one is the last if it matches the stored.
                # But we stored the delimiter and body, so we can compare.
                if delimiter == last_delim and statement_body == last_body:
                    # This is the last occurrence, output it
                    output_parts.append(delimiter)
                    output_parts.append(statement_body)
                # else: skip (this is an earlier duplicate)
            # else: should not happen
        else:
            # Not a CREATE TABLE statement? Should not happen in this loop because we split by 'CREATE TABLE'
            # But just in case, output the delimiter and body as is.
            output_parts.append(delimiter)
            output_parts.append(statement_body)
        i += 2

    # Join the output parts
    new_content = ''.join(output_parts)

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(new_content)

    print(f"Processed file: {input_file}")
    print(f"Written to: {output_file}")

if __name__ == '__main__':
    main()