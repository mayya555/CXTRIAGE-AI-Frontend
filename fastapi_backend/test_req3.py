import requests
try:
    response = requests.get("http://127.0.0.1:8000/test-reload")
    with open("resp3.txt", "w") as f:
        f.write(str(response.status_code))
        f.write("\n")
        f.write(response.text)
except Exception as e:
    print(str(e))
