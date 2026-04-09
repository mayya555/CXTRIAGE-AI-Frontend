
import os

search_str = b"saitejaswi71@gmail.com"
found = False

for root, dirs, files in os.walk("."):
    for file in files:
        path = os.path.join(root, file)
        try:
            with open(path, "rb") as f:
                if search_str in f.read():
                    print(f"FOUND IN: {path}")
                    found = True
        except Exception:
            pass

if not found:
    print("NOT FOUND ANYWHERE")
