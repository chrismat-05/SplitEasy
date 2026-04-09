package com.spliteasy.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/users/")
    suspend fun createUser(@Body user: UserCreate): UserResponse

    @GET("/users/me")
    suspend fun getMe(): UserResponse

    @POST("/groups/")
    suspend fun createGroup(@Body group: GroupCreate): GroupResponse

    @GET("/groups/")
    suspend fun getMyGroups(): List<GroupResponse>

    @POST("/groups/{group_id}/members")
    suspend fun addGroupMember(
        @Path("group_id") groupId: Int,
        @Body member: GroupMemberCreate
    ): GroupMemberResponse

    @GET("/groups/{group_id}/members")
    suspend fun getGroupMembers(@Path("group_id") groupId: Int): List<GroupMemberResponse>

    @POST("/groups/{group_id}/expenses")
    suspend fun addExpense(
        @Path("group_id") groupId: Int,
        @Body expense: ExpenseCreate
    ): ExpenseResponse

    @GET("/groups/{group_id}/expenses")
    suspend fun getGroupExpenses(@Path("group_id") groupId: Int): List<ExpenseResponse>

    @GET("/groups/{group_id}/dashboard")
    suspend fun getGroupDashboard(@Path("group_id") groupId: Int): List<DashboardBalanceResponse>
}
