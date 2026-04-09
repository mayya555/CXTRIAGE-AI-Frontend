from fastapi import FastAPI, Depends, HTTPException, UploadFile, File
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, EmailStr, validator
from typing import List, Optional
import random
import datetime
import re
from enum import Enum
import io
import numpy as np
import tensorflow as tf
from PIL import Image

# SQLAlchemy Imports
from sqlalchemy import create_engine, Column, Integer, String, DateTime, Enum as SQLEnum, ForeignKey, Boolean
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session

# --- DATABASE SETUP ---
SQLALCHEMY_DATABASE_URL = "sqlite:///./sql_app.db"
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Dependency to get DB session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# --- MODELS ---

class StatusEnum(str, Enum):
    PENDING = "pending"
    COMPLETED = "completed"
    CANCELLED = "cancelled"
    REVIEWED = "reviewed"

class Doctor(Base):
    __tablename__ = "doctors"
    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String)
    last_name = Column(String)
    hospital_email = Column(String, unique=True, index=True)
    phone_number = Column(String, unique=True, index=True)
    role_requested = Column(String)
    password = Column(String)

class Technician(Base):
    __tablename__ = "technicians"
    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String)
    last_name = Column(String)
    email = Column(String, unique=True, index=True)
    phone_number = Column(String, unique=True, index=True)
    role_requested = Column(String)
    password = Column(String)
    profile_photo_url = Column(String, nullable=True)

class Patient(Base):
    __tablename__ = "patients"
    id = Column(Integer, primary_key=True, index=True)
    full_name = Column(String)
    mrn = Column(String, unique=True, index=True)
    date_of_birth = Column(String)
    gender = Column(String)
    reason_for_xray = Column(String)
    height = Column(String, nullable=True)
    weight = Column(String, nullable=True)
    blood_type = Column(String, nullable=True)

class TriageCase(Base):
    __tablename__ = "triage_cases"
    id = Column(Integer, primary_key=True, index=True)
    case_code = Column(String, unique=True, index=True)
    
    patient_id = Column(Integer, ForeignKey("patients.id"))
    technician_id = Column(Integer, ForeignKey("technicians.id"))
    doctor_id = Column(Integer, ForeignKey("doctors.id"), nullable=True)

    patient_name = Column(String)
    mrn = Column(String)
    priority = Column(String, default="ROUTINE") # CRITICAL, URGENT, ROUTINE
    ai_result = Column(String) # Normal, Pneumonia, etc.
    ai_confidence = Column(String) # e.g. "98.4%"
    ai_findings = Column(String) # More detailed string
    image_url = Column(String)
    doctor_notes = Column(String)
    impression = Column(String, nullable=True)
    recommendation = Column(String, nullable=True)
    status = Column(SQLEnum(StatusEnum), default=StatusEnum.PENDING)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

# Create tables
Base.metadata.create_all(bind=engine)

# --- SCHEMAS ---

class DoctorCreate(BaseModel):
    first_name: str
    last_name: str
    hospital_email: EmailStr
    phone_number: str
    role_requested: str
    password: str
    confirm_password: str

    @validator("first_name", "last_name")
    def name_not_empty(cls, v):
        if not v.strip():
            raise ValueError("Name cannot be empty")
        return v

    @validator("phone_number")
    def phone_validation(cls, v):
        if not re.fullmatch(r"\d{10}", v):
            raise ValueError("Phone number must be 10 digits")
        return v

    @validator("password")
    def password_strength(cls, v):
        if len(v) < 6:
            raise ValueError("Password must be at least 6 characters")
        if not re.search(r"[A-Z]", v):
            raise ValueError("Password must contain uppercase letter")
        if not re.search(r"[a-z]", v):
            raise ValueError("Password must contain lowercase letter")
        if not re.search(r"\d", v):
            raise ValueError("Password must contain number")
        return v

class TechnicianCreate(BaseModel):
    first_name: str
    last_name: str
    email: EmailStr
    phone_number: str
    role_requested: str
    password: str
    confirm_password: str

    @validator("first_name", "last_name")
    def name_not_empty(cls, v):
        if not v.strip():
            raise ValueError("Name cannot be empty")
        return v

    @validator("phone_number")
    def phone_validation(cls, v):
        if not re.fullmatch(r"\d{10}", v):
            raise ValueError("Phone number must be 10 digits")
        return v

    @validator("password")
    def password_strength(cls, v):
        if len(v) < 6:
            raise ValueError("Password must be at least 6 characters")
        if not re.search(r"[A-Z]", v):
            raise ValueError("Password must contain uppercase letter")
        if not re.search(r"[a-z]", v):
            raise ValueError("Password must contain lowercase letter")
        if not re.search(r"\d", v):
            raise ValueError("Password must contain number")
        return v

class DoctorLogin(BaseModel):
    hospital_email: str
    password: str

class TechnicianLoginRequest(BaseModel):
    email: str
    password: str

class LoginResponse(BaseModel):
    message: str
    token: Optional[str] = None
    technician_id: Optional[int] = None
    doctor_id: Optional[int] = None
    name: Optional[str] = None
    full_name: Optional[str] = None
    email: Optional[str] = None
    profile_photo_url: Optional[str] = None

class CreatePatientRequest(BaseModel):
    full_name: str
    mrn: str
    date_of_birth: str
    gender: str
    reason_for_xray: Optional[str] = ""
    height: Optional[str] = None
    weight: Optional[str] = None
    blood_type: Optional[str] = None
    technician_id: Optional[int] = None

class CreatePatientResponse(BaseModel):
    message: str
    patient_id: int

class ScanHistoryItem(BaseModel):
    id: str
    patient_name: Optional[str] = "Unknown"
    mrn: Optional[str] = "MRN-0000"
    status: str
    date: str
    technician_id: int

    status: str
    disease: Optional[str] = "Normal"
    confidence: Optional[str] = "98.4%"

class TriageCaseResponse(BaseModel):
    case_id: int
    case_code: Optional[str]
    patient_name: Optional[str]
    patient_age: Optional[int] = 35
    diagnosis: Optional[str] = None
    priority: Optional[str]
    image_url: Optional[str]
    ai_findings: Optional[str]
    ai_result: Optional[str]
    ai_confidence: Optional[str] = "98.4%"
    final_diagnosis: Optional[str] = None
    doctor_notes: Optional[str] = None
    decision: Optional[str] = None
    status: Optional[str]
    created_at: Optional[str]
    
    # Patient metadata
    date_of_birth: Optional[str] = None
    gender: Optional[str] = None
    mrn: Optional[str] = None
    height: Optional[str] = None
    weight: Optional[str] = None
    blood_type: Optional[str] = None

class TriageDashboardResponse(BaseModel):
    totalCases: int
    criticalCases: int
    urgentCases: int
    pendingCases: int

class UploadScanResponse(BaseModel):
    message: str
    case_id: int
    disease: Optional[str] = None
    confidence: Optional[str] = None
    priority: Optional[str] = None

class ApiResponse(BaseModel):
    message: str
    status: Optional[str] = None

class AiChatRequest(BaseModel):
    message: str

class AiChatResponse(BaseModel):
    response: str

class StartScanResponse(BaseModel):
    scan_id: int
    scan_code: str
    status: str

class ScanPreparationRequest(BaseModel):
    position_patient: bool
    proper_distance: bool
    radiation_safety: bool
    remove_metal: bool
    calibration_verified: bool
    exposure_settings: bool

class ScanPreparationResponse(BaseModel):
    message: str
    scan_id: int

class CreateStudyResponse(BaseModel):
    message: str
    study_id: Optional[int] = None

class DistributeStudyResponse(BaseModel):
    message: str
    status: str

class ReportPreviewResponse(BaseModel):
    patient: Optional[str]
    findings: Optional[str]
    impression: Optional[str]
    signedBy: Optional[str] = None
    finalized: Optional[bool] = False
    reportId: Optional[str] = None

class DoctorResponse(BaseModel):
    doctor_id: int
    name: str
    specialization: Optional[str] = None

class SignRequest(BaseModel):
    doctor_id: int
    doctor_name: str

class CaseActionResponse(BaseModel):
    message: str
    status: str

class EditNotesRequest(BaseModel):
    doctor_id: int
    doctor_notes: str

class FinalizeRequest(BaseModel):
    doctor_id: int
    doctor_name: str
    impression: str
    recommendation: str
    notes: Optional[str] = None

class ScanDetailResponse(BaseModel):
    scan_id: str
    patient_name: str
    mrn: str
    date_of_birth: str
    gender: str
    scan_date: str
    technician: str
    view_type: str
    orientation: str
    study_id: str
    status: str
    disease: Optional[str] = None
    confidence: Optional[str] = None


    total_count: int

class TechnicianProfileResponse(BaseModel):
    id: int
    full_name: str
    email: str
    phone_number: Optional[str] = None
    employee_id: Optional[str] = None
    role: Optional[str] = "Technician"
    profile_photo_url: Optional[str] = None

class UpdateTechnicianProfileRequest(BaseModel):
    first_name: str
    last_name: str
    phone_number: str
    email: str

app = FastAPI(title="CXTRIAGE AI Backend")

@app.get("/test-reload")
def test_reload():
    return {"status": "reloaded"}

app.mount("/static", StaticFiles(directory="fastapi_backend/static"), name="static")

# Load AI Model (Pneumonia Detection)
MODEL_PATH = "fastapi_backend/model.h5"
try:
    model = tf.keras.models.load_model(MODEL_PATH)
    print(f"AI Model loaded successfully from {MODEL_PATH}")
except Exception as e:
    model = None
    print(f"Error loading AI model: {e}")

def preprocess_image(image_bytes):
    """
    Standardize image for model input: Resize -> RGB -> Normalize -> Batch Dim
    Model expects (1, 224, 224, 3)
    """
    img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    img = img.resize((224, 224))
    img_array = np.array(img) / 255.0  # Normalize to [0, 1]
    img_array = np.expand_dims(img_array, axis=0)  # Add batch dimension
    return img_array

# --- AUTH API ---

@app.post("/register")
def register_doctor(data: DoctorCreate, db: Session = Depends(get_db)):

    if data.password != data.confirm_password:
        raise HTTPException(status_code=400, detail="Passwords do not match")

    existing_email = db.query(Doctor).filter(
        Doctor.hospital_email == data.hospital_email
    ).first()

    if existing_email:
        raise HTTPException(status_code=400, detail="Email already registered")

    existing_phone = db.query(Doctor).filter(
        Doctor.phone_number == data.phone_number
    ).first()

    if existing_phone:
        raise HTTPException(status_code=400, detail="Phone number already registered")

    new_doctor = Doctor(
        first_name=data.first_name.strip(),
        last_name=data.last_name.strip(),
        hospital_email=data.hospital_email.lower(),
        phone_number=data.phone_number,
        role_requested=data.role_requested,
        password=data.password
    )

    db.add(new_doctor)
    db.commit()
    db.refresh(new_doctor)

    return {"message": "Doctor registered successfully", "id": new_doctor.id}

@app.post("/technician/register")
def register_technician(data: TechnicianCreate, db: Session = Depends(get_db)):

    if data.password != data.confirm_password:
        raise HTTPException(status_code=400, detail="Passwords do not match")

    existing_email = db.query(Technician).filter(
        Technician.email == data.email
    ).first()

    if existing_email:
        raise HTTPException(status_code=400, detail="Email already registered")

    existing_phone = db.query(Technician).filter(
        Technician.phone_number == data.phoneNumber
    ).first()

    if existing_phone:
        raise HTTPException(status_code=400, detail="Phone number already registered")

    new_tech = Technician(
        first_name=data.first_name.strip(),
        last_name=data.last_name.strip(),
        email=data.email.lower(),
        phone_number=data.phone_number,
        role_requested=data.role_requested,
        password=data.password
    )

    db.add(new_tech)
    db.commit()
    db.refresh(new_tech)

    return {"message": "Technician registered successfully", "id": new_tech.id}

@app.post("/login")
@app.post("/doctor/login")
def login_doctor(data: DoctorLogin, db: Session = Depends(get_db)):

    if not data.hospital_email or not data.password:
        raise HTTPException(status_code=400, detail="Email and password required")

    user = db.query(Doctor).filter(
        (Doctor.hospital_email == data.hospital_email) |
        (Doctor.phone_number == data.hospital_email)
    ).first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    if user.password != data.password:
        raise HTTPException(status_code=401, detail="Incorrect password")

    res = {
        "message": "Login successful",
        "doctor_id": user.id,
        "name": f"{user.first_name} {user.last_name}"
    }
    print(f"DEBUG: login_doctor response: {res}")
    return res

@app.post("/technician/login")
def login_technician(data: TechnicianLoginRequest, db: Session = Depends(get_db)):

    if not data.email or not data.password:
        raise HTTPException(status_code=400, detail="Email and password required")

    # For technician, we'll follow the same pattern (email or phone)
    user = db.query(Technician).filter(
        (Technician.email == data.email) |
        (Technician.phone_number == data.email)
    ).first()

    if not user:
        raise HTTPException(status_code=404, detail="Technician not found")

    if user.password != data.password:
        raise HTTPException(status_code=401, detail="Incorrect password")

    res = {
        "message": "Login successful",
        "technician_id": user.id,
        "full_name": f"{user.first_name} {user.last_name}",
        "email": user.email,
        "token": "mock-jwt-token-123"
    }
    print(f"DEBUG: login_technician response: {res}")
    return res

# --- CASE QUEUE API ---

@app.get("/case-queue")
def case_queue(priority: Optional[str] = None, doctor_id: Optional[int] = None, db: Session = Depends(get_db)):
    query = db.query(TriageCase).filter(TriageCase.status == StatusEnum.PENDING)
    
    if doctor_id:
        query = query.filter(TriageCase.doctor_id == doctor_id)
        
    if priority:
        query = query.filter(TriageCase.priority == priority.upper())
    
    cases = query.all()
    
    # Sort in memory since order by on string might be alphabetical instead of logical
    priority_map = {"CRITICAL": 0, "URGENT": 1, "ROUTINE": 2, "NORMAL": 2}
    
    def get_sort_key(case_obj):
        prio_val = priority_map.get(case_obj.priority, 3)
        created_val = case_obj.created_at if case_obj.created_at else datetime.datetime.max
        return (prio_val, -created_val.timestamp())
        
    cases.sort(key=get_sort_key)
    
    # Map to schema with case_id
    result = []
    for c in cases:
        result.append({
            "case_id": c.id,
            "case_code": c.case_code,
            "patient_name": c.patient_name,
            "priority": c.priority,
            "ai_result": c.ai_result,
            "ai_confidence": c.ai_confidence,
            "image_url": c.image_url,
            "status": c.status,
            "created_at": c.created_at.strftime("%Y-%m-%dT%H:%M:%S") if c.created_at else None
        })
    return result

@app.get("/critical-alerts")
def critical_alerts(priority: Optional[str] = None, doctor_id: Optional[int] = None, db: Session = Depends(get_db)):
    # Fetch alerts that are Critical and Pending
    query = db.query(TriageCase).filter(
        TriageCase.priority == "CRITICAL",
        TriageCase.status == StatusEnum.PENDING
    )
    
    if doctor_id:
        query = query.filter(TriageCase.doctor_id == doctor_id)
        
    alerts = query.order_by(TriageCase.created_at.desc()).all()
    
    # Map to AlertResponse format expected by Android
    result = []
    for alert in alerts:
        result.append({
            "case_id": alert.id,
            "case_code": alert.case_code,
            "patient_name": alert.patient_name,
            "priority": alert.priority,
            "ai_result": alert.ai_result,
            "image_url": alert.image_url,
            "created_at": alert.created_at.strftime("%I:%M %p")
        })
    return result

@app.get("/triage-dashboard")
def get_triage_dashboard(doctor_id: int, db: Session = Depends(get_db)):
    total = db.query(TriageCase).filter(TriageCase.doctor_id == doctor_id).count()
    pending = db.query(TriageCase).filter(TriageCase.doctor_id == doctor_id, TriageCase.status == StatusEnum.PENDING).count()
    completed = db.query(TriageCase).filter(TriageCase.doctor_id == doctor_id, TriageCase.status == StatusEnum.COMPLETED).count()
    critical = db.query(TriageCase).filter(TriageCase.doctor_id == doctor_id, TriageCase.priority == "CRITICAL", TriageCase.status == StatusEnum.PENDING).count()
    urgent = db.query(TriageCase).filter(TriageCase.doctor_id == doctor_id, TriageCase.priority == "URGENT", TriageCase.status == StatusEnum.PENDING).count()
    
    return {
        "total_cases": total,
        "pending_cases": pending,
        "completed_cases": completed,
        "critical_cases": critical,
        "urgent_cases": urgent
    }

@app.get("/case-history")
def get_case_history(doctor_id: Optional[int] = None, db: Session = Depends(get_db)):
    query = db.query(TriageCase)
    if doctor_id:
        query = query.filter(TriageCase.doctor_id == doctor_id)
    cases = query.order_by(TriageCase.created_at.desc()).all()
    result = []
    for c in cases:
        result.append({
            "case_id": c.id,
            "case_code": c.case_code,
            "patient_name": c.patient_name,
            "priority": c.priority,
            "ai_result": c.ai_result,
            "status": c.status,
            "created_at": c.created_at.strftime("%Y-%m-%dT%H:%M:%S") if c.created_at else None
        })
    return result

# --- OTHER ENDPOINTS ---

@app.post("/technician/register-patient", response_model=CreatePatientResponse)
async def register_patient(request: CreatePatientRequest, db: Session = Depends(get_db)):
    existing_patient = db.query(Patient).filter(Patient.mrn == request.mrn).first()
    if existing_patient:
        return {"message": "Patient already registered", "patient_id": existing_patient.id}
    
    new_patient = Patient(
        full_name=request.full_name,
        mrn=request.mrn,
        date_of_birth=request.date_of_birth,
        gender=request.gender,
        reason_for_xray=request.reason_for_xray,
        height=request.height,
        weight=request.weight,
        blood_type=request.blood_type
    )
    db.add(new_patient)
    db.commit()
    db.refresh(new_patient)
    
    return {
        "message": "Patient registered successfully",
        "patient_id": new_patient.id
    }

@app.get("/technician/dashboard-stats/{technician_id}", response_model=TechnicianDashboardStats)
async def get_technician_stats(technician_id: int, db: Session = Depends(get_db)):
    # 1. Total Scans for this technician
    total_count = db.query(TriageCase).filter(TriageCase.technician_id == technician_id).count()

    # 2. Pending Scans (status='pending')
    pending_count = db.query(TriageCase).filter(
        TriageCase.technician_id == technician_id,
        TriageCase.status == StatusEnum.PENDING
    ).count()

    # 3. Today's Scans
    # SQLite-compatible way to get date or just filter created_at
    today_start = datetime.datetime.utcnow().replace(hour=0, minute=0, second=0, microsecond=0)
    today_count = db.query(TriageCase).filter(
        TriageCase.technician_id == technician_id,
        TriageCase.created_at >= today_start
    ).count()

    return {
        "today_count": today_count,
        "pending_count": pending_count,
        "total_count": total_count
    }

@app.get("/technician/profile/{technician_id}", response_model=TechnicianProfileResponse)
def get_technician_profile(technician_id: int, db: Session = Depends(get_db)):
    user = db.query(Technician).filter(Technician.id == technician_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Technician not found")
    
    return {
        "id": user.id,
        "full_name": f"{user.first_name} {user.last_name}",
        "email": user.email,
        "phone_number": user.phone_number,
        "employee_id": f"TECH-{user.id:04d}",
        "role": user.role_requested,
        "profile_photo_url": user.profile_photo_url
    }

@app.put("/technician/update-profile")
def update_technician_profile(data: UpdateTechnicianProfileRequest, db: Session = Depends(get_db)):
    user = db.query(Technician).filter(Technician.email == data.email).first()
    if not user:
        raise HTTPException(status_code=404, detail="Technician not found")
    
    user.first_name = data.first_name
    user.last_name = data.lastName
    user.phone_number = data.phone_number
    db.commit()
    return {"message": "Profile updated successfully"}

@app.post("/technician/upload-profile-photo/{technician_id}")
async def upload_technician_photo(technician_id: int, file: UploadFile = File(...), db: Session = Depends(get_db)):
    user = db.query(Technician).filter(Technician.id == technician_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Technician not found")
    
    # Save file locally for simulation
    file_location = f"fastapi_backend/static/profile_{technician_id}.jpg"
    import os
    os.makedirs("fastapi_backend/static", exist_ok=True)
    with open(file_location, "wb+") as file_object:
        file_object.write(await file.read())
    
    # Update DB with URL (assuming the server serves the static folder)
    user.profile_photo_url = f"profile_{technician_id}.jpg"
    db.commit()
    
    return {"message": "Photo uploaded successfully", "profile_photo_url": user.profile_photo_url}

@app.get("/technician/scans/{technician_id}", response_model=List[ScanHistoryItem])
async def get_technician_scans(technician_id: int):
    return [
        {"id": "SCN-8472", "patientName": "Sarah Wilson", "mrn": "MRN-4521", "status": "Completed", "date": "Today, 11:20 AM", "technician_id": 101},
        {"id": "SCN-8468", "patientName": "John Peterson", "mrn": "MRN-3982", "status": "Completed", "date": "Today, 09:15 AM", "technician_id": 101}
    ]

@app.get("/scan-details/{scan_id}", response_model=ScanDetailResponse)
async def get_scan_details(scan_id: str, db: Session = Depends(get_db)):
    # Try to find the case in the database
    case = db.query(TriageCase).filter(TriageCase.id == scan_id).first()
    if not case:
        # Check by case_code as fallback
        case = db.query(TriageCase).filter(TriageCase.case_code == scan_id).first()
        
    if case:
        return {
            "scan_id": str(case.id),
            "patient_name": case.patient_name,
            "mrn": case.mrn,
            "date_of_birth": "Mar 15, 1985", # Mock DOB
            "gender": "Female",
            "scan_date": case.created_at.strftime("%b %d, %Y, %I:%M %p"),
            "technician": "James Chen",
            "view_type": "PA Chest",
            "orientation": "Anterior",
            "study_id": f"ST-{case.case_code.split('-')[-1] if '-' in case.case_code else case.id}",
            "status": case.status.value.capitalize(),
            "disease": case.ai_result,
            "confidence": case.ai_confidence or "N/A"
        }
    
    # Ultimate fallback for demo
    return {
        "scan_id": scan_id, "patient_name": "Sarah Wilson", "mrn": "MRN-4521", "date_of_birth": "Mar 15, 1985",
        "gender": "Female", "scan_date": "Today, 11:20 AM", "technician": "James Chen", "view_type": "PA Chest",
        "orientation": "Anterior", "study_id": "ST-82941", "status": "Completed", "disease": "Normal", "confidence": "98.4%"
    }

@app.get("/triage-dashboard", response_model=TriageDashboardResponse)
async def get_triage_dashboard(doctor_id: int, db: Session = Depends(get_db)):
    # 1. Total Cases for this doctor
    total_count = db.query(TriageCase).filter(TriageCase.doctor_id == doctor_id).count()

    # 2. Pending Cases
    pending_count = db.query(TriageCase).filter(
        TriageCase.doctor_id == doctor_id,
        TriageCase.status == StatusEnum.PENDING
    ).count()

    # 3. Critical Cases (Priority + Pending)
    critical_count = db.query(TriageCase).filter(
        TriageCase.doctor_id == doctor_id,
        TriageCase.status == StatusEnum.PENDING,
        TriageCase.priority == "CRITICAL"
    ).count()

    # 4. Urgent Cases
    urgent_count = db.query(TriageCase).filter(
        TriageCase.doctor_id == doctor_id,
        TriageCase.status == StatusEnum.PENDING,
        TriageCase.priority == "URGENT"
    ).count()

    return {
        "totalCases": total_count,
        "criticalCases": critical_count,
        "urgentCases": urgent_count,
        "pendingCases": pending_count
    }

@app.get("/doctors", response_model=List[DoctorResponse])
async def get_doctors(db: Session = Depends(get_db)):
    doctors = db.query(Doctor).all()
    return [{"doctor_id": d.id, "name": f"Dr. {d.first_name} {d.last_name}"} for d in doctors]

@app.post("/start-scan/{patient_id}", response_model=StartScanResponse)
async def start_scan(patient_id: int, technician_id: int, db: Session = Depends(get_db)):
    # Create or find a case for this scan
    case_code = f"CS-{random.randint(1000, 9999)}"
    
    # Check if patient exists
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    if not patient:
        raise HTTPException(status_code=404, detail="Patient not found")
        
    return {
        "scan_id": random.randint(1000, 9999), 
        "scan_code": case_code,
        "status": "Started"
    }

@app.post("/scan-preparation/{scan_id}", response_model=ScanPreparationResponse)
async def save_scan_preparation(scan_id: int, request: ScanPreparationRequest):
    return {
        "message": "Preparation saved successfully",
        "scan_id": scan_id
    }

@app.post("/create-study/{scan_id}", response_model=CreateStudyResponse)
async def create_study(scan_id: int, doctor_id: int, db: Session = Depends(get_db)):
    case = db.query(TriageCase).filter(TriageCase.id == scan_id).first()
    if not case:
        return {"message": "Scan not found", "study_id": None}
    
    case.doctor_id = doctor_id
    case.status = StatusEnum.PENDING
    db.commit()
    return {"message": "Study created successfully", "study_id": case.id}

@app.post("/distribute-study/{study_id}", response_model=DistributeStudyResponse)
async def distribute_study(study_id: int, db: Session = Depends(get_db)):
    case = db.query(TriageCase).filter(TriageCase.id == study_id).first()
    if not case:
        return {"message": "Study not found", "status": "Error"}
    
    case.status = StatusEnum.PENDING
    db.commit()
    return {"message": "Study distributed successfully", "status": "Sent"}

@app.get("/case/{case_id}/report-preview", response_model=ReportPreviewResponse)
async def get_report_preview(case_id: int, db: Session = Depends(get_db)):
    case = db.query(TriageCase).filter(TriageCase.id == case_id).first()
    if not case:
        raise HTTPException(status_code=404, detail="Case not found")
    
    # Format a human-readable summary for the report
    report_id = f"RPT-2026-{case.id:03d}"
    
    return {
        "patient": case.patient_name,
        "findings": case.ai_findings or "No AI findings available.",
        "impression": case.impression or "Awaiting clinical impression...",
        "signedBy": f"Dr. {case.doctor.first_name} {case.doctor.last_name}" if case.doctor else "Awaiting Signature",
        "finalized": case.status == StatusEnum.COMPLETED,
        "reportId": report_id
    }

@app.get("/view-report/{case_id}", response_model=TriageCaseResponse)
async def get_case_details(case_id: int, db: Session = Depends(get_db)):
    case = db.query(TriageCase).filter(TriageCase.id == case_id).first()
    if not case:
        raise HTTPException(status_code=404, detail="Case not found")
    
    patient = db.query(Patient).filter(Patient.id == case.patient_id).first()
    return {
        "case_id": case.id,
        "case_code": case.case_code,
        "patient_name": case.patient_name,
        "patient_age": 35,
        "diagnosis": case.ai_result,
        "priority": case.priority,
        "image_url": case.image_url,
        "ai_findings": case.ai_findings,
        "ai_result": case.ai_result,
        "ai_confidence": case.ai_confidence,
        "final_diagnosis": case.final_diagnosis,
        "doctor_notes": case.doctor_notes,
        "decision": case.decision,
        "status": case.status.value,
        "created_at": case.created_at.strftime("%Y-%m-%d %H:%M:%S"),
        "date_of_birth": patient.date_of_birth if patient else None,
        "gender": patient.gender if patient else None,
        "mrn": case.mrn,
        "height": patient.height if patient else None,
        "weight": patient.weight if patient else None,
        "blood_type": patient.blood_type if patient else None
    }

@app.get("/case/{case_id}/patient-history", response_model=List[TriageCaseResponse])
def get_patient_history(case_id: int, db: Session = Depends(get_db)):
    case_obj = db.query(TriageCase).filter(TriageCase.id == case_id).first()
    if not case_obj or not case_obj.patient_id:
        raise HTTPException(status_code=404, detail="Patient or case not found")
        
    history = db.query(TriageCase).filter(TriageCase.patient_id == case_obj.patient_id).order_by(TriageCase.created_at.desc()).all()
    return history

@app.post("/upload-scan/{scan_id}", response_model=UploadScanResponse)
async def upload_scan(scan_id: int, doctor_id: int = 1, technician_id: int = 1, file: UploadFile = File(...), db: Session = Depends(get_db)):
    if not model:
        # Fallback to simulation if model failed to load
        diseases = ["Pneumonia", "Normal"]
        selected_disease = random.choice(diseases)
        confidence = f"{random.uniform(85.0, 99.9):.1f}%"
        priority = "URGENT" if selected_disease != "Normal" else "ROUTINE"
    else:
        # Real AI analysis
        content = await file.read()
        processed_img = preprocess_image(content)
        prediction = model.predict(processed_img)[0]
        
        # Output shape is (2,) -> [Normal, Pneumonia] or vice versa
        # Based on common chest x-ray datasets, usually [Normal, Pneumonia]
        # We'll take the class with highest probability
        labels = ["Normal", "Pneumonia"]
        class_idx = np.argmax(prediction)
        selected_disease = labels[class_idx]
        conf_val = prediction[class_idx] * 100
        confidence = f"{conf_val:.1f}%"
        
        priority = "URGENT" if selected_disease == "Pneumonia" else "ROUTINE"
        if selected_disease == "Pneumonia" and conf_val > 95:
            priority = "CRITICAL"

    # Get the TriageCase to update
    case_obj = db.query(TriageCase).filter(TriageCase.id == scan_id).first()
    if not case_obj:
        raise HTTPException(status_code=404, detail="Scan/Case not found")

    # ✅ Assign IDs and update Case
    case_obj.doctor_id = doctor_id
    case_obj.technician_id = technician_id
    case_obj.ai_result = selected_disease
    case_obj.ai_confidence = confidence
    case_obj.ai_findings = f"{selected_disease} detected with {confidence} confidence."
    case_obj.priority = priority
    case_obj.status = StatusEnum.PENDING
    
    db.add(case_obj)
    db.commit()
    db.refresh(case_obj)

    print(f"DEBUG: Saved case_id={case_obj.id}, doctor_id={doctor_id}, technician_id={technician_id}")

    return {
        "message": "Scan uploaded successfully", 
        "case_id": case_obj.id,
        "disease": selected_disease, 
        "confidence": confidence,
        "priority": priority
    }

@app.post("/start-scan/{patient_id}")
async def start_scan(patient_id: int, technician_id: int = 101, db: Session = Depends(get_db)):
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    if not patient:
        raise HTTPException(status_code=404, detail="Patient not found")
        
    case_code = f"SCN-{random.randint(1000, 9999)}"
    new_case = TriageCase(
        case_code=case_code,
        patient_id=patient.id,
        technician_id=technician_id,
        patient_name=patient.full_name,
        mrn=patient.mrn,
        priority="ROUTINE", # Will be updated after upload
        status=StatusEnum.PENDING
    )
    db.add(new_case)
    db.commit()
    db.refresh(new_case)

    return {"scan_id": new_case.id, "scan_code": case_code, "status": "Started"}

@app.put("/case/{case_id}/accept", response_model=CaseActionResponse)
async def accept_case(case_id: int, request: SignRequest, db: Session = Depends(get_db)):
    case_obj = db.query(TriageCase).filter(TriageCase.id == case_id).first()
    if not case_obj:
        raise HTTPException(status_code=404, detail="Case not found")
        
    case_obj.status = StatusEnum.REVIEWED
    case_obj.doctor_id = request.doctor_id
    
    prefix = f"Accepted by {request.doctor_name}: "
    if case_obj.doctor_notes:
        case_obj.doctor_notes = f"{prefix}\n{case_obj.doctor_notes}"
    else:
        case_obj.doctor_notes = prefix
        
    db.commit()
    return {"message": "Case accepted successfully", "status": "Success"}

@app.put("/case/{case_id}/reject", response_model=CaseActionResponse)
async def reject_case(case_id: int, request: SignRequest, db: Session = Depends(get_db)):
    case_obj = db.query(TriageCase).filter(TriageCase.id == case_id).first()
    if not case_obj:
        raise HTTPException(status_code=404, detail="Case not found")
        
    case_obj.status = StatusEnum.CANCELLED
    case_obj.doctor_id = request.doctor_id
    
    prefix = f"Rejected by {request.doctor_name}: "
    if case_obj.doctor_notes:
        case_obj.doctor_notes = f"{prefix}\n{case_obj.doctor_notes}"
    else:
        case_obj.doctor_notes = prefix
        
    db.commit()
    return {"message": "Case rejected successfully", "status": "Success"}

@app.put("/case/{case_id}/edit-notes", response_model=CaseActionResponse)
async def edit_case_notes(case_id: int, request: EditNotesRequest, db: Session = Depends(get_db)):
    case_obj = db.query(TriageCase).filter(TriageCase.id == case_id).first()
    if not case_obj:
        raise HTTPException(status_code=404, detail="Case not found")
    
    case_obj.doctor_id = request.doctor_id
    case_obj.doctor_notes = request.doctor_notes
    db.commit()
    return {"message": "Notes updated successfully", "status": "Success"}

@app.put("/case/{case_id}/finalize-sign", response_model=ReportPreviewResponse)
async def finalize_sign(case_id: int, request: FinalizeRequest, db: Session = Depends(get_db)):
    case_obj = db.query(TriageCase).filter(TriageCase.id == case_id).first()
    if not case_obj:
        raise HTTPException(status_code=404, detail="Case not found")
        
    case_obj.status = StatusEnum.COMPLETED
    case_obj.doctor_id = request.doctor_id
    case_obj.impression = request.impression
    case_obj.recommendation = request.recommendation
    
    # We can also keep notes if provided
    if request.notes:
        case_obj.doctor_notes = request.notes
    
    db.commit()
    return {
        "report_id": f"REP-{case_id}",
        "patient": case_obj.patient_name,
        "findings": case_obj.ai_findings,
        "impression": request.impression,
        "signed_by": request.doctor_name,
        "finalized": True
    }

@app.get("/case/{case_id}/report-preview", response_model=ReportPreviewResponse)
async def get_report_preview(case_id: int, db: Session = Depends(get_db)):
    case_obj = db.query(TriageCase).filter(TriageCase.id == case_id).first()
    if not case_obj:
        raise HTTPException(status_code=404, detail="Case not found")
    
    return {
        "report_id": f"REP-{case_id}",
        "patient": case_obj.patient_name,
        "findings": case_obj.ai_findings,
        "impression": case_obj.impression or case_obj.ai_result,
        "signed_by": "Dr. Bennett", # Simulation
        "finalized": case_obj.status == StatusEnum.COMPLETED
    }

@app.get("/case/{case_id}/generate-report", response_model=ApiResponse)
async def generate_report(case_id: int, db: Session = Depends(get_db)):
    return {"message": "Report generated successfully", "status": "Success"}

@app.get("/case/{case_id}/download-pdf")
async def download_pdf(case_id: int, db: Session = Depends(get_db)):
    from fastapi.responses import Response
    content = b"%PDF-1.4\n1 0 obj\n<< /Title (Medical Report) >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF"
    return Response(content=content, media_type="application/pdf", headers={
        "Content-Disposition": f"attachment; filename=report_{case_id}.pdf"
    })

@app.post("/distribute-study/{study_id}")
async def distribute_study(study_id: str):
    return {"message": "Distributed", "status": "Success"}

@app.post("/create-study/{scan_id}")
async def create_study(scan_id: int, doctor_id: int = None, db: Session = Depends(get_db)):
    case_obj = db.query(TriageCase).filter(TriageCase.id == scan_id).first()
    if case_obj and doctor_id:
        case_obj.doctor_id = doctor_id
        db.commit()
    return {"message": "Created", "status": "Success", "studyId": scan_id}

@app.post("/ai-chat", response_model=AiChatResponse)
async def ai_chat(request: AiChatRequest):
    user_msg = request.message.lower()
    
    # Smarter rule-based AI Response
    if "pneumonia" in user_msg:
        response = "Cortex AI: Pneumonia appears as areas of opacification (whitening) in the lung fields on a chest X-ray. Our model detects these patterns with high precision."
    elif "accuracy" in user_msg or "reliable" in user_msg:
        response = "Cortex AI: This model has been validated on clinical data and achieves robust performance in identifying thoracic abnormalities."
    elif "help" in user_msg or "how" in user_msg:
        response = "Cortex AI: I can help you interpret triage results, explain medical terms, or provide guidance on using this triage system."
    elif "priority" in user_msg:
        response = "Cortex AI: I flag cases as URGENT or CRITICAL based on the severity of the findings to help you prioritize your workflow."
    else:
        response = "Cortex AI: I am your medical assistant. I can assist with thoracic X-ray analysis and triage prioritization. How can I help further?"
        
    return {"response": response}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
