package com.notificationlogger

import org.json.JSONArray
import org.json.JSONObject

data class NotificationRecord(
    val id: String,
    val timestamp: Long,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val bigText: String?,
    val infoText: String?,
    val summaryText: String?,
    val tickerText: String?,
    val notificationId: Int,
    val tag: String?,
    val channelId: String?,
    val groupKey: String?,
    val sortKey: String?,
    val color: Int?,
    val smallIcon: String?,
    val largeIcon: String?,
    val priority: Int,
    val category: String?,
    val visibility: Int,
    val actions: List<NotificationAction>,
    val extras: Map<String, Any>,
    val isOngoing: Boolean,
    val isGroupSummary: Boolean,
    val isClearable: Boolean
) {
    fun toJSON(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("timestamp", timestamp)
            put("packageName", packageName)
            put("appName", appName)
            put("title", title)
            put("text", text)
            put("subText", subText)
            put("bigText", bigText)
            put("infoText", infoText)
            put("summaryText", summaryText)
            put("tickerText", tickerText)
            put("notificationId", notificationId)
            put("tag", tag)
            put("channelId", channelId)
            put("groupKey", groupKey)
            put("sortKey", sortKey)
            put("color", color)
            put("smallIcon", smallIcon)
            put("largeIcon", largeIcon)
            put("priority", priority)
            put("category", category)
            put("visibility", visibility)
            put("actions", JSONArray(actions.map { it.toJSON() }))
            put("extras", JSONObject(extras))
            put("isOngoing", isOngoing)
            put("isGroupSummary", isGroupSummary)
            put("isClearable", isClearable)
        }
    }
}

data class NotificationAction(
    val title: String,
    val icon: String?
) {
    fun toJSON(): JSONObject {
        return JSONObject().apply {
            put("title", title)
            put("icon", icon)
        }
    }
}
