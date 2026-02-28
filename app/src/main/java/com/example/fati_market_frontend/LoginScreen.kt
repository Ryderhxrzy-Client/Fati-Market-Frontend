package com.example.fati_market_frontend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fati_market_frontend.ui.theme.DarkGreen
import com.example.fati_market_frontend.ui.theme.DarkGreenLight
import com.example.fati_market_frontend.ui.theme.Gold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val headerGradient = Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header with tab switcher ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(headerGradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(2.dp, Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Text(
                    text = "Fati-Market ni Ofelia",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Login Your Account",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                // ── Login / Sign Up tab switcher ──────────────────────────────────
                AuthTabSwitcher(
                    isLoginSelected = true,
                    onLoginClick = { /* already here */ },
                    onSignUpClick = { navController.navigate("signup") }
                )
            }
        }

        // ── Form Card ────────────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-28).dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Log in to your account",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = DarkGreen
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = DarkGreen
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = DarkGreen
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )

                // Forgot Password link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ClickableText(
                        text = AnnotatedString("Forgot Password?"),
                        onClick = { navController.navigate("forgot_password") },
                        style = TextStyle(
                            color = DarkGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // ── Success Dialog ────────────────────────────────────────────────
                successMessage?.let { msg ->
                    AlertDialog(
                        onDismissRequest = {
                            successMessage = null
                            navController.navigate("admin_home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = DarkGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = { Text("Login Successful", fontWeight = FontWeight.Bold) },
                        text = { Text(msg) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    successMessage = null
                                    navController.navigate("admin_home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Text("Go to Dashboard", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                // ── Error Message ─────────────────────────────────────────────────
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // ── Login Button ──────────────────────────────────────────────────
                Button(
                    onClick = {
                        scope.launch {
                            if (email.isBlank()) { errorMessage = "Email is required"; return@launch }
                            if (password.isBlank()) { errorMessage = "Password is required"; return@launch }
                            errorMessage = null
                            isLoading = true
                            try {
                                val (success, message, responseBody) = withContext(Dispatchers.IO) {
                                    loginUser(email.trim(), password)
                                }
                                if (success) {
                                    val editor = context.getSharedPreferences("fatimarket_prefs", 0).edit()
                                    editor.putLong("login_timestamp", System.currentTimeMillis())
                                    if (responseBody != null) {
                                        parseToken(responseBody)?.let { editor.putString("auth_token", it) }
                                        try {
                                            val data = org.json.JSONObject(responseBody).optJSONObject("data")
                                            data?.let { d ->
                                                editor.putInt("user_id", d.optInt("user_id", 0))
                                                editor.putString("user_email", d.optString("email", ""))
                                                editor.putString("user_first_name", d.optString("first_name", ""))
                                                editor.putString("user_last_name", d.optString("last_name", ""))
                                                val pic = if (d.isNull("profile_picture")) "" else d.optString("profile_picture", "")
                                                editor.putString("user_profile_picture", pic)
                                                editor.putString("user_role", d.optString("role", "admin"))
                                                editor.putInt("user_wallet_points", d.optInt("wallet_points", 0))
                                            }
                                        } catch (_: Exception) {}
                                    }
                                    editor.apply()
                                    successMessage = message
                                } else {
                                    errorMessage = message
                                }
                            } catch (e: Exception) {
                                errorMessage = "Login failed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Shared tab switcher used by both Login and SignUp screens ──────────────────

@Composable
fun AuthTabSwitcher(
    isLoginSelected: Boolean,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        // Login tab
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50.dp))
                .background(if (isLoginSelected) Color.White else Color.Transparent)
                .clickable { onLoginClick() }
                .padding(vertical = 10.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Login",
                fontSize = 14.sp,
                fontWeight = if (isLoginSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isLoginSelected) DarkGreen else Color.White.copy(alpha = 0.85f)
            )
        }
        // Sign Up tab
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50.dp))
                .background(if (!isLoginSelected) Color.White else Color.Transparent)
                .clickable { onSignUpClick() }
                .padding(vertical = 10.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sign Up",
                fontSize = 14.sp,
                fontWeight = if (!isLoginSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (!isLoginSelected) DarkGreen else Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

// ── Network ────────────────────────────────────────────────────────────────────

private val loginHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

private fun loginUser(email: String, password: String): Triple<Boolean, String, String?> {
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

private fun parseToken(body: String): String? {
    val match = Regex("\"token\"\\s*:\\s*\"([^\"]+)\"").find(body)
    return match?.groupValues?.get(1)
}

private fun parseLoginError(body: String): String {
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
