from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
import models
import schemas
from database import engine, SessionLocal

models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="Doctor Registration API")

# Database Dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.post("/register", response_model=schemas.DoctorResponse)
def register_doctor(doctor: schemas.DoctorCreate, db: Session = Depends(get_db)):

    if doctor.password != doctor.confirm_password:
        raise HTTPException(status_code=400, detail="Passwords do not match")

    existing_email = db.query(models.Doctor).filter(
        models.Doctor.hospital_email == doctor.hospital_email
    ).first()

    if existing_email:
        raise HTTPException(status_code=400, detail="Email already registered")

    existing_emp = db.query(models.Doctor).filter(
        models.Doctor.employee_id == doctor.employee_id
    ).first()

    if existing_emp:
        raise HTTPException(status_code=400, detail="Employee ID already registered")

    new_doctor = models.Doctor(
        first_name=doctor.first_name,
        last_name=doctor.last_name,
        hospital_email=doctor.hospital_email,
        employee_id=doctor.employee_id,
        role_requested=doctor.role_requested,
        password=doctor.password  # Stored directly (not secure)
    )

    db.add(new_doctor)
    db.commit()
    db.refresh(new_doctor)

    return new_doctor

@app.post("/login", response_model=schemas.LoginResponse)
def login_doctor(request: schemas.LoginRequest, db: Session = Depends(get_db)):
    # Check if username is email or employee ID
    doctor = db.query(models.Doctor).filter(
        (models.Doctor.hospital_email == request.username) | 
        (models.Doctor.employee_id == request.username)
    ).first()

    if not doctor or doctor.password != request.password:
        raise HTTPException(status_code=401, detail="Invalid credentials")

    return {
        "message": "Login successful",
        "doctor": doctor
    }
