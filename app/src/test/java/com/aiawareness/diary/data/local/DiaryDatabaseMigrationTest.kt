package com.aiawareness.diary.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import java.nio.file.Files
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiaryDatabaseMigrationTest {

    @Test
    fun migration_1_2_addsPhotoPathColumnWithDefaultValue() {
        Class.forName("org.sqlite.JDBC")

        val databasePath = Files.createTempFile("diary-db-migration", ".db")
        DriverManager.getConnection("jdbc:sqlite:${databasePath.toAbsolutePath()}", Properties()).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TABLE records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        time TEXT NOT NULL,
                        content TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    INSERT INTO records (date, time, content, createdAt, updatedAt)
                    VALUES ('2026-04-15', '09:20', '晨间散步', 100, 120)
                    """.trimIndent()
                )
            }

            DiaryDatabase.MIGRATION_1_2.migrate(connection.asSupportSQLiteDatabase())

            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA table_info(records)").use { columns ->
                    val seenColumns = mutableListOf<String>()
                    var defaultValue: String? = null
                    while (columns.next()) {
                        val name = columns.getString("name")
                        seenColumns += name
                        if (name == "photoPath") {
                            defaultValue = columns.getString("dflt_value")
                        }
                    }

                    assertTrue(seenColumns.contains("photoPath"))
                    assertEquals("''", defaultValue)
                }

                statement.executeQuery("SELECT photoPath FROM records WHERE id = 1").use { rows ->
                    assertTrue(rows.next())
                    assertEquals("", rows.getString("photoPath"))
                }
            }
        }
    }

    private fun Connection.asSupportSQLiteDatabase(): SupportSQLiteDatabase =
        java.lang.reflect.Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            when (method.name) {
                "execSQL" -> {
                    val sql = requireNotNull(args?.first()) as String
                    this@asSupportSQLiteDatabase.createStatement().use { statement ->
                        statement.execute(sql)
                    }
                    null
                }
                "close" -> {
                    this@asSupportSQLiteDatabase.close()
                    null
                }
                "isOpen" -> !this@asSupportSQLiteDatabase.isClosed
                "getPath" -> this@asSupportSQLiteDatabase.metaData.url
                "setVersion" -> {
                    val version = requireNotNull(args?.first()) as Int
                    this@asSupportSQLiteDatabase.createStatement().use { statement ->
                        statement.execute("PRAGMA user_version = $version")
                    }
                    null
                }
                "getVersion" -> this@asSupportSQLiteDatabase.createStatement().use { statement ->
                    statement.executeQuery("PRAGMA user_version").use { cursor ->
                        cursor.next()
                        cursor.getInt(1)
                    }
                }
                "beginTransaction",
                "beginTransactionNonExclusive",
                "beginTransactionWithListener",
                "beginTransactionWithListenerNonExclusive",
                "endTransaction",
                "setTransactionSuccessful",
                "setLocale",
                "setMaxSqlCacheSize",
                "setForeignKeyConstraintsEnabled",
                "disableWriteAheadLogging",
                "setPageSize" -> null
                "inTransaction",
                "isDbLockedByCurrentThread",
                "yieldIfContendedSafely",
                "isReadOnly",
                "needUpgrade",
                "isWriteAheadLoggingEnabled" -> false
                "enableWriteAheadLogging",
                "isDatabaseIntegrityOk" -> false
                "getPageSize",
                "getMaximumSize" -> 0L
                "setMaximumSize" -> 0L
                "getAttachedDbs" -> mutableListOf<android.util.Pair<String, String>>()
                "compileStatement" -> error("Not used in this test")
                "query" -> error("Not used in this test")
                else -> defaultValue(method.returnType)
            }
        } as SupportSQLiteDatabase

    private fun defaultValue(returnType: Class<*>): Any? = when (returnType) {
        java.lang.Boolean.TYPE -> false
        java.lang.Byte.TYPE -> 0.toByte()
        java.lang.Short.TYPE -> 0.toShort()
        java.lang.Integer.TYPE -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Float.TYPE -> 0f
        java.lang.Double.TYPE -> 0.0
        java.lang.Character.TYPE -> 0.toChar()
        else -> null
    }
}
