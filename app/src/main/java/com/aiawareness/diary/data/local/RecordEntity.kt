package com.aiawareness.diary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aiawareness.diary.data.model.Record

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val time: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel(): Record = Record(
        id = id,
        date = date,
        time = time,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: Record): RecordEntity = RecordEntity(
            id = model.id,
            date = model.date,
            time = model.time,
            content = model.content,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}