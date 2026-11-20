package com.example.data.model

import java.time.LocalDate

data class HijriDateModel(
    val year: Int,
    val month: Int,
    val day: Int,
    val monthNameEn: String,
    val monthNameAr: String,
    val formattedEn: String,
    val formattedAr: String
)

data class CalendarDay(
    val date: LocalDate,
    val hijriDate: HijriDateModel,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isFriday: Boolean,
    val isWeekend: Boolean,
    val holidays: List<Holiday> = emptyList(),
    val islamicEvents: List<IslamicEvent> = emptyList(),
    val personalEventsCount: Int = 0
) {
    val hasHoliday: Boolean get() = holidays.isNotEmpty()
    val hasIslamicEvent: Boolean get() = islamicEvents.isNotEmpty()
    val hasPersonalEvent: Boolean get() = personalEventsCount > 0
}

data class DateConversionResult(
    val gregorianDate: LocalDate,
    val hijriDate: HijriDateModel,
    val dayOfWeekEn: String,
    val dayOfWeekAr: String,
    val islamicEvents: List<IslamicEvent> = emptyList(),
    val holidays: List<Holiday> = emptyList()
)
