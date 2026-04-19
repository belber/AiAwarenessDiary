package com.aiawareness.diary.data.local

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

class RecordPhotoStorageHelpersTest {

    @Test
    fun copySourceStreamToTempFile_readsImportedSourceOnlyOnce() {
        val tempDir = createTempDir(prefix = "record-photo-test")
        val bytes = "preview-image".encodeToByteArray()
        var openCount = 0

        val tempFile = copySourceStreamToTempFile(
            openInputStream = {
                openCount += 1
                ByteArrayInputStream(bytes)
            },
            tempDirectory = tempDir,
            errorMessage = "无法读取图片"
        )

        assertEquals(1, openCount)
        assertArrayEquals(bytes, tempFile.readBytes())
        tempFile.delete()
        tempDir.delete()
    }

    @Test(expected = IOException::class)
    fun copySourceStreamToTempFile_throwsConfiguredErrorWhenSourceMissing() {
        val tempDir = createTempDir(prefix = "record-photo-test")

        try {
            copySourceStreamToTempFile(
                openInputStream = { null },
                tempDirectory = tempDir,
                errorMessage = "无法读取图片"
            )
        } finally {
            tempDir.delete()
        }
    }

    @Test
    fun copyTempFileToPersistedPhoto_preservesOriginalBytes() {
        val sourceDir = createTempDir(prefix = "record-photo-source")
        val targetDir = createTempDir(prefix = "record-photo-target")
        val source = File(sourceDir, "photo.tmp").apply {
            writeBytes("raw-image-bits".encodeToByteArray())
        }

        val persisted = copyTempFileToPersistedPhoto(source, targetDir)

        assertArrayEquals(source.readBytes(), persisted.readBytes())
        persisted.delete()
        source.delete()
        sourceDir.delete()
        targetDir.delete()
    }

    private fun createTempDir(prefix: String): File {
        val dir = File(System.getProperty("java.io.tmpdir"), "$prefix-${System.nanoTime()}")
        dir.mkdirs()
        return dir
    }
}
