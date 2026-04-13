import requests
import json
url = "http://180.235.121.245:8033/case-queue"
response = requests.get(url)
with open("resp5.txt", "w") as f:
    f.write(str(response.status_code))
    f.write("\n")
    f.write(response.text)
