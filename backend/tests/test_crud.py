import pytest
from app import schemas, models, crud
from sqlalchemy.orm import Session

def test_expense_split(db_session: Session):
    # 1. Create a user
    user1 = crud.create_user(db_session, schemas.UserCreate(uid="user1", name="Alice", email="alice@test.com"))
    user2 = crud.create_user(db_session, schemas.UserCreate(uid="user2", name="Bob", email="bob@test.com"))
    user3 = crud.create_user(db_session, schemas.UserCreate(uid="user3", name="Charlie", email="charlie@test.com"))
    
    # 2. Create a group
    group = crud.create_group(db_session, schemas.GroupCreate(group_name="Trip"), current_user_uid=user1.uid)
    
    # Add other members to group
    member1 = crud.get_group_members(db_session, group.group_id)[0] # Alice is automatically added
    member2 = crud.add_group_member(db_session, group.group_id, schemas.GroupMemberCreate(name=user2.name, user_id=user2.uid))
    member3 = crud.add_group_member(db_session, group.group_id, schemas.GroupMemberCreate(name=user3.name, user_id=user3.uid))
    
    # 3. Add an expense of $10 for 3 members.
    # $10 / 3 = 3 with remainder 1. 
    # One person should pay 4, two should pay 3.
    expense_create = schemas.ExpenseCreate(amount=10, description="Taxi", paid_by=member1.member_id)
    expense = crud.add_expense(db_session, group.group_id, expense_create)
    
    # Verifying expense is added
    assert expense.amount == 10
    
    # Fetch participants and verify shares
    participants = db_session.query(models.ExpenseParticipant).filter(models.ExpenseParticipant.expense_id == expense.expense_id).all()
    assert len(participants) == 3
    
    shares = [p.share_amount for p in participants]
    shares.sort()
    assert shares == [3, 3, 4] # Remainder 1 distributed to one random person

def test_group_dashboard_balances(db_session: Session):
    user1 = crud.create_user(db_session, schemas.UserCreate(uid="user_a", name="A", email="a@test.com"))
    user2 = crud.create_user(db_session, schemas.UserCreate(uid="user_b", name="B", email="b@test.com"))
    user3 = crud.create_user(db_session, schemas.UserCreate(uid="user_c", name="C", email="c@test.com"))
    
    group = crud.create_group(db_session, schemas.GroupCreate(group_name="Apartment"), current_user_uid=user1.uid)
    member1 = crud.get_group_members(db_session, group.group_id)[0]
    member2 = crud.add_group_member(db_session, group.group_id, schemas.GroupMemberCreate(name=user2.name, user_id=user2.uid))
    member3 = crud.add_group_member(db_session, group.group_id, schemas.GroupMemberCreate(name=user3.name, user_id=user3.uid))
    
    # Member 1 pays $30. 
    # M1 paid $30. Expected share $10 each. M1: +20, M2: -10, M3: -10
    crud.add_expense(db_session, group.group_id, schemas.ExpenseCreate(amount=30, description="Dinner", paid_by=member1.member_id))
    
    balances = crud.get_group_dashboard(db_session, group.group_id)
    balance_map = {b['member_id']: b['balance'] for b in balances}
    
    assert balance_map[member1.member_id] == 20
    assert balance_map[member2.member_id] == -10
    assert balance_map[member3.member_id] == -10

    # Settlement: Member 2 pays Member 1 $10
    crud.add_settlement(db_session, group.group_id, schemas.SettlementCreate(from_member_id=member2.member_id, to_member_id=member1.member_id, amount=10))

    balances = crud.get_group_dashboard(db_session, group.group_id)
    balance_map = {b['member_id']: b['balance'] for b in balances}
    
    # M2 paid $10 to M1. -> M2 balance becomes 0. M1 balance becomes +10. M3 still -10.
    assert balance_map[member1.member_id] == 10
    assert balance_map[member2.member_id] == 0
    assert balance_map[member3.member_id] == -10
