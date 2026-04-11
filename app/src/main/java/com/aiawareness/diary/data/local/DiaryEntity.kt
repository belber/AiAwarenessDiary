package com.aiawareness.diary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aiawareness.diary.data.model.Diary

@Entity(tableName = "diaries")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val aiDiary: String,
    val aiInsight: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel(): Diary = Diary(
        id = id,
        date = date,
        aiDiary = aiDiary,
        aiInsight = aiInsight,
        generatedAt = generatedAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: Diary): DiaryEntity = DiaryEntity(
            id = model.id,
            date = model.date,
            aiDiary = model.aiDiary,
            aiInsight = model.aiInsight,
            generatedAt = model.generatedAt,
            updatedAt = model.updatedAt
        )
    }
}