package com.example.fati_market_frontend.auth.network

import android.content.Context
import android.net.Uri
import com.example.fati_market_frontend.auth.utils.getFileName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// ── HTTP Client ────────────────────────────────────────────────────────────────

val signUpHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

// ── API Call ───────────────────────────────────────────────────────────────────

fun registerUser(
    context: Context,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    passwordConfirmation: String,
    profilePictureUri: Uri,
    studentIdPhotoUri: Uri,
    verificationUse: String
): Pair<Boolean, String> {
    val profileFileName = getFileName(context, profilePictureUri).ifEmpty { "profile.jpg" }
    val profileMimeType = context.contentResolver.getType(profilePictureUri) ?: "image/jpeg"
    val profileBytes = context.contentResolver.openInputStream(profilePictureUri)?.readBytes()
        ?: return Pair(false, "Could not read the profile picture")

    val fileName = getFileName(context, studentIdPhotoUri).ifEmpty { "photo.jpg" }
    val mimeType = context.contentResolver.getType(studentIdPhotoUri) ?: "image/jpeg"
    val fileBytes = context.contentResolver.openInputStream(studentIdPhotoUri)?.readBytes()
        ?: return Pair(false, "Could not read the selected file")

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("first_name", firstName.trim())
        .addFormDataPart("last_name", lastName.trim())
        .addFormDataPart("email", email.trim())
        .addFormDataPart("password", password)
        .addFormDataPart("password_confirmation", passwordConfirmation)
        .addFormDataPart("verification_use", verificationUse)
        .addFormDataPart(
            "profile_picture",
            profileFileName,
            profileBytes.toRequestBody(profileMimeType.toMediaType())
        )
        .addFormDataPart(
            "student_id_photo",
            fileName,
            fileBytes.toRequestBody(mimeType.toMediaType())
        )
        .build()

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/register")
        .header("Accept", "application/json")
        .header("X-Requested-With", "XMLHttpRequest")
        .post(requestBody)
        .build()

    signUpHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string() ?: ""
        return if (response.isSuccessful) {
            val serverMsg = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
                ?: "Your account has been created and is pending verification."
            Pair(true, serverMsg)
        } else {
            Pair(false, "[HTTP ${response.code}] ${parseRegistrationError(body)}")
        }
    }
}

// ── Response Parsers ───────────────────────────────────────────────────────────

fun parseRegistrationError(body: String): String {
    val errorsBlock = Regex("\"errors\"\\s*:\\s*\\{(.*?)\\}", RegexOption.DOT_MATCHES_ALL).find(body)
    if (errorsBlock != null) {
        val fieldErrors = Regex("\"[^\"]+\"\\s*:\\s*\\[\\s*\"([^\"]+)\"").findAll(errorsBlock.groupValues[1])
        val messages = fieldErrors.map { it.groupValues[1] }.toList()
        if (messages.isNotEmpty()) return messages.joinToString("\n")
    }
    val msgMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
    if (msgMatch != null) return msgMatch.groupValues[1]
    val errMatch = Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(body)
    if (errMatch != null) return errMatch.groupValues[1]
    return if (body.isNotBlank()) "Server response: $body" else "Registration failed. Please try again."
}
