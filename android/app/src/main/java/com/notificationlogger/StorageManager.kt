package com.notificationlogger

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import kotlinx.coroutines.*
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class StorageManager(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val pendingNotifications = mutableListOf<NotificationRecord>()
    private var lastBatchTime = System.currentTimeMillis()
    
    companion object {
        private const val BATCH_SIZE = 5
        private const val BATCH_TIMEOUT_MS = 10000L
    }
    
    fun insertNotification(notification: NotificationRecord) {
        scope.launch {
            synchronized(pendingNotifications) {
                pendingNotifications.add(notification)
                
                if (pendingNotifications.size >= BATCH_SIZE || 
                    System.currentTimeMillis() - lastBatchTime >= BATCH_TIMEOUT_MS) {
                    flushBatch()
                }
            }
        }
    }
    
    private fun flushBatch() {
        if (pendingNotifications.isEmpty()) return
        
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            pendingNotifications.forEach { notification ->
                val values = ContentValues().apply {
                    put("id", notification.id)
                    put("timestamp", notification.timestamp)
                    put("package_name", notification.packageName)
                    put("app_name", notification.appName)
                    put("title", notification.title)
                    put("text", notification.text)
                    put("sub_text", notification.subText)
                    put("big_text", notification.bigText)
                    put("info_text", notification.infoText)
                    put("summary_text", notification.summaryText)
                    put("ticker_text", notification.tickerText)
                    put("notification_id", notification.notificationId)
                    put("tag", notification.tag)
                    put("channel_id", notification.channelId)
                    put("group_key", notification.groupKey)
                    put("sort_key", notification.sortKey)
                    put("color", notification.color)
                    put("small_icon", notification.smallIcon)
                    put("large_icon", notification.largeIcon)
                    put("priority", notification.priority)
                    put("category", notification.category)
                    put("visibility", notification.visibility)
                    put("actions", JSONArray(notification.actions.map { it.toJSON() }).toString())
                    put("extras", JSONObject(notification.extras).toString())
                    put("is_ongoing", if (notification.isOngoing) 1 else 0)
                    put("is_group_summary", if (notification.isGroupSummary) 1 else 0)
                    put("is_clearable", if (notification.isClearable) 1 else 0)
                }
                db.insert("notifications", null, values)
            }
            db.setTransactionSuccessful()
            pendingNotifications.clear()
            lastBatchTime = System.currentTimeMillis()
        } finally {
            db.endTransaction()
        }
        
        // Cleanup old notifications after successful insert
        cleanupOldNotifications()
    }
    
    private fun cleanupOldNotifications() {
        val retentionPeriod = getRetentionPeriod()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -retentionPeriod)
        
        val threshold = calendar.timeInMillis
        val db = dbHelper.writableDatabase
        db.delete("notifications", "timestamp < ?", arrayOf(threshold.toString()))
    }
    
    fun getNotifications(
        packageNames: List<String>? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        searchQuery: String? = null,
        priorities: List<Int>? = null,
        sortBy: String = "timestamp",
        sortOrder: String = "desc",
        limit: Int = 100,
        offset: Int = 0
    ): List<NotificationRecord> {
        val db = dbHelper.readableDatabase
        val selection = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()
        
        packageNames?.let {
            selection.add("package_name IN (${it.joinToString(",") { "?" }})")
            selectionArgs.addAll(it)
        }
        
        startDate?.let {
            selection.add("timestamp >= ?")
            selectionArgs.add(it.toString())
        }
        
        endDate?.let {
            selection.add("timestamp <= ?")
            selectionArgs.add(it.toString())
        }
        
        searchQuery?.let { query ->
            selection.add("(title LIKE ? OR text LIKE ? OR sub_text LIKE ? OR big_text LIKE ? OR app_name LIKE ?)")
            val searchPattern = "%$query%"
            repeat(5) { selectionArgs.add(searchPattern) }
        }
        
        priorities?.let {
            selection.add("priority IN (${it.joinToString(",") { "?" }})")
            selectionArgs.addAll(it.map { it.toString() })
        }
        
        val orderBy = "$sortBy ${sortOrder.uppercase()}"
        
        val cursor = db.query(
            "notifications",
            null,
            if (selection.isNotEmpty()) selection.joinToString(" AND ") else null,
            if (selectionArgs.isNotEmpty()) selectionArgs.toTypedArray() else null,
            null,
            null,
            orderBy,
            "$limit OFFSET $offset"
        )
        
        return cursor.use { parseNotifications(it) }
    }
    
    private fun parseNotifications(cursor: Cursor): List<NotificationRecord> {
        val notifications = mutableListOf<NotificationRecord>()
        while (cursor.moveToNext()) {
            notifications.add(parseNotificationRecord(cursor))
        }
        return notifications
    }
    
    private fun parseNotificationRecord(cursor: Cursor): NotificationRecord {
        return NotificationRecord(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
            packageName = cursor.getString(cursor.getColumnIndexOrThrow("package_name")),
            appName = cursor.getString(cursor.getColumnIndexOrThrow("app_name")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            text = cursor.getString(cursor.getColumnIndexOrThrow("text")),
            subText = cursor.getString(cursor.getColumnIndexOrThrow("sub_text")),
            bigText = cursor.getString(cursor.getColumnIndexOrThrow("big_text")),
            infoText = cursor.getString(cursor.getColumnIndexOrThrow("info_text")),
            summaryText = cursor.getString(cursor.getColumnIndexOrThrow("summary_text")),
            tickerText = cursor.getString(cursor.getColumnIndexOrThrow("ticker_text")),
            notificationId = cursor.getInt(cursor.getColumnIndexOrThrow("notification_id")),
            tag = cursor.getString(cursor.getColumnIndexOrThrow("tag")),
            channelId = cursor.getString(cursor.getColumnIndexOrThrow("channel_id")),
            groupKey = cursor.getString(cursor.getColumnIndexOrThrow("group_key")),
            sortKey = cursor.getString(cursor.getColumnIndexOrThrow("sort_key")),
            color = if (cursor.isNull(cursor.getColumnIndexOrThrow("color"))) null else cursor.getInt(cursor.getColumnIndexOrThrow("color")),
            smallIcon = cursor.getString(cursor.getColumnIndexOrThrow("small_icon")),
            largeIcon = cursor.getString(cursor.getColumnIndexOrThrow("large_icon")),
            priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority")),
            category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
            visibility = cursor.getInt(cursor.getColumnIndexOrThrow("visibility")),
            actions = parseActions(cursor.getString(cursor.getColumnIndexOrThrow("actions"))),
            extras = parseExtras(cursor.getString(cursor.getColumnIndexOrThrow("extras"))),
            isOngoing = cursor.getInt(cursor.getColumnIndexOrThrow("is_ongoing")) == 1,
            isGroupSummary = cursor.getInt(cursor.getColumnIndexOrThrow("is_group_summary")) == 1,
            isClearable = cursor.getInt(cursor.getColumnIndexOrThrow("is_clearable")) == 1
        )
    }
    
    private fun parseActions(json: String?): List<NotificationAction> {
        if (json == null) return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map {
            val obj = array.getJSONObject(it)
            NotificationAction(
                title = obj.getString("title"),
                icon = obj.optString("icon", null)
            )
        }
    }
    
    private fun parseExtras(json: String?): Map<String, Any> {
        if (json == null) return emptyMap()
        val obj = JSONObject(json)
        return obj.keys().asSequence().associateWith { obj.get(it) }
    }
    
    fun deleteNotifications(ids: List<String>) {
        val db = dbHelper.writableDatabase
        db.delete("notifications", "id IN (${ids.joinToString(",") { "?" }})", ids.toTypedArray())
    }
    
    fun deleteAllNotifications() {
        val db = dbHelper.writableDatabase
        db.delete("notifications", null, null)
    }
    
    fun getRetentionPeriod(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.query("settings", arrayOf("value"), "key = ?", arrayOf("retention_period"), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) it.getString(0).toInt() else 10
        }
    }
    
    fun setRetentionPeriod(days: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("value", days.toString())
        }
        db.update("settings", values, "key = ?", arrayOf("retention_period"))
    }
    
    fun getExcludedApps(): List<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("excluded_apps", arrayOf("package_name"), null, null, null, null, null)
        return cursor.use {
            val apps = mutableListOf<String>()
            while (it.moveToNext()) {
                apps.add(it.getString(0))
            }
            apps
        }
    }
    
    fun setExcludedApps(packageNames: List<String>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("excluded_apps", null, null)
            packageNames.forEach { packageName ->
                val values = ContentValues().apply {
                    put("package_name", packageName)
                }
                db.insert("excluded_apps", null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
