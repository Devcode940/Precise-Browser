package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class TabState(
    val id: String,
    val title: String,
    val url: String,
    val loading: Boolean = false,
    val navStack: List<String> = listOf("lite://newtab"),
    val navIndex: Int = 0,
    val progress: Int = 0
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BrowserRepository

    private val _settingsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val settingsMap: StateFlow<Map<String, String>> = _settingsMap.asStateFlow()

    private val _tabs = MutableStateFlow<List<TabState>>(emptyList())
    val tabs: StateFlow<List<TabState>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>("")
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _vaultLocked = MutableStateFlow(true)
    val vaultLocked: StateFlow<Boolean> = _vaultLocked.asStateFlow()

    private val downloadJobs = mutableMapOf<String, Job>()

    val bookmarks: StateFlow<List<BookmarkEntity>>
    val history: StateFlow<List<HistoryEntity>>
    val passwords: StateFlow<List<PasswordEntity>>
    val downloads: StateFlow<List<DownloadEntity>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = BrowserRepository(db)

        bookmarks = repository.allBookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        history = repository.allHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        passwords = repository.allPasswords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        downloads = repository.allDownloads.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Load settings from DB and initialize defaults
        viewModelScope.launch {
            repository.allSettings.collect { settingsList ->
                val map = settingsList.associate { it.key to it.value }.toMutableMap()
                var updated = false

                val defaults = mapOf(
                    "theme" to "dark",
                    "search_engine" to "duckduckgo",
                    "show_bookmarks_bar" to "true",
                    "auto_hide_toolbar" to "false",
                    "language" to "en",
                    "user_agent" to "chrome",
                    "vault_master_hash" to "",
                    "dl_turbo" to "false"
                )

                for ((key, default) in defaults) {
                    if (!map.containsKey(key)) {
                        map[key] = default
                        repository.saveSetting(key, default)
                        updated = true
                    }
                }

                _settingsMap.value = map

                // Create initial tab if empty
                if (_tabs.value.isEmpty()) {
                    createNewTab()
                }
            }
        }
    }

    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
            val updated = _settingsMap.value.toMutableMap()
            updated[key] = value
            _settingsMap.value = updated
        }
    }

    fun createNewTab(url: String = "lite://newtab") {
        val newId = UUID.randomUUID().toString()
        val newTab = TabState(
            id = newId,
            title = if (url == "lite://newtab") "New Tab" else url,
            url = url,
            navStack = listOf(url),
            navIndex = 0
        )
        val currentTabs = _tabs.value.toMutableList()
        currentTabs.add(newTab)
        _tabs.value = currentTabs
        _activeTabId.value = newId
    }

    fun closeTab(id: String) {
        val currentTabs = _tabs.value.toMutableList()
        val idx = currentTabs.indexOfFirst { it.id == id }
        if (idx == -1) return

        currentTabs.removeAt(idx)
        _tabs.value = currentTabs

        if (currentTabs.isEmpty()) {
            createNewTab()
        } else if (_activeTabId.value == id) {
            val nextActiveIndex = if (idx >= currentTabs.size) currentTabs.size - 1 else idx
            _activeTabId.value = currentTabs[nextActiveIndex].id
        }
    }

    fun switchTab(id: String) {
        _activeTabId.value = id
    }

    fun updateTabUrl(id: String, url: String, title: String? = null) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == id) {
                val updatedStack = tab.navStack.subList(0, tab.navIndex + 1).toMutableList()
                if (updatedStack.lastOrNull() != url) {
                    updatedStack.add(url)
                }
                tab.copy(
                    url = url,
                    title = title ?: getTitleFromUrl(url),
                    navStack = updatedStack,
                    navIndex = updatedStack.size - 1
                )
            } else tab
        }

        // Only save to history if not internal, not incognito
        if (!url.startsWith("lite://") && _settingsMap.value["incognito"] != "true") {
            viewModelScope.launch {
                repository.insertHistoryItem(
                    HistoryEntity(
                        title = title ?: getTitleFromUrl(url),
                        url = url
                    )
                )
            }
        }
    }

    fun updateTabLoadingAndProgress(id: String, loading: Boolean, progress: Int) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == id) {
                tab.copy(loading = loading, progress = progress)
            } else tab
        }
    }

    fun updateTabTitleOnly(id: String, title: String) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == id) {
                tab.copy(title = title)
            } else tab
        }
    }

    fun navigateTabBack(id: String) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == id && tab.navIndex > 0) {
                val nextIdx = tab.navIndex - 1
                tab.copy(url = tab.navStack[nextIdx], navIndex = nextIdx)
            } else tab
        }
    }

    fun navigateTabForward(id: String) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == id && tab.navIndex < tab.navStack.size - 1) {
                val nextIdx = tab.navIndex + 1
                tab.copy(url = tab.navStack[nextIdx], navIndex = nextIdx)
            } else tab
        }
    }

    private fun getTitleFromUrl(url: String): String {
        return try {
            val domain = url.substringAfter("://").substringBefore("/")
            domain.replace("www.", "")
        } catch (e: Exception) {
            url
        }
    }

    // Bookmarks & History
    fun toggleBookmark(url: String, title: String) {
        viewModelScope.launch {
            val list = bookmarks.value
            val existing = list.find { it.url == url }
            if (existing != null) {
                repository.deleteBookmark(existing)
            } else {
                repository.insertBookmark(BookmarkEntity(title = title, url = url))
            }
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) = viewModelScope.launch {
        repository.deleteBookmark(bookmark)
    }

    fun deleteHistoryItem(item: HistoryEntity) = viewModelScope.launch {
        repository.deleteHistoryItem(item)
    }

    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllBookmarks()
            repository.clearHistory()
            repository.clearPasswords()
            repository.clearDownloads()
            repository.clearAllSettings()
            _vaultLocked.value = true
            _tabs.value = emptyList()
            createNewTab()
        }
    }

    // Vault Passwords
    fun lockVault() {
        _vaultLocked.value = true
    }

    fun unlockVault(pwd: String): Boolean {
        val currentHash = _settingsMap.value["vault_master_hash"] ?: ""
        if (currentHash.isEmpty()) {
            _vaultLocked.value = false
            return true
        }
        val hashed = hashPassword(pwd)
        return if (hashed == currentHash) {
            _vaultLocked.value = false
            true
        } else {
            false
        }
    }

    fun setMasterPassword(pwd: String) {
        val hashed = hashPassword(pwd)
        updateSetting("vault_master_hash", hashed)
        _vaultLocked.value = false
    }

    fun addPasswordEntry(site: String, url: String, user: String, pass: String, notes: String) {
        viewModelScope.launch {
            repository.insertPassword(
                PasswordEntity(
                    site = site,
                    url = url,
                    username = user,
                    encryptedPass = encryptPassword(pass),
                    notes = notes
                )
            )
        }
    }

    fun deletePassword(pwdEntry: PasswordEntity) = viewModelScope.launch {
        repository.deletePassword(pwdEntry)
    }

    private fun hashPassword(pwd: String): String {
        return "hash_" + pwd.hashCode()
    }

    fun encryptPassword(plain: String): String {
        val key = _settingsMap.value["vault_master_hash"]?.ifEmpty { null } ?: "default_crypt_key"
        val out = StringBuilder()
        for (i in plain.indices) {
            out.append((plain[i].code xor key[i % key.length].code).toChar())
        }
        return android.util.Base64.encodeToString(out.toString().toByteArray(), android.util.Base64.NO_WRAP)
    }

    fun decryptPassword(encrypted: String): String {
        return try {
            val decodedBytes = android.util.Base64.decode(encrypted, android.util.Base64.NO_WRAP)
            val decodedString = String(decodedBytes)
            val key = _settingsMap.value["vault_master_hash"]?.ifEmpty { null } ?: "default_crypt_key"
            val out = StringBuilder()
            for (i in decodedString.indices) {
                out.append((decodedString[i].code xor key[i % key.length].code).toChar())
            }
            out.toString()
        } catch (e: Exception) {
            "***"
        }
    }

    fun generateRandomPassword(length: Int, upper: Boolean, lower: Boolean, numbers: Boolean, symbols: Boolean): String {
        val uChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lChars = "abcdefghijklmnopqrstuvwxyz"
        val numChars = "0123456789"
        val symChars = "!@#$%^&*()_+-=[]{}|;:<>?"

        var pool = ""
        if (upper) pool += uChars
        if (lower) pool += lChars
        if (numbers) pool += numChars
        if (symbols) pool += symChars

        if (pool.isEmpty()) pool = lChars + numChars

        val sb = java.lang.StringBuilder()
        val rand = java.security.SecureRandom()
        for (i in 0 until length) {
            val idx = rand.nextInt(pool.length)
            sb.append(pool[idx])
        }
        return sb.toString()
    }

    fun getPasswordStrength(pwd: String): Int {
        if (pwd.isEmpty()) return 0
        var score = 0
        if (pwd.length >= 8) score++
        if (pwd.length >= 12) score++
        if (pwd.any { it.isUpperCase() }) score++
        if (pwd.any { it.isDigit() }) score++
        if (pwd.any { !it.isLetterOrDigit() }) score++
        return score.coerceAtMost(4)
    }

    // Downloader
    fun startDownload(url: String, filename: String? = null) {
        val dlId = "dl_" + System.currentTimeMillis()
        val finalName = filename?.ifEmpty { null } ?: try {
            val rawName = url.substringAfterLast("/").substringBefore("?")
            if (rawName.isNotEmpty()) rawName else "download"
        } catch (e: Exception) {
            "download"
        }

        val isTurbo = _settingsMap.value["dl_turbo"] == "true"
        val totalSize = (5 * 1024 * 1024..80 * 1024 * 1024).random().toLong()

        val initialEntity = DownloadEntity(
            id = dlId,
            url = url,
            filename = finalName,
            status = "downloading",
            receivedBytes = 0L,
            totalSize = totalSize,
            downloadSpeed = 0.0,
            etaSeconds = 0L,
            progressPercent = 0.0f,
            isTurbo = isTurbo
        )

        viewModelScope.launch {
            repository.insertDownload(initialEntity)
        }

        val job = viewModelScope.launch {
            var received = 0L
            val startTime = System.currentTimeMillis()
            val chunkCount = if (isTurbo) 4 else 1
            val chunkSizes = List(chunkCount) { totalSize / chunkCount }
            val chunkProgress = MutableList(chunkCount) { 0L }

            while (received < totalSize) {
                delay(300)
                val step = (400 * 1024..2000 * 1024).random().toLong()
                for (i in 0 until chunkCount) {
                    val remainingInChunk = chunkSizes[i] - chunkProgress[i]
                    if (remainingInChunk > 0) {
                        val inc = (step / chunkCount).coerceAtMost(remainingInChunk)
                        chunkProgress[i] += inc
                    }
                }
                received = chunkProgress.sum()
                if (received > totalSize) received = totalSize

                val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                val speed = received / if (elapsedSeconds <= 0.0) 0.1 else elapsedSeconds
                val eta = if (speed > 0) ((totalSize - received) / speed).toLong() else 0L
                val percent = (received.toFloat() / totalSize) * 100

                val updated = DownloadEntity(
                    id = dlId,
                    url = url,
                    filename = finalName,
                    status = if (received >= totalSize) "completed" else "downloading",
                    receivedBytes = received,
                    totalSize = totalSize,
                    downloadSpeed = speed,
                    etaSeconds = eta,
                    progressPercent = percent,
                    isTurbo = isTurbo
                )
                repository.insertDownload(updated)
            }
        }
        downloadJobs[dlId] = job
    }

    fun pauseDownload(id: String) {
        downloadJobs[id]?.cancel()
        viewModelScope.launch {
            val entity = repository.getDownloadById(id)
            if (entity != null && entity.status == "downloading") {
                repository.insertDownload(entity.copy(status = "paused", downloadSpeed = 0.0, etaSeconds = 0))
            }
        }
    }

    fun resumeDownload(id: String) {
        viewModelScope.launch {
            val entity = repository.getDownloadById(id) ?: return@launch
            if (entity.status == "paused") {
                downloadJobs[entity.id]?.cancel()
                val job = viewModelScope.launch {
                    var received = entity.receivedBytes
                    val totalSize = entity.totalSize
                    val startTime = System.currentTimeMillis() - (received / if (entity.downloadSpeed > 0) entity.downloadSpeed else 1.0).toLong() * 1000
                    val isTurbo = entity.isTurbo
                    
                    repository.insertDownload(entity.copy(status = "downloading"))

                    while (received < totalSize) {
                        delay(300)
                        val inc = (400 * 1024..2000 * 1024).random().toLong()
                        received = (received + inc).coerceAtMost(totalSize)

                        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                        val speed = received / if (elapsedSeconds <= 0.0) 0.1 else elapsedSeconds
                        val eta = if (speed > 0) ((totalSize - received) / speed).toLong() else 0L
                        val percent = (received.toFloat() / totalSize) * 100

                        val updated = entity.copy(
                            status = if (received >= totalSize) "completed" else "downloading",
                            receivedBytes = received,
                            downloadSpeed = speed,
                            etaSeconds = eta,
                            progressPercent = percent
                        )
                        repository.insertDownload(updated)
                    }
                }
                downloadJobs[entity.id] = job
            }
        }
    }

    fun cancelDownload(id: String) {
        downloadJobs[id]?.cancel()
        viewModelScope.launch {
            val entity = repository.getDownloadById(id)
            if (entity != null) {
                repository.insertDownload(entity.copy(status = "cancelled", downloadSpeed = 0.0, etaSeconds = 0))
            }
        }
    }

    fun deleteDownload(entity: DownloadEntity) {
        downloadJobs[entity.id]?.cancel()
        viewModelScope.launch {
            repository.deleteDownload(entity)
        }
    }
}
