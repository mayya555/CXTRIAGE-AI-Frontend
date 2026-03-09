from pydantic import BaseModel, EmailStr
from typing import Optional

class DoctorBase(BaseModel):
    first_name: str
    last_name: str
    hospital_email: str
    employee_id: str
    role_requested: str

class DoctorCreate(DoctorBase):
    password: str
    confirm_password: str

class DoctorResponse(DoctorBase):
    id: int

    class Config:
        from_attributes = True

class LoginRequest(BaseModel):
    username: str # hospital_email or employee_id
    password: str

class LoginResponse(BaseModel):
    message: str
    doctor: DoctorResponse
