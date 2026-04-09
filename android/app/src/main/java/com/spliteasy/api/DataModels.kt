package com.spliteasy.api

import com.google.gson.annotations.SerializedName

data class UserCreate(
    val uid: String,
    val name: String,
    val email: String
)

data class UserResponse(
    @SerializedName("user_id") val userId: Int,
    val uid: String,
    val name: String,
    val email: String,
    @SerializedName("created_at") val createdAt: String
)

data class GroupCreate(
    @SerializedName("group_name") val groupName: String
)

data class GroupResponse(
    @SerializedName("group_id") val groupId: Int,
    @SerializedName("group_name") val groupName: String,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("created_at") val createdAt: String
)

data class GroupMemberCreate(
    val name: String,
    @SerializedName("user_id") val userId: String? = null
)

data class GroupMemberResponse(
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("group_id") val groupId: Int,
    val name: String,
    @SerializedName("user_id") val userId: String?,
    val status: String
)

data class ExpenseCreate(
    val amount: Int,
    val description: String,
    @SerializedName("paid_by") val paidBy: Int
)

data class ExpenseResponse(
    @SerializedName("expense_id") val expenseId: Int,
    @SerializedName("group_id") val groupId: Int,
    @SerializedName("paid_by") val paidBy: Int,
    val amount: Int,
    val description: String,
    @SerializedName("date_added") val dateAdded: String
)

data class DashboardBalanceResponse(
    @SerializedName("member_id") val memberId: Int,
    val name: String,
    @SerializedName("user_id") val userId: String?,
    val balance: Int
)
