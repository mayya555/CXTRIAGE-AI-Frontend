import os
import re

# We will recursively look through the drawable folder
layout_dir = r"c:\Users\Mallavarapu Mahendra\AndroidStudioProjects\CXTRIAGEAI\app\src\main\res\drawable"

# Regex patterns for colors that were "blue" or system variants thereof.
patterns_to_replace = [
    (re.compile(r'#2563EB', re.IGNORECASE), '#10B981'),  # The hardcoded blue
    (re.compile(r'#3B82F6', re.IGNORECASE), '#34D399'),  # Light blue hardcoded
]

def process_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()
    
    modified_content = content
    changed = False
    
    for pattern, replacement in patterns_to_replace:
        if pattern.search(modified_content):
            modified_content = pattern.sub(replacement, modified_content)
            changed = True
            
    if changed:
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(modified_content)
        print(f"Updated {os.path.basename(file_path)}")

count_files = 0
for root, dirs, files in os.walk(layout_dir):
    for file in files:
        if file.endswith('.xml'):
            process_file(os.path.join(root, file))
            count_files += 1

print(f"Processed {count_files} files.")
