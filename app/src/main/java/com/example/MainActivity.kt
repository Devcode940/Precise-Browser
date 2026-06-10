package com.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.ChromiumTheme
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.TabState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BrowserViewModel = viewModel()
            val settingsMap by viewModel.settingsMap.collectAsState()
            val themeMode = settingsMap["theme"] ?: "dark"
            val isIncognito = settingsMap["incognito"] == "true"

            val activeTheme = if (isIncognito) "incognito" else themeMode

            ChromiumTheme(themeMode = activeTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrowserApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserApp(viewModel: BrowserViewModel) {
    val context = LocalContext.current
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val settingsMap by viewModel.settingsMap.collectAsState()

    val langCode = settingsMap["language"] ?: "en"
    val showBB = settingsMap["show_bookmarks_bar"] == "true"
    val autoHide = settingsMap["auto_hide_toolbar"] == "true"
    val isIncognito = settingsMap["incognito"] == "true"

    val activeTab = tabs.find { it.id == activeTabId }

    // Dropdown menu state
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Toast manager state
    var toastText by remember { mutableStateOf("") }
    LaunchedEffect(toastText) {
        if (toastText.isNotEmpty()) {
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
            delay(2000)
            toastText = ""
        }
    }

    // Keep cache map of WebViews in memory to preserve tab navigation states cleanly!
    val webViewCache = remember { mutableMapOf<String, WebView>() }

    // Clean up closed tab WebViews to prevent memory leaks
    DisposableEffect(tabs) {
        onDispose {
            val tabIds = tabs.map { it.id }.toSet()
            val keysToRemove = webViewCache.keys.filter { it !in tabIds }
            keysToRemove.forEach { key ->
                webViewCache[key]?.destroy()
                webViewCache.remove(key)
            }
        }
    }

    if (activeTab != null) {
        // Back press support inside WebView tabs!
        BackHandler {
            val activeWebView = webViewCache[activeTab.id]
            if (activeWebView != null && activeWebView.canGoBack()) {
                activeWebView.goBack()
            } else if (activeTab.navIndex > 0) {
                viewModel.navigateTabBack(activeTab.id)
            } else if (tabs.size > 1) {
                viewModel.closeTab(activeTab.id)
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column {
                    // 1. MAC OSX WINDOW TITLE BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mac window red, yellow, green control dots
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F57), CircleShape))
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFFEBC2E), CircleShape))
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF28C840), CircleShape))
                        }

                        // Centered browser title
                        Text(
                            text = "${activeTab.title} — Chromium Lite",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.width(36.dp)) // Equalizer space for control dots
                    }

                    // 2. HORIZONTAL TAB SWITCHER BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                            .padding(top = 4.dp, bottom = 0.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            tabs.forEach { tab ->
                                val isActive = tab.id == activeTabId
                                TabCard(
                                    tab = tab,
                                    isActive = isActive,
                                    onSelect = { viewModel.switchTab(tab.id) },
                                    onClose = { viewModel.closeTab(tab.id) }
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.createNewTab() },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("add_tab_button")
                                .padding(bottom = 2.dp, end = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Tab", modifier = Modifier.size(18.dp))
                        }
                    }

                    // 3. MAIN NAVIGATION TOOLBAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Navigation controls
                        IconButton(
                            onClick = {
                                val activeWebView = webViewCache[activeTab.id]
                                if (activeWebView != null && activeWebView.canGoBack()) {
                                    activeWebView.goBack()
                                } else {
                                    viewModel.navigateTabBack(activeTab.id)
                                }
                            },
                            enabled = activeTab.navIndex > 0 || (webViewCache[activeTab.id]?.canGoBack() ?: false),
                            modifier = Modifier.size(32.dp).testTag("nav_back")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = {
                                val activeWebView = webViewCache[activeTab.id]
                                if (activeWebView != null && activeWebView.canGoForward()) {
                                    activeWebView.goForward()
                                } else {
                                    viewModel.navigateTabForward(activeTab.id)
                                }
                            },
                            enabled = activeTab.navIndex < activeTab.navStack.size - 1 || (webViewCache[activeTab.id]?.canGoForward() ?: false),
                            modifier = Modifier.size(32.dp).testTag("nav_forward")
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Forward", modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = {
                                val web = webViewCache[activeTab.id]
                                if (web != null && !activeTab.url.startsWith("lite://")) {
                                    web.reload()
                                } else {
                                    viewModel.createNewTab(activeTab.url)
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("nav_reload")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = { viewModel.updateTabUrl(activeTab.id, "lite://newtab") },
                            modifier = Modifier.size(32.dp).testTag("nav_home")
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(16.dp))
                        }

                        // Address Bar input
                        var addressInput by remember(activeTab.url) {
                            mutableStateOf(if (activeTab.url.startsWith("lite://")) "" else activeTab.url)
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (activeTab.url.startsWith("https://")) Icons.Default.Lock else Icons.Default.Public,
                                contentDescription = "Security Indicator",
                                tint = if (activeTab.url.startsWith("https://")) Color(0xFF34D399) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = addressInput,
                                onValueChange = { addressInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("address_bar_text_field"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(onGo = {
                                    if (addressInput.trim().isNotEmpty()) {
                                        val destination = formatBrowserUrl(addressInput.trim(), settingsMap["search_engine"] ?: "duckduckgo")
                                        viewModel.updateTabUrl(activeTab.id, destination)
                                    }
                                }),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            )

                            // Bookmark trigger star
                            val isBookmarked = viewModel.bookmarks.collectAsState().value.any { it.url == activeTab.url }
                            IconButton(
                                onClick = { viewModel.toggleBookmark(activeTab.url, activeTab.title) },
                                modifier = Modifier.size(24.dp).testTag("bookmark_star")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Downloads Shortcut Badge
                        val activeDownloadsCount = viewModel.downloads.collectAsState().value.count { it.status == "downloading" }
                        IconButton(
                            onClick = { viewModel.updateTabUrl(activeTab.id, "lite://downloads") },
                            modifier = Modifier.size(32.dp).testTag("downloads_badge")
                        ) {
                            Box {
                                Icon(Icons.Default.FileDownload, contentDescription = "Downloads", modifier = Modifier.size(16.dp))
                                if (activeDownloadsCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            activeDownloadsCount.toString(),
                                            color = Color.Black,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Passwords key
                        IconButton(
                            onClick = { viewModel.updateTabUrl(activeTab.id, "lite://passwords") },
                            modifier = Modifier.size(32.dp).testTag("passwords_shortcut")
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = "Passwords Vault", modifier = Modifier.size(16.dp))
                        }

                        // Incognito mask icon
                        IconButton(
                            onClick = {
                                val currentIncognito = settingsMap["incognito"] == "true"
                                viewModel.updateSetting("incognito", if (currentIncognito) "false" else "true")
                                toastText = if (currentIncognito) "Incognito off" else "Incognito on — history won't be saved"
                            },
                            modifier = Modifier.size(32.dp).testTag("incognito_shortcut")
                        ) {
                            Icon(
                                imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Incognito Mode",
                                tint = if (isIncognito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Menu Expand
                        IconButton(
                            onClick = { isMenuExpanded = true },
                            modifier = Modifier.size(32.dp).testTag("menu_burger")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu Options", modifier = Modifier.size(18.dp))
                        }

                        // Hamburger dropdown options
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Tab", fontSize = 12.sp) },
                                onClick = {
                                    viewModel.createNewTab()
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bookmarks", fontSize = 12.sp) },
                                onClick = {
                                    viewModel.updateTabUrl(activeTab.id, "lite://bookmarks")
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("History", fontSize = 12.sp) },
                                onClick = {
                                    viewModel.updateTabUrl(activeTab.id, "lite://history")
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Vault Passwords", fontSize = 12.sp) },
                                onClick = {
                                    viewModel.updateTabUrl(activeTab.id, "lite://passwords")
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings", fontSize = 12.sp) },
                                onClick = {
                                    viewModel.updateTabUrl(activeTab.id, "lite://settings")
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }

                    // 4. PORTABLE BOOKMARKS BAR
                    if (showBB) {
                        val bookList by viewModel.bookmarks.collectAsState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bookList.take(6).forEach { b ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { viewModel.updateTabUrl(activeTab.id, b.url) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        b.title,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // 5. SECURE LOADING PROGRESS BAR
                    if (activeTab.loading) {
                        LinearProgressIndicator(
                            progress = { activeTab.progress / 100f },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.Transparent)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // CONTENT ROUTING
                when {
                    activeTab.url == "lite://newtab" -> {
                        NewTabScreen(viewModel, langCode) { targetUrl ->
                            viewModel.updateTabUrl(activeTab.id, targetUrl)
                        }
                    }
                    activeTab.url == "lite://bookmarks" -> {
                        BookmarksScreen(viewModel, langCode) { targetUrl ->
                            viewModel.updateTabUrl(activeTab.id, targetUrl)
                        }
                    }
                    activeTab.url == "lite://history" -> {
                        HistoryScreen(viewModel, langCode) { targetUrl ->
                            viewModel.updateTabUrl(activeTab.id, targetUrl)
                        }
                    }
                    activeTab.url == "lite://settings" -> {
                        SettingsScreen(viewModel, langCode) { text ->
                            toastText = text
                        }
                    }
                    activeTab.url == "lite://passwords" -> {
                        PasswordVaultScreen(viewModel, langCode) { text ->
                            toastText = text
                        }
                    }
                    activeTab.url == "lite://downloads" -> {
                        DownloadsScreen(viewModel, langCode) { text ->
                            toastText = text
                        }
                    }
                    activeTab.url.startsWith("lite://pdf") -> {
                        PDFViewerScreen(activeTab.url.substringAfter("url="), langCode) { text ->
                            toastText = text
                        }
                    }
                    else -> {
                        // NATIVE ANDROID WEBVIEW RENDERING FOR INTERNET ADDRESSES
                        AndroidView(
                            factory = { ctx ->
                                webViewCache.getOrPut(activeTab.id) {
                                    WebView(ctx).apply {
                                        layoutParams = android.view.ViewGroup.LayoutParams(
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                        )

                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            databaseEnabled = true
                                            cacheMode = WebSettings.LOAD_DEFAULT
                                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                                            // Apply selected custom user-agent simulation string!
                                            val selectedUA = settingsMap["user_agent"] ?: "chrome"
                                            userAgentString = when (selectedUA) {
                                                "firefox" -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0"
                                                "safari" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_2) Gecko/17.2 Safari/605.1.15"
                                                "edge" -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Edg/120.0.0.0"
                                                "mobile" -> "Mozilla/5.0 (Linux; Android 14; Pixel 8) Mobile Safari/537.36"
                                                else -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0"
                                            }
                                        }

                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                                url?.let {
                                                    viewModel.updateTabLoadingAndProgress(activeTab.id, true, 20)
                                                }
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                url?.let {
                                                    viewModel.updateTabLoadingAndProgress(activeTab.id, false, 100)
                                                    view?.title?.let { t ->
                                                        viewModel.updateTabTitleOnly(activeTab.id, t)
                                                    }
                                                }
                                            }
                                        }

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                viewModel.updateTabLoadingAndProgress(activeTab.id, newProgress < 100, newProgress)
                                            }

                                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                                title?.let { viewModel.updateTabTitleOnly(activeTab.id, it) }
                                            }
                                        }

                                        setDownloadListener { url, _, contentDisposition, mimeType, _ ->
                                            viewModel.startDownload(url)
                                            toastText = "Download started safely"
                                            viewModel.updateTabUrl(activeTab.id, "lite://downloads")
                                        }

                                        loadUrl(activeTab.url)
                                    }
                                }
                            },
                            update = { webView ->
                                if (webView.url != activeTab.url) {
                                    webView.loadUrl(activeTab.url)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabCard(
    tab: TabState,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier
            .width(140.dp)
            .height(34.dp)
            .testTag("tab_${tab.id}"),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        tab.url == "lite://newtab" -> Icons.Default.Add
                        tab.url == "lite://bookmarks" -> Icons.Default.Star
                        tab.url == "lite://history" -> Icons.Default.History
                        tab.url == "lite://passwords" -> Icons.Default.VpnKey
                        tab.url == "lite://downloads" -> Icons.Default.FileDownload
                        tab.url == "lite://settings" -> Icons.Default.Settings
                        tab.url.startsWith("lite://pdf") -> Icons.Default.PictureAsPdf
                        else -> Icons.Default.Public
                    },
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tab.title,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(10.dp))
            }
        }
    }
}

fun formatBrowserUrl(input: String, engine: String): String {
    val trimmed = input.trim()
    if (trimmed.startsWith("lite://")) return trimmed
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    if (trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length > 3) {
        return "https://$trimmed"
    }

    val searchBase = when (engine) {
        "google" -> "https://www.google.com/search?q="
        "bing" -> "https://www.bing.com/search?q="
        "yahoo" -> "https://search.yahoo.com/search?p="
        "brave" -> "https://search.brave.com/search?q="
        "ecosia" -> "https://www.ecosia.org/search?q="
        "yandex" -> "https://yandex.com/search/?text="
        "baidu" -> "https://www.baidu.com/s?wd="
        else -> "https://duckduckgo.com/?q="
    }
    return searchBase + java.net.URLEncoder.encode(trimmed, "UTF-8")
}

// Low-level helper block for older jetpack compose dependency mappings
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textStyle: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle.Default
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = textStyle,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxHeight()
            ) {
                innerTextField()
            }
        }
    )
}
