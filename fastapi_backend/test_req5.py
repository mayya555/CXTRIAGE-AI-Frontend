import requests
import json
url = "http://127.0.0.1:8000/case-queue"
response = requests.get(url)
with open("resp5.txt", "w") as f:
    f.write(str(response.status_code))
    f.write("\n")
    f.write(response.text)
