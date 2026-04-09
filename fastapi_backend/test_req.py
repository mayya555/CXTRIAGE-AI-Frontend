import requests

url = "http://127.0.0.1:8000/case-queue"
response = requests.get(url)
print(f"Status Code: {response.status_code}")
print(f"Response Body: {response.text}")

url_with_params = "http://127.0.0.1:8000/case-queue?doctor_id=11"
response2 = requests.get(url_with_params)
print(f"With params - Status Code: {response2.status_code}")
print(f"With params - Response Body: {response2.text}")
