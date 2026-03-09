from sqlalchemy import Column, Integer, String
from .database import Base

class Doctor(Base):
    __tablename__ = "doctors"

    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String)
    last_name = Column(String)
    hospital_email = Column(String, unique=True, index=True)
    employee_id = Column(String, unique=True, index=True)
    role_requested = Column(String)
    password = Column(String)
