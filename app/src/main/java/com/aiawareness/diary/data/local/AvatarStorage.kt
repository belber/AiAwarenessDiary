package com.aiawareness.diary.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvatarStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun importAvatar(uri: Uri): String {
        val avatarDir = File(context.filesDir, "avatars").apply { mkdirs() }
        val extension = guessExtension(uri)
        val target = File(avatarDir, "avatar_${System.currentTimeMillis()}.$extension")

        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("无法读取头像文件")

        avatarDir.listFiles()
            ?.filter { it.absolutePath != target.absolutePath }
            ?.forEach(File::delete)

        return target.absolutePath
    }

    private fun guessExtension(uri: Uri): String {
        val mime = context.contentResolver.getType(uri).orEmpty()
        return when {
            mime.endsWith("png") -> "png"
            mime.endsWith("webp") -> "webp"
            mime.endsWith("gif") -> "gif"
            else -> "jpg"
        }
    }
}
