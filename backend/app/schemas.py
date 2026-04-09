from pydantic import BaseModel, EmailStr
from typing import Optional, List
from datetime import datetime

class ORMBase(BaseModel):
    class Config:
        from_attributes = True

# --- User Schemas ---
class UserCreate(BaseModel):
    uid: str
    name: str
    email: EmailStr

class UserResponse(ORMBase):
    uid: str
    name: str
    email: EmailStr
    created_at: datetime

# --- Group Member Schemas ---
class GroupMemberCreate(BaseModel):
    name: str
    user_id: Optional[str] = None

class GroupMemberResponse(ORMBase):
    member_id: int
    group_id: int
    name: str
    user_id: Optional[str]
    status: str
    joined_at: datetime

# --- Group Schemas ---
class GroupCreate(BaseModel):
    group_name: str

class GroupResponse(ORMBase):
    group_id: int
    group_name: str
    created_by: str
    created_at: datetime
    members: List[GroupMemberResponse] = []

# --- Expense Schemas ---
class ExpenseCreate(BaseModel):
    paid_by: int
    amount: int # Amount in paise
    description: str

class ExpenseParticipantResponse(ORMBase):
    id: int
    member_id: int
    share_amount: int

class ExpenseResponse(ORMBase):
    expense_id: int
    group_id: int
    paid_by: int
    amount: int
    description: str
    created_at: datetime
    participants: List[ExpenseParticipantResponse] = []

# --- Settlement Schemas ---
class SettlementCreate(BaseModel):
    from_member_id: int
    to_member_id: int
    amount: int

class SettlementResponse(ORMBase):
    settlement_id: int
    group_id: int
    from_member_id: int
    to_member_id: int
    amount: int
    created_at: datetime
