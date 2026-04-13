import requests
try:
    response = requests.get("http://180.235.121.245:8033/test-reload")
    with open("resp3.txt", "w") as f:
        f.write(str(response.status_code))
        f.write("\n")
        f.write(response.text)
except Exception as e:
    print(str(e))
