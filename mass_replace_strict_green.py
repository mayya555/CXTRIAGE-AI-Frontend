import os
import re

# We will recursively look through the layout and drawable folders
layout_dir = r"c:\Users\Mallavarapu Mahendra\AndroidStudioProjects\CXTRIAGEAI\app\src\main\res\layout"
drawable_dir = r"c:\Users\Mallavarapu Mahendra\AndroidStudioProjects\CXTRIAGEAI\app\src\main\res\drawable"

# Regex patterns for colors mapping to green and white
patterns_to_replace = [
    # Blues to Greens
    (re.compile(r'#1E62F0', re.IGNORECASE), '#10B981'), # Vivid Blue -> Green
    (re.compile(r'#3B32D1', re.IGNORECASE), '#064E3B'), # Deep Blue -> Dark Green
    (re.compile(r'#1E3A8A', re.IGNORECASE), '#064E3B'), # Dark blue -> Dark green
    (re.compile(r'#2563EB', re.IGNORECASE), '#10B981'), # Blue -> Green
    (re.compile(r'#3B82F6', re.IGNORECASE), '#34D399'), # Light blue -> Light green
    (re.compile(r'#EFF6FF', re.IGNORECASE), '#F0FDF4'), # Soft blue bg -> Soft green bg
    (re.compile(r'#F0F9FF', re.IGNORECASE), '#ECFDF5'), # Soft blue bg 2 -> Soft green bg 2
    (re.compile(r'#DBEAFE', re.IGNORECASE), '#D1FAE5'), # Light blue bg -> Light green bg
    (re.compile(r'#BFDBFE', re.IGNORECASE), '#A7F3D0'), # Light blue border -> Light green border
    (re.compile(r'#60A5FA', re.IGNORECASE), '#6EE7B7'), # Mid blue -> Mid green
    (re.compile(r'#1E40AF', re.IGNORECASE), '#064E3B'), # Navy blue -> Dark Green
    (re.compile(r'#EFF6FF', re.IGNORECASE), '#F0FDF4'), # Light blue bg -> Light green bg

    # Purples & Indigos to Greens
    (re.compile(r'#4F46E5', re.IGNORECASE), '#10B981'), # Indigo -> Green
    (re.compile(r'#8B5CF6', re.IGNORECASE), '#10B981'), # Purple -> Green
    (re.compile(r'#4B3B8B', re.IGNORECASE), '#047857'), # Dark purple -> Dark green
    
    # Reds to Greens (since user wants ALL green and white)
    (re.compile(r'#E11D48', re.IGNORECASE), '#047857'), # Crimson -> Dark Green
    (re.compile(r'#EF4444', re.IGNORECASE), '#059669'), # Red -> Green
    (re.compile(r'#FB7185', re.IGNORECASE), '#34D399'), # Light Red -> Light Green
    (re.compile(r'#FFF1F2', re.IGNORECASE), '#F0FDF4'), # Light Red BG -> Light Green BG
    (re.compile(r'#FEE2E2', re.IGNORECASE), '#D1FAE5'), # Light Red BG 2 -> Light Green BG 2
    
    # Oranges/Yellows to Greens
    (re.compile(r'#D97706', re.IGNORECASE), '#059669'), # Orange -> Green
    (re.compile(r'#F59E0B', re.IGNORECASE), '#10B981'), # Yellow/Orange -> Green
    (re.compile(r'#FBBF24', re.IGNORECASE), '#34D399'), # Yellow -> Light Green
    (re.compile(r'#FFF7ED', re.IGNORECASE), '#F0FDF4'), # Light Orange BG -> Light Green BG
    (re.compile(r'#FEF3C7', re.IGNORECASE), '#ECFDF5'), # Light Yellow BG -> Light Green BG
    
    # Grays to Greens / White
    (re.compile(r'#1E293B', re.IGNORECASE), '#022C22'), # Very Dark Gray -> Very Dark Green
    (re.compile(r'#334155', re.IGNORECASE), '#064E3B'), # Dark Gray -> Dark Green
    (re.compile(r'#475569', re.IGNORECASE), '#065F46'), # Gray -> Green
    (re.compile(r'#64748B', re.IGNORECASE), '#047857'), # Medium Gray -> Green
    (re.compile(r'#94A3B8', re.IGNORECASE), '#10B981'), # Light Gray -> Bright Green
    (re.compile(r'#4B5563', re.IGNORECASE), '#064E3B'), # Card Gray -> Dark Green
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
for directory in [layout_dir, drawable_dir]:
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.xml'):
                process_file(os.path.join(root, file))
                count_files += 1

print(f"Processed {count_files} files.")
