package com.aiawareness.diary.data.repository

import com.aiawareness.diary.data.local.RecordDao
import com.aiawareness.diary.data.local.RecordEntity
import com.aiawareness.diary.data.model.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepository @Inject constructor(
    private val recordDao: RecordDao
) {

    suspend fun insertRecord(record: Record): Long = recordDao.insert(RecordEntity.fromModel(record))

    suspend fun updateRecord(record: Record) {
        recordDao.update(RecordEntity.fromModel(record))
    }

    suspend fun deleteRecord(id: Long) {
        recordDao.deleteById(id)
    }

    suspend fun deleteRecordAndReturn(id: Long): Record? {
        val record = recordDao.getRecordById(id)?.toModel()
        if (record != null) {
            recordDao.deleteById(id)
        }
        return record
    }

    suspend fun getRecordsByDate(date: String): List<Record> =
        recordDao.getRecordsByDate(date).map { it.toModel() }

    fun getRecordsByDateFlow(date: String): Flow<List<Record>> =
        recordDao.getRecordsByDateFlow(date).map { entities ->
            entities.map { it.toModel() }
        }

    suspend fun getDatesWithRecords(): List<String> = recordDao.getDatesWithRecords()

    suspend fun getRecordCountByDate(date: String): Int = recordDao.getRecordCountByDate(date)

    suspend fun getRecordById(id: Long): Record? = recordDao.getRecordById(id)?.toModel()

    suspend fun getAllRecords(): List<Record> = recordDao.getAllRecords().map { it.toModel() }

    suspend fun deleteByFingerprint(record: Record) {
        recordDao.deleteByFingerprint(
            date = record.date,
            time = record.time,
            content = record.content
        )
    }
}
