import requests
import json
url = "http://180.235.121.245:8033/case-queue"
response = requests.get(url)
with open("resp1.json", "w") as f:
    f.write(str(response.status_code) + "\n")
    f.write(response.text)

url2 = "http://180.235.121.245:8033/case-queue?doctor_id=11"
response2 = requests.get(url2)
with open("resp2.json", "w") as f:
    f.write(str(response2.status_code) + "\n")
    f.write(response2.text)
