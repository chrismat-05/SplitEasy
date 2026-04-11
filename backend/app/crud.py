from sqlalchemy.orm import Session
from app import models, schemas
import random

def get_user(db: Session, user_uid: str):
    return db.query(models.User).filter(models.User.uid == user_uid).first()

def update_user(db: Session, user_uid: str, name: str):
    db_user = db.query(models.User).filter(models.User.uid == user_uid).first()
    if db_user:
        db_user.name = name
        # Also update GroupMember names for this user across all groups
        db.query(models.GroupMember).filter(models.GroupMember.user_id == user_uid).update({"name": name})
        db.commit()
        db.refresh(db_user)
    return db_user

def create_group(db: Session, group: schemas.GroupCreate, current_user_uid: str):
    db_group = models.Group(group_name=group.group_name, created_by=current_user_uid)
    db.add(db_group)
    db.commit()
    db.refresh(db_group)
    # Automatically add the creator as an active member
    # Find the user's name
    user = get_user(db, current_user_uid)
    add_group_member(db, db_group.group_id, schemas.GroupMemberCreate(name=user.name if user else "Creator", user_id=current_user_uid))
    return db_group

def get_groups_for_user(db: Session, user_uid: str):
    # Get all groups where the user is either the creator OR a member
    # Based on the BRD, users can see groups they belong to.
    memberships = db.query(models.GroupMember).filter(models.GroupMember.user_id == user_uid).all()
    group_ids = [m.group_id for m in memberships]
    
    # Alternatively query all groups created by user
    created_groups = db.query(models.Group).filter(models.Group.created_by == user_uid).all()
    for g in created_groups:
        if g.group_id not in group_ids:
            group_ids.append(g.group_id)

    return db.query(models.Group).filter(models.Group.group_id.in_(group_ids)).all()

def add_group_member(db: Session, group_id: int, member: schemas.GroupMemberCreate):
    db_member = models.GroupMember(
        group_id=group_id,
        name=member.name,
        user_id=member.user_id
    )
    db.add(db_member)
    db.commit()
    db.refresh(db_member)
    return db_member

def get_group_members(db: Session, group_id: int):
    return db.query(models.GroupMember).filter(models.GroupMember.group_id == group_id).all()

def get_group_member_by_id(db: Session, group_id: int, member_id: int):
    return db.query(models.GroupMember).filter(
        models.GroupMember.group_id == group_id,
        models.GroupMember.member_id == member_id
    ).first()

def get_group_expenses(db: Session, group_id: int):
    return db.query(models.Expense).filter(models.Expense.group_id == group_id).all()

def get_group_settlements(db: Session, group_id: int):
    return db.query(models.Settlement).filter(models.Settlement.group_id == group_id).all()

def add_expense(db: Session, group_id: int, expense: schemas.ExpenseCreate):
    # Verify payer is part of the group
    payer = get_group_member_by_id(db, group_id, expense.paid_by)
    if not payer:
        raise ValueError("Payer is not in the group")

    # Get active members
    members = db.query(models.GroupMember).filter(
        models.GroupMember.group_id == group_id,
        models.GroupMember.status == "active"
    ).all()

    if not members:
        raise ValueError("No active members to split the expense with")

    # Algorithm for equal split
    n = len(members)
    base_share = expense.amount // n
    remainder = expense.amount % n

    db_expense = models.Expense(
        group_id=group_id,
        paid_by=expense.paid_by,
        amount=expense.amount,
        description=expense.description
    )
    db.add(db_expense)
    db.commit()
    db.refresh(db_expense)

    # Distribute remainder randomly
    members_to_receive_extra = random.sample(members, remainder)
    extra_assign_ids = {m.member_id for m in members_to_receive_extra}

    for member in members:
        share = base_share + (1 if member.member_id in extra_assign_ids else 0)
        db_participant = models.ExpenseParticipant(
            expense_id=db_expense.expense_id,
            member_id=member.member_id,
            share_amount=share
        )
        db.add(db_participant)

    db.commit()
    db.refresh(db_expense)
    return db_expense

def add_settlement(db: Session, group_id: int, settlement: schemas.SettlementCreate):
    db_settlement = models.Settlement(
        group_id=group_id,
        from_member_id=settlement.from_member_id,
        to_member_id=settlement.to_member_id,
        amount=settlement.amount
    )
    db.add(db_settlement)
    db.commit()
    db.refresh(db_settlement)
    return db_settlement

def get_group_dashboard(db: Session, group_id: int):
    # Calculate balance for each member in a group
    # Balance = Total Paid - Total Owed
    members = get_group_members(db, group_id)
    balances = {m.member_id: 0 for m in members}
    
    # 1. Add what they paid
    expenses = get_group_expenses(db, group_id)
    for exp in expenses:
        if exp.paid_by in balances:
            balances[exp.paid_by] += exp.amount
        
    # 2. Subtract what they owe
    participants = db.query(models.ExpenseParticipant).join(models.Expense).filter(models.Expense.group_id == group_id).all()
    for part in participants:
         if part.member_id in balances:
             balances[part.member_id] -= part.share_amount
         
    # 3. Account for settlements
    settlements = get_group_settlements(db, group_id)
    for stl in settlements:
        if stl.from_member_id in balances:
            balances[stl.from_member_id] += stl.amount # they paid off some debt
        if stl.to_member_id in balances:
            balances[stl.to_member_id] -= stl.amount   # they received money
        
    return [{"member_id": m.member_id, "name": m.name, "user_id": m.user_id, "balance": balances[m.member_id]} for m in members]
