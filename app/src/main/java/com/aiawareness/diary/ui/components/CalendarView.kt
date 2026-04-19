package com.aiawareness.diary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aiawareness.diary.ui.theme.JournalTokens
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarView(
    year: Int,
    month: Int,
    datesWithRecords: Set<String>,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val yearMonth = remember(year, month) { YearMonth.of(year, month) }
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { cellIndex ->
                    val dayNumber = rowIndex * 7 + cellIndex - firstDayOfWeek + 1
                    if (dayNumber in 1..daysInMonth) {
                        val date = LocalDate.of(year, month, dayNumber).toString()
                        val hasRecord = date in datesWithRecords
                        DayCell(
                            day = dayNumber,
                            hasRecord = hasRecord,
                            isSelected = date == selectedDate,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    hasRecord: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isSelected -> JournalTokens.Sage.copy(alpha = 0.14f)
        else -> MaterialTheme.colorScheme.surface
    }
    val dayColor = when {
        isSelected -> JournalTokens.SageDim
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = dayColor.copy(alpha = if (hasRecord || isSelected) 1f else 0.5f)
                )
            if (hasRecord) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) JournalTokens.SageDim else JournalTokens.Sage)
                )
            }
        }
    }
}
