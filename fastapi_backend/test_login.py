import requests

# Test if /doctor/login returns doctor_id correctly
# Replace email/password with a real registered doctor account
url = "http://180.235.121.245:8033/doctor/login"
payload = {"email": "test@doctor.com", "password": "Test@123"}
res = requests.post(url, json=payload)
with open("login_result.txt", "w") as f:
    f.write(f"Status: {res.status_code}\n")
    f.write(f"Body: {res.text}\n")
