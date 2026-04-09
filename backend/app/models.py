from sqlalchemy import Column, Integer, String, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
from app.database import Base

class User(Base):
    __tablename__ = "users"

    uid = Column(String, primary_key=True, index=True)
    name = Column(String, index=True)
    email = Column(String, unique=True, index=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    groups_created = relationship("Group", back_populates="creator")
    group_memberships = relationship("GroupMember", back_populates="user")

class Group(Base):
    __tablename__ = "groups"

    group_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    group_name = Column(String, index=True)
    created_by = Column(String, ForeignKey("users.uid"))
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    creator = relationship("User", back_populates="groups_created")
    members = relationship("GroupMember", back_populates="group")
    expenses = relationship("Expense", back_populates="group")
    settlements = relationship("Settlement", back_populates="group")

class GroupMember(Base):
    __tablename__ = "group_members"

    member_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    group_id = Column(Integer, ForeignKey("groups.group_id"))
    name = Column(String)  # Offline members have a name but no userId
    user_id = Column(String, ForeignKey("users.uid"), nullable=True)
    status = Column(String, default="active") # active, left
    joined_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    group = relationship("Group", back_populates="members")
    user = relationship("User", back_populates="group_memberships")
    expenses_paid = relationship("Expense", back_populates="payer", foreign_keys="[Expense.paid_by]")
    expense_shares = relationship("ExpenseParticipant", back_populates="member")

class Expense(Base):
    __tablename__ = "expenses"

    expense_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    group_id = Column(Integer, ForeignKey("groups.group_id"))
    paid_by = Column(Integer, ForeignKey("group_members.member_id"))
    amount = Column(Integer) # In paise
    description = Column(String)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    group = relationship("Group", back_populates="expenses")
    payer = relationship("GroupMember", back_populates="expenses_paid", foreign_keys=[paid_by])
    participants = relationship("ExpenseParticipant", back_populates="expense")

class ExpenseParticipant(Base):
    __tablename__ = "expense_participants"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    expense_id = Column(Integer, ForeignKey("expenses.expense_id"))
    member_id = Column(Integer, ForeignKey("group_members.member_id"))
    share_amount = Column(Integer) # In paise

    expense = relationship("Expense", back_populates="participants")
    member = relationship("GroupMember", back_populates="expense_shares")

class Settlement(Base):
    __tablename__ = "settlements"

    settlement_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    group_id = Column(Integer, ForeignKey("groups.group_id"))
    from_member_id = Column(Integer, ForeignKey("group_members.member_id"))
    to_member_id = Column(Integer, ForeignKey("group_members.member_id"))
    amount = Column(Integer) # In paise
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    group = relationship("Group", back_populates="settlements")
    from_member = relationship("GroupMember", foreign_keys=[from_member_id])
    to_member = relationship("GroupMember", foreign_keys=[to_member_id])
