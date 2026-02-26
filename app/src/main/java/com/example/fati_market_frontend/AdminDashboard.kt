package com.example.fati_market_frontend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fati_market_frontend.ui.theme.DarkGreen
import com.example.fati_market_frontend.ui.theme.DarkGreenLight
import kotlinx.coroutines.launch

// ── Drawer Pages ───────────────────────────────────────────────────────────────

private sealed class DrawerPage(val label: String) {
    object Dashboard         : DrawerPage("Dashboard")
    // Inventory Management
    object PrivateOffers     : DrawerPage("Private Offers")
    object AcquiredItems     : DrawerPage("Acquired Items")
    object PublicListings    : DrawerPage("Public Listings")
    object ReservedItems     : DrawerPage("Reserved Items")
    object SoldItems         : DrawerPage("Sold Items")
    // Transactions
    object PointsGiven       : DrawerPage("Points Given")
    object PointsReceived    : DrawerPage("Points Received")
    object CashTransactions  : DrawerPage("Cash Transactions")
    object TradeTransactions : DrawerPage("Trade Transactions")
    object TransactionHistory: DrawerPage("Transaction History")
    object ProfitSummary     : DrawerPage("Profit Summary")
    // Reports / Analytics
    object TotalItemAcquired : DrawerPage("Total Item Acquired")
    object TotalItemSold     : DrawerPage("Total Item Sold")
    object TotalProfit       : DrawerPage("Total Profit (from markup)")
    object MostSoldCategory  : DrawerPage("Most Sold Category")
    object ActiveUsers       : DrawerPage("Active Users")
    // Other
    object Categories        : DrawerPage("Categories")
    object ActivityLogs      : DrawerPage("Activity Logs")
}

private enum class AdminTab { HOME, CHAT, USERS, SETTINGS, PROFILE }

// ── Admin Dashboard ────────────────────────────────────────────────────────────

@Composable
fun AdminDashboard(isDarkMode: Boolean, onThemeToggle: () -> Unit) {
    var selectedTab  by remember { mutableStateOf(AdminTab.HOME) }
    var drawerPage   by remember { mutableStateOf<DrawerPage?>(null) }
    val drawerState  = rememberDrawerState(DrawerValue.Closed)
    val scope        = rememberCoroutineScope()
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
                    currentPage = drawerPage,
                    onPageSelect = { page ->
                        if (page == DrawerPage.Dashboard) {
                            drawerPage = null
                            selectedTab = AdminTab.HOME
                        } else {
                            drawerPage = page
                        }
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                AdminBottomBar(
                    selected = selectedTab,
                    onSelect = { tab ->
                        selectedTab = tab
                        drawerPage = null
                    }
                )
            },
            contentWindowInsets = WindowInsets(0),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                if (drawerPage != null) {
                    DrawerPageContent(page = drawerPage!!, onMenuClick = openDrawer)
                } else {
                    when (selectedTab) {
                        AdminTab.HOME     -> AdminHomeContent(onMenuClick = openDrawer)
                        AdminTab.CHAT     -> AdminChatContent(onMenuClick = openDrawer)
                        AdminTab.USERS    -> AdminUsersContent(onMenuClick = openDrawer)
                        AdminTab.SETTINGS -> AdminSettingsContent(isDarkMode, onThemeToggle, onMenuClick = openDrawer)
                        AdminTab.PROFILE  -> AdminProfileContent(onMenuClick = openDrawer)
                    }
                }
            }
        }
    }
}

// ── Drawer Content ─────────────────────────────────────────────────────────────

@Composable
private fun AdminDrawerContent(
    currentPage: DrawerPage?,
    onPageSelect: (DrawerPage) -> Unit
) {
    var inventoryExpanded    by remember { mutableStateOf(false) }
    var transactionsExpanded by remember { mutableStateOf(false) }
    var reportsExpanded      by remember { mutableStateOf(false) }

    // Auto-expand section if a child is active
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

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Drawer header — green fills behind status bar, content below it ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Admin Panel", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("FatiMarket", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                }
            }
            } // end Box
        } // end drawer header Column

        Spacer(modifier = Modifier.height(8.dp))

        // ── Dashboard ──
        DrawerItem(
            icon = Icons.Filled.Dashboard,
            label = "Dashboard",
            selected = currentPage == null || currentPage == DrawerPage.Dashboard,
            onClick = { onPageSelect(DrawerPage.Dashboard) }
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // ── Inventory Management ──
        DrawerSectionHeader(
            icon = Icons.Filled.Inventory,
            label = "Inventory Management",
            expanded = inventoryExpanded,
            onClick = { inventoryExpanded = !inventoryExpanded }
        )
        AnimatedVisibility(visible = inventoryExpanded) {
            Column {
                DrawerSubItem("Private Offers",  currentPage == DrawerPage.PrivateOffers)  { onPageSelect(DrawerPage.PrivateOffers) }
                DrawerSubItem("Acquired Items",  currentPage == DrawerPage.AcquiredItems)  { onPageSelect(DrawerPage.AcquiredItems) }
                DrawerSubItem("Public Listings", currentPage == DrawerPage.PublicListings) { onPageSelect(DrawerPage.PublicListings) }
                DrawerSubItem("Reserved Items",  currentPage == DrawerPage.ReservedItems)  { onPageSelect(DrawerPage.ReservedItems) }
                DrawerSubItem("Sold Items",      currentPage == DrawerPage.SoldItems)      { onPageSelect(DrawerPage.SoldItems) }
            }
        }

        // ── Transactions ──
        DrawerSectionHeader(
            icon = Icons.Filled.Receipt,
            label = "Transactions",
            expanded = transactionsExpanded,
            onClick = { transactionsExpanded = !transactionsExpanded }
        )
        AnimatedVisibility(visible = transactionsExpanded) {
            Column {
                DrawerSubItem("Points Given",        currentPage == DrawerPage.PointsGiven)       { onPageSelect(DrawerPage.PointsGiven) }
                DrawerSubItem("Points Received",     currentPage == DrawerPage.PointsReceived)    { onPageSelect(DrawerPage.PointsReceived) }
                DrawerSubItem("Cash Transactions",   currentPage == DrawerPage.CashTransactions)  { onPageSelect(DrawerPage.CashTransactions) }
                DrawerSubItem("Trade Transactions",  currentPage == DrawerPage.TradeTransactions) { onPageSelect(DrawerPage.TradeTransactions) }
                DrawerSubItem("Transaction History", currentPage == DrawerPage.TransactionHistory){ onPageSelect(DrawerPage.TransactionHistory) }
                DrawerSubItem("Profit Summary",      currentPage == DrawerPage.ProfitSummary)     { onPageSelect(DrawerPage.ProfitSummary) }
            }
        }

        // ── Reports / Analytics ──
        DrawerSectionHeader(
            icon = Icons.Filled.BarChart,
            label = "Reports / Analytics",
            expanded = reportsExpanded,
            onClick = { reportsExpanded = !reportsExpanded }
        )
        AnimatedVisibility(visible = reportsExpanded) {
            Column {
                DrawerSubItem("Total Item Acquired",       currentPage == DrawerPage.TotalItemAcquired) { onPageSelect(DrawerPage.TotalItemAcquired) }
                DrawerSubItem("Total Item Sold",           currentPage == DrawerPage.TotalItemSold)     { onPageSelect(DrawerPage.TotalItemSold) }
                DrawerSubItem("Total Profit (from markup)",currentPage == DrawerPage.TotalProfit)       { onPageSelect(DrawerPage.TotalProfit) }
                DrawerSubItem("Most Sold Category",        currentPage == DrawerPage.MostSoldCategory)  { onPageSelect(DrawerPage.MostSoldCategory) }
                DrawerSubItem("Active Users",              currentPage == DrawerPage.ActiveUsers)       { onPageSelect(DrawerPage.ActiveUsers) }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // ── Categories ──
        DrawerItem(
            icon = Icons.Filled.Category,
            label = "Categories",
            selected = currentPage == DrawerPage.Categories,
            onClick = { onPageSelect(DrawerPage.Categories) }
        )

        // ── Activity Logs ──
        DrawerItem(
            icon = Icons.Filled.EventNote,
            label = "Activity Logs",
            selected = currentPage == DrawerPage.ActivityLogs,
            onClick = { onPageSelect(DrawerPage.ActivityLogs) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Drawer Item Helpers ────────────────────────────────────────────────────────

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
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
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DrawerSectionHeader(
    icon: ImageVector,
    label: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DrawerSubItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 44.dp, end = 12.dp, top = 1.dp, bottom = 1.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) DarkGreen.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                    if (selected) DarkGreen
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
    }
}

// ── Drawer Page Placeholder ────────────────────────────────────────────────────

@Composable
private fun DrawerPageContent(page: DrawerPage, onMenuClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = page.label, onMenuClick = onMenuClick)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Construction,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = page.label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Coming soon", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Modern Bottom Nav Bar ──────────────────────────────────────────────────────

@Composable
private fun AdminBottomBar(selected: AdminTab, onSelect: (AdminTab) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Nav surface — extends behind system nav bar so white fills all the way down
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavBarItem(
                        icon = Icons.Filled.Home,
                        label = "Home",
                        selected = selected == AdminTab.HOME,
                        modifier = Modifier.weight(1f)
                    ) { onSelect(AdminTab.HOME) }

                    NavBarItem(
                        icon = Icons.Filled.Chat,
                        label = "Chat",
                        selected = selected == AdminTab.CHAT,
                        modifier = Modifier.weight(1f)
                    ) { onSelect(AdminTab.CHAT) }

                    // Centre gap — Users label sits here below the FAB
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp)
                            .clickable { onSelect(AdminTab.USERS) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Users",
                            fontSize = 11.sp,
                            fontWeight = if (selected == AdminTab.USERS) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected == AdminTab.USERS) DarkGreen
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    NavBarItem(
                        icon = Icons.Filled.Settings,
                        label = "Settings",
                        selected = selected == AdminTab.SETTINGS,
                        modifier = Modifier.weight(1f)
                    ) { onSelect(AdminTab.SETTINGS) }

                    NavBarItem(
                        icon = Icons.Filled.Person,
                        label = "Profile",
                        selected = selected == AdminTab.PROFILE,
                        modifier = Modifier.weight(1f)
                    ) { onSelect(AdminTab.PROFILE) }
                }
                // Fills the system navigation bar area with the surface (white) color
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        // Centre FAB — DarkGreen with surface-color border ring for floating/cutout look
        FloatingActionButton(
            onClick = { onSelect(AdminTab.USERS) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
                .size(60.dp)
                .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
            shape = CircleShape,
            containerColor = DarkGreen,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Group,
                contentDescription = "Users",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// ── Nav Item — circle ripple on icon, DarkGreen tint when active ───────────────

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val iconTint = if (selected) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    // Box 44dp + Text ~14dp + vertical padding 8dp*2 = 74dp → fits within 76dp row
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
        // No spacer — text sits directly below the box so they read as a unit
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = iconTint
        )
    }
}

// ── Admin Home ─────────────────────────────────────────────────────────────────

@Composable
private fun AdminHomeContent(onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AdminPageHeader(title = "Dashboard", onMenuClick = onMenuClick)

        // Stat cards
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "OVERVIEW",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkGreen,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Total Students", "--", Icons.Filled.School, Modifier.weight(1f))
                StatCard("Pending", "--", Icons.Filled.HourglassEmpty, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Products", "--", Icons.Filled.Storefront, Modifier.weight(1f))
                StatCard("Active Chats", "--", Icons.Filled.Chat, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DarkGreen,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkGreen.copy(alpha = 0.1f))
                    .padding(6.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Admin Chat ─────────────────────────────────────────────────────────────────

@Composable
private fun AdminChatContent(onMenuClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "Messages", onMenuClick = onMenuClick)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No messages yet", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Admin Users ────────────────────────────────────────────────────────────────

@Composable
private fun AdminUsersContent(onMenuClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "User Management", onMenuClick = onMenuClick)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.PeopleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No users yet", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Admin Settings ─────────────────────────────────────────────────────────────

@Composable
private fun AdminSettingsContent(isDarkMode: Boolean, onThemeToggle: () -> Unit, onMenuClick: () -> Unit) {
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0" }
        catch (e: Exception) { "1.0" }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        AdminPageHeader(title = "Settings", onMenuClick = onMenuClick)

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            SettingsSectionLabel("PREFERENCES")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeToggle() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Dark Mode", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = if (isDarkMode) "Currently enabled" else "Currently disabled",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onThemeToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DarkGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionLabel("ABOUT")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    SettingsInfoRow(Icons.Filled.ShoppingBag, "App Name", "FatiMarket")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsInfoRow(Icons.Filled.Info, "Version", "v$versionName")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsInfoRow(Icons.Filled.AdminPanelSettings, "Role", "Administrator")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsInfoRow(Icons.Filled.School, "Institution", "Our Lady of Fatima University")
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = DarkGreen,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 10.dp)
    )
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

// ── Admin Profile ──────────────────────────────────────────────────────────────

@Composable
private fun AdminProfileContent(onMenuClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "Admin Profile", onMenuClick = onMenuClick)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(DarkGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.AdminPanelSettings, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Administrator", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = "FatiMarket Admin", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Shared Page Header — green fills behind status bar, content is vertically centered ──

@Composable
private fun AdminPageHeader(title: String, onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight)))
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
            }
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
