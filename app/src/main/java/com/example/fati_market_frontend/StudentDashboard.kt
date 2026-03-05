package com.example.fati_market_frontend

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.fati_market_frontend.ui.theme.DarkGreen
import com.example.fati_market_frontend.ui.theme.DarkGreenLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

// ── Tabs ───────────────────────────────────────────────────────────────────────

private enum class StudentTab { HOME, CHAT, ADD_ITEM, SETTINGS, PROFILE }
private enum class SortOption { NEWEST, PRICE_LOW_HIGH, PRICE_HIGH_LOW }

// ── HTTP client ────────────────────────────────────────────────────────────────

private val studentHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

// ── Student Dashboard ──────────────────────────────────────────────────────────

@Composable
fun StudentDashboard(isDarkMode: Boolean, onThemeToggle: () -> Unit, onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }

    var userFirstName    by remember { mutableStateOf(prefs.getString("user_first_name", "") ?: "") }
    var userLastName     by remember { mutableStateOf(prefs.getString("user_last_name",  "") ?: "") }
    val userEmail        = remember { prefs.getString("user_email", "") ?: "" }
    val userRole         = remember { prefs.getString("user_role", "student") ?: "student" }
    val token            = remember { prefs.getString("auth_token", "") ?: "" }
    val userWalletPoints = remember { prefs.getInt("user_wallet_points", 0) }
    var userProfilePic   by remember { mutableStateOf(prefs.getString("user_profile_picture", "") ?: "") }

    var selectedTab      by remember { mutableStateOf(StudentTab.HOME) }
    var chatConversation by remember { mutableStateOf<Conversation?>(null) }
    var showMyListings   by remember { mutableStateOf(false) }

    // ── Global favorites state (shared across all pages) ──────────────────────
    var favoritedIds    by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showFavorites   by remember { mutableStateOf(false) }
    var selectedFavItem by remember { mutableStateOf<Item?>(null) }
    val drawerState  = rememberDrawerState(DrawerValue.Closed)
    val scope        = rememberCoroutineScope()
    val openDrawer   = { scope.launch { drawerState.open() } }

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

    LaunchedEffect(Unit) {
        favoritedIds = withContext(Dispatchers.IO) { fetchFavoriteIds(token) }
    }

    // ── Favorites overlays (accessible from any page) ─────────────────────────
    selectedFavItem?.let { item ->
        ItemDetailDialog(
            item             = item,
            token            = token,
            isFavorited      = favoritedIds.contains(item.itemId),
            onFavoriteToggle = { itemId, nowFav ->
                favoritedIds = if (nowFav) favoritedIds + itemId else favoritedIds - itemId
            },
            onGoToChat       = { selectedFavItem = null; selectedTab = StudentTab.CHAT },
            onDismiss        = { selectedFavItem = null }
        )
    }
    if (showFavorites) {
        FavoritesScreen(
            token             = token,
            onFavoriteRemoved = { itemId -> favoritedIds = favoritedIds - itemId },
            onItemClick       = { item -> selectedFavItem = item },
            onDismiss         = { showFavorites = false }
        )
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
                drawerShape          = RoundedCornerShape(0.dp),
                windowInsets         = WindowInsets(0),
                modifier             = Modifier.width(280.dp)
            ) {
                StudentDrawerContent(
                    showMyListings = showMyListings,
                    userFirstName  = userFirstName,
                    userLastName   = userLastName,
                    userEmail      = userEmail,
                    userRole       = userRole,
                    userProfilePic = userProfilePic,
                    onHome         = {
                        showMyListings = false
                        selectedTab    = StudentTab.HOME
                        scope.launch { drawerState.close() }
                    },
                    onMyListings   = {
                        showMyListings = true
                        scope.launch { drawerState.close() }
                    },
                    onLogout       = onLogout
                )
            }
        }
    ) {
        val chatIsOpen = selectedTab == StudentTab.CHAT && chatConversation != null

        Scaffold(
            bottomBar = {
                if (!chatIsOpen) {
                    StudentBottomBar(
                        selected       = selectedTab,
                        userProfilePic = userProfilePic,
                        userInitial    = userFirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                        onSelect       = { tab ->
                            selectedTab      = tab
                            showMyListings   = false
                            chatConversation = null
                        }
                    )
                }
            },
            contentWindowInsets = WindowInsets(0),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (chatIsOpen) 0.dp else innerPadding.calculateBottomPadding())
            ) {
                if (showMyListings) {
                    StudentMyListingsContent(
                        onMenuClick      = { openDrawer() },
                        favoritesCount   = favoritedIds.size,
                        onFavoritesClick = { showFavorites = true },
                        onGoToChat       = { showMyListings = false; selectedTab = StudentTab.CHAT }
                    )
                } else {
                    when (selectedTab) {
                        StudentTab.HOME     -> StudentHomeContent(
                            onMenuClick          = { openDrawer() },
                            favoritedIds         = favoritedIds,
                            onFavoritedIdsChange = { favoritedIds = it },
                            onFavoritesClick     = { showFavorites = true },
                            onGoToChat           = { selectedTab = StudentTab.CHAT }
                        )
                        StudentTab.CHAT     -> AdminChatContent(
                            onMenuClick          = { openDrawer() },
                            selectedConversation = chatConversation,
                            onSelectConversation = { chatConversation = it },
                            favoritesCount       = favoritedIds.size,
                            onFavoritesClick     = { showFavorites = true }
                        )
                        StudentTab.ADD_ITEM -> StudentAddItemContent(
                            onMenuClick      = { openDrawer() },
                            favoritesCount   = favoritedIds.size,
                            onFavoritesClick = { showFavorites = true }
                        )
                        StudentTab.SETTINGS -> AdminSettingsContent(
                            isDarkMode       = isDarkMode,
                            onThemeToggle    = onThemeToggle,
                            onMenuClick      = { openDrawer() },
                            role             = userRole.replaceFirstChar { it.uppercaseChar() },
                            favoritesCount   = favoritedIds.size,
                            onFavoritesClick = { showFavorites = true }
                        )
                        StudentTab.PROFILE  -> AdminProfileContent(
                            onMenuClick         = { openDrawer() },
                            firstName           = userFirstName,
                            lastName            = userLastName,
                            email               = userEmail,
                            role                = userRole,
                            walletPoints        = userWalletPoints,
                            profilePic          = userProfilePic,
                            onProfilePicUpdated = { path -> userProfilePic = path },
                            favoritesCount      = favoritedIds.size,
                            onFavoritesClick    = { showFavorites = true }
                        )
                    }
                }
            }
        }
    }
}

// ── Home ───────────────────────────────────────────────────────────────────────

internal data class Item(
    val itemId: Int,
    val sellerId: Int,
    val sellerEmail: String,
    val title: String,
    val description: String,
    val categoryId: Int,
    val pricePoints: Int,
    val markupPoints: Int,
    val status: String,
    val photos: List<String>,
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentHomeContent(
    onMenuClick: () -> Unit,
    favoritedIds: Set<Int>,
    onFavoritedIdsChange: (Set<Int>) -> Unit,
    onFavoritesClick: () -> Unit,
    onGoToChat: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token   = remember { prefs.getString("auth_token", "") ?: "" }
    val scope   = rememberCoroutineScope()

    var allItems         by remember { mutableStateOf<List<Item>>(emptyList()) }
    var categories       by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(true) }
    var errorMessage     by remember { mutableStateOf<String?>(null) }
    var searchQuery      by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var sortOption       by remember { mutableStateOf(SortOption.NEWEST) }
    var selectedItem     by remember { mutableStateOf<Item?>(null) }

    fun loadData() {
        scope.launch {
            isLoading    = true
            errorMessage = null
            try {
                val itemsResult = withContext(Dispatchers.IO) { fetchItems(token, "public") }
                val catsResult  = withContext(Dispatchers.IO) { fetchCategories(token) }
                allItems   = itemsResult
                categories = catsResult
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load items"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    val categoryMap  = remember(categories) { categories.associateBy { it.id } }
    val isFiltered   = searchQuery.isNotBlank() || selectedCategory != null || sortOption != SortOption.NEWEST

    val displayItems = remember(allItems, searchQuery, selectedCategory, sortOption) {
        allItems
            .filter { item ->
                (selectedCategory == null || item.categoryId == selectedCategory) &&
                (searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true))
            }
            .let { filtered ->
                when (sortOption) {
                    SortOption.NEWEST         -> filtered
                    SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.markupPoints }
                    SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.markupPoints }
                }
            }
    }

    val categoryRows = remember(allItems, categories) {
        categories.mapNotNull { cat ->
            val catItems = allItems.filter { it.categoryId == cat.id }
            if (catItems.isNotEmpty()) Pair(cat, catItems) else null
        }
    }

    // ── Item detail overlay (home page items) ─────────────────────────────────
    selectedItem?.let { item ->
        ItemDetailDialog(
            item             = item,
            token            = token,
            isFavorited      = favoritedIds.contains(item.itemId),
            onFavoriteToggle = { itemId, nowFav ->
                onFavoritedIdsChange(if (nowFav) favoritedIds + itemId else favoritedIds - itemId)
            },
            onGoToChat       = { selectedItem = null; onGoToChat() },
            onDismiss        = { selectedItem = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MarketplaceHeader(
            onMenuClick      = onMenuClick,
            searchQuery      = searchQuery,
            onSearchChange   = { searchQuery = it },
            onClearSearch    = { searchQuery = "" },
            favoritesCount   = favoritedIds.size,
            onFavoritesClick = onFavoritesClick
        )

        // ── Category filter chips (always visible) ────────────────────────────
        if (categories.isNotEmpty()) {
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected    = selectedCategory == null,
                        onClick     = { selectedCategory = null },
                        label       = { Text("All", fontSize = 12.sp) },
                        leadingIcon = if (selectedCategory == null) {
                            { Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor   = DarkGreen,
                            selectedLabelColor       = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected    = selectedCategory == cat.id,
                        onClick     = { selectedCategory = if (selectedCategory == cat.id) null else cat.id },
                        label       = { Text(cat.name, fontSize = 12.sp) },
                        leadingIcon = if (selectedCategory == cat.id) {
                            { Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor   = DarkGreen,
                            selectedLabelColor       = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        }

        // ── Sort + count row (always visible) ─────────────────────────────────
        var sortExpanded by remember { mutableStateOf(false) }
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                if (isFiltered) "${displayItems.size} items found" else "${allItems.size} listings",
                fontSize = 12.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box {
                OutlinedButton(
                    onClick        = { sortExpanded = true },
                    border         = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape          = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier       = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.Sort, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(when (sortOption) {
                        SortOption.NEWEST         -> "Newest"
                        SortOption.PRICE_LOW_HIGH -> "Price ↑"
                        SortOption.PRICE_HIGH_LOW -> "Price ↓"
                    }, fontSize = 12.sp)
                }
                DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                    DropdownMenuItem(text = { Text("Newest") }, onClick = { sortOption = SortOption.NEWEST; sortExpanded = false },
                        leadingIcon = { if (sortOption == SortOption.NEWEST) Icon(Icons.Filled.Check, null, tint = DarkGreen, modifier = Modifier.size(16.dp)) })
                    DropdownMenuItem(text = { Text("Price: Low to High") }, onClick = { sortOption = SortOption.PRICE_LOW_HIGH; sortExpanded = false },
                        leadingIcon = { if (sortOption == SortOption.PRICE_LOW_HIGH) Icon(Icons.Filled.Check, null, tint = DarkGreen, modifier = Modifier.size(16.dp)) })
                    DropdownMenuItem(text = { Text("Price: High to Low") }, onClick = { sortOption = SortOption.PRICE_HIGH_LOW; sortExpanded = false },
                        leadingIcon = { if (sortOption == SortOption.PRICE_HIGH_LOW) Icon(Icons.Filled.Check, null, tint = DarkGreen, modifier = Modifier.size(16.dp)) })
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // ── Content ───────────────────────────────────────────────────────────
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkGreen)
            }
            errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.padding(horizontal = 24.dp)
                ) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { loadData() }, colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            // Filtered: 2-column grid
            isFiltered && displayItems.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.SearchOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                    Text("No items match your search.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { searchQuery = ""; selectedCategory = null }) {
                        Text("Clear filters", color = DarkGreen)
                    }
                }
            }
            isFiltered -> LazyVerticalGrid(
                columns               = GridCells.Fixed(2),
                contentPadding        = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement   = Arrangement.spacedBy(10.dp),
                modifier              = Modifier.fillMaxSize()
            ) {
                items(displayItems) { item ->
                    PublicItemCard(
                        item             = item,
                        categoryName     = categoryMap[item.categoryId]?.name ?: "",
                        isFavorited      = favoritedIds.contains(item.itemId),
                        onFavoriteToggle = {
                            scope.launch {
                                val nowFav = !favoritedIds.contains(item.itemId)
                                val ok = withContext(Dispatchers.IO) {
                                    if (nowFav) addFavorite(token, item.itemId)
                                    else removeFavorite(token, item.itemId)
                                }
                                if (ok) onFavoritedIdsChange(if (nowFav) favoritedIds + item.itemId else favoritedIds - item.itemId)
                            }
                        },
                        onItemClick = { selectedItem = item }
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
            }
            // Default: category rows
            categoryRows.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                    Text("No items available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(categoryRows) { (category, catItems) ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(category.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(
                                onClick        = { selectedCategory = category.id },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("See all", color = DarkGreen, fontSize = 12.sp)
                                Icon(Icons.Filled.ChevronRight, null, tint = DarkGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(catItems.take(10)) { item ->
                                PublicItemCard(
                                    item             = item,
                                    categoryName     = "",
                                    modifier         = Modifier.width(160.dp),
                                    isFavorited      = favoritedIds.contains(item.itemId),
                                    onFavoriteToggle = {
                                        scope.launch {
                                            val nowFav = !favoritedIds.contains(item.itemId)
                                            val ok = withContext(Dispatchers.IO) {
                                                if (nowFav) addFavorite(token, item.itemId)
                                                else removeFavorite(token, item.itemId)
                                            }
                                            if (ok) onFavoritedIdsChange(if (nowFav) favoritedIds + item.itemId else favoritedIds - item.itemId)
                                        }
                                    },
                                    onItemClick = { selectedItem = item }
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 14.dp),
                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

// ── Marketplace header with search ─────────────────────────────────────────────

@Composable
private fun MarketplaceHeader(
    onMenuClick: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {}
) {
    val context      = LocalContext.current
    val walletPoints = remember { context.getSharedPreferences("fatimarket_prefs", 0).getInt("user_wallet_points", 0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, null, tint = Color.White)
            }
            Text(
                "Marketplace",
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                modifier   = Modifier.weight(1f)
            )
            // Wallet pts chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier
                    .padding(end = 4.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Filled.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("$walletPoints pts", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Box {
                IconButton(onClick = onFavoritesClick) {
                    Icon(Icons.Outlined.FavoriteBorder, null, tint = Color.White)
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
                Icon(Icons.Filled.NotificationsNone, null, tint = Color.White)
            }
        }
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            placeholder   = { Text("Search items…", color = Color.White.copy(alpha = 0.55f), fontSize = 14.sp) },
            leadingIcon   = { Icon(Icons.Filled.Search, null, tint = Color.White.copy(alpha = 0.75f)) },
            trailingIcon  = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = onClearSearch) { Icon(Icons.Filled.Close, null, tint = Color.White.copy(alpha = 0.75f)) } }
            } else null,
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor    = Color.White.copy(alpha = 0.35f),
                focusedBorderColor      = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                focusedContainerColor   = Color.White.copy(alpha = 0.18f),
                cursorColor             = Color.White,
                unfocusedTextColor      = Color.White,
                focusedTextColor        = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 14.dp),
            shape    = RoundedCornerShape(50.dp)
        )
    }
}

// ── My Listings (private, with edit) ──────────────────────────────────────────

@Composable
private fun StudentMyListingsContent(
    onMenuClick: () -> Unit,
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {},
    onGoToChat: () -> Unit = {}
) {
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
                errorMessage = e.message ?: "Failed to load listings"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadItems() }

    editingItem?.let { item ->
        EditItemDialog(
            item      = item,
            token     = token,
            onDismiss = { editingItem = null },
            onSuccess = { editingItem = null; loadItems() }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "My Listings", onMenuClick = onMenuClick, favoritesCount = favoritesCount, onFavoritesClick = onFavoritesClick)

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkGreen)
            }
            errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.padding(horizontal = 24.dp)
                ) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { loadItems() }, colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            itemList.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                    Text("You have no listings yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(itemList) { item ->
                    PrivateItemCard(
                        item       = item,
                        token      = token,
                        onEdit     = { editingItem = item },
                        onDelete   = { itemList = itemList.filter { it.itemId != item.itemId } },
                        onGoToChat = onGoToChat
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ── Public item card (2-column grid, marketplace style) ────────────────────────

@Composable
private fun PublicItemCard(
    item: Item,
    categoryName: String = "",
    modifier: Modifier = Modifier,
    isFavorited: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onItemClick: () -> Unit = {}
) {
    Card(
        modifier  = modifier.fillMaxWidth().clickable(onClick = onItemClick),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // ── Photo + category badge overlay ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (item.photos.isNotEmpty()) {
                    AsyncImage(
                        model              = item.photos.first(),
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Photo, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp))
                    }
                }
                // Heart / favorite button (top-left overlay)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(30.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        .clickable(onClick = onFavoriteToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        null,
                        tint     = if (isFavorited) Color(0xFFFF4444) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                // Category badge (top-right)
                if (categoryName.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(7.dp),
                        shape  = RoundedCornerShape(50),
                        color  = DarkGreen.copy(alpha = 0.88f)
                    ) {
                        Text(
                            categoryName,
                            modifier  = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize  = 9.sp,
                            color     = Color.White,
                            fontWeight = FontWeight.Medium,
                            maxLines  = 1,
                            overflow  = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ── Info ──────────────────────────────────────────────────────────
            Column(
                modifier            = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                    Text(
                        item.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 13.sp,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Filled.MonetizationOn, null, tint = DarkGreen, modifier = Modifier.size(14.dp))
                        Text(
                            "${item.markupPoints} pts",
                            fontWeight = FontWeight.Bold,
                            color      = DarkGreen,
                            fontSize   = 13.sp
                        )
                    }
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Filled.Person, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp))
                        Text(
                            item.sellerEmail.substringBefore("@"),
                            fontSize = 10.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

// ── Private item card (full-width, editable) ───────────────────────────────────

@Composable
private fun PrivateItemCard(item: Item, token: String, onEdit: () -> Unit, onDelete: () -> Unit, onGoToChat: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var chatText          by remember { mutableStateOf("") }
    var isSendingChat     by remember { mutableStateOf(false) }
    var chatError         by remember { mutableStateOf<String?>(null) }
    var chatSent          by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting        by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon  = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
            title = { Text("Remove Listing", fontWeight = FontWeight.Bold) },
            text  = { Text("Are you sure you want to delete \"${item.title}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            isDeleting = true
                            val ok = withContext(Dispatchers.IO) { deleteItem(token, item.itemId) }
                            isDeleting = false
                            if (ok) onDelete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove", color = Color.White, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Photo
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
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Photo, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                }
            }

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title + status chip
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
                        else       -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val statusBg = when (item.status.lowercase()) {
                        "approved" -> DarkGreen.copy(alpha = 0.12f)
                        "rejected" -> MaterialTheme.colorScheme.errorContainer
                        else       -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Surface(shape = RoundedCornerShape(50), color = statusBg) {
                        Text(
                            item.status.replaceFirstChar { it.uppercaseChar() },
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color      = statusColor
                        )
                    }
                }

                Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                HorizontalDivider()

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.MonetizationOn, null, tint = DarkGreen, modifier = Modifier.size(16.dp))
                        Text("${item.pricePoints} pts", fontWeight = FontWeight.Bold, color = DarkGreen, fontSize = 14.sp)
                    }
                    Text("Cat. [${item.categoryId}]", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider()

                // ── Chat to Ofelia Store ───────────────────────────────────────
                Text(
                    "Message Ofelia Store",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                chatError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                if (chatSent) {
                    AlertDialog(
                        onDismissRequest = { chatSent = false },
                        icon  = { Icon(Icons.Filled.CheckCircle, null, tint = DarkGreen, modifier = Modifier.size(36.dp)) },
                        title = { Text("Message Sent!", fontWeight = FontWeight.Bold) },
                        text  = { Text("Your message has been sent to Ofelia Store successfully.") },
                        confirmButton = {
                            Button(
                                onClick = { chatSent = false; onGoToChat() },
                                colors  = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) { Text("Go to Chat", color = Color.White, fontWeight = FontWeight.SemiBold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { chatSent = false }) { Text("Close") }
                        }
                    )
                }
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value           = chatText,
                        onValueChange   = { chatText = it; chatError = null; chatSent = false },
                        placeholder     = { Text("Ask about this listing...", fontSize = 12.sp) },
                        modifier        = Modifier.weight(1f),
                        shape           = RoundedCornerShape(12.dp),
                        maxLines        = 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (!isSendingChat && chatText.isNotBlank()) {
                                scope.launch {
                                    isSendingChat = true
                                    val ok = withContext(Dispatchers.IO) {
                                        sendMessageToAdmin(token, item.itemId, chatText.trim())
                                    }
                                    isSendingChat = false
                                    if (ok) { chatSent = true; chatText = "" }
                                    else chatError = "Failed to send. Please try again."
                                }
                            }
                        })
                    )
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (isSendingChat) DarkGreen.copy(alpha = 0.5f) else DarkGreen,
                                CircleShape
                            )
                            .clickable(enabled = !isSendingChat) {
                                if (chatText.isBlank()) { chatError = "Please enter a message."; return@clickable }
                                scope.launch {
                                    isSendingChat = true
                                    chatError = null
                                    val ok = withContext(Dispatchers.IO) {
                                        sendMessageToAdmin(token, item.itemId, chatText.trim())
                                    }
                                    isSendingChat = false
                                    if (ok) { chatSent = true; chatText = "" }
                                    else chatError = "Failed to send. Please try again."
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSendingChat) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // ── Action buttons ────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick  = onEdit,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick  = { showDeleteConfirm = true },
                        enabled  = !isDeleting,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Remove", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ── Edit Item dialog ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemDialog(
    item: Item,
    token: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var title             by remember { mutableStateOf(item.title) }
    var description       by remember { mutableStateOf(item.description) }
    var pricePoints       by remember { mutableStateOf(item.pricePoints.toString()) }
    var selectedCategory  by remember { mutableStateOf<Category?>(null) }
    var categories        by remember { mutableStateOf<List<Category>>(emptyList()) }
    var categoriesLoading by remember { mutableStateOf(true) }
    var categoryExpanded  by remember { mutableStateOf(false) }
    var newUris           by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading         by remember { mutableStateOf(false) }
    var errorMessage      by remember { mutableStateOf<String?>(null) }
    var showSuccess       by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val cats = withContext(Dispatchers.IO) { fetchCategories(token) }
        categories       = cats
        selectedCategory = cats.find { it.id == item.categoryId }
        categoriesLoading = false
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        newUris = (newUris + uris).distinctBy { it.toString() }.take(5)
    }

    fun validate(): String? {
        if (title.isBlank())                        return "Title is required"
        if (description.isBlank())                  return "Description is required"
        if (selectedCategory == null)               return "Category is required"
        if (pricePoints.isBlank())                  return "Price points is required"
        if ((pricePoints.toIntOrNull() ?: -1) < 0) return "Price points must be 0 or more"
        return null
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

            if (showSuccess) {
                AlertDialog(
                    onDismissRequest = onSuccess,
                    icon    = { Icon(Icons.Filled.CheckCircle, null, tint = DarkGreen, modifier = Modifier.size(40.dp)) },
                    title   = { Text("Item Updated!", fontWeight = FontWeight.Bold) },
                    text    = { Text("Your listing has been updated successfully.") },
                    confirmButton = {
                        Button(onClick = onSuccess, colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) {
                            Text("Done", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGreen)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text("Edit Listing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                // Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    OutlinedTextField(
                        value          = title,
                        onValueChange  = { if (it.length <= 255) title = it },
                        label          = { Text("Title") },
                        leadingIcon    = { Icon(Icons.Filled.Title, null, tint = DarkGreen) },
                        singleLine     = true,
                        supportingText = { Text("${title.length}/255") },
                        modifier       = Modifier.fillMaxWidth(),
                        shape          = RoundedCornerShape(12.dp)
                    )

                    // Description
                    OutlinedTextField(
                        value          = description,
                        onValueChange  = { if (it.length <= 1000) description = it },
                        label          = { Text("Description") },
                        leadingIcon    = { Icon(Icons.Filled.Description, null, tint = DarkGreen) },
                        minLines       = 4,
                        maxLines       = 6,
                        supportingText = { Text("${description.length}/1000") },
                        modifier       = Modifier.fillMaxWidth(),
                        shape          = RoundedCornerShape(12.dp)
                    )

                    // Category
                    ExposedDropdownMenuBox(
                        expanded         = categoryExpanded,
                        onExpandedChange = { if (!categoriesLoading) categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value         = selectedCategory?.let { "[${it.id}] ${it.name}" } ?: "",
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Category") },
                            leadingIcon   = {
                                if (categoriesLoading)
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = DarkGreen)
                                else
                                    Icon(Icons.Filled.Category, null, tint = DarkGreen)
                            },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(),
                            shape         = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            if (categories.isEmpty()) {
                                DropdownMenuItem(
                                    text    = { Text("No categories available", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { categoryExpanded = false }
                                )
                            } else {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text    = { Text("[${cat.id}] ${cat.name}") },
                                        onClick = { selectedCategory = cat; categoryExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // Price Points
                    OutlinedTextField(
                        value           = pricePoints,
                        onValueChange   = { v -> if (v.all { it.isDigit() } && v.length <= 8) pricePoints = v },
                        label           = { Text("Price Points") },
                        leadingIcon     = { Icon(Icons.Filled.MonetizationOn, null, tint = DarkGreen) },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.fillMaxWidth(),
                        shape           = RoundedCornerShape(12.dp)
                    )

                    // Current photos (read-only display)
                    if (item.photos.isNotEmpty()) {
                        Text("Current Photos", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(item.photos) { url ->
                                AsyncImage(
                                    model              = url,
                                    contentDescription = null,
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                )
                            }
                        }
                    }

                    // New photos (optional replacement)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("Replace Photos (optional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${newUris.size} / 5", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (newUris.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(newUris) { uri ->
                                    Box {
                                        AsyncImage(
                                            model              = uri,
                                            contentDescription = null,
                                            contentScale       = ContentScale.Crop,
                                            modifier           = Modifier
                                                .size(90.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.55f))
                                                .clickable { newUris = newUris - uri },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(13.dp))
                                        }
                                    }
                                }
                            }
                        }
                        if (newUris.size < 5) {
                            OutlinedButton(
                                onClick  = { photoPicker.launch("image/*") },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape    = RoundedCornerShape(12.dp),
                                border   = BorderStroke(1.5.dp, DarkGreen)
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, null, tint = DarkGreen, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (newUris.isEmpty()) "Pick New Photos" else "Add More",
                                    color = DarkGreen, fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            "Leave empty to keep current photos",
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Error
                    errorMessage?.let { err ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(err, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                            }
                        }
                    }

                    // Submit
                    Button(
                        onClick = {
                            val err = validate()
                            if (err != null) { errorMessage = err; return@Button }
                            errorMessage = null
                            scope.launch {
                                isLoading = true
                                try {
                                    val photoFiles = if (newUris.isNotEmpty()) {
                                        withContext(Dispatchers.IO) {
                                            newUris.mapIndexedNotNull { index, uri ->
                                                val rawMime  = context.contentResolver.getType(uri) ?: "image/jpeg"
                                                val mimeType = if (rawMime.contains("png")) "image/png" else "image/jpeg"
                                                val ext      = if (mimeType == "image/png") "png" else "jpg"
                                                val input    = context.contentResolver.openInputStream(uri) ?: return@mapIndexedNotNull null
                                                val file     = File(context.cacheDir, "edit_photo_$index.$ext")
                                                input.use { src -> file.outputStream().use { dst -> src.copyTo(dst) } }
                                                if (file.length() > 5 * 1024 * 1024) { file.delete(); return@mapIndexedNotNull null }
                                                Pair(file, mimeType)
                                            }
                                        }
                                    } else emptyList()

                                    val (success, msg) = withContext(Dispatchers.IO) {
                                        updateItem(
                                            token       = token,
                                            itemId      = item.itemId,
                                            title       = title.trim(),
                                            description = description.trim(),
                                            categoryId  = selectedCategory!!.id,
                                            pricePoints = pricePoints.toInt(),
                                            photoFiles  = photoFiles
                                        )
                                    }
                                    if (success) showSuccess = true else errorMessage = msg
                                } catch (e: Exception) {
                                    errorMessage = "Unexpected error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled  = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ── Item Detail Dialog ─────────────────────────────────────────────────────────

@Composable
private fun ItemDetailDialog(
    item: Item,
    token: String,
    isFavorited: Boolean,
    onFavoriteToggle: (itemId: Int, nowFav: Boolean) -> Unit,
    onGoToChat: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var detailItem        by remember { mutableStateOf(item) }
    var isLoading         by remember { mutableStateOf(true) }
    var isFav             by remember { mutableStateOf(isFavorited) }
    var isToggling        by remember { mutableStateOf(false) }
    var currentImageIndex by remember { mutableStateOf(0) }
    var messageText       by remember { mutableStateOf("Available paba?") }
    var isSending         by remember { mutableStateOf(false) }
    var showSentDialog    by remember { mutableStateOf(false) }
    var sendError         by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item.itemId) {
        val detail    = withContext(Dispatchers.IO) { fetchItemDetail(token, item.itemId) }
        val favStatus = withContext(Dispatchers.IO) { checkFavorite(token, item.itemId) }
        if (detail != null) detailItem = detail
        isFav     = favStatus
        isLoading = false
    }

    // ── Success dialog ──────────────────────────────────────────────────────────
    if (showSentDialog) {
        AlertDialog(
            onDismissRequest = { showSentDialog = false },
            icon = {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint     = DarkGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title   = { Text("Message Sent!", fontWeight = FontWeight.Bold) },
            text    = { Text("Your message has been sent to Ofelia Store successfully.") },
            confirmButton = {
                Button(
                    onClick = { showSentDialog = false; onGoToChat() },
                    colors  = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Go to Chat", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSentDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Top bar ────────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGreen)
                ) {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text(
                            "Item Details",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp,
                            modifier   = Modifier.weight(1f)
                        )
                        if (!isLoading) {
                            IconButton(
                                onClick = {
                                    if (!isToggling) {
                                        isToggling = true
                                        scope.launch {
                                            val nowFav = !isFav
                                            val ok = withContext(Dispatchers.IO) {
                                                if (nowFav) addFavorite(token, detailItem.itemId)
                                                else removeFavorite(token, detailItem.itemId)
                                            }
                                            if (ok) { isFav = nowFav; onFavoriteToggle(detailItem.itemId, nowFav) }
                                            isToggling = false
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    null,
                                    tint = if (isFav) Color(0xFFFF4444) else Color.White
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DarkGreen)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ── Images ─────────────────────────────────────────────
                        if (detailItem.photos.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            ) {
                                AsyncImage(
                                    model              = detailItem.photos[currentImageIndex],
                                    contentDescription = null,
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize()
                                )
                                if (detailItem.photos.size > 1) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp),
                                        color    = Color.Black.copy(alpha = 0.55f),
                                        shape    = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            "${currentImageIndex + 1} / ${detailItem.photos.size}",
                                            color    = Color.White,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            // Thumbnail strip
                            if (detailItem.photos.size > 1) {
                                LazyRow(
                                    contentPadding        = PaddingValues(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(detailItem.photos) { index, url ->
                                        AsyncImage(
                                            model              = url,
                                            contentDescription = null,
                                            contentScale       = ContentScale.Crop,
                                            modifier           = Modifier
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
                                Icon(Icons.Filled.Photo, null,
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp))
                            }
                        }

                        // ── Info ───────────────────────────────────────────────
                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(detailItem.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)

                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.MonetizationOn, null, tint = DarkGreen, modifier = Modifier.size(22.dp))
                                Text("${detailItem.markupPoints} pts", fontWeight = FontWeight.Bold, color = DarkGreen, fontSize = 20.sp)
                            }

                            // Status chip
                            val statusColor = when (detailItem.status.lowercase()) {
                                "approved" -> DarkGreen
                                "rejected" -> MaterialTheme.colorScheme.error
                                else       -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = statusColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    detailItem.status.replaceFirstChar { it.uppercaseChar() },
                                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = statusColor
                                )
                            }

                            HorizontalDivider()

                            // Seller
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Person, null,
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp))
                                Text(detailItem.sellerEmail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            HorizontalDivider()

                            // Description
                            Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(detailItem.description, fontSize = 14.sp, lineHeight = 20.sp)

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // ── Send message ──────────────────────────────────
                            sendError?.let { err ->
                                Text(
                                    text     = err,
                                    color    = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                            OutlinedTextField(
                                value         = messageText,
                                onValueChange = { messageText = it; sendError = null },
                                placeholder   = { Text("Available paba?") },
                                modifier      = Modifier.weight(1f),
                                shape         = RoundedCornerShape(12.dp),
                                maxLines      = 3,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (!isSending && messageText.isNotBlank()) {
                                        scope.launch {
                                            isSending = true
                                            sendError = null
                                            val ok = withContext(Dispatchers.IO) {
                                                sendMessageToAdmin(token, detailItem.itemId, messageText.trim())
                                            }
                                            isSending = false
                                            if (ok) { showSentDialog = true; messageText = "Available paba?" }
                                            else sendError = "Failed to send. Please try again."
                                        }
                                    }
                                })
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isSending) DarkGreen.copy(alpha = 0.5f) else DarkGreen,
                                        CircleShape
                                    )
                                    .clickable(enabled = !isSending) {
                                        if (messageText.isBlank()) {
                                            sendError = "Please enter a message first."
                                            return@clickable
                                        }
                                        scope.launch {
                                            isSending = true
                                            sendError = null
                                            val ok = withContext(Dispatchers.IO) {
                                                sendMessageToAdmin(token, detailItem.itemId, messageText.trim())
                                            }
                                            isSending = false
                                            if (ok) { showSentDialog = true; messageText = "Available paba?" }
                                            else sendError = "Failed to send. Please try again."
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(
                                        color       = Color.White,
                                        modifier    = Modifier.size(22.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            }   // closes send Row
                        }
                    }
                }
            }
        }
    }
}

// ── Favorites Screen ───────────────────────────────────────────────────────────

@Composable
private fun FavoritesScreen(
    token: String,
    onFavoriteRemoved: (Int) -> Unit,
    onItemClick: (Item) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var favoriteItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var removingId    by remember { mutableStateOf<Int?>(null) }
    var pendingRemove by remember { mutableStateOf<Item?>(null) }
    var categories    by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val favs = withContext(Dispatchers.IO) { fetchFavoriteItems(token) }
            val cats = withContext(Dispatchers.IO) { fetchCategories(token) }
            favoriteItems = favs
            categories = cats
        } catch (_: Exception) {}
        isLoading = false
    }

    val filteredItems = remember(favoriteItems, selectedCategoryId) {
        if (selectedCategoryId == null) favoriteItems
        else favoriteItems.filter { it.categoryId == selectedCategoryId }
    }

    // Undo-style: brief confirmation before actually removing
    pendingRemove?.let { toRemove ->
        LaunchedEffect(toRemove) {
            delay(1200)
            if (pendingRemove?.itemId == toRemove.itemId) {
                removingId = toRemove.itemId
                val ok = withContext(Dispatchers.IO) { removeFavorite(token, toRemove.itemId) }
                if (ok) {
                    favoriteItems = favoriteItems.filter { it.itemId != toRemove.itemId }
                    onFavoriteRemoved(toRemove.itemId)
                }
                removingId    = null
                pendingRemove = null
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Modern Gradient Header ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight))
                        )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        // Navigation Bar (Clean Centered Title)
                        // Navigation Bar (Left-aligned Title)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                            Text(
                                "My Favorites",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 19.sp,
                                modifier   = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                // ── Category Filter ───────────────────────────────────────────
                if (!isLoading && favoriteItems.isNotEmpty() && categories.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                label = { Text("All", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = {
                                    selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id
                                },
                                label = { Text(cat.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                // ── Main Content Area ───────────────────────────────────────────
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = DarkGreen, strokeWidth = 3.dp)
                                Spacer(Modifier.height(16.dp))
                                Text("Refreshing your items…", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp)
                            }
                        }
                    }

                    favoriteItems.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 40.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(CircleShape)
                                        .background(DarkGreen.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.FavoriteBorder, null,
                                        tint = DarkGreen.copy(alpha = 0.2f),
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                                Spacer(Modifier.height(24.dp))
                                Text("Nothing Saved Yet", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Your favorite listings will appear here so you can find them easily later.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                                Spacer(Modifier.height(32.dp))
                                Button(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Icon(Icons.Filled.Storefront, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Browse Marketplace", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Count label outside of header
                            Text(
                                "${favoriteItems.size} items saved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 4.dp)
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredItems, key = { it.itemId }) { item ->
                                    FavoriteItemCard(
                                        item = item,
                                        isRemoving = removingId == item.itemId || pendingRemove?.itemId == item.itemId,
                                        onClick = { onItemClick(item) },
                                        onRemove = { pendingRemove = item }
                                    )
                                }
                                item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(24.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItemCard(item: Item, isRemoving: Boolean, onClick: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = !isRemoving, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                // Photo Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    val photoUrl = item.photos.firstOrNull() ?: ""
                    if (photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Image, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Price overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Text(
                            "${item.markupPoints} pts",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Favorite toggle overlay
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            .size(32.dp)
                    ) {
                        if (isRemoving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFFF4444)
                            )
                        } else {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = "Remove",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Info Section
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Person, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            item.sellerEmail.substringBefore("@"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            if (isRemoving) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.7f))
                )
            }
        }
    }
}

// ── Add Item ───────────────────────────────────────────────────────────────────

private data class Category(val id: Int, val name: String)

private fun fetchCategories(token: String): List<Category> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/categories")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptyList()
        return try {
            // Resolve the array regardless of whether the response is wrapped in an
            // object or is a plain JSON array — preserves the exact database order.
            val arr = try {
                val obj = JSONObject(raw)
                when {
                    obj.has("data")       -> obj.getJSONArray("data")
                    obj.has("categories") -> obj.getJSONArray("categories")
                    else                  -> obj.getJSONArray("data")
                }
            } catch (_: Exception) {
                org.json.JSONArray(raw) // plain array response
            }
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                // API may use "category_id" or "id"
                val catId = if (obj.has("category_id")) obj.optInt("category_id") else obj.optInt("id")
                Category(id = catId, name = obj.optString("name"))
            }.sortedBy { it.id }
        } catch (_: Exception) { emptyList() }
    }
}

private fun fetchItems(token: String, status: String): List<Item> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items?status=$status")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptyList()
        return try {
            val json = JSONObject(raw)
            val arr  = json.getJSONArray("data")
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
        } catch (_: Exception) { emptyList() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentAddItemContent(
    onMenuClick: () -> Unit = {},
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token   = remember { prefs.getString("auth_token", "") ?: "" }
    val scope   = rememberCoroutineScope()

    var title              by remember { mutableStateOf("") }
    var description        by remember { mutableStateOf("") }
    var selectedCategory   by remember { mutableStateOf<Category?>(null) }
    var pricePoints        by remember { mutableStateOf("") }
    var selectedUris       by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var categoryExpanded   by remember { mutableStateOf(false) }
    var categories         by remember { mutableStateOf<List<Category>>(emptyList()) }
    var categoriesLoading  by remember { mutableStateOf(true) }
    var isLoading          by remember { mutableStateOf(false) }
    var errorMessage       by remember { mutableStateOf<String?>(null) }
    var showSuccess        by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        categories        = withContext(Dispatchers.IO) { fetchCategories(token) }
        categoriesLoading = false
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedUris = (selectedUris + uris)
            .distinctBy { it.toString() }
            .take(5)
    }

    fun validate(): String? {
        if (title.isBlank())                         return "Title is required"
        if (title.length > 255)                      return "Title must be under 255 characters"
        if (description.isBlank())                   return "Description is required"
        if (description.length > 1000)               return "Description must be under 1000 characters"
        if (selectedCategory == null)                return "Category is required"
        if (pricePoints.isBlank())                   return "Price points is required"
        if ((pricePoints.toIntOrNull() ?: -1) < 0)   return "Price points must be 0 or more"
        if (selectedUris.isEmpty())                  return "At least one photo is required"
        return null
    }

    fun resetForm() {
        title            = ""
        description      = ""
        selectedCategory = null
        pricePoints      = ""
        selectedUris     = emptyList()
        showSuccess      = false
    }

    // Success dialog
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { resetForm() },
            icon = {
                Icon(
                    Icons.Filled.CheckCircle, null,
                    tint = DarkGreen, modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Item Submitted!", fontWeight = FontWeight.Bold) },
            text  = { Text("Your item has been sent to the admin.") },
            confirmButton = {
                Button(
                    onClick = { resetForm() },
                    colors  = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) { Text("Done", color = Color.White, fontWeight = FontWeight.SemiBold) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "Add Item", onMenuClick = onMenuClick, favoritesCount = favoritesCount, onFavoritesClick = onFavoritesClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Title ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = title,
                onValueChange = { if (it.length <= 255) title = it },
                label         = { Text("Title") },
                leadingIcon   = {
                    Icon(Icons.Filled.Title, null, tint = DarkGreen)
                },
                singleLine      = true,
                supportingText  = { Text("${title.length}/255") },
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp)
            )

            // ── Description ────────────────────────────────────────────────────
            OutlinedTextField(
                value         = description,
                onValueChange = { if (it.length <= 1000) description = it },
                label         = { Text("Description") },
                leadingIcon   = {
                    Icon(Icons.Filled.Description, null, tint = DarkGreen)
                },
                minLines       = 4,
                maxLines       = 6,
                supportingText = { Text("${description.length}/1000") },
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(12.dp)
            )

            // ── Category dropdown ──────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded         = categoryExpanded,
                onExpandedChange = { if (!categoriesLoading) categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value         = selectedCategory?.let { "[${it.id}] ${it.name}" } ?: "",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Category") },
                    leadingIcon   = {
                        if (categoriesLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = DarkGreen
                            )
                        } else {
                            Icon(Icons.Filled.Category, null, tint = DarkGreen)
                        }
                    },
                    trailingIcon  = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    placeholder = { Text("Select a category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded         = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    if (categories.isEmpty()) {
                        DropdownMenuItem(
                            text    = { Text("No categories available", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            onClick = { categoryExpanded = false }
                        )
                    } else {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text("[${cat.id}] ${cat.name}") },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Price Points ───────────────────────────────────────────────────
            OutlinedTextField(
                value         = pricePoints,
                onValueChange = { v -> if (v.all { it.isDigit() } && v.length <= 8) pricePoints = v },
                label         = { Text("Price Points") },
                leadingIcon   = {
                    Icon(Icons.Filled.MonetizationOn, null, tint = DarkGreen)
                },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp)
            )

            // ── Photos ─────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    Text(
                        "Photos",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp
                    )
                    Text(
                        "${selectedUris.size} / 5",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Thumbnail row
                if (selectedUris.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selectedUris) { uri ->
                            Box {
                                AsyncImage(
                                    model              = uri,
                                    contentDescription = null,
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(10.dp)
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .clickable { selectedUris = selectedUris - uri },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Close, null,
                                        tint     = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Add photos button (hidden when 5 already picked)
                if (selectedUris.size < 5) {
                    OutlinedButton(
                        onClick  = { photoPicker.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape  = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, DarkGreen)
                    ) {
                        Icon(
                            Icons.Filled.AddPhotoAlternate, null,
                            tint     = DarkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (selectedUris.isEmpty()) "Add Photos" else "Add More",
                            color      = DarkGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    "JPG / PNG only  •  Max 5 MB each  •  Up to 5 photos",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Error ──────────────────────────────────────────────────────────
            errorMessage?.let { err ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier          = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.ErrorOutline, null,
                            tint     = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            err,
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ── Submit ─────────────────────────────────────────────────────────
            Button(
                onClick = {
                    val err = validate()
                    if (err != null) { errorMessage = err; return@Button }
                    errorMessage = null
                    scope.launch {
                        isLoading = true
                        try {
                            val photoFiles = withContext(Dispatchers.IO) {
                                selectedUris.mapIndexedNotNull { index, uri ->
                                    // Normalise to jpeg or png — avoids sending heic/webp
                                    val rawMime = context.contentResolver.getType(uri) ?: "image/jpeg"
                                    val mimeType = if (rawMime.contains("png")) "image/png" else "image/jpeg"
                                    val ext      = if (mimeType == "image/png") "png" else "jpg"
                                    val input = context.contentResolver.openInputStream(uri)
                                        ?: return@mapIndexedNotNull null
                                    val file = File(context.cacheDir, "photo_$index.$ext")
                                    input.use { src -> file.outputStream().use { dst -> src.copyTo(dst) } }
                                    if (file.length() > 5 * 1024 * 1024) {
                                        file.delete()
                                        return@mapIndexedNotNull null
                                    }
                                    Pair(file, mimeType)
                                }
                            }
                            if (photoFiles.size < selectedUris.size) {
                                errorMessage = "One or more photos exceed 5 MB. Please remove them and try again."
                                isLoading = false
                                return@launch
                            }
                            val (success, msg) = withContext(Dispatchers.IO) {
                                submitItem(
                                    token       = token,
                                    title       = title.trim(),
                                    description = description.trim(),
                                    categoryId  = selectedCategory!!.id,
                                    pricePoints = pricePoints.toInt(),
                                    photoFiles  = photoFiles
                                )
                            }
                            if (success) showSuccess = true else errorMessage = msg
                        } catch (e: Exception) {
                            errorMessage = "Unexpected error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.CloudUpload, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Post Item", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Network ────────────────────────────────────────────────────────────────────

private fun submitItem(
    token: String,
    title: String,
    description: String,
    categoryId: Int,
    pricePoints: Int,
    photoFiles: List<Pair<File, String>>
): Pair<Boolean, String> {
    val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
    builder.addFormDataPart("title", title)
    builder.addFormDataPart("description", description)
    builder.addFormDataPart("category_id", categoryId.toString())
    builder.addFormDataPart("price_points", pricePoints.toString())
    photoFiles.forEach { (file, mimeType) ->
        builder.addFormDataPart(
            "photos[]",
            file.name,
            file.asRequestBody(mimeType.toMediaType())
        )
    }
    val body = builder.build()

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body)
        .build()

    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: ""
        return if (response.isSuccessful) {
            Pair(true, "")
        } else {
            val msg = try {
                val json   = JSONObject(raw)
                val errors = json.optJSONObject("errors")
                if (errors != null) {
                    val msgs = mutableListOf<String>()
                    errors.keys().forEach { key ->
                        errors.optJSONArray(key)?.let { arr ->
                            (0 until arr.length()).forEach { i -> msgs.add(arr.getString(i)) }
                        }
                    }
                    if (msgs.isNotEmpty()) msgs.joinToString("\n")
                    else json.optString("message", "Failed to post item")
                } else {
                    json.optString("message", "Failed to post item")
                }
            } catch (_: Exception) {
                if (raw.isNotBlank()) "Server error: ${raw.take(200)}" else "Failed to post item"
            }
            Pair(false, msg)
        }
    }
}

private fun updateItem(
    token: String,
    itemId: Int,
    title: String,
    description: String,
    categoryId: Int,
    pricePoints: Int,
    photoFiles: List<Pair<File, String>>
): Pair<Boolean, String> {
    val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
    builder.addFormDataPart("_method", "PUT")
    builder.addFormDataPart("title", title)
    builder.addFormDataPart("description", description)
    builder.addFormDataPart("category_id", categoryId.toString())
    builder.addFormDataPart("price_points", pricePoints.toString())
    photoFiles.forEach { (file, mimeType) ->
        builder.addFormDataPart("photos[]", file.name, file.asRequestBody(mimeType.toMediaType()))
    }
    val body = builder.build()

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body)
        .build()

    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: ""
        return if (response.isSuccessful) {
            Pair(true, "")
        } else {
            val msg = try {
                val json   = JSONObject(raw)
                val errors = json.optJSONObject("errors")
                if (errors != null) {
                    val msgs = mutableListOf<String>()
                    errors.keys().forEach { key ->
                        errors.optJSONArray(key)?.let { arr ->
                            (0 until arr.length()).forEach { i -> msgs.add(arr.getString(i)) }
                        }
                    }
                    if (msgs.isNotEmpty()) msgs.joinToString("\n")
                    else json.optString("message", "Failed to update item")
                } else {
                    json.optString("message", "Failed to update item")
                }
            } catch (_: Exception) {
                if (raw.isNotBlank()) "Server error: ${raw.take(200)}" else "Failed to update item"
            }
            Pair(false, msg)
        }
    }
}

private fun deleteItem(token: String, itemId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .delete()
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

// ── Favorites network ──────────────────────────────────────────────────────────

private fun fetchFavoriteIds(token: String): Set<Int> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptySet()
        return try {
            val arr = try {
                JSONObject(raw).getJSONArray("data")
            } catch (_: Exception) {
                org.json.JSONArray(raw)
            }
            (0 until arr.length()).mapNotNull { i ->
                arr.optJSONObject(i)?.optInt("item_id")
            }.toSet()
        } catch (_: Exception) { emptySet() }
    }
}

private fun fetchFavoriteItems(token: String): List<Item> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptyList()
        return try {
            val arr = try {
                JSONObject(raw).getJSONArray("data")
            } catch (_: Exception) {
                org.json.JSONArray(raw)
            }
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                // favorites may be nested under "item" key
                val itemObj = obj.optJSONObject("item") ?: obj
                val photosArr = itemObj.optJSONArray("photos")
                val photos = if (photosArr != null) {
                    (0 until photosArr.length()).map { j -> photosArr.getString(j) }
                } else emptyList()
                Item(
                    itemId       = itemObj.optInt("item_id"),
                    sellerId     = itemObj.optInt("seller_id"),
                    sellerEmail  = itemObj.optString("seller_email"),
                    title        = itemObj.optString("title"),
                    description  = itemObj.optString("description"),
                    categoryId   = itemObj.optInt("category_id"),
                    pricePoints  = itemObj.optInt("price_points"),
                    markupPoints = itemObj.optInt("markup_points"),
                    status       = itemObj.optString("status"),
                    photos       = photos,
                    createdAt    = itemObj.optString("created_at")
                )
            }
        } catch (_: Exception) { emptyList() }
    }
}

private fun addFavorite(token: String, itemId: Int): Boolean {
    val body = "{\"item_id\":$itemId}".toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body)
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private fun removeFavorite(token: String, itemId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .delete()
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private fun checkFavorite(token: String, itemId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites/$itemId/check")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: return false
            try {
                val json = JSONObject(raw)
                json.optBoolean("is_favorited", false)
                    || json.optJSONObject("data")?.optBoolean("is_favorited", false) ?: false
            } catch (_: Exception) { false }
        }
    } catch (_: Exception) { false }
}

private fun fetchItemDetail(token: String, itemId: Int): Item? {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: return null
            val json = JSONObject(raw)
            val obj  = json.optJSONObject("data") ?: json
            val photosArr = obj.optJSONArray("photos")
            val photos = if (photosArr != null) {
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
    } catch (_: Exception) { null }
}

private fun sendMessageToAdmin(token: String, itemId: Int, message: String): Boolean {
    val payload = "{\"receiver_id\":1,\"message\":${JSONObject.quote(message)}}"
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/messages/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .post(payload.toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private fun fetchConversationCount(token: String): Int {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/conversations")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return 0
            val body = response.body?.string() ?: return 0
            val arr = try { org.json.JSONArray(body) }
                      catch (_: Exception) { JSONObject(body).optJSONArray("data") ?: return 0 }
            arr.length()
        }
    } catch (_: Exception) { 0 }
}

// ── Student Drawer ─────────────────────────────────────────────────────────────

@Composable
private fun StudentDrawerContent(
    showMyListings: Boolean,
    userFirstName: String,
    userLastName: String,
    userEmail: String,
    userRole: String,
    userProfilePic: String,
    onHome: () -> Unit,
    onMyListings: () -> Unit,
    onLogout: () -> Unit
) {
    val fullName = "$userFirstName $userLastName".trim().ifBlank { "Student" }
    val initial  = userFirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Filled.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text  = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Logout", color = Color.White, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
        // ── Header ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (userProfilePic.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model              = userProfilePic,
                            contentDescription = null,
                            modifier           = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale       = ContentScale.Crop,
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
                    Text(
                        userEmail.ifBlank { userRole.replaceFirstChar { it.uppercaseChar() } },
                        color    = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                    Text(
                        userRole.replaceFirstChar { it.uppercaseChar() },
                        color    = Color.White.copy(alpha = 0.55f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Navigation items ───────────────────────────────────────────────────
        StudentDrawerItem(
            icon     = Icons.Filled.Home,
            label    = "All Listings",
            selected = !showMyListings,
            onClick  = onHome
        )
        StudentDrawerItem(
            icon     = Icons.Filled.ListAlt,
            label    = "My Listings",
            selected = showMyListings,
            onClick  = onMyListings
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color    = MaterialTheme.colorScheme.outlineVariant
        )

        // ── Logout ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { showLogoutDialog = true }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text("Logout", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StudentDrawerItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) DarkGreen.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, null,
            tint     = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            label,
            fontSize   = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Bottom Nav ─────────────────────────────────────────────────────────────────

@Composable
private fun StudentBottomBar(
    selected: StudentTab,
    userProfilePic: String,
    userInitial: String,
    onSelect: (StudentTab) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier         = Modifier.fillMaxWidth(),
            tonalElevation   = 0.dp,
            shadowElevation  = 12.dp,
            color            = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier          = Modifier.fillMaxWidth().height(72.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentNavItem(
                        outlinedIcon = Icons.Outlined.Home,
                        filledIcon   = Icons.Filled.Home,
                        label        = "Home",
                        selected     = selected == StudentTab.HOME,
                        modifier     = Modifier.weight(1f)
                    ) { onSelect(StudentTab.HOME) }
                    StudentNavItem(
                        outlinedIcon = Icons.Outlined.Chat,
                        filledIcon   = Icons.Filled.Chat,
                        label        = "Chat",
                        selected     = selected == StudentTab.CHAT,
                        modifier     = Modifier.weight(1f)
                    ) { onSelect(StudentTab.CHAT) }
                    Spacer(modifier = Modifier.weight(1f))
                    StudentNavItem(
                        outlinedIcon = Icons.Outlined.Settings,
                        filledIcon   = Icons.Filled.Settings,
                        label        = "Settings",
                        selected     = selected == StudentTab.SETTINGS,
                        modifier     = Modifier.weight(1f)
                    ) { onSelect(StudentTab.SETTINGS) }
                    StudentProfileNavItem(
                        userProfilePic = userProfilePic,
                        userInitial    = userInitial,
                        selected       = selected == StudentTab.PROFILE,
                        modifier       = Modifier.weight(1f)
                    ) { onSelect(StudentTab.PROFILE) }
                }
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
        // Center FAB — Add Item
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick        = { onSelect(StudentTab.ADD_ITEM) },
                modifier       = Modifier
                    .size(54.dp)
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
                shape          = CircleShape,
                containerColor = if (selected == StudentTab.ADD_ITEM) DarkGreenLight else DarkGreen,
                contentColor   = Color.White,
                elevation      = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp, pressedElevation = 10.dp
                )
            ) {
                Icon(Icons.Filled.Add, "Add Item", modifier = Modifier.size(28.dp))
            }
            Text(
                text       = "Sell Item",
                fontSize   = 10.sp,
                fontWeight = if (selected == StudentTab.ADD_ITEM) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (selected == StudentTab.ADD_ITEM) DarkGreen
                             else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier   = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun StudentNavItem(
    outlinedIcon: ImageVector,
    filledIcon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tint = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Column(
        modifier              = modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Box {
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
                    imageVector        = if (selected) filledIcon else outlinedIcon,
                    contentDescription = label,
                    tint               = tint,
                    modifier           = Modifier.size(26.dp)
                )
            }
        }
        Text(
            text       = label,
            fontSize   = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = tint,
            modifier   = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun StudentProfileNavItem(
    userProfilePic: String,
    userInitial: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tint = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Column(
        modifier              = modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
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
                        model              = userProfilePic,
                        contentDescription = null,
                        modifier           = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale       = ContentScale.Crop,
                        error = {
                            Text(
                                userInitial,
                                fontSize   = 9.sp,
                                color      = if (selected) Color.White
                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                } else {
                    Text(
                        userInitial,
                        fontSize   = 9.sp,
                        color      = if (selected) Color.White
                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Text(
            text       = "Profile",
            fontSize   = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = tint,
            modifier   = Modifier.padding(top = 2.dp)
        )
    }
}