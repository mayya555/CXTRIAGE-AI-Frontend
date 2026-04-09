import requests

# Test /doctor/login with hospital_email (as Android sends)
url = "http://10.136.52.10:8000/doctor/login"
# Use the registered doctor email - update this if different
payload = {"hospital_email": "jaga@gmail.com", "password": "Jaga@1234"}
res = requests.post(url, json=payload)
with open("login_result2.txt", "w") as f:
    f.write(f"Status: {res.status_code}\n")
    f.write(f"Body: {res.text}\n")
