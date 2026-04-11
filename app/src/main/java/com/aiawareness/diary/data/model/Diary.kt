package com.aiawareness.diary.data.model

data class Diary(
    val id: Long = 0,
    val date: String,
    val aiDiary: String,
    val aiInsight: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)