package com.notificationlogger

import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*

class BiometricAuthModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    
    private val authManager = BiometricAuthManager(reactContext)
    
    override fun getName(): String {
        return "BiometricAuthModule"
    }
    
    @ReactMethod
    fun isBiometricAvailable(promise: Promise) {
        try {
            val available = authManager.isBiometricAvailable()
            promise.resolve(available)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
    
    @ReactMethod
    fun isBiometricEnrolled(promise: Promise) {
        try {
            val enrolled = authManager.isBiometricEnrolled()
            promise.resolve(enrolled)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
    
    @ReactMethod
    fun authenticateBiometric(options: ReadableMap, promise: Promise) {
        try {
            val activity = reactApplicationContext.currentActivity as? FragmentActivity
            if (activity == null) {
                promise.reject("ERROR", "Activity is not available")
                return
            }
            
            val title = options.getString("title") ?: "Authenticate"
            val subtitle = options.getString("subtitle") ?: "Use your fingerprint"
            
            authManager.authenticateBiometric(
                activity = activity,
                title = title,
                subtitle = subtitle,
                onSuccess = {
                    val result = Arguments.createMap().apply {
                        putBoolean("success", true)
                    }
                    promise.resolve(result)
                },
                onError = { error ->
                    val result = Arguments.createMap().apply {
                        putBoolean("success", false)
                        putString("error", error)
                    }
                    promise.resolve(result)
                }
            )
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
    
    @ReactMethod
    fun isAuthenticated(promise: Promise) {
        try {
            val authenticated = authManager.isAuthenticated()
            promise.resolve(authenticated)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
    
    @ReactMethod
    fun lockApp() {
        authManager.lockApp()
    }
    
    @ReactMethod
    fun setAutoLockTimeout(minutes: Int, promise: Promise) {
        try {
            authManager.setAutoLockTimeout(minutes)
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
    
    @ReactMethod
    fun getAutoLockTimeout(promise: Promise) {
        try {
            val timeout = authManager.getAutoLockTimeout()
            promise.resolve(timeout)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
}
