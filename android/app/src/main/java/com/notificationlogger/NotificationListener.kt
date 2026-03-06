package com.notificationlogger

import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.util.*

class NotificationListener : NotificationListenerService() {
    
    private lateinit var storageManager: StorageManager
    private var excludedApps: Set<String> = emptySet()
    
    companion object {
        private const val TAG = "NotificationListener"
    }
    
    override fun onCreate() {
        super.onCreate()
        storageManager = StorageManager(applicationContext)
        loadExcludedApps()
    }
    
    private fun loadExcludedApps() {
        excludedApps = storageManager.getExcludedApps().toSet()
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected")
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected, attempting to reconnect")
        requestRebind(ComponentName(this, NotificationListener::class.java))
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        // Check if app is excluded
        if (excludedApps.contains(sbn.packageName)) {
            return
        }
        
        val notification = sbn.notification
        val extras = notification.extras
        
        // Extract app name
        val appName = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            sbn.packageName
        }
        
        // Extract notification content
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val subText = extras.getCharSequence("android.subText")?.toString()
        val bigText = extras.getCharSequence("android.bigText")?.toString()
        val infoText = extras.getCharSequence("android.infoText")?.toString()
        val summaryText = extras.getCharSequence("android.summaryText")?.toString()
        val tickerText = notification.tickerText?.toString()
        
        // Extract actions
        val actions = notification.actions?.map { action ->
            NotificationAction(
                title = action.title.toString(),
                icon = null // Skip icon encoding for actions to save space
            )
        } ?: emptyList()
        
        // Extract extras as map
        val extrasMap = mutableMapOf<String, Any>()
        extras.keySet().forEach { key ->
            extras.get(key)?.let { value ->
                when (value) {
                    is String, is Int, is Long, is Boolean, is Double, is Float -> {
                        extrasMap[key] = value
                    }
                }
            }
        }
        
        // Create notification record
        val record = NotificationRecord(
            id = UUID.randomUUID().toString(),
            timestamp = sbn.postTime,
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            infoText = infoText,
            summaryText = summaryText,
            tickerText = tickerText,
            notificationId = sbn.id,
            tag = sbn.tag,
            channelId = notification.channelId,
            groupKey = sbn.groupKey,
            sortKey = sbn.notification.sortKey,
            color = if (notification.color != 0) notification.color else null,
            smallIcon = null, // Skip small icon to save space
            largeIcon = null, // Skip large icon to save space
            priority = notification.priority,
            category = notification.category,
            visibility = notification.visibility,
            actions = actions,
            extras = extrasMap,
            isOngoing = (notification.flags and android.app.Notification.FLAG_ONGOING_EVENT) != 0,
            isGroupSummary = (notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0,
            isClearable = (notification.flags and android.app.Notification.FLAG_NO_CLEAR) == 0
        )
        
        // Store notification
        storageManager.insertNotification(record)
    }
    
    private fun drawableToBase64(drawable: Drawable?, maxSizeKb: Int = 10): String? {
        if (drawable == null) return null
        
        try {
            val bitmap = when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> {
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                }
            }
            
            val stream = ByteArrayOutputStream()
            var quality = 90
            
            do {
                stream.reset()
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream)
                quality -= 10
            } while (stream.size() > maxSizeKb * 1024 && quality > 0)
            
            return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding drawable to base64", e)
            return null
        }
    }
}
