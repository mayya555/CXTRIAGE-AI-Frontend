import requests
try:
    response = requests.get("http://10.136.52.10:8000/test-reload")
    with open("resp4.txt", "w") as f:
        f.write(str(response.status_code))
        f.write("\n")
        f.write(response.text)
except Exception as e:
    print(str(e))
