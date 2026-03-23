package com.wu.my_todolist

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class DeadlineInfo(
    val text: String,
    val isUrgent: Boolean
)

fun getDeadlineInfo(deadline: Long?): DeadlineInfo? {
    if (deadline == null) return null

    val deadlineDate = Instant.ofEpochMilli(deadline)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today = LocalDate.now()

    val daysRemaining = ChronoUnit.DAYS.between(today, deadlineDate)

    return when {
        daysRemaining < 0 -> DeadlineInfo("Overdue by ${-daysRemaining} days", true)
        daysRemaining == 0L -> DeadlineInfo("Due today", true)
        daysRemaining == 1L -> DeadlineInfo("1 day left", false)
        else -> DeadlineInfo("$daysRemaining days left", false)
    }
}
