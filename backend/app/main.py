from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from contextlib import asynccontextmanager
from typing import List

from app import models, schemas, crud
from app.database import engine, get_db
from app.core.security import get_current_user
from fastapi.middleware.cors import CORSMiddleware

@asynccontextmanager
async def lifespan(app: FastAPI):
    models.Base.metadata.create_all(bind=engine)
    yield

app = FastAPI(title="SplitEasy API", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
def read_root():
    return {"message": "Welcome to SplitEasy API"}

# --- Users ---
@app.post("/users/", response_model=schemas.UserResponse)
def create_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = crud.get_user(db, user_uid=user.uid)
    if db_user:
        raise HTTPException(status_code=400, detail="User already registered")
    return crud.create_user(db=db, user=user)

@app.get("/users/me", response_model=schemas.UserResponse)
def get_me(current_user: models.User = Depends(get_current_user)):
    return current_user

# --- Groups ---
@app.post("/groups/", response_model=schemas.GroupResponse)
def create_group(group: schemas.GroupCreate, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    return crud.create_group(db=db, group=group, current_user_uid=current_user.uid)

@app.get("/groups/", response_model=List[schemas.GroupResponse])
def get_my_groups(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    return crud.get_groups_for_user(db=db, user_uid=current_user.uid)

@app.post("/groups/{group_id}/members", response_model=schemas.GroupMemberResponse)
def add_group_member(group_id: int, member: schemas.GroupMemberCreate, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    # Need to check if user has access to this group ideally
    return crud.add_group_member(db=db, group_id=group_id, member=member)

@app.get("/groups/{group_id}/members", response_model=List[schemas.GroupMemberResponse])
def get_group_members(group_id: int, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    return crud.get_group_members(db=db, group_id=group_id)

# --- Expenses ---
@app.post("/groups/{group_id}/expenses", response_model=schemas.ExpenseResponse)
def add_expense(group_id: int, expense: schemas.ExpenseCreate, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    try:
        return crud.add_expense(db=db, group_id=group_id, expense=expense)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.get("/groups/{group_id}/expenses", response_model=List[schemas.ExpenseResponse])
def get_group_expenses_api(group_id: int, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    return crud.get_group_expenses(db=db, group_id=group_id)

# --- Settlements ---
@app.post("/groups/{group_id}/settlements", response_model=schemas.SettlementResponse)
def add_settlement(group_id: int, settlement: schemas.SettlementCreate, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    return crud.add_settlement(db=db, group_id=group_id, settlement=settlement)

@app.get("/groups/{group_id}/settlements", response_model=List[schemas.SettlementResponse])
def get_group_settlements(group_id: int, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    return crud.get_group_settlements(db=db, group_id=group_id)

# --- Dashboard ---
@app.get("/groups/{group_id}/dashboard")
def get_group_dashboard(group_id: int, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    return crud.get_group_dashboard(db=db, group_id=group_id)
