import os

def replace_in_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    replacements = {
        'bg_button_blue': 'bg_button_green',
        'bg_button_rounded_blue': 'bg_button_rounded_green',
        'bg_button_outline_blue': 'bg_button_outline_green',
        'bg_circle_blue_light': 'bg_circle_green_light',
        'bg_badge_light_blue': 'bg_badge_light_green',
        'bg_button_pill_blue': 'bg_button_pill_green',
        'ic_gallery_blue': 'ic_gallery', # Assuming a generic ic_gallery exists or will be tinted
        'vibrant_blue': 'brand_green',
        'purple_primary': 'brand_green',
        '#E0E9F9': '#F0FDF4', # Light blue to light green hex
        '#2563EB': '#10B981', # Vibrant blue to brand green hex
        '#F5F3FF': '#F0FDF4', # Soft purple to light green hex
        '#FEE2E2': '#DCFCE7', # Light red to light green (if used for non-error backgrounds)
    }

    modified = False
    for old, new in replacements.items():
        if old in content:
            content = content.replace(old, new)
            modified = True

    if modified:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated: {file_path}")

layout_dir = r"c:\Users\Mallavarapu Mahendra\AndroidStudioProjects\CXTRIAGEAI\app\src\main\res\layout"
for filename in os.listdir(layout_dir):
    if filename.endswith(".xml"):
        replace_in_file(os.path.join(layout_dir, filename))
