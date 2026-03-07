package com.notificationlogger

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class DatabaseHelper private constructor(context: Context, password: ByteArray) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    password,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "notifications.db"
        private const val DATABASE_VERSION = 1
        private const val KEYSTORE_ALIAS = "NotificationLoggerKey"

        init {
            System.loadLibrary("sqlcipher")
        }

        @Volatile
        private var instance: DatabaseHelper? = null
        
        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val password = getOrCreateEncryptionKey()
                    DatabaseHelper(context.applicationContext, password).also { instance = it }
                }
            }
        }

        private fun getOrCreateEncryptionKey(): ByteArray {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKey = if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            } else {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(
                        KEYSTORE_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                keyGenerator.generateKey()
            }

            return secretKey.encoded
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE notifications (
                id TEXT PRIMARY KEY,
                timestamp INTEGER NOT NULL,
                package_name TEXT NOT NULL,
                app_name TEXT NOT NULL,
                title TEXT,
                text TEXT,
                sub_text TEXT,
                big_text TEXT,
                info_text TEXT,
                summary_text TEXT,
                ticker_text TEXT,
                notification_id INTEGER NOT NULL,
                tag TEXT,
                channel_id TEXT,
                group_key TEXT,
                sort_key TEXT,
                color INTEGER,
                small_icon TEXT,
                large_icon TEXT,
                priority INTEGER NOT NULL,
                category TEXT,
                visibility INTEGER NOT NULL,
                actions TEXT,
                extras TEXT,
                is_ongoing INTEGER NOT NULL,
                is_group_summary INTEGER NOT NULL,
                is_clearable INTEGER NOT NULL
            )
        """)
        
        db.execSQL("CREATE INDEX idx_timestamp ON notifications(timestamp DESC)")
        db.execSQL("CREATE INDEX idx_package_name ON notifications(package_name)")
        db.execSQL("CREATE INDEX idx_priority ON notifications(priority)")
        db.execSQL("CREATE INDEX idx_timestamp_package ON notifications(timestamp DESC, package_name)")
        
        db.execSQL("""
            CREATE TABLE settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """)
        
        db.execSQL("INSERT INTO settings (key, value) VALUES ('retention_period', '10')")
        db.execSQL("INSERT INTO settings (key, value) VALUES ('auto_lock_timeout', '5')")
        db.execSQL("INSERT INTO settings (key, value) VALUES ('grouping_enabled', 'true')")
        db.execSQL("INSERT INTO settings (key, value) VALUES ('animation_intensity', 'normal')")
        
        db.execSQL("""
            CREATE TABLE excluded_apps (
                package_name TEXT PRIMARY KEY
            )
        """)
        
        db.execSQL("""
            CREATE TABLE app_metadata (
                package_name TEXT PRIMARY KEY,
                app_name TEXT NOT NULL,
                icon TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades
    }
}
