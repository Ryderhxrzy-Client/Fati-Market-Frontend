package com.example.fati_market_frontend

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fati_market_frontend.ui.theme.DarkGreen
import com.example.fati_market_frontend.ui.theme.DarkGreenLight
import com.example.fati_market_frontend.ui.theme.DarkText
import com.example.fati_market_frontend.ui.theme.Gold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Profile picture
    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var profileUri by remember { mutableStateOf<Uri?>(null) }

    // Verification
    val verificationOptions = listOf("Student ID", "Registration Card")
    var verificationExpanded by remember { mutableStateOf(false) }
    var selectedVerification by remember { mutableStateOf("") }
    var documentUri by remember { mutableStateOf<Uri?>(null) }
    var documentName by remember { mutableStateOf("") }

    // Image picker for profile photo
    val profileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileUri = uri
        uri?.let {
            scope.launch(Dispatchers.IO) {
                val stream = context.contentResolver.openInputStream(it)
                val bmp = BitmapFactory.decodeStream(stream)?.asImageBitmap()
                withContext(Dispatchers.Main) { profileBitmap = bmp }
            }
        }
    }

    // File picker for verification document
    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        documentUri = uri
        uri?.let { documentName = getFileName(context, it) }
    }

    val headerGradient = Brush.verticalGradient(
        colors = listOf(DarkGreen, DarkGreenLight)
    )

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
                // Logo — same as login screen
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
                    text = "Create Your Account",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                // ── Login / Sign Up tab switcher ──────────────────────────────────
                AuthTabSwitcher(
                    isLoginSelected = false,
                    onLoginClick = { navController.navigate("login") },
                    onSignUpClick = { /* already here */ }
                )
            }
        }

        // ── Floating Form Card ─────────────────────────────────────────────────
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

                // ── Profile Picture ──────────────────────────────────────────────
                Text(
                    text = "Profile Photo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Avatar with camera badge
                Box(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .clickable { profileLauncher.launch("image/*") }
                ) {
                    val bitmap = profileBitmap
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .border(2.dp, DarkGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                painter = BitmapPainter(bitmap),
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = DarkGreen,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

                    // Camera badge overlay
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Gold)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "Add photo",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // ── First Name ───────────────────────────────────────────────────
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    placeholder = { Text("Juan") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Last Name ────────────────────────────────────────────────────
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    placeholder = { Text("Dela Cruz") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Email ────────────────────────────────────────────────────────
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Password ─────────────────────────────────────────────────────
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Confirm Password ─────────────────────────────────────────────
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Section Divider ──────────────────────────────────────────────
                Divider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Student Verification",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // ── Verification Type Dropdown ───────────────────────────────────
                ExposedDropdownMenuBox(
                    expanded = verificationExpanded,
                    onExpandedChange = { verificationExpanded = !verificationExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (selectedVerification.isNotEmpty()) 16.dp else 24.dp)
                ) {
                    OutlinedTextField(
                        value = selectedVerification,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Verification Type") },
                        placeholder = { Text("Select a verification type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = verificationExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = verificationExpanded,
                        onDismissRequest = { verificationExpanded = false }
                    ) {
                        verificationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Badge,
                                            contentDescription = null,
                                            tint = DarkGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = option, fontSize = 15.sp)
                                    }
                                },
                                onClick = {
                                    selectedVerification = option
                                    verificationExpanded = false
                                    documentUri = null
                                    documentName = ""
                                }
                            )
                        }
                    }
                }

                // ── File Upload Section (shows when a type is selected) ───────────
                if (selectedVerification.isNotEmpty()) {
                    Text(
                        text = "Upload $selectedVerification",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    val docUri = documentUri
                    if (docUri == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                )
                                .clickable { docLauncher.launch("image/*") }
                                .padding(vertical = 32.dp, horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Tap to upload",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "JPG, PNG accepted · Max 5 MB",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    DarkGreen.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .background(DarkGreen.copy(alpha = 0.06f))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = DarkGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = documentName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Uploaded successfully",
                                    fontSize = 11.sp,
                                    color = DarkGreen
                                )
                            }
                            TextButton(
                                onClick = {
                                    documentUri = null
                                    documentName = ""
                                }
                            ) {
                                Text(
                                    text = "Remove",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // ── Success Dialog ────────────────────────────────────────────────
                successMessage?.let { msg ->
                    AlertDialog(
                        onDismissRequest = {
                            successMessage = null
                            navController.navigate("login")
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = DarkGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = { Text("Registration Successful", fontWeight = FontWeight.Bold) },
                        text = { Text(msg) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    successMessage = null
                                    navController.navigate("login")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Text("Go to Login", color = Color.White, fontWeight = FontWeight.SemiBold)
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

                // ── Sign Up Button ────────────────────────────────────────────────
                Button(
                    onClick = {
                        scope.launch {
                            // Validation
                            if (firstName.isBlank()) {
                                errorMessage = "First name is required"
                                return@launch
                            }
                            if (lastName.isBlank()) {
                                errorMessage = "Last name is required"
                                return@launch
                            }
                            if (email.isEmpty()) {
                                errorMessage = "Email is required"
                                return@launch
                            }
                            if (!email.endsWith("@student.fatima.edu.ph")) {
                                errorMessage = "Email must end with @student.fatima.edu.ph"
                                return@launch
                            }
                            if (profileUri == null) {
                                errorMessage = "Please upload a profile photo"
                                return@launch
                            }
                            if (password.isEmpty()) {
                                errorMessage = "Password is required"
                                return@launch
                            }
                            if (password.length < 8) {
                                errorMessage = "Password must be at least 8 characters"
                                return@launch
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                                return@launch
                            }
                            if (selectedVerification.isEmpty()) {
                                errorMessage = "Please select a verification type"
                                return@launch
                            }
                            if (documentUri == null) {
                                errorMessage = "Please upload your $selectedVerification"
                                return@launch
                            }

                            // Map display label → API value
                            val verificationUseValue = when (selectedVerification) {
                                "Student ID" -> "student_id"
                                "Registration Card" -> "registration_card"
                                else -> selectedVerification.lowercase().replace(" ", "_")
                            }

                            errorMessage = null
                            isLoading = true

                            try {
                                val (success, message) = withContext(Dispatchers.IO) {
                                    registerUser(
                                        context = context,
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        password = password,
                                        passwordConfirmation = confirmPassword,
                                        profilePictureUri = profileUri!!,
                                        studentIdPhotoUri = documentUri!!,
                                        verificationUse = verificationUseValue
                                    )
                                }
                                if (success) {
                                    successMessage = message
                                } else {
                                    errorMessage = message
                                }
                            } catch (e: Exception) {
                                errorMessage = "Registration failed: ${e.message}"
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
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

private fun registerUser(
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

    httpClient.newCall(request).execute().use { response ->
        val body = response.body?.string() ?: ""
        return if (response.isSuccessful) {
            val serverMsg = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
                ?: "Your account has been created and is pending verification."
            Pair(true, serverMsg)
        } else {
            Pair(false, "[HTTP ${response.code}] ${parseErrorMessage(body)}")
        }
    }
}

private fun parseErrorMessage(body: String): String {
    // Laravel returns validation errors as: {"errors": {"field": ["msg1", "msg2"]}, "message": "..."}
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

private fun getFileName(context: Context, uri: Uri): String {
    var result = "Document"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) result = cursor.getString(index)
            }
        }
    } catch (e: Exception) {
        result = "Selected document"
    }
    return result
}