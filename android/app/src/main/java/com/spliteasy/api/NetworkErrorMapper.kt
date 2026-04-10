package com.spliteasy.api

import com.spliteasy.app.BuildConfig
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

object NetworkErrorMapper {
    fun map(e: Throwable): String {
        return when (e) {
            is FirebaseAuthException -> {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                    is FirebaseAuthInvalidUserException -> "No account found with this email."
                    else -> e.localizedMessage ?: "Authentication failed."
                }
            }
            is IOException -> {
                when (e) {
                    is SocketTimeoutException -> "The server is taking too long to respond."
                    is ConnectException -> "Could not connect to the server at ${BuildConfig.BASE_URL}. Check if the backend is running and your phone is on the same Wi-Fi."
                    else -> "Network error: ${e.localizedMessage ?: "Please check your connection."}"
                }
            }
            is HttpException -> {
                // Try to extract the "detail" message from the backend response body
                val errorBody = e.response()?.errorBody()?.string()
                val backendMessage = try {
                    errorBody?.let { JSONObject(it).getString("detail") }
                } catch (ex: Exception) {
                    null
                }

                when (e.code()) {
                    401 -> backendMessage ?: "Unauthorized: Please log in again."
                    403 -> "Forbidden: You don't have permission to access this."
                    404 -> "Not Found: The requested resource doesn't exist."
                    500 -> "Internal Server Error: ${backendMessage ?: "Something went wrong on the backend."}"
                    else -> backendMessage ?: "Server error (${e.code()}): ${e.message()}"
                }
            }
            else -> e.localizedMessage ?: "An unexpected error occurred."
        }
    }
}
