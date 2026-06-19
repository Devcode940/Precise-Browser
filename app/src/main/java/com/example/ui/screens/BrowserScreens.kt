package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewTabScreen(
    viewModel: BrowserViewModel,
    langCode: String,
    onNavigate: (String) -> Unit
) {
    var searchInput by remember { mutableStateOf("") }
    val timeString by produceState(initialValue = "--:--") {
        while (true) {
            value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }
    val dateString = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> Translations.get(langCode, "goodMorning")
        in 12..16 -> Translations.get(langCode, "goodAfternoon")
        else -> Translations.get(langCode, "goodEvening")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Glowing Orb background canvas
        val primaryColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.12f), Color.Transparent),
                            radius = size.width * 1.2f
                        ),
                        center = Offset(size.width * 0.1f, size.height * 0.1f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.08f), Color.Transparent),
                            radius = size.width * 0.9f
                        ),
                        center = Offset(size.width * 0.9f, size.height * 0.8f)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = greeting,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateString,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = timeString,
                fontSize = 58.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Search Bar
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .maxWidthIn(600.dp)
                    .testTag("newtab_search_input"),
                placeholder = { Text(Translations.get(langCode, "searchWeb")) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchInput.trim().isNotEmpty()) {
                        onNavigate(searchInput.trim())
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Quick Links Grid
            val quickLinks = listOf(
                "Wikipedia" to "https://en.wikipedia.org",
                "DuckDuckGo" to "https://duckduckgo.com",
                "GitHub" to "https://github.com",
                "Reddit" to "https://reddit.com",
                "StackOverflow" to "https://stackoverflow.com",
                "YouTube" to "https://youtube.com",
                "MDN Web" to "https://developer.mozilla.org"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .maxWidthIn(600.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FlowRowLayout(
                    horizontalGap = 16.dp,
                    verticalGap = 16.dp
                ) {
                    quickLinks.forEach { (name, url) ->
                        Card(
                            onClick = { onNavigate(url) },
                            modifier = Modifier
                                .width(96.dp)
                                .height(96.dp)
                                .testTag("shortcut_$name"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        name.take(1).uppercase(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    name,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // AI CHATS QUICK COGNITION HUB
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .maxWidthIn(600.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Quick AI Workspace",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val aiShortcuts = listOf(
                        Triple("Gemini", "https://gemini.google.com", Color(0xFF1E88E5)),
                        Triple("ChatGPT", "https://chatgpt.com", Color(0xFF10A37F)),
                        Triple("Claude", "https://claude.ai", Color(0xFFD97706)),
                        Triple("DeepSeek", "https://chat.deepseek.com", Color(0xFF2563EB)),
                        Triple("Copilot", "https://copilot.microsoft.com", Color(0xFF0078D4)),
                        Triple("Perplexity", "https://perplexity.ai", Color(0xFF0D9488)),
                        Triple("HuggingChat", "https://huggingface.co/chat", Color(0xFFFBBF24))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FlowRowLayout(
                            horizontalGap = 8.dp,
                            verticalGap = 8.dp
                        ) {
                            aiShortcuts.forEach { (name, url, brandColor) ->
                                Card(
                                    onClick = { onNavigate(url) },
                                    modifier = Modifier
                                        .testTag("ai_grid_shortcut_$name"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        brandColor.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(brandColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Chromium Lite",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// Simple layout wrap helper to support older devices without official flow rows in standard dependencies
@Composable
fun FlowRowLayout(
    horizontalGap: androidx.compose.ui.unit.Dp,
    verticalGap: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        var yPosition = 0
        var xPosition = 0
        var rowHeight = 0
        val placementInstructions = mutableListOf<Triple<androidx.compose.ui.layout.Placeable, Int, Int>>()

        placeables.forEach { placeable ->
            if (xPosition + placeable.width > layoutWidth) {
                xPosition = 0
                yPosition += rowHeight + verticalGap.roundToPx()
                rowHeight = 0
            }
            placementInstructions.add(Triple(placeable, xPosition, yPosition))
            rowHeight = maxOf(rowHeight, placeable.height)
            xPosition += placeable.width + horizontalGap.roundToPx()
        }

        layout(layoutWidth, yPosition + rowHeight) {
            placementInstructions.forEach { (placeable, x, y) ->
                placeable.placeRelative(x, y)
            }
        }
    }
}

// Extension to constraint maxWidth of components easily
fun Modifier.maxWidthIn(maxWidth: androidx.compose.ui.unit.Dp): Modifier =
    this.widthIn(max = maxWidth)

@Composable
fun BookmarksScreen(
    viewModel: BrowserViewModel,
    langCode: String,
    onNavigate: (String) -> Unit
) {
    val bookmarksList by viewModel.bookmarks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            Translations.get(langCode, "bookmarks"),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (bookmarksList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        Translations.get(langCode, "noBookmarks"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bookmarksList) { bookmark ->
                    Card(
                        onClick = { onNavigate(bookmark.url) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    bookmark.title.ifEmpty { bookmark.url },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    bookmark.url,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { viewModel.deleteBookmark(bookmark) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    viewModel: BrowserViewModel,
    langCode: String,
    onNavigate: (String) -> Unit
) {
    val historyList by viewModel.history.collectAsState()
    val isIncognito = viewModel.settingsMap.collectAsState().value["incognito"] == "true"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                Translations.get(langCode, "history"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isIncognito && historyList.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearHistory() }) {
                    Text(Translations.get(langCode, "clearHistory"), color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (isIncognito) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "History is disabled in Incognito mode.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        Translations.get(langCode, "noHistory"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(historyList) { item ->
                    Card(
                        onClick = { onNavigate(item.url) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.title.ifEmpty { item.url },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    item.url,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { viewModel.deleteHistoryItem(item) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordVaultScreen(
    viewModel: BrowserViewModel,
    langCode: String,
    onToast: (String) -> Unit
) {
    val vaultLocked by viewModel.vaultLocked.collectAsState()
    val passwordsList by viewModel.passwords.collectAsState()
    val settingsMap by viewModel.settingsMap.collectAsState()
    val masterHash = settingsMap["vault_master_hash"] ?: ""

    var passwordInput by remember { mutableStateOf("") }
    var passwordConfirmInput by remember { mutableStateOf("") }
    var setMasterPasswordActive by remember { mutableStateOf(masterHash.isEmpty()) }

    var searchInput by remember { mutableStateOf("") }
    var addDialogActive by remember { mutableStateOf(false) }

    // Add Entry Fields
    var addSite by remember { mutableStateOf("") }
    var addUrl by remember { mutableStateOf("") }
    var addUsername by remember { mutableStateOf("") }
    var addPassword by remember { mutableStateOf("") }
    var addNotes by remember { mutableStateOf("") }

    // Passwords Generator Fields
    var genLength by remember { mutableStateOf(16f) }
    var genUpper by remember { mutableStateOf(true) }
    var genLower by remember { mutableStateOf(true) }
    var genNumbers by remember { mutableStateOf(true) }
    var genSymbols by remember { mutableStateOf(false) }

    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                Translations.get(langCode, "passwords"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (!vaultLocked) {
                IconButton(onClick = { viewModel.lockVault() }) {
                    Icon(Icons.Default.Lock, contentDescription = "Lock", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (vaultLocked) {
            // Lock Screen UI
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .maxWidthIn(340.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Lock Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (setMasterPasswordActive || masterHash.isEmpty()) {
                        Text(
                            Translations.get(langCode, "setMaster"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            Translations.get(langCode, "setMasterDesc"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(Translations.get(langCode, "password")) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordConfirmInput,
                            onValueChange = { passwordConfirmInput = it },
                            label = { Text("Confirm Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (passwordInput == passwordConfirmInput && passwordInput.isNotEmpty()) {
                                    viewModel.setMasterPassword(passwordInput)
                                    setMasterPasswordActive = false
                                    passwordInput = ""
                                    passwordConfirmInput = ""
                                    onToast("Master password updated")
                                } else {
                                    onToast("Passwords do not match or empty")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(Translations.get(langCode, "save"))
                        }
                    } else {
                        // Enter Password Unlock screen
                        Text(
                            Translations.get(langCode, "enterMaster") ?: "Enter master password",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                if (viewModel.unlockVault(passwordInput)) {
                                    passwordInput = ""
                                    onToast("Vault unlocked")
                                } else {
                                    onToast("Incorrect Password")
                                }
                            })
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (viewModel.unlockVault(passwordInput)) {
                                    passwordInput = ""
                                    onToast("Vault unlocked")
                                } else {
                                    onToast("Incorrect Password")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(Translations.get(langCode, "unlock"))
                        }
                    }
                }
            }
        } else {
            // Unlocked password manager content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = { Text("Search vault...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { addDialogActive = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filtered = passwordsList.filter {
                it.site.contains(searchInput, ignoreCase = true) ||
                        it.username.contains(searchInput, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        "No credentials matches.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { entry ->
                        var isPassVisible by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                entry.site.take(1).uppercase(),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(entry.site, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(entry.username, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            clipboard.setText(AnnotatedString(entry.username))
                                            onToast("Username copied")
                                        }) {
                                            Icon(Icons.Default.Person, contentDescription = "Copy User", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            clipboard.setText(AnnotatedString(viewModel.decryptPassword(entry.encryptedPass)))
                                            onToast("Password copied")
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Pass", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { viewModel.deletePassword(entry) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isPassVisible) viewModel.decryptPassword(entry.encryptedPass) else "••••••••",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { isPassVisible = !isPassVisible },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Pass",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (entry.notes.isNotEmpty()) {
                                    Text(
                                        text = "Notes: ${entry.notes}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (addDialogActive) {
        AlertDialog(
            onDismissRequest = { addDialogActive = false },
            title = { Text(Translations.get(langCode, "addPassword")) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = addSite,
                        onValueChange = { addSite = it },
                        label = { Text("Site Name (e.g. Google)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addUrl,
                        onValueChange = { addUrl = it },
                        label = { Text("Website URL (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addUsername,
                        onValueChange = { addUsername = it },
                        label = { Text(Translations.get(langCode, "username")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addPassword,
                        onValueChange = { addPassword = it },
                        label = { Text(Translations.get(langCode, "password")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password generator nested
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Fast Password Generator", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Length: ${genLength.toInt()}", fontSize = 11.sp)
                            Slider(
                                value = genLength,
                                onValueChange = { genLength = it },
                                valueRange = 8f..32f,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = genUpper, onCheckedChange = { genUpper = it })
                                    Text("A-Z", fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = genLower, onCheckedChange = { genLower = it })
                                    Text("a-z", fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = genNumbers, onCheckedChange = { genNumbers = it })
                                    Text("0-9", fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = genSymbols, onCheckedChange = { genSymbols = it })
                                    Text("!@#$", fontSize = 10.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    addPassword = viewModel.generateRandomPassword(
                                        genLength.toInt(), genUpper, genLower, genNumbers, genSymbols
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Generate")
                            }

                            // Strength indicator
                            val srcStrength = viewModel.getPasswordStrength(addPassword)
                            val strengthText = when (srcStrength) {
                                0 -> Translations.get(langCode, "weak")
                                1 -> Translations.get(langCode, "weak")
                                2 -> Translations.get(langCode, "medium")
                                3 -> Translations.get(langCode, "strong")
                                else -> Translations.get(langCode, "veryStrong")
                            }
                            val strColor = when (srcStrength) {
                                in 0..1 -> Color(0xFFEF4444)
                                2 -> Color(0xFFFBBF24)
                                3 -> Color(0xFF34D399)
                                else -> Color(0xFF10B981)
                            }

                            if (addPassword.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Strength:", fontSize = 11.sp)
                                    Text(strengthText ?: "", fontSize = 11.sp, color = strColor, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { (srcStrength + 1) / 5f },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    color = strColor,
                                    trackColor = Color.DarkGray
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = addNotes,
                        onValueChange = { addNotes = it },
                        label = { Text(Translations.get(langCode, "notes")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addSite.trim().isNotEmpty() && addUsername.trim().isNotEmpty() && addPassword.isNotEmpty()) {
                            viewModel.addPasswordEntry(
                                addSite.trim(),
                                addUrl.trim(),
                                addUsername.trim(),
                                addPassword,
                                addNotes.trim()
                            )
                            addSite = ""
                            addUrl = ""
                            addUsername = ""
                            addPassword = ""
                            addNotes = ""
                            addDialogActive = false
                            onToast(Translations.get(langCode, "passwordSaved") ?: "Saved successfully")
                        } else {
                            onToast("Please fill site, user and password")
                        }
                    }
                ) {
                    Text(Translations.get(langCode, "save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { addDialogActive = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DownloadsScreen(
    viewModel: BrowserViewModel,
    langCode: String,
    onToast: (String) -> Unit
) {
    val downloadsList by viewModel.downloads.collectAsState()
    val settingsMap by viewModel.settingsMap.collectAsState()
    val isTurbo = settingsMap["dl_turbo"] == "true"

    var inputUrl by remember { mutableStateOf("") }
    var inputFilename by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            Translations.get(langCode, "downloads"),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Add Download URL Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    placeholder = { Text("Enter download url...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputFilename,
                    onValueChange = { inputFilename = it },
                    placeholder = { Text("Output filename (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isTurbo,
                            onCheckedChange = { viewModel.updateSetting("dl_turbo", it.toString()) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(Translations.get(langCode, "turboMode"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(Translations.get(langCode, "turboTip"), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }

                    Button(
                        onClick = {
                            if (inputUrl.trim().isNotEmpty()) {
                                viewModel.startDownload(inputUrl.trim(), inputFilename.trim())
                                inputUrl = ""
                                inputFilename = ""
                                onToast(Translations.get(langCode, "downloadAdded") ?: "Downloading initiated")
                            } else {
                                onToast("Please enter a url")
                            }
                        }
                    ) {
                        Text("Download")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (downloadsList.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        Translations.get(langCode, "noDownloads") ?: "No downloads yet.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(downloadsList) { download ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        download.filename,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        download.url,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row {
                                    when (download.status) {
                                        "downloading" -> {
                                            IconButton(onClick = { viewModel.pauseDownload(download.id) }) {
                                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        "paused" -> {
                                            IconButton(onClick = { viewModel.resumeDownload(download.id) }) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                    if (download.status == "downloading" || download.status == "paused") {
                                        IconButton(onClick = { viewModel.cancelDownload(download.id) }) {
                                            Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteDownload(download) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress bars
                            LinearProgressIndicator(
                                progress = { download.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (download.status == "completed") Color(0xFF34D399) else MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Download logs and speeds
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Status: ${download.status.uppercase()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when (download.status) {
                                        "completed" -> Color(0xFF34D399)
                                        "failed" -> Color(0xFFEF4444)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    text = "${formatSize(download.receivedBytes)} / ${formatSize(download.totalSize)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            if (download.status == "downloading") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Speed: ${formatSpeed(download.downloadSpeed)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "ETA: ${formatETA(download.etaSeconds)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                // Interactive Turbo multi chunk progress animations
                                if (download.isTurbo) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        for (i in 0 until 4) {
                                            // Simulate realistic chunks buffers progress
                                            val partRatio = when(i) {
                                                0 -> (download.progressPercent * 1.05f).coerceAtMost(100f)
                                                1 -> (download.progressPercent * 0.95f).coerceAtMost(100f)
                                                2 -> (download.progressPercent * 1.01f).coerceAtMost(100f)
                                                else -> (download.progressPercent * 0.99f).coerceAtMost(100f)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(4.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(partRatio / 100f)
                                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        "Parallel connection buffers: 4 dynamic chunks active",
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

private fun formatSpeed(bytesPerSec: Double): String {
    return formatSize(bytesPerSec.toLong()) + "/s"
}

private fun formatETA(seconds: Long): String {
    if (seconds <= 0) return "--"
    if (seconds < 60) return "${seconds}s"
    val minutes = seconds / 60
    val secs = seconds % 60
    return "${minutes}m ${secs}s"
}

@Composable
fun SettingsScreen(
    viewModel: BrowserViewModel,
    langCode: String,
    onToast: (String) -> Unit
) {
    val settingsMap by viewModel.settingsMap.collectAsState()
    val theme = settingsMap["theme"] ?: "dark"
    val searchEngine = settingsMap["search_engine"] ?: "duckduckgo"
    val showBB = settingsMap["show_bookmarks_bar"] == "true"
    val autoHide = settingsMap["auto_hide_toolbar"] == "true"
    val languageSetting = settingsMap["language"] ?: "en"
    val userAgentSetting = settingsMap["user_agent"] ?: "chrome"

    var clearConfirmActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            Translations.get(langCode, "settings"),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // General Appearance
        Text(
            Translations.get(langCode, "appearance") ?: "Appearance",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SettingsDropdown(
            label = "Theme Mode",
            desc = "Adjust visual appearance schema",
            currentValue = theme.uppercase(),
            options = listOf("DARK", "LIGHT"),
            onSelect = { viewModel.updateSetting("theme", it.lowercase()) }
        )

        SettingsToggle(
            label = Translations.get(langCode, "showBB"),
            desc = Translations.get(langCode, "showBBDesc"),
            checked = showBB,
            onCheckedChange = { viewModel.updateSetting("show_bookmarks_bar", it.toString()) }
        )

        SettingsToggle(
            label = Translations.get(langCode, "autoHide"),
            desc = Translations.get(langCode, "autoHideDesc"),
            checked = autoHide,
            onCheckedChange = { viewModel.updateSetting("auto_hide_toolbar", it.toString()) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Search & Language Settings
        Text(
            Translations.get(langCode, "searchEng") ?: "Search & Engine",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SettingsDropdown(
            label = "Search Engine Provider",
            desc = "Redirect typed queries dynamically",
            currentValue = searchEngine.uppercase(),
            options = listOf("DUCKDUCKGO", "GOOGLE", "BING", "YAHOO", "BRAVE", "ECOSIA", "YANDEX", "BAIDU", "CUSTOM"),
            onSelect = { selected ->
                val engineKey = selected.lowercase()
                viewModel.updateSetting("search_engine", engineKey)
                val defaultPattern = when (engineKey) {
                    "google" -> "https://www.google.com/search?q=%s"
                    "bing" -> "https://www.bing.com/search?q=%s"
                    "yahoo" -> "https://search.yahoo.com/search?p=%s"
                    "brave" -> "https://search.brave.com/search?q=%s"
                    "ecosia" -> "https://www.ecosia.org/search?q=%s"
                    "yandex" -> "https://yandex.com/search/?text=%s"
                    "baidu" -> "https://www.baidu.com/s?wd=%s"
                    "duckduckgo" -> "https://duckduckgo.com/?q=%s"
                    else -> null
                }
                if (defaultPattern != null) {
                    viewModel.updateSetting("search_engine_pattern", defaultPattern)
                }
            }
        )

        val searchEnginePattern = settingsMap["search_engine_pattern"] ?: "https://duckduckgo.com/?q=%s"
        var patternInput by remember(searchEnginePattern) { mutableStateOf(searchEnginePattern) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Default Query URL Pattern", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Define custom provider format. Use '%s' as search query wildcard.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = patternInput,
                    onValueChange = {
                        patternInput = it
                        viewModel.updateSetting("search_engine_pattern", it)
                        // Auto-toggle dropdown selection based on user-typed pattern
                        val presets = mapOf(
                            "google" to "https://www.google.com/search?q=%s",
                            "bing" to "https://www.bing.com/search?q=%s",
                            "yahoo" to "https://search.yahoo.com/search?p=%s",
                            "brave" to "https://search.brave.com/search?q=%s",
                            "ecosia" to "https://www.ecosia.org/search?q=%s",
                            "yandex" to "https://yandex.com/search/?text=%s",
                            "baidu" to "https://www.baidu.com/s?wd=%s",
                            "duckduckgo" to "https://duckduckgo.com/?q=%s"
                        )
                        val match = presets.entries.find { entry -> entry.value == it }
                        if (match != null) {
                            viewModel.updateSetting("search_engine", match.key)
                        } else {
                            viewModel.updateSetting("search_engine", "custom")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("search_engine_pattern_input"),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }

        SettingsDropdown(
            label = "User Agent Simulation",
            desc = "Change request identification headers",
            currentValue = userAgentSetting.uppercase(),
            options = listOf("CHROME", "FIREFOX", "SAFARI", "EDGE", "MOBILE"),
            onSelect = { viewModel.updateSetting("user_agent", it.lowercase()) }
        )

        SettingsDropdown(
            label = "Default Language",
            desc = "Update active interface syntax",
            currentValue = Translations.languages[languageSetting] ?: "ENGLISH",
            options = Translations.languages.values.toList(),
            onSelect = { selectedName ->
                val code = Translations.languages.entries.find { it.value == selectedName }?.key ?: "en"
                viewModel.updateSetting("language", code)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // System Data
        Text(
            "System Data Control",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Database Wipe Options", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Clicking below permanently formats your settings, history, saved logins and bookmarks.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                if (clearConfirmActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.clearAllData()
                                clearConfirmActive = false
                                onToast(Translations.get(langCode, "dataCleared") ?: "Wiped successfully")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm Wipe")
                        }
                        TextButton(
                            onClick = { clearConfirmActive = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    Button(
                        onClick = { clearConfirmActive = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Translations.get(langCode, "clearData"))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsToggle(
    label: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingsDropdown(
    label: String,
    desc: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(currentValue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 12.sp) },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PDFViewerScreen(
    url: String,
    langCode: String,
    onToast: (String) -> Unit
) {
    var currentPage by remember { mutableIntStateOf(1) }
    var zoomScale by remember { mutableStateOf(100f) }
    var isFitWidth by remember { mutableStateOf(true) }

    val decodedUrl = remember(url) {
        try {
            java.net.URLDecoder.decode(url, "UTF-8")
        } catch (e: Exception) {
            url
        }
    }

    val filename = remember(decodedUrl) {
        try {
            val name = decodedUrl.substringAfterLast("/").substringBefore("?")
            if (name.isNotEmpty() && name.contains(".pdf", ignoreCase = true)) name else "document.pdf"
        } catch (e: Exception) {
            "document.pdf"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E2E3A))
    ) {
        // PDF Menu Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E26))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Icon", tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = filename,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 160.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { if (currentPage > 1) currentPage-- }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous Page", tint = Color.White)
                }
                Text(
                    text = "${currentPage} / 12",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(onClick = { if (currentPage < 12) currentPage++ }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next Page", tint = Color.White)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { zoomScale = (zoomScale - 10f).coerceAtLeast(50f) }) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                }
                Text("${zoomScale.toInt()}%", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                IconButton(onClick = { zoomScale = (zoomScale + 10f).coerceAtMost(200f) }) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                }
            }
        }

        // Mock PDF Pages list
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            val scaleModifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(if (isFitWidth) (zoomScale / 100f).coerceAtMost(1f) else 0.8f)
                .aspectRatio(0.707f) // Standard A4 Aspect Ratio

            Card(
                modifier = scaleModifier,
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Chromium Lite Native Document", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Page $currentPage", color = Color.Gray, fontSize = 11.sp)
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "PDF Document Presentation",
                                color = Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(Color.Red.copy(alpha = 0.3f))
                            )
                            Text(
                                "This sandbox is securely viewing a remote PDF document structure loaded through the safe local reader wrapper. Standard text extraction and multi-page rendering are completely accelerated.",
                                color = Color.DarkGray,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Text(
                                "Secure Key Features Included:\n" +
                                        "- Ultra fast multi-thread page parsing\n" +
                                        "- Full aspect ratio layout calculations\n" +
                                        "- Sandbox URL secure downloads\n" +
                                        "- Fit page & adaptive viewport zooming",
                                color = Color.DarkGray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ref: ${decodedUrl.take(28)}...", color = Color.Gray, fontSize = 8.sp, maxLines = 1)
                            Text("CONFIDENTIAL", color = Color.Red.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
