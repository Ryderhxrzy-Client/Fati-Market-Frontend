package com.example.fati_market_frontend.auth.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// ── HTTP Client ────────────────────────────────────────────────────────────────

val loginHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

// ── API Call ───────────────────────────────────────────────────────────────────

fun loginUser(email: String, password: String): Triple<Boolean, String, String?> {
    val json = """{"email":"$email","password":"$password"}"""
    val requestBody = json.toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/login")
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .header("X-Requested-With", "XMLHttpRequest")
        .post(requestBody)
        .build()

    loginHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string() ?: ""
        return if (response.isSuccessful) {
            Triple(true, "Login successful", body)   // full body — caller extracts token + user fields
        } else {
            Triple(false, parseLoginError(body), null)
        }
    }
}

// ── Response Parsers ───────────────────────────────────────────────────────────

fun parseToken(body: String): String? {
    val match = Regex("\"token\"\\s*:\\s*\"([^\"]+)\"").find(body)
    return match?.groupValues?.get(1)
}

fun parseLoginError(body: String): String {
    val errorsBlock = Regex("\"errors\"\\s*:\\s*\\{(.*?)\\}", RegexOption.DOT_MATCHES_ALL).find(body)
    if (errorsBlock != null) {
        val fieldErrors = Regex("\"[^\"]+\"\\s*:\\s*\\[\\s*\"([^\"]+)\"").findAll(errorsBlock.groupValues[1])
        val messages = fieldErrors.map { it.groupValues[1] }.toList()
        if (messages.isNotEmpty()) return messages.joinToString("\n")
    }
    val msgMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
    if (msgMatch != null) return msgMatch.groupValues[1]
    return "Login failed. Please try again."
}
