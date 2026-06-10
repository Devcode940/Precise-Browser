package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BrowserRepository(private val db: AppDatabase) {
    private val bookmarkDao = db.bookmarkDao()
    private val historyDao = db.historyDao()
    private val passwordDao = db.passwordDao()
    private val downloadDao = db.downloadDao()
    private val settingDao = db.settingDao()

    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val allPasswords: Flow<List<PasswordEntity>> = passwordDao.getAllPasswords()
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()
    val allSettings: Flow<List<UserSettingEntity>> = settingDao.getAllSettings()

    suspend fun insertBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(bookmark)
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    suspend fun deleteBookmarkByUrl(url: String) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkByUrl(url)
    }

    suspend fun clearAllBookmarks() = withContext(Dispatchers.IO) {
        bookmarkDao.clearAllBookmarks()
    }

    suspend fun insertHistoryItem(item: HistoryEntity) = withContext(Dispatchers.IO) {
        historyDao.insertHistoryItem(item)
    }

    suspend fun deleteHistoryItem(item: HistoryEntity) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryItem(item)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }

    suspend fun insertPassword(pwd: PasswordEntity) = withContext(Dispatchers.IO) {
        passwordDao.insertPassword(pwd)
    }

    suspend fun deletePassword(pwd: PasswordEntity) = withContext(Dispatchers.IO) {
        passwordDao.deletePassword(pwd)
    }

    suspend fun clearPasswords() = withContext(Dispatchers.IO) {
        passwordDao.clearPasswords()
    }

    suspend fun insertDownload(dl: DownloadEntity) = withContext(Dispatchers.IO) {
        downloadDao.insertDownload(dl)
    }

    suspend fun getDownloadById(id: String): DownloadEntity? = withContext(Dispatchers.IO) {
        downloadDao.getDownloadById(id)
    }

    suspend fun deleteDownload(dl: DownloadEntity) = withContext(Dispatchers.IO) {
        downloadDao.deleteDownload(dl)
    }

    suspend fun clearDownloads() = withContext(Dispatchers.IO) {
        downloadDao.clearDownloads()
    }

    suspend fun getSettingByKey(key: String): String? = withContext(Dispatchers.IO) {
        settingDao.getSettingByKey(key)
    }

    suspend fun saveSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        settingDao.saveSetting(UserSettingEntity(key, value))
    }

    suspend fun clearAllSettings() = withContext(Dispatchers.IO) {
        settingDao.clearAllSettings()
    }
}
