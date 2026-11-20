package com.example.data.local.repository

import com.example.calendar.ArabHolidaysProvider
import com.example.calendar.HijriCalendarEngine
import com.example.calendar.IslamicEventsProvider
import com.example.data.local.dao.EventDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.entity.PersonalEventEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.model.ArabCountry
import com.example.data.model.CalendarDay
import com.example.data.model.DateConversionResult
import com.example.data.model.Holiday
import com.example.data.model.IslamicEvent
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarRepository(
    private val eventDao: EventDao,
    private val userSettingsDao: UserSettingsDao
) {
    val allEvents: Flow<List<PersonalEventEntity>> = eventDao.getAllEvents()
    val favoriteEvents: Flow<List<PersonalEventEntity>> = eventDao.getFavoriteEvents()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getSettings()

    private suspend fun ensureSettingsRowExists() {
        if (userSettingsDao.getSettingsOnce() == null) {
            userSettingsDao.insertDefaultIfMissing(
                UserSettingsEntity(
                    id = 1,
                    selectedCountryCode = "SA",
                    hijriOffset = 0,
                    languageCode = "en",
                    firstDayOfWeek = 6,
                    darkMode = "system",
                    holidayReminderEnabled = false
                )
            )
        }
    }

    suspend fun saveSettings(settings: UserSettingsEntity) = userSettingsDao.saveSettings(settings)
    suspend fun insertDefaultIfMissing(settings: UserSettingsEntity) = userSettingsDao.insertDefaultIfMissing(settings)
    suspend fun updateCountry(countryCode: String) {
        ensureSettingsRowExists()
        userSettingsDao.updateCountry(countryCode)
    }
    suspend fun updateHijriOffset(offset: Int) {
        ensureSettingsRowExists()
        userSettingsDao.updateHijriOffset(offset)
    }
    suspend fun updateLanguage(lang: String) {
        ensureSettingsRowExists()
        userSettingsDao.updateLanguage(lang)
    }
    suspend fun updateFirstDayOfWeek(firstDay: Int) {
        ensureSettingsRowExists()
        userSettingsDao.updateFirstDayOfWeek(firstDay)
    }
    suspend fun updateDarkMode(mode: String) {
        ensureSettingsRowExists()
        userSettingsDao.updateDarkMode(mode)
    }
    suspend fun updateHolidayReminderEnabled(enabled: Boolean) {
        ensureSettingsRowExists()
        userSettingsDao.updateHolidayReminderEnabled(enabled)
    }

    suspend fun insertEvent(event: PersonalEventEntity): Long = eventDao.insertEvent(event)
    suspend fun updateEvent(event: PersonalEventEntity) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: PersonalEventEntity) = eventDao.deleteEvent(event)
    suspend fun deleteEventById(id: Long) = eventDao.deleteEventById(id)

    fun searchEvents(query: String): Flow<List<PersonalEventEntity>> = eventDao.searchEvents(query)

    fun getCountryHolidays(countryCode: String, year: Int, hijriOffset: Int = 0): List<Holiday> {
        return ArabHolidaysProvider.getHolidays(countryCode, year, hijriOffset)
    }

    fun getIslamicEvents(year: Int): List<IslamicEvent> {
        return IslamicEventsProvider.events
    }

    /**
     * Generates a complete month calendar matrix for the given YearMonth,
     * country weekend rules, and user manual Hijri offset.
     */
    fun getMonthDays(
        yearMonth: YearMonth,
        selectedDate: LocalDate,
        country: ArabCountry,
        hijriOffset: Int,
        firstDayOfWeek: DayOfWeek,
        personalEvents: List<PersonalEventEntity>
    ): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val today = LocalDate.now()
        val prevMonth = yearMonth.minusMonths(1)
        val nextMonth = yearMonth.plusMonths(1)
        val yearsToLoad = setOf(prevMonth.year, yearMonth.year, nextMonth.year)
        val holidaysInYear = yearsToLoad.flatMap { y -> getCountryHolidays(country.code, y, hijriOffset) }

        val firstOfMonth = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()

        // Calculate leading padding days based on firstDayOfWeek
        val firstDayVal = firstOfMonth.dayOfWeek.value // Mon=1..Sun=7
        val targetFirstVal = firstDayOfWeek.value
        val leadingDaysCount = (firstDayVal - targetFirstVal + 7) % 7

        val daysInPrevMonth = prevMonth.lengthOfMonth()

        // Add leading days from previous month
        for (i in leadingDaysCount downTo 1) {
            val date = prevMonth.atDay(daysInPrevMonth - i + 1)
            val hijri = HijriCalendarEngine.toHijri(date, hijriOffset)
            val dateHolidays = holidaysInYear.filter { it.date == date }
            val dateIslamic = IslamicEventsProvider.getEventsForDate(date, date.year)
            val pCount = personalEvents.count { it.gregorianDate == date.toString() }

            days.add(
                CalendarDay(
                    date = date,
                    hijriDate = hijri,
                    isCurrentMonth = false,
                    isToday = date == today,
                    isSelected = date == selectedDate,
                    isFriday = date.dayOfWeek == DayOfWeek.FRIDAY,
                    isWeekend = country.isWeekend(date.dayOfWeek),
                    holidays = dateHolidays,
                    islamicEvents = dateIslamic,
                    personalEventsCount = pCount
                )
            )
        }

        // Add days of current month
        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            val hijri = HijriCalendarEngine.toHijri(date, hijriOffset)
            val dateHolidays = holidaysInYear.filter { it.date == date }
            val dateIslamic = IslamicEventsProvider.getEventsForDate(date, date.year)
            val pCount = personalEvents.count { it.gregorianDate == date.toString() }

            days.add(
                CalendarDay(
                    date = date,
                    hijriDate = hijri,
                    isCurrentMonth = true,
                    isToday = date == today,
                    isSelected = date == selectedDate,
                    isFriday = date.dayOfWeek == DayOfWeek.FRIDAY,
                    isWeekend = country.isWeekend(date.dayOfWeek),
                    holidays = dateHolidays,
                    islamicEvents = dateIslamic,
                    personalEventsCount = pCount
                )
            )
        }

        // Add trailing days to complete full grid (multiple of 7)
        val remaining = (7 - (days.size % 7)) % 7
        for (day in 1..remaining) {
            val date = nextMonth.atDay(day)
            val hijri = HijriCalendarEngine.toHijri(date, hijriOffset)
            val dateHolidays = holidaysInYear.filter { it.date == date }
            val dateIslamic = IslamicEventsProvider.getEventsForDate(date, date.year)
            val pCount = personalEvents.count { it.gregorianDate == date.toString() }

            days.add(
                CalendarDay(
                    date = date,
                    hijriDate = hijri,
                    isCurrentMonth = false,
                    isToday = date == today,
                    isSelected = date == selectedDate,
                    isFriday = date.dayOfWeek == DayOfWeek.FRIDAY,
                    isWeekend = country.isWeekend(date.dayOfWeek),
                    holidays = dateHolidays,
                    islamicEvents = dateIslamic,
                    personalEventsCount = pCount
                )
            )
        }

        return days
    }

    /**
     * Converts Gregorian date to Hijri with complete context
     */
    fun convertGregorianToHijri(
        date: LocalDate,
        countryCode: String,
        offset: Int
    ): DateConversionResult {
        val hijri = HijriCalendarEngine.toHijri(date, offset)
        val holidays = getCountryHolidays(countryCode, date.year, offset).filter { it.date == date }
        val islamic = IslamicEventsProvider.getEventsForDate(date, date.year)

        return DateConversionResult(
            gregorianDate = date,
            hijriDate = hijri,
            dayOfWeekEn = HijriCalendarEngine.getWeekdayName(date.dayOfWeek, false),
            dayOfWeekAr = HijriCalendarEngine.getWeekdayName(date.dayOfWeek, true),
            islamicEvents = islamic,
            holidays = holidays
        )
    }

    /**
     * Converts Hijri date to Gregorian with complete context
     */
    fun convertHijriToGregorian(
        hijriYear: Int,
        hijriMonth: Int,
        hijriDay: Int,
        countryCode: String,
        offset: Int
    ): DateConversionResult {
        val gregorian = HijriCalendarEngine.toGregorian(hijriYear, hijriMonth, hijriDay, offset)
        val hijri = HijriCalendarEngine.toHijri(gregorian, offset)
        val holidays = getCountryHolidays(countryCode, gregorian.year, offset).filter { it.date == gregorian }
        val islamic = IslamicEventsProvider.getEventsForDate(gregorian, gregorian.year)

        return DateConversionResult(
            gregorianDate = gregorian,
            hijriDate = hijri,
            dayOfWeekEn = HijriCalendarEngine.getWeekdayName(gregorian.dayOfWeek, false),
            dayOfWeekAr = HijriCalendarEngine.getWeekdayName(gregorian.dayOfWeek, true),
            islamicEvents = islamic,
            holidays = holidays
        )
    }
}
