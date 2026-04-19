package com.aiawareness.diary.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.cancel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun isPrivacyPolicyAccepted_defaultsToFalse() = runBlocking {
        val preferences = createUserPreferences()

        try {
            assertFalse(preferences.userPreferences.isPrivacyPolicyAccepted.first())
        } finally {
            preferences.close()
            deleteBackingFile(preferences.backingFile)
        }
    }

    @Test
    fun setPrivacyPolicyAccepted_persistsTrue() = runBlocking {
        val preferences = createUserPreferences()

        try {
            preferences.userPreferences.setPrivacyPolicyAccepted(true)

            preferences.close()

            val reloadedPreferences = createUserPreferences(preferences.backingFile)
            try {
                assertTrue(reloadedPreferences.userPreferences.isPrivacyPolicyAccepted.first())
            } finally {
                reloadedPreferences.close()
            }
        } finally {
            deleteBackingFile(preferences.backingFile)
        }
    }

    private fun createUserPreferences(backingFile: File = createBackingFile()): TestUserPreferences {
        val dataStoreScope = backingFileDataStoreScope()
        return TestUserPreferences(
            userPreferences = UserPreferences(createDataStore(backingFile, dataStoreScope)),
            dataStoreScope = dataStoreScope,
            backingFile = backingFile
        )
    }

    private fun createDataStore(backingFile: File, scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { backingFile }
        )

    private fun createBackingFile(): File =
        File.createTempFile("user-preferences", ".preferences_pb").apply {
            delete()
        }

    private fun backingFileDataStoreScope(): CoroutineScope =
        CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun deleteBackingFile(backingFile: File) {
        backingFile.delete()
    }

    private data class TestUserPreferences(
        val userPreferences: UserPreferences,
        val dataStoreScope: CoroutineScope,
        val backingFile: File
    ) {
        fun close() {
            dataStoreScope.cancel()
        }
    }
}
