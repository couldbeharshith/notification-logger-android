package com.notificationlogger

import android.content.Intent
import android.provider.Settings
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationListenerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    
    private val storageManager = StorageManager(reactContext)
    private val scope = CoroutineScope(Dispatchers.Main)
    
    override fun getName(): String {
        return "NotificationListenerModule"
    }
    
    @ReactMethod
    fun hasNotificationPermission(promise: Promise) {
        try {
            val enabledListeners = Settings.Secure.getString(
                reactApplicationContext.contentResolver,
                "enabled_notification_listeners"
            )
            val packageName = reactApplicationContext.packageName
            val hasPermission = enabledListeners?.contains(packageName) == true
            promise.resolve(hasPermission)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
    
    @ReactMethod
    fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        reactApplicationContext.startActivity(intent)
    }
    
    @ReactMethod
    fun getNotifications(filters: ReadableMap, promise: Promise) {
        scope.launch(Dispatchers.IO) {
            try {
                val packageNames = filters.getArray("packageNames")?.toArrayList()?.map { it.toString() }
                val startDate = if (filters.hasKey("startDate")) filters.getDouble("startDate").toLong() else null
                val endDate = if (filters.hasKey("endDate")) filters.getDouble("endDate").toLong() else null
                val searchQuery = if (filters.hasKey("searchQuery")) filters.getString("searchQuery") else null
                val priorities = filters.getArray("priorities")?.toArrayList()?.map { (it as Double).toInt() }
                val sortBy = if (filters.hasKey("sortBy")) filters.getString("sortBy") ?: "timestamp" else "timestamp"
                val sortOrder = if (filters.hasKey("sortOrder")) filters.getString("sortOrder") ?: "desc" else "desc"
                val limit = if (filters.hasKey("limit")) filters.getInt("limit") else 100
                val offset = if (filters.hasKey("offset")) filters.getInt("offset") else 0
                
                val notifications = storageManager.getNotifications(
                    packageNames = packageNames,
                    startDate = startDate,
                    endDate = endDate,
                    searchQuery = searchQuery,
                    priorities = priorities,
                    sortBy = sortBy,
                    sortOrder = sortOrder,
                    limit = limit,
                    offset = offset
                )
                
                val result = Arguments.createArray()
                notifications.forEach { notification ->
                    result.pushMap(notificationToMap(notification))
                }
                
                promise.resolve(result)
            } catch (e: Exception) {
                promise.reject("ERROR", e.message)
            }
        }
    }
    
    @ReactMethod
    fun getNotificationById(id: String, promise: Promise) {
        scope.launch(Dispatchers.IO) {
            try {
                val notifications = storageManager.getNotifications(limit = 1, offset = 0)
                val notification = notifications.firstOrNull { it.id == id }
                if (notification != null) {
                    promise.resolve(notificationToMap(notification))
                } else {
                    promise.resolve(null)
                }
            } catch (e: Exception) {
                promise.reject("ERROR", e.message)
            }
        }
    }
    
    @ReactMethod
    fun deleteNotifications(ids: ReadableArray, promise: Promise) {
        scope.launch(Dispatchers.IO) {
            try {
                val idList = ids.toArrayList().map { it.toString() }
                storageManager.deleteNotifications(idList)
                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("ERROR", e.message)
            }
        }
    }
    
    @ReactMethod
    fun deleteAllNotifications(promise: Promise) {
        scope.launch(Dispatchers.IO) {
            try {
                storageManager.deleteAllNotifications()
                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("ERROR", e.message)
            }
        }
    }
    
    @ReactMethod
    fun setRetentionPeriod(days: Int, promise: Promise) {
        scope.launch(Dispatchers.IO) {
            try {
                storageManager.setRetentionPeriod(days)
                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("ERROR", e.message)
            }
        }
    }
    
    @ReactMethod
    fun getRetentionPeriod(promise: Promise) {
        scope.launch(Dispatchers.IO) {
            try {
                val period = storageManager.getRetentionPeriod()
                promise.resolve(period)
            } catch (e: Exception) {
                promise.reject("ERROR", e.message)
            }
        }
    }
    
    @ReactMethod
    fun setExcludedApps(packageNames: ReadableArray, promise: Promise) {
        scope.launch(Dispatchers.IO) {
            try {
                val apps = packageNames.toArrayList().map { it.toString() }
                storageManager.setExcludedApps(apps)
                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("ERROR", e.message)
            }
        }
    }
    
    @ReactMethod
    fun getExcludedApps(promise: Promise) {
        scope.launch(Dispatchers.IO) {
            try {
                val apps = storageManager.getExcludedApps()
                val result = Arguments.createArray()
                apps.forEach { result.pushString(it) }
                promise.resolve(result)
            } catch (e: Exception) {
                promise.reject("ERROR", e.message)
            }
        }
    }
    
    private fun notificationToMap(notification: NotificationRecord): WritableMap {
        return Arguments.createMap().apply {
            putString("id", notification.id)
            putDouble("timestamp", notification.timestamp.toDouble())
            putString("packageName", notification.packageName)
            putString("appName", notification.appName)
            putString("title", notification.title)
            putString("text", notification.text)
            putString("subText", notification.subText)
            putString("bigText", notification.bigText)
            putString("infoText", notification.infoText)
            putString("summaryText", notification.summaryText)
            putString("tickerText", notification.tickerText)
            putInt("notificationId", notification.notificationId)
            putString("tag", notification.tag)
            putString("channelId", notification.channelId)
            putString("groupKey", notification.groupKey)
            putString("sortKey", notification.sortKey)
            if (notification.color != null) putInt("color", notification.color) else putNull("color")
            putString("smallIcon", notification.smallIcon)
            putString("largeIcon", notification.largeIcon)
            putInt("priority", notification.priority)
            putString("category", notification.category)
            putInt("visibility", notification.visibility)
            putBoolean("isOngoing", notification.isOngoing)
            putBoolean("isGroupSummary", notification.isGroupSummary)
            putBoolean("isClearable", notification.isClearable)
        }
    }
}
