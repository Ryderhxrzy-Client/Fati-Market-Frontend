package com.example.fati_market_frontend

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.fati_market_frontend.ui.theme.DarkGreen
import com.example.fati_market_frontend.ui.theme.DarkGreenLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// import com.pusher.client.Pusher
// import com.pusher.client.PusherOptions
// import android.util.Log
// import com.pusher.client.Authorizer
// import com.pusher.client.AuthorizationFailureException
// import com.pusher.client.channel.PrivateChannelEventListener
// import com.pusher.client.channel.PusherEvent
// import com.pusher.client.channel.SubscriptionEventListener
// import com.pusher.client.connection.ConnectionEventListener
// import com.pusher.client.connection.ConnectionState
// import com.pusher.client.connection.ConnectionStateChange
// import androidx.compose.foundation.Canvas
// import android.os.Handler
// import android.os.Looper
// import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

// ── Pusher global debug state (readable from any composable) ───────────────────
// private val pusherGlobalStatus = mutableStateOf("idle")
// private val pusherGlobalLog    = mutableStateOf("")

// ── Drawer Pages ───────────────────────────────────────────────────────────────

private sealed class DrawerPage(val label: String) {
    object Dashboard         : DrawerPage("Dashboard")
    object PrivateOffers     : DrawerPage("Private Offers")
    object AcquiredItems     : DrawerPage("Acquired Items")
    object PublicListings    : DrawerPage("Public Listings")
    object ReservedItems     : DrawerPage("Reserved Items")
    object SoldItems         : DrawerPage("Sold Items")
    object PointsGiven       : DrawerPage("Points Given")
    object PointsReceived    : DrawerPage("Points Received")
    object CashTransactions  : DrawerPage("Cash Transactions")
    object TradeTransactions : DrawerPage("Trade Transactions")
    object TransactionHistory: DrawerPage("Transaction History")
    object ProfitSummary     : DrawerPage("Profit Summary")
    object TotalItemAcquired : DrawerPage("Total Item Acquired")
    object TotalItemSold     : DrawerPage("Total Item Sold")
    object TotalProfit       : DrawerPage("Total Profit (from markup)")
    object MostSoldCategory  : DrawerPage("Most Sold Category")
    object ActiveUsers       : DrawerPage("Active Users")
    object Categories        : DrawerPage("Categories")
    object ActivityLogs      : DrawerPage("Activity Logs")
}

private enum class AdminTab { HOME, CHAT, USERS, SETTINGS, PROFILE }

// ── Student model (fields match the API response exactly) ──────────────────────

private data class Student(
    val studentVerificationId: Int,
    val userId: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val profilePicture: String?,
    val verificationDocument: String?,
    val verificationType: String?,
    val isVerified: Boolean,
    val walletPoints: Int,
    val isActive: Boolean,
    val registeredDate: String?,
    val status: String,
    val reason: String?
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val initial:  String get() = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    /** Bridges cases where is_verified=true but the status field hasn't synced yet */
    val displayStatus: String get() = when {
        status.trim().lowercase() == "declined"              -> "declined"
        status.trim().lowercase() == "blocked"               -> "blocked"
        status.trim().lowercase() == "approved" || isVerified -> "approved"
        else                                                 -> "pending"
    }
}




// ── Network helpers ────────────────────────────────────────────────────────────

private val adminHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
    .build()

// ── Pusher config ──────────────────────────────────────────────────────────────
// private const val PUSHER_APP_KEY  = "c7e99894a56ad96376f6"
// private const val PUSHER_CLUSTER  = "ap2"
// private const val PUSHER_AUTH_URL = "https://fati-api.alertaraqc.com/broadcasting/auth"

// /**
//  * Authorizer that attaches the user's Bearer token to every Pusher private-channel
//  * auth request.  Runs on Pusher's internal thread, so blocking I/O is fine here.
//  */
// private class BearerTokenAuthorizer(
//     private val authUrl: String,
//     private val token: String
// ) : Authorizer {
//     override fun authorize(channelName: String, socketId: String): String {
//         val body = FormBody.Builder()
//             .add("socket_id", socketId)
//             .add("channel_name", channelName)
//             .build()
//         val request = Request.Builder()
//             .url(authUrl)
//             .post(body)
//             .header("Authorization", "Bearer $token")
//             .header("Content-Type", "application/x-www-form-urlencoded")
//             .build()
//         return adminHttpClient.newCall(request).execute().use { response ->
//             val rawBody = response.body?.string() ?: ""
//             // Logcat only — never touch Compose state from a background thread
//             Log.d("PusherAuth", "HTTP ${response.code} | channel=$channelName | body=$rawBody")
//
//             if (!response.isSuccessful) {
//                 // Must throw AuthorizationFailureException — Pusher 2.4.x only catches this type.
//                 // A generic Exception escapes uncaught on the background thread and kills the app.
//                 throw AuthorizationFailureException("HTTP ${response.code}: $rawBody")
//             }
//
//             // Pusher expects {"auth":"appKey:sig"} at the top level.
//             // If the server wraps it (e.g. {"data":{"auth":...}}) unwrap it.
//             try {
//                 val json = JSONObject(rawBody)
//                 if (json.has("auth")) {
//                     rawBody
//                 } else {
//                     val inner = json.optJSONObject("data")
//                         ?: json.optJSONObject("result")
//                         ?: json.optJSONObject("payload")
//                     if (inner != null && inner.has("auth")) {
//                         Log.d("PusherAuth", "Unwrapped auth from nested object")
//                         inner.toString()
//                     } else {
//                         Log.e("PusherAuth", "No 'auth' field found. Full response: $rawBody")
//                         throw AuthorizationFailureException("missing 'auth' field. Server said: $rawBody")
//                     }
//                 }
//             } catch (e: AuthorizationFailureException) {
//                 throw e  // already correct type, re-throw directly
//             } catch (e: Exception) {
//                 Log.e("PusherAuth", "Auth response error: $rawBody", e)
//                 throw AuthorizationFailureException(e.message ?: "parse error")
//             }
//         }
//     }
// }

private fun fetchStudents(token: String, status: String? = null): List<Student> {
    val url = if (status != null)
        "https://fati-api.alertaraqc.com/api/admin/students?status=$status"
    else
        "https://fati-api.alertaraqc.com/api/admin/students"

    val request = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .get()
        .build()

    adminHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string() ?: "[]"
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}: $body")
        return parseStudents(body)
    }
}

/** Update a student's verification status. Returns true on success.
 *  action must be "approve" or "decline" — maps directly to the API endpoint path. */
private fun updateStudentStatus(
    token: String,
    userId: Int,
    action: String,       // "approve" or "decline"
    reason: String? = null
): Boolean {
    val payload = buildString {
        append("{")
        if (!reason.isNullOrBlank()) append("\"reason\":\"$reason\"")
        append("}")
    }
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/admin/students/$userId/$action")
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .put(payload.toRequestBody("application/json".toMediaType()))
        .build()

    adminHttpClient.newCall(request).execute().use { return it.isSuccessful }
}


/** Upload the admin's own profile picture. Returns the new picture URL on success, null otherwise. */
private fun uploadProfilePicture(token: String, file: File, mimeType: String = "image/jpeg"): String? {
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "profile_picture",
            file.name,
            file.asRequestBody(mimeType.toMediaType())
        )
        .build()

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/profile/picture")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(requestBody)
        .build()

    adminHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string()
        if (!response.isSuccessful) return null
        if (body == null) return null
        return try {
            val json = JSONObject(body)
            // Try common response shapes
            json.strOrNull("profile_picture")
                ?: json.strOrNull("picture_url")
                ?: json.strOrNull("url")
                ?: json.optJSONObject("data")?.strOrNull("profile_picture")
                ?: json.optJSONObject("data")?.strOrNull("picture_url")
                ?: json.optJSONObject("user")?.strOrNull("profile_picture")
                // If upload succeeded but URL not in response, return empty string so caller knows success
                ?: ""
        } catch (_: Exception) { "" }
    }
}

private fun parseStudents(json: String): List<Student> {
    val list = mutableListOf<Student>()
    try {
        val root = JSONObject(json)
        val arr = when {
            root.has("data")     -> root.getJSONArray("data")
            root.has("students") -> root.getJSONArray("students")
            else                 -> null
        }
        arr?.let { for (i in 0 until it.length()) list.add(parseStudent(it.getJSONObject(i))) }
    } catch (_: Exception) {
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) list.add(parseStudent(arr.getJSONObject(i)))
        } catch (_: Exception) { /* empty */ }
    }
    return list
}

private fun JSONObject.strOrNull(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key, "").takeIf { it.isNotEmpty() }
}

private fun parseStudent(obj: JSONObject) = Student(
    studentVerificationId = obj.optInt("student_verification_id", 0),
    userId                = obj.optInt("user_id", 0),
    email                 = obj.optString("email", ""),
    firstName             = obj.optString("first_name", ""),
    lastName              = obj.optString("last_name", ""),
    profilePicture        = obj.strOrNull("profile_picture"),
    verificationDocument  = obj.strOrNull("verification_document"),
    verificationType      = obj.strOrNull("verification_type"),
    isVerified            = obj.optBoolean("is_verified", false),
    walletPoints          = obj.optInt("wallet_points", 0),
    isActive              = try { obj.getInt("is_active") == 1 }
                            catch (_: Exception) { obj.optBoolean("is_active", false) },
    registeredDate        = obj.strOrNull("registered_date"),
    status                = obj.optString("status", "pending"),
    reason                = obj.strOrNull("reason")
)

/** "2026-02-25T05:53:47.000000Z" → "Feb 25, 2026" */
private fun formatDate(raw: String?): String {
    if (raw == null) return "N/A"
    return try {
        val date   = raw.split("T")[0].split("-")
        val months = listOf("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        "${months[date[1].toInt()]} ${date[2].toInt()}, ${date[0]}"
    } catch (_: Exception) { raw }
}

/** Animated shimmer placeholder shown while an image is loading */
@Composable
private fun ShimmerEffect(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue  = -600f,
        targetValue   = 600f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant
                ),
                start = Offset(translateX, 0f),
                end   = Offset(translateX + 600f, 0f)
            )
        )
    )
}

private fun statusColor(status: String) = when (status.lowercase()) {
    "approved" -> Color(0xFF4CAF50)
    "declined" -> Color(0xFFF44336)
    "blocked"  -> Color(0xFF9C27B0)
    else       -> Color(0xFFFF9800)
}

// ── Admin Dashboard ────────────────────────────────────────────────────────────

@Composable
fun AdminDashboard(isDarkMode: Boolean, onThemeToggle: () -> Unit, onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }

    var userFirstName    by remember { mutableStateOf(prefs.getString("user_first_name", "") ?: "") }
    var userLastName     by remember { mutableStateOf(prefs.getString("user_last_name",  "") ?: "") }
    val userEmail        = remember { prefs.getString("user_email", "") ?: "" }
    val userRole         = remember { prefs.getString("user_role", "admin") ?: "admin" }
    val userWalletPoints = remember { prefs.getInt("user_wallet_points", 0) }
    var userProfilePic   by remember { mutableStateOf(prefs.getString("user_profile_picture", "") ?: "") }

    var selectedTab       by remember { mutableStateOf(AdminTab.HOME) }
    var drawerPage        by remember { mutableStateOf<DrawerPage?>(null) }
    var chatConversation  by remember { mutableStateOf<Conversation?>(null) }
    val drawerState       = rememberDrawerState(DrawerValue.Closed)
    val scope             = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
                drawerShape = RoundedCornerShape(0.dp),
                windowInsets = WindowInsets(0),
                modifier = Modifier.width(300.dp)
            ) {
                AdminDrawerContent(
                    currentPage   = drawerPage,
                    userFirstName = userFirstName,
                    userLastName  = userLastName,
                    userEmail     = userEmail,
                    userRole      = userRole,
                    userProfilePic = userProfilePic,
                    onPageSelect  = { page ->
                        if (page == DrawerPage.Dashboard) {
                            drawerPage = null
                            selectedTab = AdminTab.HOME
                        } else {
                            drawerPage = page
                        }
                        scope.launch { drawerState.close() }
                    },
                    onLogout = onLogout
                )
            }
        }
    ) {
        val chatIsOpen = selectedTab == AdminTab.CHAT && chatConversation != null
        Scaffold(
            bottomBar = {
                // Read inside the lambda so it picks up any CompositionLocalProvider
                // set by composables inside the content slot (e.g. DrawerPageContent)
                val showBottomBar = LocalProvidesBottomBar.current
                if (!chatIsOpen && showBottomBar) {
                    AdminBottomBar(
                        selected       = selectedTab,
                        userProfilePic = userProfilePic,
                        userInitial    = userFirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                        onSelect       = { tab ->
                            selectedTab      = tab
                            drawerPage       = null
                            chatConversation = null
                        }
                    )
                }
            },
            contentWindowInsets = WindowInsets(0),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            val showBottomBar = LocalProvidesBottomBar.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (chatIsOpen || !showBottomBar) 0.dp else innerPadding.calculateBottomPadding())
            ) {
                if (drawerPage != null) {
                    DrawerPageContent(
                        page        = drawerPage!!,
                        onMenuClick = openDrawer,
                        onGoToChat  = { drawerPage = null; selectedTab = AdminTab.CHAT }
                    )
                } else {
                    when (selectedTab) {
                        AdminTab.HOME     -> AdminHomeContent(onMenuClick = openDrawer)
                        AdminTab.CHAT     -> AdminChatContent(
                            onMenuClick          = openDrawer,
                            selectedConversation = chatConversation,
                            onSelectConversation = { chatConversation = it }
                        )
                        AdminTab.USERS    -> AdminUsersContent(onMenuClick = openDrawer)
                        AdminTab.SETTINGS -> AdminSettingsContent(isDarkMode, onThemeToggle, onMenuClick = openDrawer)
                        AdminTab.PROFILE  -> AdminProfileContent(
                            onMenuClick        = openDrawer,
                            firstName          = userFirstName,
                            lastName           = userLastName,
                            email              = userEmail,
                            role               = userRole,
                            walletPoints       = userWalletPoints,
                            profilePic         = userProfilePic,
                            onProfilePicUpdated = { path ->
                                userProfilePic = path
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Drawer ─────────────────────────────────────────────────────────────────────

@Composable
private fun AdminDrawerContent(
    currentPage: DrawerPage?,
    userFirstName: String,
    userLastName: String,
    userEmail: String,
    userRole: String,
    userProfilePic: String,
    onPageSelect: (DrawerPage) -> Unit,
    onLogout: () -> Unit
) {
    val fullName = "$userFirstName $userLastName".trim().ifBlank { "Administrator" }
    val initial  = userFirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "A"

    var showLogoutDialog     by remember { mutableStateOf(false) }
    var inventoryExpanded    by remember { mutableStateOf(false) }
    var transactionsExpanded by remember { mutableStateOf(false) }
    var reportsExpanded      by remember { mutableStateOf(false) }

    // ── Logout confirmation dialog ────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(Icons.Filled.Logout, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
            },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text  = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Logout", color = Color.White, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    LaunchedEffect(currentPage) {
        when (currentPage) {
            is DrawerPage.PrivateOffers, is DrawerPage.AcquiredItems,
            is DrawerPage.PublicListings, is DrawerPage.ReservedItems,
            is DrawerPage.SoldItems -> inventoryExpanded = true
            is DrawerPage.PointsGiven, is DrawerPage.PointsReceived,
            is DrawerPage.CashTransactions, is DrawerPage.TradeTransactions,
            is DrawerPage.TransactionHistory, is DrawerPage.ProfitSummary -> transactionsExpanded = true
            is DrawerPage.TotalItemAcquired, is DrawerPage.TotalItemSold,
            is DrawerPage.TotalProfit, is DrawerPage.MostSoldCategory,
            is DrawerPage.ActiveUsers -> reportsExpanded = true
            else -> {}
        }
    }

    Column(modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (userProfilePic.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = userProfilePic,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = { ShimmerEffect(Modifier.fillMaxSize()) },
                            error = {
                                Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        )
                    } else {
                        Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(fullName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(userEmail.ifBlank { userRole.replaceFirstChar { it.uppercaseChar() } },
                        color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                    Text(userRole.replaceFirstChar { it.uppercaseChar() },
                        color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        DrawerItem(Icons.Filled.Dashboard, "Dashboard",
            selected = currentPage == null || currentPage == DrawerPage.Dashboard
        ) { onPageSelect(DrawerPage.Dashboard) }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant)

        DrawerSectionHeader(Icons.Filled.Inventory, "Inventory Management", inventoryExpanded) { inventoryExpanded = !inventoryExpanded }
        AnimatedVisibility(visible = inventoryExpanded) {
            Column {
                DrawerSubItem("Private Offers",  currentPage == DrawerPage.PrivateOffers)  { onPageSelect(DrawerPage.PrivateOffers) }
                DrawerSubItem("Acquired Items",  currentPage == DrawerPage.AcquiredItems)  { onPageSelect(DrawerPage.AcquiredItems) }
                DrawerSubItem("Public Listings", currentPage == DrawerPage.PublicListings) { onPageSelect(DrawerPage.PublicListings) }
                DrawerSubItem("Reserved Items",  currentPage == DrawerPage.ReservedItems)  { onPageSelect(DrawerPage.ReservedItems) }
                DrawerSubItem("Sold Items",      currentPage == DrawerPage.SoldItems)      { onPageSelect(DrawerPage.SoldItems) }
            }
        }

        DrawerSectionHeader(Icons.Filled.Receipt, "Transactions", transactionsExpanded) { transactionsExpanded = !transactionsExpanded }
        AnimatedVisibility(visible = transactionsExpanded) {
            Column {
                DrawerSubItem("Points Given",        currentPage == DrawerPage.PointsGiven)        { onPageSelect(DrawerPage.PointsGiven) }
                DrawerSubItem("Points Received",     currentPage == DrawerPage.PointsReceived)     { onPageSelect(DrawerPage.PointsReceived) }
                DrawerSubItem("Cash Transactions",   currentPage == DrawerPage.CashTransactions)   { onPageSelect(DrawerPage.CashTransactions) }
                DrawerSubItem("Trade Transactions",  currentPage == DrawerPage.TradeTransactions)  { onPageSelect(DrawerPage.TradeTransactions) }
                DrawerSubItem("Transaction History", currentPage == DrawerPage.TransactionHistory) { onPageSelect(DrawerPage.TransactionHistory) }
                DrawerSubItem("Profit Summary",      currentPage == DrawerPage.ProfitSummary)      { onPageSelect(DrawerPage.ProfitSummary) }
            }
        }

        DrawerSectionHeader(Icons.Filled.BarChart, "Reports / Analytics", reportsExpanded) { reportsExpanded = !reportsExpanded }
        AnimatedVisibility(visible = reportsExpanded) {
            Column {
                DrawerSubItem("Total Item Acquired",        currentPage == DrawerPage.TotalItemAcquired) { onPageSelect(DrawerPage.TotalItemAcquired) }
                DrawerSubItem("Total Item Sold",            currentPage == DrawerPage.TotalItemSold)     { onPageSelect(DrawerPage.TotalItemSold) }
                DrawerSubItem("Total Profit (from markup)", currentPage == DrawerPage.TotalProfit)       { onPageSelect(DrawerPage.TotalProfit) }
                DrawerSubItem("Most Sold Category",         currentPage == DrawerPage.MostSoldCategory)  { onPageSelect(DrawerPage.MostSoldCategory) }
                DrawerSubItem("Active Users",               currentPage == DrawerPage.ActiveUsers)       { onPageSelect(DrawerPage.ActiveUsers) }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant)
        DrawerItem(Icons.Filled.Category, "Categories", currentPage == DrawerPage.Categories) { onPageSelect(DrawerPage.Categories) }
        DrawerItem(Icons.Filled.EventNote, "Activity Logs", currentPage == DrawerPage.ActivityLogs) { onPageSelect(DrawerPage.ActivityLogs) }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant)

        // Logout
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { showLogoutDialog = true }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Logout, null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text("Logout", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) DarkGreen.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null,
            tint = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DrawerSectionHeader(icon: ImageVector, label: String, expanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun DrawerSubItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 44.dp, end = 12.dp, top = 1.dp, bottom = 1.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) DarkGreen.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape)
            .background(if (selected) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
    }
}

@Composable
private fun DrawerPageContent(page: DrawerPage, onMenuClick: () -> Unit, onGoToChat: () -> Unit = {}) {
    when (page) {
        DrawerPage.PrivateOffers -> AdminPrivateOffersContent(onMenuClick = onMenuClick, onGoToChat = onGoToChat)
        else -> Column(modifier = Modifier.fillMaxSize()) {
            AdminPageHeader(title = page.label, onMenuClick = onMenuClick)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Construction, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(page.label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Coming soon", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private enum class ItemStatus(val displayName: String) {
    PRIVATE("private"),
    ACQUIRED("acquired"),
    PUBLIC("public"),
    RESERVED("reserved"),
    SOLD("sold");
    
    companion object {
        fun fromString(value: String): ItemStatus {
            return when (value.lowercase()) {
                "private" -> PRIVATE
                "acquired" -> ACQUIRED
                "public" -> PUBLIC
                "reserved" -> RESERVED
                "sold" -> SOLD
                else -> PRIVATE
            }
        }
    }
}

// ── Private Offers ─────────────────────────────────────────────────────────────
@Composable
private fun AdminPrivateOffersContent(onMenuClick: () -> Unit, onGoToChat: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token   = remember { prefs.getString("auth_token", "") ?: "" }
    val scope   = rememberCoroutineScope()

    var itemList     by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingItem  by remember { mutableStateOf<Item?>(null) }

    fun loadItems() {
        scope.launch {
            isLoading    = true
            errorMessage = null
            try {
                itemList = withContext(Dispatchers.IO) { fetchItems(token, "private") }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load items"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadItems() }

    // Pass editing state up to hide bottom bar in AdminDashboard
    CompositionLocalProvider(
        LocalProvidesBottomBar provides (editingItem == null)
    ) {
        AnimatedContent(
            targetState = editingItem != null,
            label = "EditItemTransition"
        ) { isEditing ->
            if (isEditing && editingItem != null) {
                EditItemPageForList(
                    item = editingItem!!,
                    token = token,
                    onBack = { editingItem = null },
                    onItemUpdated = { updatedItem ->
                        itemList = itemList.map {
                            if (it.itemId == updatedItem.itemId) {
                                it.copy(
                                    status = updatedItem.status,
                                    markupPoints = updatedItem.markupPoints
                                )
                            } else it
                        }
                        editingItem = null
                    }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    AdminPageHeader(title = "Private Offers", onMenuClick = onMenuClick)

                    when {
                        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DarkGreen)
                        }
                        errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                Icon(Icons.Filled.ErrorOutline, null,
                                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                                Button(onClick = { loadItems() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) {
                                    Text("Retry", color = Color.White)
                                }
                            }
                        }
                        itemList.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Inventory2, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                                Text("No private offers at the moment.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        else -> LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(itemList, key = { it.itemId }) { item ->
                                AdminPrivateOfferCard(
                                    item          = item,
                                    token         = token,
                                    onStatusSaved = { newStatus ->
                                        itemList = itemList.map {
                                            if (it.itemId == item.itemId) it.copy(status = newStatus) else it
                                        }
                                    },
                                    onGoToChat    = onGoToChat,
                                    onEditClick   = { editingItem = it }
                                )
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

private val LocalProvidesBottomBar = compositionLocalOf { true }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPrivateOfferCard(
    item: Item,
    token: String,
    onStatusSaved: (String) -> Unit,
    onGoToChat: () -> Unit = {},
    onEditClick: (Item) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    var showChatDialog    by remember { mutableStateOf(false) }
    var chatText          by remember { mutableStateOf("") }
    var isSendingChat     by remember { mutableStateOf(false) }
    var chatError         by remember { mutableStateOf<String?>(null) }
    var chatSent          by remember { mutableStateOf(false) }

    // ── Chat dialog ───────────────────────────────────────────────────────────
    if (showChatDialog) {
        AlertDialog(
            onDismissRequest = {
                showChatDialog = false; chatText = ""; chatError = null; chatSent = false
            },
            title = {
                Text("Message Seller", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Send a message to ${item.sellerEmail}",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (chatSent) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.CheckCircle, null,
                                tint = DarkGreen, modifier = Modifier.size(40.dp))
                            Text(
                                "Message sent successfully!",
                                color      = DarkGreen,
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign  = TextAlign.Center
                            )
                            Text(
                                "Would you like to go to the chat?",
                                fontSize  = 12.sp,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        chatError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                        OutlinedTextField(
                            value         = chatText,
                            onValueChange = { chatText = it; chatError = null },
                            placeholder   = { Text("Type your message...") },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            maxLines      = 4
                        )
                    }
                }
            },
            confirmButton = {
                if (chatSent) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            showChatDialog = false; chatText = ""; chatSent = false
                        }) {
                            Text("Close")
                        }
                        Button(
                            onClick = {
                                showChatDialog = false; chatText = ""; chatSent = false
                                onGoToChat()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                        ) {
                            Icon(Icons.Filled.Chat, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Go to Chat", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (chatText.isBlank()) { chatError = "Please enter a message."; return@Button }
                            scope.launch {
                                isSendingChat = true
                                val ok = withContext(Dispatchers.IO) {
                                    sendMessage(token, item.itemId, item.sellerId, chatText.trim())
                                }
                                isSendingChat = false
                                if (ok) chatSent = true else chatError = "Failed to send. Please try again."
                            }
                        },
                        enabled = !isSendingChat,
                        colors  = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        if (isSendingChat) {
                            CircularProgressIndicator(color = Color.White,
                                modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Send", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            dismissButton = if (!chatSent) {
                { TextButton(onClick = { showChatDialog = false; chatText = ""; chatError = null }) { Text("Cancel") } }
            } else {
                null
            }
        )
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Photo ─────────────────────────────────────────────────────────
            val photoUrl = item.photos.firstOrNull() ?: ""
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model              = photoUrl,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Photo, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp))
                }
            }

            Column(
                modifier            = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Title + current status chip ────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    val statusColor = when (item.status.lowercase()) {
                        "approved" -> DarkGreen
                        "rejected" -> MaterialTheme.colorScheme.error
                        else       -> Color(0xFFE65100)
                    }
                    val statusBg = when (item.status.lowercase()) {
                        "approved" -> DarkGreen.copy(alpha = 0.12f)
                        "rejected" -> MaterialTheme.colorScheme.errorContainer
                        else       -> Color(0xFFFF8F00).copy(alpha = 0.12f)
                    }
                    Surface(shape = RoundedCornerShape(50), color = statusBg) {
                        Text(
                            item.status.replaceFirstChar { it.uppercaseChar() },
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = statusColor
                        )
                    }
                }

                // ── Seller + price ─────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Person, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp))
                        Text(item.sellerEmail, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.MonetizationOn, null,
                            tint = DarkGreen, modifier = Modifier.size(14.dp))
                        Text("${item.pricePoints} pts", fontWeight = FontWeight.Bold,
                            color = DarkGreen, fontSize = 13.sp)
                    }
                }

                Text(item.description, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3, overflow = TextOverflow.Ellipsis)

                HorizontalDivider()

                // ── Action buttons ─────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onEditClick(item) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit Item", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick  = { showChatDialog = true },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        border   = BorderStroke(1.dp, DarkGreen),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = DarkGreen)
                    ) {
                        Icon(Icons.Filled.Chat, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Chat Seller", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Bottom Nav ─────────────────────────────────────────────────────────────────

@Composable
private fun AdminBottomBar(
    selected: AdminTab,
    userProfilePic: String,
    userInitial: String,
    onSelect: (AdminTab) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModernNavItem(
                        outlinedIcon = Icons.Outlined.Home,
                        filledIcon = Icons.Filled.Home,
                        label = "Home",
                        selected = selected == AdminTab.HOME,
                        modifier = Modifier.weight(1f)
                    ) { onSelect(AdminTab.HOME) }
                    ModernNavItem(
                        outlinedIcon = Icons.Outlined.Chat,
                        filledIcon = Icons.Filled.Chat,
                        label = "Chat",
                        selected = selected == AdminTab.CHAT,
                        modifier = Modifier.weight(1f)
                    ) { onSelect(AdminTab.CHAT) }
                    // Spacer for center FAB
                    Spacer(modifier = Modifier.weight(1f))
                    ModernNavItem(
                        outlinedIcon = Icons.Outlined.Settings,
                        filledIcon = Icons.Filled.Settings,
                        label = "Settings",
                        selected = selected == AdminTab.SETTINGS,
                        modifier = Modifier.weight(1f)
                    ) { onSelect(AdminTab.SETTINGS) }
                    ProfileNavItem(
                        userProfilePic = userProfilePic,
                        userInitial = userInitial,
                        selected = selected == AdminTab.PROFILE,
                        modifier = Modifier.weight(1f)
                    ) { onSelect(AdminTab.PROFILE) }
                }
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
        // Center FAB — sits above the bar with its label below it
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = { onSelect(AdminTab.USERS) },
                modifier = Modifier
                    .size(54.dp)
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
                shape = CircleShape,
                containerColor = if (selected == AdminTab.USERS) DarkGreenLight else DarkGreen,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp, pressedElevation = 10.dp
                )
            ) {
                Icon(Icons.Filled.Group, "Users", modifier = Modifier.size(24.dp))
            }
            Text(
                text = "Users",
                fontSize = 10.sp,
                fontWeight = if (selected == AdminTab.USERS) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected == AdminTab.USERS) DarkGreen
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun ModernNavItem(
    outlinedIcon: ImageVector,
    filledIcon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tint = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (selected) DarkGreen.copy(alpha = 0.12f) else Color.Transparent,
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 16.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) filledIcon else outlinedIcon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = tint,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun ProfileNavItem(
    userProfilePic: String,
    userInitial: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tint = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (selected) DarkGreen.copy(alpha = 0.12f) else Color.Transparent,
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (selected) 1.5.dp else 0.dp,
                        color = DarkGreen,
                        shape = CircleShape
                    )
                    .background(
                        if (selected) DarkGreen
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (userProfilePic.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = userProfilePic,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = {
                            Text(userInitial, fontSize = 9.sp,
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    )
                } else {
                    Text(
                        text = userInitial,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
        Text(
            text = "Profile",
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = tint,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ── Home ───────────────────────────────────────────────────────────────────────

@Composable
private fun AdminHomeContent(onMenuClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        AdminPageHeader(title = "Dashboard", onMenuClick = onMenuClick)
        Column(modifier = Modifier.padding(16.dp)) {
            Text("OVERVIEW", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = DarkGreen, letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total Students", "--", Icons.Filled.School, Modifier.weight(1f))
                StatCard("Pending", "--", Icons.Filled.HourglassEmpty, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Products", "--", Icons.Filled.Storefront, Modifier.weight(1f))
                StatCard("Active Chats", "--", Icons.Filled.Chat, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = DarkGreen,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(DarkGreen.copy(alpha = 0.1f)).padding(6.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Chat data models ────────────────────────────────────────────────────────────

data class ChatItem(
    val itemId: Int,
    val title: String,
    val description: String,
    val markupPoints: Int,
    val sellerEmail: String,
    val status: String,
    val photos: List<String>
)

data class Conversation(
    val otherUserId: Int,
    val otherUserEmail: String,
    val firstName: String,
    val lastName: String,
    val profilePicture: String,
    val itemId: Int,
    val itemTitle: String,
    val itemStatus: String = "",
    val latestMessage: String,
    val lastMessageAt: String,
    val messageCount: Int,
    val unreadCount: Int = 0
)

data class ChatMessage(
    val messageId: Int,
    val itemId: Int,
    val itemTitle: String,
    val senderId: Int,
    val senderName: String,
    val senderProfilePicture: String,
    val receiverId: Int,
    val receiverName: String,
    val receiverProfilePicture: String,
    val message: String,
    val sentAt: String
)

// ── Chat API ────────────────────────────────────────────────────────────────────

private fun fetchConversations(token: String): List<Conversation> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/conversations")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val list = mutableListOf<Conversation>()
            val arr = when {
                body.trimStart().startsWith("[") -> org.json.JSONArray(body)
                else -> {
                    val obj = org.json.JSONObject(body)
                    obj.optJSONArray("data")
                        ?: obj.optJSONArray("conversations")
                        ?: obj.optJSONArray("messages")
                        ?: return emptyList()
                }
            }
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                // Try every plausible field name the API might use for "the other person"
                val userId = obj.optInt("other_user_id").takeIf { it != 0 }
                    ?: obj.optInt("sender_id").takeIf { it != 0 }
                    ?: obj.optInt("receiver_id").takeIf { it != 0 }
                    ?: obj.optInt("admin_id").takeIf { it != 0 }
                    ?: obj.optInt("user_id", 0)
                val itemId = obj.optInt("item_id").takeIf { it != 0 }
                    ?: obj.optInt("item", 0)
                list.add(Conversation(
                    otherUserId      = userId,
                    otherUserEmail   = obj.optString("other_user_email").ifBlank { obj.optString("email") },
                    firstName        = obj.optString("first_name"),
                    lastName         = obj.optString("last_name"),
                    profilePicture   = obj.optString("profile_picture"),
                    itemId           = itemId,
                    itemTitle        = obj.optString("item_title").ifBlank { obj.optString("title") },
                    itemStatus       = obj.optString("item_status").ifBlank { obj.optString("status") },
                    latestMessage    = obj.optString("latest_message").ifBlank { obj.optString("last_message") },
                    lastMessageAt    = obj.optString("last_message_at").ifBlank { obj.optString("updated_at") },
                    messageCount     = obj.optInt("message_count"),
                    unreadCount      = obj.optInt("unread_count", 0)
                ))
            }
            // Keep each unique user+item pair as its own conversation
            list.distinctBy { "${it.otherUserId}_${it.itemId}" }
        }
    } catch (_: Exception) { emptyList() }
}

/**
 * Returns the message list on success.
 * Throws an Exception with the HTTP status + response body on failure so the
 * caller can display a meaningful error (instead of silently returning null).
 */
private fun fetchMessages(token: String, itemId: Int, otherUserId: Int = 0): List<ChatMessage> {
    val base = "https://fati-api.alertaraqc.com/api/messages/$itemId"
    val url  = if (otherUserId != 0) "$base?other_user_id=$otherUserId" else base
    val request = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    adminHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string() ?: ""
        if (!response.isSuccessful) {
            // Truncate body so the toast/error text stays readable
            val preview = body.take(200).replace("\n", " ")
            throw Exception("HTTP ${response.code} – $preview")
        }
        val list = mutableListOf<ChatMessage>()
        // Handle: bare array, {"data":[...]}, {"messages":[...]},
        //         {"data":{"messages":[...]}}, {"data":{"data":[...]}}
        val arr: org.json.JSONArray? = when {
            body.trimStart().startsWith("[") -> org.json.JSONArray(body)
            else -> {
                val obj = org.json.JSONObject(body)
                obj.optJSONArray("data")
                    ?: obj.optJSONArray("messages")
                    ?: obj.optJSONArray("chat_messages")
                    ?: obj.optJSONObject("data")?.let { d ->
                        d.optJSONArray("messages")
                            ?: d.optJSONArray("data")
                            ?: d.optJSONArray("chat_messages")
                    }
            }
        }
        arr ?: return emptyList()   // valid 200 body but no message array → truly empty
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val senderObj   = obj.optJSONObject("sender")
            val receiverObj = obj.optJSONObject("receiver")
            val senderId = obj.optInt("sender_id").takeIf { it != 0 }
                ?: senderObj?.optInt("id", 0) ?: obj.optInt("from_id", 0)
            val receiverId = obj.optInt("receiver_id").takeIf { it != 0 }
                ?: receiverObj?.optInt("id", 0) ?: obj.optInt("to_id", 0)
            list.add(ChatMessage(
                messageId              = obj.optInt("message_id").takeIf { it != 0 }
                                         ?: obj.optInt("id").takeIf { it != 0 } ?: (i + 1),
                itemId                 = obj.optInt("item_id").takeIf { it != 0 } ?: itemId,
                itemTitle              = obj.optString("item_title"),
                senderId               = senderId,
                senderName             = obj.optString("sender_name").ifBlank {
                                         senderObj?.optString("name") ?: obj.optString("from_name") },
                senderProfilePicture   = obj.optString("sender_profile_picture").ifBlank {
                                         senderObj?.optString("profile_picture") ?: "" },
                receiverId             = receiverId,
                receiverName           = obj.optString("receiver_name").ifBlank {
                                         receiverObj?.optString("name") ?: obj.optString("to_name") },
                receiverProfilePicture = obj.optString("receiver_profile_picture").ifBlank {
                                         receiverObj?.optString("profile_picture") ?: "" },
                message                = obj.optString("message").ifBlank { obj.optString("content") },
                sentAt                 = obj.optString("sent_at").ifBlank { obj.optString("created_at") }
            ))
        }
        return list
    }
}

private fun sendMessage(token: String, itemId: Int, receiverId: Int, message: String): Boolean {
    val json = JSONObject().apply {
        put("receiver_id", receiverId)
        put("message", message)
    }.toString()
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/messages/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private fun markMessagesRead(token: String, itemId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/messages/$itemId/read")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post("{}".toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private data class EmojiItem(
    val name: String,
    val category: String,
    val htmlCode: List<String>
)

private fun htmlCodeToChar(htmlCode: String): String {
    val code = htmlCode.removePrefix("&#").removeSuffix(";").toIntOrNull() ?: return ""
    return runCatching { String(Character.toChars(code)) }.getOrElse { "" }
}

// Hardcoded categories so we can show tabs instantly without a "fetch all" call.
// Each maps to the EmojiHub API slug used in /api/all/category/{slug}.
private val emojiCategories = listOf(
    "smileys and people"  to "smileys-and-people",
    "animals and nature"  to "animals-and-nature",
    "food and drink"      to "food-and-drink",
    "travel and places"   to "travel-and-places",
    "activities"          to "activities",
    "objects"             to "objects",
    "symbols"             to "symbols",
    "flags"               to "flags"
)

// Module-level cache: survives recompositions but is cleared when the process dies.
private val emojiCache = mutableMapOf<String, List<EmojiItem>>()

private suspend fun fetchEmojisByCategory(slug: String): List<EmojiItem> = withContext(Dispatchers.IO) {
    emojiCache[slug]?.let { return@withContext it }
    val request = Request.Builder()
        .url("https://emojihub.yurace.pro/api/all/category/$slug")
        .build()
    val body = adminHttpClient.newCall(request).execute().use { it.body?.string() ?: "[]" }
    val arr = JSONArray(body)
    val result = buildList {
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val htmlArr = obj.getJSONArray("htmlCode")
            val codes = buildList { for (j in 0 until htmlArr.length()) add(htmlArr.getString(j)) }
            add(EmojiItem(obj.optString("name"), obj.optString("category"), codes))
        }
    }
    emojiCache[slug] = result
    result
}

private fun timeAgo(dateStr: String): String {
    return try {
        val date = if (dateStr.contains("T")) {
            val cleaned = dateStr.replace(Regex("\\.\\d+Z?$"), "")
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .parse(cleaned)
        } else {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).parse(dateStr)
        } ?: return dateStr
        val diff    = System.currentTimeMillis() - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours   = minutes / 60
        val days    = hours / 24
        when {
            seconds < 60 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            hours   < 24 -> "${hours}h ago"
            days    < 7  -> "${days}d ago"
            else         -> java.text.SimpleDateFormat("MMM d", java.util.Locale.US).format(date)
        }
    } catch (_: Exception) { dateStr }
}

// ── Chat ───────────────────────────────────────────────────────────────────────

@Composable
fun AdminChatContent(
    onMenuClick: () -> Unit,
    selectedConversation: Conversation?,
    onSelectConversation: (Conversation?) -> Unit,
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE) }
    val token         = remember { prefs.getString("auth_token", "") ?: "" }
    val currentUserId = remember { prefs.getInt("user_id", 0) }

    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var loadError     by remember { mutableStateOf(false) }
    var searchQuery   by remember { mutableStateOf("") }
    // 0 = All, 1 = Unread
    var filterTab     by remember { mutableStateOf(0) }

    val filteredConversations = remember(conversations, searchQuery, filterTab) {
        var list = if (searchQuery.isBlank()) conversations
        else {
            val q = searchQuery.trim().lowercase()
            conversations.filter { c ->
                c.firstName.lowercase().contains(q) ||
                        c.lastName.lowercase().contains(q) ||
                        c.itemTitle.lowercase().contains(q) ||
                        c.latestMessage.lowercase().contains(q)
            }
        }
        if (filterTab == 1) list = list.filter { it.unreadCount > 0 }
        list
    }

    // When entering a chat: mark messages as read + zero the badge locally.
    // When leaving a chat (conv == null): re-fetch so latest_message updates.
    LaunchedEffect(selectedConversation) {
        if (selectedConversation != null) {
            val conv = selectedConversation
            withContext(Dispatchers.IO) { markMessagesRead(token, conv.itemId) }
            conversations = conversations.map {
                if (it.otherUserId == conv.otherUserId && it.itemId == conv.itemId)
                    it.copy(unreadCount = 0) else it
            }
            return@LaunchedEffect
        }
        if (conversations.isEmpty()) isLoading = true
        try {
            val result = withContext(Dispatchers.IO) { fetchConversations(token) }
            conversations = result
            loadError = false
        } catch (_: Exception) {
            if (conversations.isEmpty()) loadError = true
        } finally {
            isLoading = false
        }
    }

    // FIXED: Properly structured AnimatedContent
    AnimatedContent(
        targetState = selectedConversation,
        transitionSpec = {
            if (targetState != null) {
                // Opening a conversation — slide in from right
                slideInHorizontally(tween(300)) { it } togetherWith
                        slideOutHorizontally(tween(300)) { -it / 3 }
            } else {
                // Going back — slide in from left
                slideInHorizontally(tween(300)) { -it / 3 } togetherWith
                        slideOutHorizontally(tween(300)) { it }
            }
        },
        label = "ChatTransition"
    ) { conv ->
        if (conv != null) {
            ChatDetailContent(
                conversation  = conv,
                token         = token,
                currentUserId = currentUserId,
                onBack        = { onSelectConversation(null) }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AdminPageHeader(title = "Messages", onMenuClick = onMenuClick, favoritesCount = favoritesCount, onFavoritesClick = onFavoritesClick)

                // Pusher debug banner
                // val dbgStatus by pusherGlobalStatus
                // val dbgLog    by pusherGlobalLog
                // val (bannerBg, bannerDot, bannerLabel) = when (dbgStatus) {
                //     "subscribed"                    -> Triple(Color(0xFF1B5E20), Color(0xFF69F0AE), "Live")
                //     "connected"                     -> Triple(Color(0xFF4A3800), Color(0xFFFFD740), "Authenticating…")
                //     "reconnecting"                  -> Triple(Color(0xFF4A3800), Color(0xFFFFD740), "Reconnecting…")
                //     "auth_failed"                   -> Triple(Color(0xFF4E1A1A), Color(0xFFFF5252), "Auth failed")
                //     "error"                         -> Triple(Color(0xFF4E1A1A), Color(0xFFFF5252), "Error")
                //     "disconnected", "disconnecting" -> Triple(Color(0xFF4E1A1A), Color(0xFFFF5252), "Disconnected")
                //     "idle"                          -> Triple(Color(0xFF1A237E), Color(0xFF82B1FF), "No active chat")
                //     else                            -> Triple(Color(0xFF4A3800), Color(0xFFFFD740), "Connecting…")
                // }
                // Row(
                //     modifier = Modifier
                //         .fillMaxWidth()
                //         .background(bannerBg)
                //         .padding(horizontal = 16.dp, vertical = 6.dp),
                //     verticalAlignment = Alignment.CenterVertically
                // ) {
                //     Canvas(modifier = Modifier.size(8.dp)) { drawCircle(bannerDot) }
                //     Spacer(Modifier.width(8.dp))
                //     Text(
                //         "Pusher: $bannerLabel",
                //         fontSize = 12.sp,
                //         fontWeight = FontWeight.SemiBold,
                //         color = bannerDot
                //     )
                //     if (dbgLog.isNotEmpty()) {
                //         Text(
                //             "  —  $dbgLog",
                //             fontSize = 11.sp,
                //             color = bannerDot.copy(alpha = 0.75f),
                //             maxLines = 1,
                //             overflow = TextOverflow.Ellipsis,
                //             modifier = Modifier.weight(1f)
                //         )
                //     }
                // }

                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(24.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Search, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search conversations…",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // ── Filter chips ──────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Unread").forEachIndexed { idx, label ->
                        val isSelected = filterTab == idx
                        val unreadTotal = if (idx == 1) conversations.sumOf { it.unreadCount } else 0
                        Surface(
                            onClick = { filterTab = idx },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) DarkGreen else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (idx == 1 && unreadTotal > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.3f)
                                                else Color(0xFFE53935),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (unreadTotal > 99) "99+" else "$unreadTotal",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                when {
                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DarkGreen)
                    }
                    loadError || filteredConversations.isEmpty() -> Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ChatBubbleOutline, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                when {
                                    loadError                -> "Failed to load conversations"
                                    searchQuery.isNotEmpty() -> "No results for \"$searchQuery\""
                                    filterTab == 1           -> "No unread messages"
                                    else                     -> "No conversations yet"
                                },
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredConversations, key = { "${it.otherUserId}_${it.itemId}" }) { conv ->
                            ConversationItem(conv) { onSelectConversation(conv) }
                        }
                    }
                }
            }
        }
    } // end AnimatedContent
}

@Composable
private fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    val hasUnread = conversation.unreadCount > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (hasUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(modifier = Modifier.size(56.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(DarkGreen.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                if (conversation.profilePicture.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = conversation.profilePicture,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = {
                            Text(
                                "${conversation.firstName.firstOrNull() ?: "?"}",
                                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkGreen
                            )
                        }
                    )
                } else {
                    Text(
                        "${conversation.firstName.firstOrNull() ?: "?"}",
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkGreen
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        // Text content
        Column(modifier = Modifier.weight(1f)) {
            // Row 1: item title + time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    conversation.itemTitle,
                    fontSize = 15.sp,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = DarkGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    timeAgo(conversation.lastMessageAt),
                    fontSize = 11.sp,
                    fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (hasUnread) DarkGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }
            Spacer(Modifier.height(1.dp))
            // Row 2: sender name
            Text(
                "${conversation.firstName} ${conversation.lastName}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            // Status chip (private = negotiating, public = available)
            if (conversation.itemStatus.isNotBlank()) {
                val isPrivate = conversation.itemStatus.lowercase() == "pending"
                Surface(
                    shape    = RoundedCornerShape(4.dp),
                    color    = if (isPrivate) Color(0xFFFF8F00).copy(alpha = 0.12f)
                               else DarkGreen.copy(alpha = 0.10f),
                    modifier = Modifier.padding(bottom = 3.dp)
                ) {
                    Text(
                        if (isPrivate) "Negotiating" else conversation.itemStatus.replaceFirstChar { it.uppercaseChar() },
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isPrivate) Color(0xFFE65100) else DarkGreen,
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            // Row 3: latest message + unread badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    conversation.latestMessage,
                    fontSize = 13.sp,
                    fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (hasUnread) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (hasUnread) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                            .background(Color(0xFFE53935), CircleShape)
                            .padding(horizontal = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (conversation.unreadCount > 99) "99+" else "${conversation.unreadCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 84.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    )
}

@Composable
private fun ChatDetailContent(
    conversation: Conversation,
    token: String,
    currentUserId: Int,
    onBack: () -> Unit
) {
    var messages               by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isLoading              by remember { mutableStateOf(true) }
    var fetchError             by remember { mutableStateOf(false) }
    var fetchErrorMsg          by remember { mutableStateOf("") }
    var retryTrigger           by remember { mutableStateOf(0) }   // increment to retry
    var messageText            by remember { mutableStateOf("") }
    var isSending              by remember { mutableStateOf(false) }
    var showEmojiPicker        by remember { mutableStateOf(false) }
    var chatItem               by remember { mutableStateOf<ChatItem?>(null) }
    var showItemPreview        by remember { mutableStateOf(false) }
    var showEditItem           by remember { mutableStateOf(false) }
    // val pusherStatus   by pusherGlobalStatus
    // val pusherDebugLog by pusherGlobalLog
    val listState              = rememberLazyListState()
    val scope                  = rememberCoroutineScope()
    val focusManager           = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }

    // ── Pusher real-time ──────────────────────────────────────────────────────
    // Hot flow used as a thread-safe bridge between Pusher's callback thread
    // and the Compose main thread.
    // val incomingFlow = remember { MutableSharedFlow<ChatMessage>(extraBufferCapacity = 64) }

    // DisposableEffect(currentUserId, conversation.otherUserId) {
    //     val handler     = Handler(Looper.getMainLooper())
    //     val channelName = "private-conversation.$currentUserId.${conversation.otherUserId}"
    //     var pusherRef: Pusher? = null
    //
    //     try {
    //         val tokenPreview = if (token.length > 8) "${token.take(8)}…" else "(empty)"
    //         handler.post {
    //             pusherGlobalStatus.value = "connecting"
    //             pusherGlobalLog.value    = "token=$tokenPreview ch=$channelName"
    //         }
    //         Log.d("PusherSetup", "token=$tokenPreview channel=$channelName")
    //
    //         val options = PusherOptions().apply {
    //             setCluster(PUSHER_CLUSTER)
    //             setAuthorizer(BearerTokenAuthorizer(PUSHER_AUTH_URL, token))
    //         }
    //         val pusher = Pusher(PUSHER_APP_KEY, options)
    //         pusherRef = pusher
    //
    //         val channel = pusher.subscribePrivate(
    //             channelName,
    //             object : PrivateChannelEventListener {
    //                 override fun onEvent(event: PusherEvent) {}
    //                 override fun onSubscriptionSucceeded(cn: String) {
    //                     Log.d("Pusher", "Subscribed to $cn")
    //                     handler.post {
    //                         pusherGlobalStatus.value = "subscribed"
    //                         pusherGlobalLog.value    = "OK — $cn"
    //                     }
    //                 }
    //                 override fun onAuthenticationFailure(message: String, e: Exception) {
    //                     Log.e("Pusher", "Auth failed for $channelName: $message", e)
    //                     handler.post {
    //                         pusherGlobalStatus.value = "auth_failed"
    //                         pusherGlobalLog.value    = message
    //                     }
    //                 }
    //             }
    //         )
    //
    //         channel.bind("message.sent", object : PrivateChannelEventListener {
    //             override fun onEvent(event: PusherEvent) {
    //                 Log.d("Pusher", "message.sent: ${event.data}")
    //                 handler.post { pusherGlobalLog.value = "rx: ${event.data.take(60)}" }
    //                 try {
    //                     val d: JSONObject = when (val v = org.json.JSONTokener(event.data).nextValue()) {
    //                         is JSONObject -> v
    //                         is String     -> JSONObject(v)
    //                         else          -> return
    //                     }
    //                     incomingFlow.tryEmit(ChatMessage(
    //                         messageId              = d.optInt("message_id", d.optInt("id", -1)),
    //                         itemId                 = d.optInt("item_id", conversation.itemId),
    //                         itemTitle              = d.optString("item_title", conversation.itemTitle),
    //                         senderId               = d.optInt("sender_id"),
    //                         senderName             = d.optString("sender_name"),
    //                         senderProfilePicture   = d.optString("sender_profile_picture"),
    //                         receiverId             = d.optInt("receiver_id"),
    //                         receiverName           = d.optString("receiver_name"),
    //                         receiverProfilePicture = d.optString("receiver_profile_picture"),
    //                         message                = d.optString("message"),
    //                         sentAt                 = d.optString("sent_at", d.optString("created_at", ""))
    //                     ))
    //                 } catch (e: Exception) {
    //                     Log.e("Pusher", "Parse error: ${event.data}", e)
    //                     handler.post { pusherGlobalLog.value = "parse error: ${e.message}" }
    //                 }
    //             }
    //             override fun onSubscriptionSucceeded(cn: String) {}
    //             override fun onAuthenticationFailure(message: String, e: Exception) {
    //                 Log.e("Pusher", "Bind auth failed: $message", e)
    //                 handler.post {
    //                     pusherGlobalStatus.value = "auth_failed"
    //                     pusherGlobalLog.value    = message
    //                 }
    //             }
    //         })
    //
    //         pusher.connect(object : ConnectionEventListener {
    //             override fun onConnectionStateChange(change: ConnectionStateChange) {
    //                 Log.d("Pusher", "${change.previousState} → ${change.currentState}")
    //                 handler.post {
    //                     pusherGlobalStatus.value = when (change.currentState) {
    //                         ConnectionState.CONNECTING    -> "connecting"
    //                         ConnectionState.CONNECTED     -> "connected"
    //                         ConnectionState.DISCONNECTED  -> "disconnected"
    //                         ConnectionState.RECONNECTING  -> "reconnecting"
    //                         ConnectionState.DISCONNECTING -> "disconnecting"
    //                         else                          -> "unknown"
    //                     }
    //                 }
    //             }
    //             override fun onError(message: String, code: String?, e: Exception?) {
    //                 Log.e("Pusher", "Connection error: $message code=$code", e)
    //                 handler.post {
    //                     pusherGlobalStatus.value = "error"
    //                     pusherGlobalLog.value    = "conn error: $message"
    //                 }
    //             }
    //         }, ConnectionState.ALL)
    //
    //     } catch (e: Exception) {
    //         Log.e("Pusher", "Setup crashed", e)
    //         handler.post {
    //             pusherGlobalStatus.value = "error"
    //             pusherGlobalLog.value    = "setup error: ${e.message}"
    //         }
    //     }
    //
    //     onDispose {
    //         try { pusherRef?.unsubscribe(channelName) } catch (_: Exception) {}
    //         try { pusherRef?.disconnect() } catch (_: Exception) {}
    //         // post to avoid writing state synchronously during Compose apply phase
    //         handler.post {
    //             pusherGlobalStatus.value = "idle"
    //             pusherGlobalLog.value    = "disconnected"
    //         }
    //     }
    // }

    // Merge incoming Pusher messages into the UI message list
    // LaunchedEffect(incomingFlow) {
    //     incomingFlow.collect { msg ->
    //         when {
    //             // Message we sent: swap out our optimistic placeholder with the confirmed copy
    //             msg.senderId == currentUserId -> {
    //                 messages = messages.map { existing ->
    //                     if (existing.messageId == -1 &&
    //                         existing.message.trim() == msg.message.trim()) msg
    //                     else existing
    //                 }
    //             }
    //             // New message from the other participant — append if not already present
    //             messages.none { it.messageId == msg.messageId } -> {
    //                 messages = messages + msg
    //                 listState.scrollToItem(messages.size - 1)
    //             }
    //         }
    //     }
    // }
    // ─────────────────────────────────────────────────────────────────────────

    LaunchedEffect(conversation.otherUserId, conversation.itemId) {
        chatItem = withContext(Dispatchers.IO) { fetchChatItem(token, conversation.itemId) }
    }

    LaunchedEffect(conversation.otherUserId, conversation.itemId, retryTrigger) {
        if (conversation.itemId == 0) { isLoading = false; return@LaunchedEffect }
        isLoading = true
        fetchError = false
        fetchErrorMsg = ""
        try {
            val fetched = withContext(Dispatchers.IO) {
                fetchMessages(token, conversation.itemId, conversation.otherUserId)
            }
            messages = fetched.distinctBy { it.messageId }
        } catch (e: Exception) {
            fetchError = true
            fetchErrorMsg = e.message ?: "Unknown error"
        } finally {
            isLoading = false
            // reverseLayout=true means newest message is always at index 0 (bottom)
            // — no scroll needed after load, the list is already at the correct position
        }
    }


    fun doSend() {
        val text = messageText.trim()
        if (text.isBlank() || isSending) return

        // Resolve receiver ID:
        // 1. Use the conversation's otherUserId if it was parsed correctly (non-zero).
        // 2. Fall back to inferring from already-loaded messages — look for a
        //    receiver_id on messages WE sent, or a sender_id on messages THEY sent.
        // This handles the case where the conversations API omits other_user_id.
        val receiverId = conversation.otherUserId.takeIf { it != 0 }
            ?: messages.firstOrNull { it.senderId == currentUserId }?.receiverId?.takeIf { it != 0 }
            ?: messages.firstOrNull { it.senderId != currentUserId }?.senderId?.takeIf { it != 0 }
            ?: return  // still unknown — don't send a broken request

        messageText = ""
        isSending   = true
        val nowStr = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", java.util.Locale.US
        ).format(java.util.Date())
        val optimistic = ChatMessage(
            messageId              = -1,
            itemId                 = conversation.itemId,
            itemTitle              = conversation.itemTitle,
            senderId               = currentUserId,
            senderName             = "Me",
            senderProfilePicture   = "",
            receiverId             = receiverId,
            receiverName           = "${conversation.firstName} ${conversation.lastName}",
            receiverProfilePicture = conversation.profilePicture,
            message                = text,
            sentAt                 = nowStr
        )
        messages = messages + optimistic
        scope.launch {
            // reverseLayout=true keeps newest at index 0 (bottom) — no manual scroll needed
            withContext(Dispatchers.IO) {
                sendMessage(token, conversation.itemId, receiverId, text)
            }
            isSending = false
        }
    }

    // Back press: close emoji picker first, then go back to conversation list
    BackHandler { onBack() }
    BackHandler(enabled = showEmojiPicker) { showEmojiPicker = false }
    BackHandler(enabled = showItemPreview && !showEmojiPicker) { showItemPreview = false }
    BackHandler(enabled = showEditItem && !showEmojiPicker && !showItemPreview) { showEditItem = false }

    // ── Item detail / Edit pages (full-screen, slides in over the chat) ────────
    AnimatedContent(
        targetState = when {
            showEditItem -> "edit"
            showItemPreview -> "view"
            else -> "chat"
        },
        transitionSpec = {
            if (targetState != "chat") {
                slideInHorizontally(tween(280)) { it } togetherWith
                        slideOutHorizontally(tween(280)) { -it / 3 }
            } else {
                slideInHorizontally(tween(280)) { -it / 3 } togetherWith
                        slideOutHorizontally(tween(280)) { it }
            }
        },
        label = "ItemPageTransition"
    ) { state ->
        when (state) {
            "edit" -> {
                chatItem?.let { item ->
                    EditItemPage(
                        item = item,
                        token = token,
                        onBack = { showEditItem = false },
                        onItemUpdated = { updatedItem ->
                            chatItem = updatedItem
                            showEditItem = false
                        }
                    )
                } ?: run { showEditItem = false }
            }
            "view" -> {
                chatItem?.let { item ->
                    ChatItemDetailPage(item = item, onBack = { showItemPreview = false })
                } ?: run { showItemPreview = false }
            }
            else -> {
                // Flat Column — mirrors Messenger's layout. No Scaffold re-measure on every
                // keyboard frame; imePadding() only re-measures this simple Column + children.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
            // ── Top bar ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
            ) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (conversation.profilePicture.isNotBlank()) {
                            AsyncImage(
                                model = conversation.profilePicture,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                "${conversation.firstName.firstOrNull() ?: "?"}",
                                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${conversation.firstName} ${conversation.lastName}",
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            conversation.itemTitle,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // ── Item info bar (sticky, always visible) ────────────────────
                chatItem?.let { item ->
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkGreen.copy(alpha = 0.75f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.photos.isNotEmpty()) {
                                AsyncImage(
                                    model = item.photos.first(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Filled.Photo, null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                        // Title + price + status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Points
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Filled.MonetizationOn, null,
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(11.dp))
                                    Text(
                                        "${item.markupPoints} pts",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                // Status badge (moved next to points)
                                val isPrivate = item.status.lowercase() == "pending"
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isPrivate) Color(0xFFFF8F00).copy(alpha = 0.30f)
                                            else Color.White.copy(alpha = 0.20f)
                                ) {
                                    Text(
                                        if (isPrivate) "Negotiating" else item.status.replaceFirstChar { it.uppercaseChar() },
                                        fontSize   = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = Color.White,
                                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        // Buttons (View Item and Edit Item)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Edit Item button (visible only if status is private or acquired)
                            val canEditItem = item.status.lowercase() == "private" ||
                                             item.status.lowercase() == "acquired"
                            if (canEditItem) {
                                OutlinedButton(
                                    onClick = { showEditItem = true },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                }
                            }

                            // View Item button
                            OutlinedButton(
                                onClick = { showItemPreview = true },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("View Item", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
                            }
                        }
                    }
                }
            } // end top bar Column

            // ── Messages ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                    when {
                        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DarkGreen)
                        }
                        fetchError -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    "Failed to load messages",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Button(
                                    onClick = { retryTrigger++ },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                                ) {
                                    Text("Retry", color = Color.White)
                                }
                            }
                        }
                        messages.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No messages yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        else -> LazyColumn(
                            state = listState,
                            // reverseLayout = true: item 0 anchors to bottom.
                            // Newest messages stay visible when keyboard opens —
                            // no programmatic scroll needed (mirrors Messenger).
                            reverseLayout = true,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pass reversed list so newest message is at index 0 (bottom)
                            items(messages.asReversed(), key = { it.messageId }) { msg ->
                                ChatBubble(msg = msg, isMe = msg.senderId == currentUserId)
                            }
                        }
                    }
            } // end messages Box

            // ── Input bar ───────────────────────────────────────────────────────
            Surface(
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    // ── Pusher debug status bar ───────────────────────────────
                    // val (dotColor, statusLabel) = when (pusherStatus) {
                    //     "subscribed"    -> Color(0xFF4CAF50) to "Live"
                    //     "connected"     -> Color(0xFFFF9800) to "Authenticating…"
                    //     "reconnecting"  -> Color(0xFFFF9800) to "Reconnecting…"
                    //     "auth_failed"   -> Color(0xFFF44336) to "Auth failed"
                    //     "error"         -> Color(0xFFF44336) to "Error"
                    //     "disconnected",
                    //     "disconnecting" -> Color(0xFFF44336) to "Disconnected"
                    //     else            -> Color(0xFFFF9800) to "Connecting…"
                    // }
                    // Row(
                    //     modifier = Modifier
                    //         .fillMaxWidth()
                    //         .padding(horizontal = 12.dp, vertical = 3.dp),
                    //     verticalAlignment = Alignment.CenterVertically,
                    //     horizontalArrangement = Arrangement.Center
                    // ) {
                    //     Canvas(modifier = Modifier.size(7.dp)) { drawCircle(dotColor) }
                    //     Spacer(Modifier.width(5.dp))
                    //     Text(
                    //         statusLabel,
                    //         fontSize = 11.sp,
                    //         color = dotColor,
                    //         fontWeight = FontWeight.Medium
                    //     )
                    //     if (pusherDebugLog.isNotEmpty()) {
                    //         Text(
                    //             "  •  $pusherDebugLog",
                    //             fontSize = 10.sp,
                    //             color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    //             maxLines = 1,
                    //             overflow = TextOverflow.Ellipsis,
                    //             modifier = Modifier.weight(1f, fill = false)
                    //         )
                    //     }
                    // }
                    // ─────────────────────────────────────────────────────────
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji button — toggles picker panel; restores keyboard when closing
                        IconButton(onClick = {
                            if (showEmojiPicker) {
                                showEmojiPicker = false
                                scope.launch { textFieldFocusRequester.requestFocus() }
                            } else {
                                showEmojiPicker = true
                                focusManager.clearFocus()
                            }
                        }) {
                            Icon(
                                if (showEmojiPicker) Icons.Outlined.Keyboard else Icons.Outlined.EmojiEmotions,
                                contentDescription = if (showEmojiPicker) "Keyboard" else "Emoji",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Text field
                        androidx.compose.foundation.text.BasicTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 40.dp)
                                .focusRequester(textFieldFocusRequester)
                                .onFocusChanged { if (it.isFocused) showEmojiPicker = false }
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 0.dp),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { doSend() }),
                            maxLines = 4,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (messageText.isEmpty()) {
                                        Text(
                                            "Type a message…",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        Spacer(Modifier.width(6.dp))

                        // Send button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (messageText.isNotBlank()) DarkGreen
                                    else DarkGreen.copy(alpha = 0.35f)
                                )
                                .clickable(enabled = messageText.isNotBlank() && !isSending) {
                                    doSend()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    // Emoji picker panel — shown when emoji button is toggled
                    if (showEmojiPicker) {
                        EmojiPickerPanel(onEmojiClick = { messageText += it })
                    }
                }
            }
                } // end outer Column (chat view)
                } // end else (chat view)
        } // end when (AnimatedContent state)
    } // end AnimatedContent
}

@Composable
private fun EmojiPickerPanel(onEmojiClick: (String) -> Unit) {
    // selectedIndex drives which category slug is requested
    var selectedIndex    by remember { mutableStateOf(0) }
    var categoryEmojis   by remember { mutableStateOf<List<EmojiItem>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(false) }

    // Fetch only the selected category; result is cached in emojiCache at module level
    LaunchedEffect(selectedIndex) {
        isLoading = true
        val (_, slug) = emojiCategories[selectedIndex]
        categoryEmojis = try { fetchEmojisByCategory(slug) } catch (_: Exception) { emptyList() }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = DarkGreen,
            edgePadding = 0.dp
        ) {
            emojiCategories.forEachIndexed { index, (label, _) ->
                Tab(
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index },
                    text = {
                        Text(
                            label.replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkGreen, modifier = Modifier.size(28.dp))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(44.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(categoryEmojis) { emoji ->
                    val char = emoji.htmlCode.firstOrNull()?.let { htmlCodeToChar(it) } ?: ""
                    if (char.isNotEmpty()) {
                        Text(
                            text = char,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onEmojiClick(char) }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Item Detail Page (shown when "View Item" is tapped in chat) ────────────────

@Composable
private fun ChatItemDetailPage(item: ChatItem, onBack: () -> Unit) {
    var currentImageIndex by remember { mutableStateOf(0) }
    BackHandler(onBack = onBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
            ) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        "Item Details",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // ── Scrollable content ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Photo viewer
                if (item.photos.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        AsyncImage(
                            model = item.photos[currentImageIndex],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (item.photos.size > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    "${currentImageIndex + 1} / ${item.photos.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    // Thumbnail strip
                    if (item.photos.size > 1) {
                        LazyRow(
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(item.photos) { index, url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                                width = if (index == currentImageIndex) 2.dp else 1.dp,
                                                color = if (index == currentImageIndex) DarkGreen
                                                        else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { currentImageIndex = index }
                                    )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Photo, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                // Info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.MonetizationOn, null, tint = DarkGreen, modifier = Modifier.size(22.dp))
                        Text("${item.markupPoints} pts", fontWeight = FontWeight.Bold, color = DarkGreen, fontSize = 20.sp)
                    }
                    val statusColor = when (item.status.lowercase()) {
                        "approved" -> DarkGreen
                        "rejected" -> MaterialTheme.colorScheme.error
                        else       -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.12f)) {
                        Text(
                            item.status.replaceFirstChar { it.uppercaseChar() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                    HorizontalDivider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Person, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                        Text(item.sellerEmail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (item.description.isNotBlank()) {
                        HorizontalDivider()
                        Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(item.description, fontSize = 14.sp, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemPage(
    item: ChatItem,
    token: String,
    onBack: () -> Unit,
    onItemUpdated: (ChatItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentImageIndex by remember { mutableStateOf(0) }

    // Status dropdown
    var expanded by remember { mutableStateOf(false) }
    var editStatus by remember { mutableStateOf(item.status) }
    val statusOptions = listOf("private", "acquired", "public", "reserved", "sold")

    // Markup points - editable only if CURRENTLY SELECTED status is private/acquired
    var editMarkupPoints by remember { mutableStateOf(item.markupPoints.toString()) }
    val canEditMarkup = editStatus.lowercase() == "private" ||
                       editStatus.lowercase() == "acquired"

    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }
    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog   by remember { mutableStateOf(false) }
    var dialogErrorMsg    by remember { mutableStateOf("") }
    // Holds updated item until user dismisses the success dialog
    var savedChatItem     by remember { mutableStateOf<ChatItem?>(null) }

    BackHandler(onBack = onBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
            ) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        "Edit Item",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // ── Scrollable content ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Photo viewer
                if (item.photos.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        AsyncImage(
                            model = item.photos[currentImageIndex],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (item.photos.size > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    "${currentImageIndex + 1} / ${item.photos.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    // Thumbnail strip
                    if (item.photos.size > 1) {
                        LazyRow(
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(item.photos) { index, url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (index == currentImageIndex) 2.dp else 1.dp,
                                            color = if (index == currentImageIndex) DarkGreen
                                                    else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { currentImageIndex = index }
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Photo, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                // Editable info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)

                    // Price Points - NON-EDITABLE (display only)
                    Text("Price Points", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.MonetizationOn, null,
                                tint = DarkGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${item.markupPoints} pts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen
                            )
                        }
                    }

                    // Status field (dropdown)
                    Text("Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editStatus.replaceFirstChar { it.uppercaseChar() },
                            onValueChange = {},
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            status.replaceFirstChar { it.uppercaseChar() },
                                            fontWeight = if (status == editStatus) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        editStatus = status
                                        expanded = false
                                        saveError = null
                                    }
                                )
                            }
                        }
                    }

                    // Markup Points field (editable only if SELECTED status is private or acquired)
                    Text("Markup Points", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (canEditMarkup) {
                        OutlinedTextField(
                            value = editMarkupPoints,
                            onValueChange = { editMarkupPoints = it; saveError = null },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.TrendingUp, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    editMarkupPoints,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Person, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                        Text(item.sellerEmail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (item.description.isNotBlank()) {
                        HorizontalDivider()
                        Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(item.description, fontSize = 14.sp, lineHeight = 22.sp)
                    }

                    HorizontalDivider()

                    // ── Success Dialog ────────────────────────────────────────
                    if (showSuccessDialog) {
                        AlertDialog(
                            onDismissRequest = { showSuccessDialog = false },
                            icon = {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = DarkGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = { Text("Success", fontWeight = FontWeight.Bold) },
                            text = { Text("Item has been updated successfully.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showSuccessDialog = false
                                        savedChatItem?.let { onItemUpdated(it) }
                                        onBack()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Back", color = Color.White)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = { showSuccessDialog = false },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, DarkGreen)
                                ) {
                                    Text("Close", color = DarkGreen)
                                }
                            }
                        )
                    }

                    // ── Error Dialog ──────────────────────────────────────────
                    if (showErrorDialog) {
                        AlertDialog(
                            onDismissRequest = { showErrorDialog = false },
                            icon = {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = { Text("Update Failed", fontWeight = FontWeight.Bold) },
                            text = { Text(dialogErrorMsg) },
                            confirmButton = {
                                Button(
                                    onClick = { showErrorDialog = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Close", color = Color.White)
                                }
                            }
                        )
                    }

                    // Save button
                    Button(
                        onClick = {
                            saveError = null
                            if (editStatus.isBlank()) {
                                dialogErrorMsg = "Status cannot be empty."
                                showErrorDialog = true
                                return@Button
                            }
                            val markupPts = editMarkupPoints.toIntOrNull()
                            if (canEditMarkup && editMarkupPoints.isNotBlank() && markupPts == null) {
                                dialogErrorMsg = "Markup points must be a valid number."
                                showErrorDialog = true
                                return@Button
                            }
                            scope.launch {
                                isSaving = true
                                val (ok, errMsg) = withContext(Dispatchers.IO) {
                                    updateAdminItem(
                                        token,
                                        item.itemId,
                                        status = editStatus,
                                        markupPoints = if (canEditMarkup) markupPts else null
                                    )
                                }
                                isSaving = false
                                if (ok) {
                                    saveSuccess = true
                                    savedChatItem = item.copy(
                                        status = editStatus,
                                        markupPoints = markupPts ?: item.markupPoints
                                    )
                                    // Show dialog FIRST — onItemUpdated is called from Back button
                                    showSuccessDialog = true
                                } else {
                                    dialogErrorMsg = errMsg.ifBlank { "Failed to update. Please try again." }
                                    showErrorDialog = true
                                }
                            }
                        },
                        enabled = !isSaving && !saveSuccess,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save Changes", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemPageForList(
    item: Item,
    token: String,
    onBack: () -> Unit,
    onItemUpdated: (ChatItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentImageIndex by remember { mutableStateOf(0) }
    
    // Status dropdown
    var expanded by remember { mutableStateOf(false) }
    var editStatus by remember { mutableStateOf(item.status) }
    val statusOptions = listOf("private", "acquired", "public", "reserved", "sold")

    // Markup points - editable only if CURRENTLY SELECTED status is private/acquired
    var editMarkupPoints by remember { mutableStateOf(item.markupPoints.toString()) }
    val canEditMarkup = editStatus.lowercase() == "private" ||
                       editStatus.lowercase() == "acquired"

    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }
    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog  by remember { mutableStateOf(false) }
    var dialogErrorMsg   by remember { mutableStateOf("") }
    // Holds the updated item until the user dismisses the success dialog
    var savedChatItem    by remember { mutableStateOf<ChatItem?>(null) }

    BackHandler(onBack = onBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
            ) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        "Edit Item",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // ── Scrollable content ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Photo viewer (same as before)
                if (item.photos.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        AsyncImage(
                            model = item.photos[currentImageIndex],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (item.photos.size > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    "${currentImageIndex + 1} / ${item.photos.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    // Thumbnail strip
                    if (item.photos.size > 1) {
                        LazyRow(
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(item.photos) { index, url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (index == currentImageIndex) 2.dp else 1.dp,
                                            color = if (index == currentImageIndex) DarkGreen
                                                    else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { currentImageIndex = index }
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Photo, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                // Editable info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)

                    // Price Points - NON-EDITABLE (display only)
                    Text("Price Points", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.MonetizationOn, null,
                                tint = DarkGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${item.pricePoints} pts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen
                            )
                        }
                    }

                    // Status field (dropdown)
                    Text("Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editStatus.replaceFirstChar { it.uppercaseChar() },
                            onValueChange = {},
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            status.replaceFirstChar { it.uppercaseChar() },
                                            fontWeight = if (status == editStatus) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        editStatus = status
                                        expanded = false
                                        saveError = null
                                    }
                                )
                            }
                        }
                    }

                    // Markup Points field — editable when status is private/acquired,
                    // read-only (greyed card) for all other statuses.
                    // Always visible so the current value is never hidden from the admin.
                    Text("Markup Points", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (canEditMarkup) {
                        OutlinedTextField(
                            value = editMarkupPoints,
                            onValueChange = { editMarkupPoints = it; saveError = null },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    } else {
                        // Show the actual markup_points value from the item (read-only)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.TrendingUp, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (item.markupPoints > 0) "${item.markupPoints} pts"
                                           else "Not set",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider()
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Person, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                        Text(item.sellerEmail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    if (item.description.isNotBlank()) {
                        HorizontalDivider()
                        Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(item.description, fontSize = 14.sp, lineHeight = 22.sp)
                    }

                    HorizontalDivider()

                    // ── Success Dialog ─────────────────────────────────────────
                    if (showSuccessDialog) {
                        AlertDialog(
                            onDismissRequest = { showSuccessDialog = false },
                            icon = {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = DarkGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = { Text("Success", fontWeight = FontWeight.Bold) },
                            text = { Text("Item has been updated successfully.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showSuccessDialog = false
                                        savedChatItem?.let { onItemUpdated(it) }
                                        onBack()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Back", color = Color.White)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = { showSuccessDialog = false },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, DarkGreen)
                                ) {
                                    Text("Close", color = DarkGreen)
                                }
                            }
                        )
                    }

                    // ── Error Dialog ───────────────────────────────────────────
                    if (showErrorDialog) {
                        AlertDialog(
                            onDismissRequest = { showErrorDialog = false },
                            icon = {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = { Text("Update Failed", fontWeight = FontWeight.Bold) },
                            text = { Text(dialogErrorMsg) },
                            confirmButton = {
                                Button(
                                    onClick = { showErrorDialog = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Close", color = Color.White)
                                }
                            }
                        )
                    }

                    // Save button
                    Button(
                        onClick = {
                            saveError = null
                            if (editStatus.isBlank()) {
                                dialogErrorMsg = "Status cannot be empty."
                                showErrorDialog = true
                                return@Button
                            }
                            val markupPts = editMarkupPoints.toIntOrNull()
                            if (canEditMarkup && editMarkupPoints.isNotBlank() && markupPts == null) {
                                dialogErrorMsg = "Markup points must be a valid number."
                                showErrorDialog = true
                                return@Button
                            }
                            scope.launch {
                                isSaving = true
                                val (ok, errMsg) = withContext(Dispatchers.IO) {
                                    updateAdminItem(
                                        token,
                                        item.itemId,
                                        status = editStatus,
                                        markupPoints = if (canEditMarkup) markupPts else null
                                    )
                                }
                                isSaving = false
                                if (ok) {
                                    saveSuccess = true
                                    savedChatItem = ChatItem(
                                        itemId = item.itemId,
                                        title = item.title,
                                        description = item.description,
                                        markupPoints = markupPts ?: item.markupPoints,
                                        sellerEmail = item.sellerEmail,
                                        status = editStatus,
                                        photos = item.photos
                                    )
                                    // Show dialog FIRST — onItemUpdated is called from the Back button
                                    showSuccessDialog = true
                                } else {
                                    dialogErrorMsg = errMsg.ifBlank { "Failed to update. Please try again." }
                                    showErrorDialog = true
                                }
                            }
                        },
                        enabled = !isSaving && !saveSuccess,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save Changes", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically // Changed from Top to CenterVertically
    ) {
        if (!isMe) {
            // Profile picture - centered vertically with the message bubble
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DarkGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (msg.senderProfilePicture.isNotBlank()) {
                    AsyncImage(
                        model = msg.senderProfilePicture,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        msg.senderName.firstOrNull()?.toString() ?: "?",
                        fontSize = 13.sp,
                        color = DarkGreen
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
        }

        // Message content
        Column(
            modifier = Modifier.widthIn(max = 260.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            // Message bubble
            Box(
                modifier = Modifier.background(
                    color = if (isMe) DarkGreen else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMe) 18.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 18.dp
                    )
                ).padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    msg.message,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }

            // Timestamp
            Text(
                timeAgo(msg.sentAt),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(
                    top = 3.dp,
                    start = if (isMe) 0.dp else 4.dp,
                    end = if (isMe) 4.dp else 0.dp
                )
            )
        }

        if (isMe) Spacer(Modifier.width(6.dp))
    }
}

// ── Users ──────────────────────────────────────────────────────────────────────

@Composable
private fun AdminUsersContent(onMenuClick: () -> Unit) {
    val context = LocalContext.current
    val token = remember {
        context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

    var students        by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }
    var selectedFilter  by remember { mutableStateOf("All") }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var refreshKey      by remember { mutableStateOf(0) }

    // Always fetch ALL students — filter client-side via displayStatus so that
    // students with is_verified=true but status="pending" on the backend still
    // appear correctly under the Approved tab.
    LaunchedEffect(refreshKey) {
        if (token != null) {
            isLoading = true
            errorMessage = null
            try {
                students = withContext(Dispatchers.IO) { fetchStudents(token, null) }
            } catch (e: Exception) {
                errorMessage = "Failed to load students: ${e.message}"
            } finally {
                isLoading = false
            }
        } else {
            errorMessage = "Not authenticated. Please log in again."
        }
    }

    // Client-side filter using displayStatus (bridges is_verified / status mismatch)
    // Wrapped in remember so it only recomputes when students list or filter changes
    val filteredStudents = remember(students, selectedFilter) {
        when (selectedFilter) {
            "Pending"  -> students.filter { it.displayStatus == "pending" }
            "Approved" -> students.filter { it.displayStatus == "approved" }
            "Declined" -> students.filter { it.displayStatus == "declined" }
            "Blocked"  -> students.filter { it.displayStatus == "blocked" }
            else       -> students
        }
    }

    // Pre-compute counts once per students change — avoids 4x .count() on every recomposition
    val pendingCount  = remember(students) { students.count { it.displayStatus == "pending" } }
    val approvedCount = remember(students) { students.count { it.displayStatus == "approved" } }
    val declinedCount = remember(students) { students.count { it.displayStatus == "declined" } }
    val blockedCount  = remember(students) { students.count { it.displayStatus == "blocked" } }

    // Student detail modal
    selectedStudent?.let { student ->
        StudentDetailDialog(
            student         = student,
            token           = token,
            onDismiss       = { selectedStudent = null },
            onStatusUpdated = {
                selectedStudent = null
                refreshKey++
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "User Management", onMenuClick = onMenuClick)

        // ── Filter chips ─────────────────────────────────────────────────────
        val filters = listOf("All", "Pending", "Approved", "Declined", "Blocked")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                val count = when (filter) {
                    "Pending"  -> pendingCount
                    "Approved" -> approvedCount
                    "Declined" -> declinedCount
                    "Blocked"  -> blockedCount
                    else       -> students.size
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) DarkGreen else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("$filter ($count)", fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Content ──────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), color = DarkGreen)

                errorMessage != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                }

                filteredStudents.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.PeopleOutline, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedFilter == "All") "No students found"
                               else "No ${selectedFilter.lowercase()} students",
                        fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredStudents, key = { it.studentVerificationId }) { student ->
                        StudentCard(student = student, onClick = { selectedStudent = student })
                    }
                }
            }
        }
    }
}

// ── Student Card ───────────────────────────────────────────────────────────────

@Composable
private fun StudentCard(student: Student, onClick: () -> Unit) {
    val sColor = statusColor(student.displayStatus)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {

            // Avatar — shimmer while loading, initial on error/no URL
            Box(modifier = Modifier.size(50.dp).clip(CircleShape)
                .background(DarkGreen.copy(alpha = 0.1f))) {
                if (student.profilePicture != null) {
                    SubcomposeAsyncImage(
                        model = student.profilePicture,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = { ShimmerEffect(Modifier.fillMaxSize()) },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(student.initial, fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold, color = DarkGreen)
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(student.initial, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(student.fullName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(student.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = buildString {
                        student.verificationType?.let { append(it.replace("_", " ").replaceFirstChar { c -> c.uppercaseChar() }) }
                        if (student.isVerified) append(" · ✓ Verified")
                    },
                    fontSize = 11.sp,
                    color = if (student.isVerified) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status badge — uses displayStatus so is_verified:true always shows Approved
            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                .background(sColor.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(student.displayStatus.replaceFirstChar { it.uppercaseChar() },
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = sColor)
            }
        }
    }
}

// ── Student Detail Page (full-screen) ─────────────────────────────────────────

@Composable
private fun StudentDetailDialog(
    student: Student,
    token: String?,
    onDismiss: () -> Unit,
    onStatusUpdated: () -> Unit
) {
    val sColor = statusColor(student.displayStatus)
    val scope  = rememberCoroutineScope()

    var declineReason    by remember { mutableStateOf(student.reason ?: "") }
    var showDeclineInput by remember { mutableStateOf(false) }
    var blockReason      by remember { mutableStateOf("") }
    var showBlockInput   by remember { mutableStateOf(false) }
    var actionLoading    by remember { mutableStateOf(false) }
    var resultDialog     by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var showDocViewer    by remember { mutableStateOf(false) }

    // ── Full-screen document viewer ───────────────────────────────────────────
    if (showDocViewer && student.verificationDocument != null) {
        var scale       by remember { mutableStateOf(1f) }
        var offsetX     by remember { mutableStateOf(0f) }
        var offsetY     by remember { mutableStateOf(0f) }
        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            scale   = (scale * zoomChange).coerceIn(1f, 5f)
            offsetX += panChange.x
            offsetY += panChange.y
        }
        Dialog(
            onDismissRequest = { showDocViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .transformable(state = transformState)
            ) {
                SubcomposeAsyncImage(
                    model = student.verificationDocument,
                    contentDescription = "Verification Document",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX       = scale,
                            scaleY       = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit,
                    loading = { ShimmerEffect(Modifier.fillMaxSize()) },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Could not load image", color = Color.White)
                        }
                    }
                )
                IconButton(
                    onClick = { showDocViewer = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close",
                        tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Text(
                    "Pinch to zoom",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
                )
            }
        }
    }

    // ── Success / Error alert dialog ─────────────────────────────────────────
    resultDialog?.let { (success, message) ->
        AlertDialog(
            onDismissRequest = {
                resultDialog = null
                if (success) onStatusUpdated()
            },
            icon = {
                Icon(
                    if (success) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = if (success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text(if (success) "Success" else "Failed", fontWeight = FontWeight.Bold) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    resultDialog = null
                    if (success) onStatusUpdated()
                }) { Text("OK", fontWeight = FontWeight.SemiBold) }
            }
        )
    }

    // ── Full-screen page (Dialog that fills the whole screen) ─────────────────
    Dialog(
        onDismissRequest = { if (!actionLoading) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Top App Bar ───────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
                ) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (!actionLoading) onDismiss() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            "Student Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                student.displayStatus.replaceFirstChar { it.uppercaseChar() },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // ── Scrollable Content ────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Profile header ────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkGreenLight.copy(alpha = 0.85f))
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        ) {
                            if (student.profilePicture != null) {
                                SubcomposeAsyncImage(
                                    model = student.profilePicture,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    loading = { ShimmerEffect(Modifier.fillMaxSize()) },
                                    error = {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(student.initial, fontSize = 26.sp,
                                                fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(student.initial, fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(student.fullName, fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text(student.email, fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f))
                        }
                    }

                    // ── Info section ──────────────────────────────────────────
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("STUDENT INFORMATION", fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold, color = DarkGreen, letterSpacing = 1.sp)

                        InfoRow(Icons.Filled.Badge,          "Verification ID", "#${student.studentVerificationId}")
                        InfoRow(Icons.Filled.Person,         "Full Name",       student.fullName)
                        InfoRow(Icons.Filled.Email,          "Email",           student.email)
                        InfoRow(Icons.Filled.VerifiedUser,   "Verified",
                            if (student.isVerified) "Yes ✓" else "No",
                            valueColor = if (student.isVerified) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface)
                        student.verificationType?.let {
                            InfoRow(Icons.Filled.CreditCard, "ID Type",
                                it.replace("_", " ").replaceFirstChar { c -> c.uppercaseChar() })
                        }
                        InfoRow(Icons.Filled.AccountBalanceWallet,
                            "Wallet Points", "${student.walletPoints} pts")
                        InfoRow(Icons.Filled.Circle, "Active",
                            if (student.isActive) "Yes" else "No",
                            valueColor = if (student.isActive) Color(0xFF4CAF50)
                                         else MaterialTheme.colorScheme.onSurfaceVariant)
                        InfoRow(Icons.Filled.CalendarToday, "Registered",
                            formatDate(student.registeredDate))
                        if (!student.reason.isNullOrBlank()) {
                            InfoRow(Icons.Filled.Info, "Reason", student.reason,
                                valueColor = MaterialTheme.colorScheme.error)
                        }
                    }

                    // ── Verification document ─────────────────────────────────
                    student.verificationDocument?.let { docUrl ->
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            Text("VERIFICATION DOCUMENT", fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold, color = DarkGreen,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 160.dp, max = 280.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showDocViewer = true }
                            ) {
                                SubcomposeAsyncImage(
                                    model = docUrl,
                                    contentDescription = "Verification Document",
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 280.dp),
                                    contentScale = ContentScale.FillWidth,
                                    loading = {
                                        ShimmerEffect(
                                            Modifier.fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    },
                                    error = {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(120.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Filled.BrokenImage, null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(36.dp))
                                                Spacer(Modifier.height(4.dp))
                                                Text("Could not load document", fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Tap to zoom", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // ── Decline reason input ──────────────────────────────────
                    if (showDeclineInput) {
                        OutlinedTextField(
                            value = declineReason,
                            onValueChange = { declineReason = it },
                            label = { Text("Reason for declining") },
                            placeholder = { Text("Enter reason (optional)") },
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )
                    }

                    // ── Block reason input ────────────────────────────────────
                    if (showBlockInput) {
                        OutlinedTextField(
                            value = blockReason,
                            onValueChange = { blockReason = it },
                            label = { Text("Reason for blocking") },
                            placeholder = { Text("Enter reason for blocking this student") },
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )
                    }

                    // ── Action buttons ────────────────────────────────────────
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (actionLoading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = DarkGreen, modifier = Modifier.size(32.dp))
                            }
                        } else {
                            // Approve — shown when not already approved
                            if (student.displayStatus != "approved") {
                                Button(
                                    onClick = {
                                        if (token == null) return@Button
                                        showDeclineInput = false
                                        showBlockInput = false
                                        scope.launch {
                                            actionLoading = true
                                            val ok = withContext(Dispatchers.IO) {
                                                updateStudentStatus(token, student.userId, "approve")
                                            }
                                            actionLoading = false
                                            resultDialog = if (ok)
                                                Pair(true, "Student has been approved successfully.")
                                            else
                                                Pair(false, "Approval failed. Please try again.")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Icon(Icons.Filled.CheckCircle, null,
                                        modifier = Modifier.size(18.dp).padding(end = 4.dp))
                                    Text("Approve", fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }

                            // Decline — shown when not already declined
                            if (student.displayStatus != "declined") {
                                if (!showDeclineInput) {
                                    OutlinedButton(
                                        onClick = { showDeclineInput = true; showBlockInput = false },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF44336))
                                    ) {
                                        Icon(Icons.Filled.Cancel, null,
                                            modifier = Modifier.size(18.dp).padding(end = 4.dp))
                                        Text("Decline", fontWeight = FontWeight.SemiBold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (token == null) return@Button
                                            scope.launch {
                                                actionLoading = true
                                                val ok = withContext(Dispatchers.IO) {
                                                    updateStudentStatus(
                                                        token,
                                                        student.userId,
                                                        "decline",
                                                        declineReason.trim().ifBlank { null }
                                                    )
                                                }
                                                actionLoading = false
                                                resultDialog = if (ok)
                                                    Pair(true, "Student has been declined successfully.")
                                                else
                                                    Pair(false, "Decline failed. Please try again.")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                                    ) {
                                        Icon(Icons.Filled.Cancel, null,
                                            modifier = Modifier.size(18.dp).padding(end = 4.dp))
                                        Text("Confirm Decline", fontWeight = FontWeight.SemiBold, color = Color.White)
                                    }
                                }
                            }

                            // Block — shown when not already blocked
                            if (student.displayStatus != "blocked") {
                                if (!showBlockInput) {
                                    OutlinedButton(
                                        onClick = { showBlockInput = true; showDeclineInput = false },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9C27B0)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9C27B0))
                                    ) {
                                        Icon(Icons.Filled.Block, null,
                                            modifier = Modifier.size(18.dp).padding(end = 4.dp))
                                        Text("Block Student", fontWeight = FontWeight.SemiBold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (token == null) return@Button
                                            scope.launch {
                                                actionLoading = true
                                                val ok = withContext(Dispatchers.IO) {
                                                    updateStudentStatus(token, student.userId, "block", blockReason.trim().ifBlank { null })
                                                }
                                                actionLoading = false
                                                resultDialog = if (ok)
                                                    Pair(true, "Student has been blocked successfully.")
                                                else
                                                    Pair(false, "Block failed. Please try again.")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                                    ) {
                                        Icon(Icons.Filled.Block, null,
                                            modifier = Modifier.size(18.dp).padding(end = 4.dp))
                                        Text("Confirm Block", fontWeight = FontWeight.SemiBold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Back / Close
                        TextButton(
                            onClick = { if (!actionLoading) onDismiss() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = DarkGreen.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("$label:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium, modifier = Modifier.width(110.dp))
        Text(value, fontSize = 13.sp,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
            modifier = Modifier.weight(1f))
    }
}

// ── Settings ───────────────────────────────────────────────────────────────────

@Composable
fun AdminSettingsContent(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onMenuClick: () -> Unit,
    role: String = "Administrator",
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0" }
        catch (e: Exception) { "1.0" }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        AdminPageHeader(title = "Settings", onMenuClick = onMenuClick, favoritesCount = favoritesCount, onFavoritesClick = onFavoritesClick)
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            SettingsSectionLabel("PREFERENCES")
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Row(modifier = Modifier.fillMaxWidth().clickable { onThemeToggle() }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                        .background(DarkGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            null, tint = DarkGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(if (isDarkMode) "Currently enabled" else "Currently disabled",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isDarkMode, onCheckedChange = { onThemeToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White, checkedTrackColor = DarkGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionLabel("ABOUT")
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    SettingsInfoRow(Icons.Filled.ShoppingBag, "App Name", "FatiMarket")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsInfoRow(Icons.Filled.Info, "Version", "v$versionName")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsInfoRow(Icons.Filled.AdminPanelSettings, "Role", role)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsInfoRow(Icons.Filled.School, "Institution", "Our Lady of Fatima University")
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkGreen,
        letterSpacing = 1.sp, modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 10.dp))
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = DarkGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium)
    }
}

// ── Profile ────────────────────────────────────────────────────────────────────

@Composable
fun AdminProfileContent(
    onMenuClick: () -> Unit,
    firstName: String,
    lastName: String,
    email: String,
    role: String,
    walletPoints: Int,
    profilePic: String,
    onProfilePicUpdated: (String) -> Unit,
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {}
) {
    val context     = LocalContext.current
    val prefs       = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val scope       = rememberCoroutineScope()
    val fullName    = "$firstName $lastName".trim().ifBlank { "Administrator" }
    val initial     = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // Auto-clear error after 3 s
    LaunchedEffect(uploadError) {
        if (uploadError != null) {
            delay(3000)
            uploadError = null
        }
    }

    // Image picker — uploads selected image to the profile picture API
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploading = true
                uploadError = null
                try {
                    val mimeType = context.contentResolver.getType(selectedUri) ?: "image/jpeg"
                    val ext = when {
                        mimeType.contains("png")  -> "png"
                        mimeType.contains("webp") -> "webp"
                        mimeType.contains("gif")  -> "gif"
                        else                      -> "jpg"
                    }
                    val file = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(selectedUri)
                            ?: return@withContext null
                        val f = File(context.filesDir, "user_profile_pic.$ext")
                        input.use { src -> f.outputStream().use { dst -> src.copyTo(dst) } }
                        f
                    }
                    if (file == null) {
                        uploadError = "Could not read the selected image."
                        isUploading = false
                        return@launch
                    }
                    val token  = prefs.getString("auth_token", "") ?: ""
                    val newUrl = withContext(Dispatchers.IO) { uploadProfilePicture(token, file, mimeType) }
                    if (newUrl != null) {
                        if (newUrl.isNotEmpty()) {
                            prefs.edit().putString("user_profile_picture", newUrl).apply()
                            onProfilePicUpdated(newUrl)
                        }
                        // newUrl == "" means upload succeeded but server didn't return a new URL — treat as success
                    } else {
                        uploadError = "Upload failed. Please try again."
                    }
                } catch (e: Exception) {
                    uploadError = e.message ?: "Upload failed."
                } finally {
                    isUploading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // ── Green header (no overlay buttons — fully centred) ─────────────────
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AdminPageHeader(title = "Profile", onMenuClick = onMenuClick, favoritesCount = favoritesCount, onFavoritesClick = onFavoritesClick)
            Spacer(modifier = Modifier.height(8.dp))

            // Avatar with camera-icon overlay
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else if (profilePic.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = profilePic,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = { ShimmerEffect(Modifier.fillMaxSize()) },
                            error = {
                                Text(initial, fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        )
                    } else {
                        Text(initial, fontSize = 36.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                // Camera badge
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isUploading) DarkGreen.copy(alpha = 0.5f) else DarkGreen)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable(enabled = !isUploading) { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, "Change Photo",
                        tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = Color.White, textAlign = TextAlign.Center)
            Text(role.replaceFirstChar { it.uppercaseChar() },
                fontSize = 14.sp, color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center)
            if (uploadError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uploadError!!,
                    fontSize = 12.sp,
                    color = Color(0xFFFF6B6B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // ── Info cards ────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("ACCOUNT INFORMATION", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = DarkGreen, letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    ProfileInfoRow(Icons.Filled.Person, "Full Name", fullName.ifBlank { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    ProfileInfoRow(Icons.Filled.Email, "Email", email.ifBlank { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    ProfileInfoRow(Icons.Filled.AdminPanelSettings, "Role",
                        role.replaceFirstChar { it.uppercaseChar() })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    ProfileInfoRow(Icons.Filled.AccountBalanceWallet, "Wallet Points",
                        "$walletPoints pts", valueColor = DarkGreen)
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
            .background(DarkGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = DarkGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor)
        }
    }
}

// ── Shared Page Header ─────────────────────────────────────────────────────────

@Composable
fun AdminPageHeader(
    title: String,
    onMenuClick: () -> Unit,
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val walletPoints = remember { prefs.getInt("user_wallet_points", 0) }

    Column(modifier = Modifier.fillMaxWidth()
        .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, "Menu", tint = Color.White)
            }
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.weight(1f))
            // Wallet points chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Filled.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$walletPoints pts",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Box {
                IconButton(onClick = onFavoritesClick) {
                    Icon(Icons.Outlined.FavoriteBorder, "Favorites", tint = Color.White)
                }
                if (favoritesCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .size(18.dp)
                            .background(Color(0xFFFF4444), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = if (favoritesCount > 99) "99+" else favoritesCount.toString(),
                            fontSize   = 8.sp,
                            lineHeight = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            textAlign  = TextAlign.Center
                        )
                    }
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.NotificationsNone, "Notifications", tint = Color.White)
            }
        }
    }
}

// ── Chat item network ──────────────────────────────────────────────────────────

private fun fetchChatItem(token: String, itemId: Int): ChatItem? {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: return null
            val json = JSONObject(raw)
            val obj  = json.optJSONObject("data") ?: json
            val photosArr = obj.optJSONArray("photos")
            val photos = if (photosArr != null) {
                (0 until photosArr.length()).map { j -> photosArr.getString(j) }
            } else emptyList()
            ChatItem(
                itemId       = obj.optInt("item_id"),
                title        = obj.optString("title"),
                description  = obj.optString("description"),
                markupPoints = obj.optInt("markup_points"),
                sellerEmail  = obj.optString("seller_email"),
                status       = obj.optString("status"),
                photos       = photos
            )
        }
    } catch (_: Exception) { null }
}


// Get all information of items base on status (admin endpoint — returns all users' items)
private fun fetchItems(token: String, status: String): List<Item> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/admin/items?status=$status")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    adminHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptyList()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}: $raw")
        return try {
            val json = JSONObject(raw)
            // handle both {"data":[...]} and a bare array
            val arr = when {
                json.has("data") -> json.getJSONArray("data")
                else             -> org.json.JSONArray(raw)
            }
            (0 until arr.length()).map { i ->
                val obj       = arr.getJSONObject(i)
                val photosArr = obj.optJSONArray("photos")
                val photos    = if (photosArr != null) {
                    (0 until photosArr.length()).map { j -> photosArr.getString(j) }
                } else emptyList()
                Item(
                    itemId       = obj.optInt("item_id"),
                    sellerId     = obj.optInt("seller_id"),
                    sellerEmail  = obj.optString("seller_email"),
                    title        = obj.optString("title"),
                    description  = obj.optString("description"),
                    categoryId   = obj.optInt("category_id"),
                    pricePoints  = obj.optInt("price_points"),
                    markupPoints = obj.optInt("markup_points"),
                    status       = obj.optString("status"),
                    photos       = photos,
                    createdAt    = obj.optString("created_at")
                )
            }
        } catch (e: Exception) { throw Exception("Parse error: ${e.message}") }
    }
}

private fun updateItemStatus(token: String, itemId: Int, status: String): Pair<Boolean, String> {
    val body = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("_method", "PUT")
        .addFormDataPart("status", status)
        .build()
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body)
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Pair(true, "")
            } else {
                val msg = try {
                    val json = JSONObject(raw)
                    json.optString("message", "HTTP ${response.code}")
                } catch (_: Exception) {
                    "HTTP ${response.code}"
                }
                Pair(false, msg)
            }
        }
    } catch (e: Exception) {
        Pair(false, e.message ?: "Network error")
    }
}

// Update item for admin with status and/or markup_points
private fun updateAdminItem(token: String, itemId: Int, status: String? = null, markupPoints: Int? = null): Pair<Boolean, String> {
    val body = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("_method", "PUT")

    if (status != null) {
        body.addFormDataPart("status", status)
    }
    if (markupPoints != null) {
        body.addFormDataPart("markup_points", markupPoints.toString())
    }

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/admin/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body.build())
        .build()

    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Pair(true, "")
            } else {
                val msg = try {
                    val json = JSONObject(raw)
                    json.optString("message", "HTTP ${response.code}")
                } catch (_: Exception) {
                    "HTTP ${response.code}"
                }
                Pair(false, msg)
            }
        }
    } catch (e: Exception) {
        Pair(false, e.message ?: "Network error")
    }
}