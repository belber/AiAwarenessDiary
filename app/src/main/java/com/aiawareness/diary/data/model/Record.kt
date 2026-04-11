package com.aiawareness.diary.data.model

data class Record(
    val id: Long = 0,
    val date: String,
    val time: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)