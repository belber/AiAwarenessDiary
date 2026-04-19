package com.aiawareness.diary.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object DateUtil {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
    private val weekdays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    fun getCurrentDate(): String = LocalDate.now().format(dateFormatter)

    fun getCurrentTime(): String = LocalTime.now().format(timeFormatter)

    fun getCurrentDisplayDate(): String = LocalDate.now().format(displayDateFormatter)

    fun getCurrentDisplayDateWithWeekday(): String = formatDisplayDateWithWeekday(getCurrentDate())

    fun formatDisplayDate(date: String): String =
        LocalDate.parse(date, dateFormatter).format(displayDateFormatter)

    fun formatDisplayDateWithWeekday(date: String): String {
        val localDate = LocalDate.parse(date, dateFormatter)
        val weekday = weekdays[localDate.dayOfWeek.value - 1]
        return "${localDate.format(displayDateFormatter)} $weekday"
    }

    fun getCurrentHour(): Int = LocalTime.now().hour

    fun getCurrentMinute(): Int = LocalTime.now().minute

    fun isAfterGenerationTime(hour: Int, minute: Int): Boolean {
        val now = LocalTime.now()
        val generationTime = LocalTime.of(hour, minute)
        return now.isAfter(generationTime)
    }

    fun getDaysInMonth(year: Int, month: Int): Int = LocalDate.of(year, month, 1).lengthOfMonth()

    fun getMonthDates(year: Int, month: Int): List<String> {
        val days = getDaysInMonth(year, month)
        return (1..days).map { day ->
            LocalDate.of(year, month, day).format(dateFormatter)
        }
    }

    fun parseYearMonth(date: String): Pair<Int, Int> {
        val localDate = LocalDate.parse(date, dateFormatter)
        return localDate.year to localDate.monthValue
    }
}
