
from main import SessionLocal, Doctor, DoctorLogin, login_doctor
import json

db = SessionLocal()
# Create a test doctor if not exists
test_email = 'test_debug@cxtriage.ai'
user = db.query(Doctor).filter(Doctor.hospital_email == test_email).first()
if not user:
    user = Doctor(
        first_name="Test",
        last_name="Doctor",
        hospital_email=test_email,
        phone_number="0000000000",
        role_requested="Doctor",
        password="Password123"
    )
    db.add(user)
    db.commit()
    db.refresh(user)

print(f"User ID in DB: {user.id}")

# Simulate login
data = DoctorLogin(hospital_email=test_email, password="Password123")
response = login_doctor(data, db)
print("Response JSON:")
print(json.dumps(response, indent=2))
db.close()
