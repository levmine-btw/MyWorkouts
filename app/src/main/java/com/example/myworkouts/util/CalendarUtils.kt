package com.example.myworkouts.util

import com.example.myworkouts.data.models.CalendarWeek
import java.time.LocalDate
import java.time.YearMonth


fun generateCalendarWeeks(yearMonth: YearMonth): List<CalendarWeek> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val dayOfWeek = firstDayOfMonth.dayOfWeek.value

    val weeks = mutableListOf<CalendarWeek>()
    var currentWeekDays = mutableListOf<LocalDate?>()

    repeat(dayOfWeek - 1) { currentWeekDays.add(null) }

    for (day in 1..daysInMonth) {
        currentWeekDays.add(yearMonth.atDay(day))
        if (currentWeekDays.size == 7 || day == daysInMonth) {
            while (currentWeekDays.size < 7) currentWeekDays.add(null)
            weeks.add(CalendarWeek(days = currentWeekDays.toList()))
            currentWeekDays = mutableListOf()
        }
    }
    return weeks
}