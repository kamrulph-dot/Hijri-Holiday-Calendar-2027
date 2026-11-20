package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendar.ArabHolidaysProvider
import com.example.calendar.HijriCalendarEngine
import com.example.calendar.IslamicEventsProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PersonalEventEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.local.repository.CalendarRepository
import com.example.data.model.ArabCountry
import com.example.data.model.CalendarDay
import com.example.data.model.DateConversionResult
import com.example.data.model.Holiday
import com.example.data.model.IslamicEvent
import com.example.notifications.HolidayReminderScheduler
import com.example.notifications.HolidayReminderTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class CalendarUiState(
    val currentYearMonth: YearMonth = YearMonth.of(2027, 1),
    val selectedDate: LocalDate = LocalDate.of(2027, 1, 1),
    val selectedCountry: ArabCountry = ArabCountry.SAUDI_ARABIA,
    val hijriOffset: Int = 0,
    val languageCode: String = "en",
    val firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    val darkMode: String = "system",
    val calendarViewMode: String = "month", // "month" or "year"
    val daysMatrix: List<CalendarDay> = emptyList(),
    val personalEventsForSelectedDate: List<PersonalEventEntity> = emptyList(),
    val holidaysForSelectedDate: List<Holiday> = emptyList(),
    val islamicEventsForSelectedDate: List<IslamicEvent> = emptyList(),
    val daysUntilRamadan: Long = 0,
    val daysUntilEidFitr: Long = 0,
    val daysUntilEidAdha: Long = 0,
    val ramadanDate: LocalDate = LocalDate.of(2027, 2, 8),
    val eidFitrDate: LocalDate = LocalDate.of(2027, 3, 10),
    val eidAdhaDate: LocalDate = LocalDate.of(2027, 5, 17),
    val nextHoliday: Holiday? = null,
    val daysUntilNextHoliday: Long = 0,
    val searchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    val conversionResult: DateConversionResult? = null,
    val holidayReminderEnabled: Boolean = false,
    val nextHolidayReminder: HolidayReminderTarget? = null
) {
    val isArabic: Boolean get() = languageCode == "ar"
}

data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val date: LocalDate,
    val type: String, // "Islamic", "Holiday", "Personal"
    val isTentative: Boolean = false
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository

    private val _currentYearMonth = MutableStateFlow(YearMonth.of(2027, 1))
    private val _selectedDate = MutableStateFlow(LocalDate.of(2027, 1, 1))
    private val _selectedCountry = MutableStateFlow(ArabCountry.SAUDI_ARABIA)
    private val _hijriOffset = MutableStateFlow(0)
    private val _languageCode = MutableStateFlow("en")
    private val _firstDayOfWeek = MutableStateFlow(DayOfWeek.SATURDAY)
    private val _darkMode = MutableStateFlow("system")
    private val _calendarViewMode = MutableStateFlow("month")
    private val _searchQuery = MutableStateFlow("")
    private val _conversionResult = MutableStateFlow<DateConversionResult?>(null)
    private val _holidayReminderEnabled = MutableStateFlow(false)

    val personalEvents: StateFlow<List<PersonalEventEntity>>

    val uiState: StateFlow<CalendarUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CalendarRepository(db.eventDao(), db.userSettingsDao())

        personalEvents = repository.allEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Observe stored settings
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                if (settings != null) {
                    _selectedCountry.value = ArabCountry.fromCode(settings.selectedCountryCode)
                    _hijriOffset.value = settings.hijriOffset
                    _languageCode.value = settings.languageCode
                    _firstDayOfWeek.value = when (settings.firstDayOfWeek) {
                        7 -> DayOfWeek.SUNDAY
                        1 -> DayOfWeek.MONDAY
                        else -> DayOfWeek.SATURDAY
                    }
                    _darkMode.value = settings.darkMode
                    _holidayReminderEnabled.value = settings.holidayReminderEnabled
                    HolidayReminderScheduler.syncAndSchedule(
                        context = getApplication(),
                        enabled = settings.holidayReminderEnabled,
                        countryCode = settings.selectedCountryCode,
                        hijriOffset = settings.hijriOffset,
                        languageCode = settings.languageCode
                    )
                } else {
                    // Seed initial settings safely without overwriting concurrent user actions
                    repository.insertDefaultIfMissing(
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
        }

        // Initialize default conversion
        _conversionResult.value = repository.convertGregorianToHijri(
            LocalDate.of(2027, 1, 1),
            "SA",
            0
        )

        uiState = combine(
            _currentYearMonth,
            _selectedDate,
            _selectedCountry,
            _hijriOffset,
            _languageCode,
            _firstDayOfWeek,
            _darkMode,
            _calendarViewMode,
            personalEvents,
            _searchQuery,
            _conversionResult,
            _holidayReminderEnabled
        ) { args ->
            val ym = args[0] as YearMonth
            val selDate = args[1] as LocalDate
            val country = args[2] as ArabCountry
            val offset = args[3] as Int
            val lang = args[4] as String
            val fDay = args[5] as DayOfWeek
            val dark = args[6] as String
            val vMode = args[7] as String
            val pEvents = args[8] as List<PersonalEventEntity>
            val query = args[9] as String
            val conv = args[10] as DateConversionResult?
            val reminderEnabled = args[11] as Boolean

            val matrix = repository.getMonthDays(
                yearMonth = ym,
                selectedDate = selDate,
                country = country,
                hijriOffset = offset,
                firstDayOfWeek = fDay,
                personalEvents = pEvents
            )

            val pEventsForDate = pEvents.filter { it.gregorianDate == selDate.toString() }
            val holidaysForYear = repository.getCountryHolidays(country.code, selDate.year, offset)
            val holidaysForDate = holidaysForYear.filter { it.date == selDate }
            val islamicForDate = IslamicEventsProvider.getEventsForDate(selDate, selDate.year)

            // Compute countdowns for 2027+ and automatically roll over passed events to next year
            val refDate = LocalDate.now()
            val targetYear = if (refDate.year >= 2027) refDate.year else 2027

            fun nextIslamicEventDate(eventId: String): LocalDate {
                val ev = IslamicEventsProvider.events.first { it.id == eventId }
                val thisYearDate = ev.getDateForYear(targetYear)
                return if (thisYearDate >= refDate) thisYearDate else ev.getDateForYear(targetYear + 1)
            }

            val ramadanDate = nextIslamicEventDate("ramadan_start")
            val daysRamadan = ChronoUnit.DAYS.between(refDate, ramadanDate)

            val eidFitrDate = nextIslamicEventDate("eid_al_fitr")
            val daysEidFitr = ChronoUnit.DAYS.between(refDate, eidFitrDate)

            val eidAdhaDate = nextIslamicEventDate("eid_al_adha")
            val daysEidAdha = ChronoUnit.DAYS.between(refDate, eidAdhaDate)

            // Next upcoming public holiday across targetYear and targetYear + 1
            val allHolidays = repository.getCountryHolidays(country.code, targetYear, offset) +
                repository.getCountryHolidays(country.code, targetYear + 1, offset)
            val nextHol = allHolidays.firstOrNull { it.date >= refDate } ?: allHolidays.firstOrNull()
            val daysNextHol = if (nextHol != null) ChronoUnit.DAYS.between(refDate, nextHol.date) else 0L

            // Search results
            val searchList = if (query.isNotBlank()) {
                buildSearchResults(query, country, ym.year, offset, pEvents, lang == "ar")
            } else {
                emptyList()
            }

            val nextReminder = HolidayReminderScheduler.getNextUpcomingTarget(country.code, offset)

            CalendarUiState(
                currentYearMonth = ym,
                selectedDate = selDate,
                selectedCountry = country,
                hijriOffset = offset,
                languageCode = lang,
                firstDayOfWeek = fDay,
                darkMode = dark,
                calendarViewMode = vMode,
                daysMatrix = matrix,
                personalEventsForSelectedDate = pEventsForDate,
                holidaysForSelectedDate = holidaysForDate,
                islamicEventsForSelectedDate = islamicForDate,
                daysUntilRamadan = daysRamadan,
                daysUntilEidFitr = daysEidFitr,
                daysUntilEidAdha = daysEidAdha,
                ramadanDate = ramadanDate,
                eidFitrDate = eidFitrDate,
                eidAdhaDate = eidAdhaDate,
                nextHoliday = nextHol,
                daysUntilNextHoliday = daysNextHol,
                searchQuery = query,
                searchResults = searchList,
                conversionResult = conv,
                holidayReminderEnabled = reminderEnabled,
                nextHolidayReminder = nextReminder
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalendarUiState()
        )
    }

    private fun buildSearchResults(
        query: String,
        country: ArabCountry,
        year: Int,
        hijriOffset: Int,
        personalEvents: List<PersonalEventEntity>,
        isArabic: Boolean
    ): List<SearchResultItem> {
        val q = query.trim().lowercase()
        val results = mutableListOf<SearchResultItem>()

        // 1. Islamic Events
        IslamicEventsProvider.events.forEach { event ->
            val title = if (isArabic) event.titleAr else event.titleEn
            val desc = if (isArabic) event.descriptionAr else event.descriptionEn
            if (title.lowercase().contains(q) || desc.lowercase().contains(q)) {
                results.add(
                    SearchResultItem(
                        title = title,
                        subtitle = "${HijriCalendarEngine.HIJRI_MONTHS_EN[event.hijriMonth - 1]} ${event.hijriDay}",
                        date = event.getDateForYear(year),
                        type = "Islamic",
                        isTentative = true
                    )
                )
            }
        }

        // 2. Country Holidays
        val holidays = repository.getCountryHolidays(country.code, year, hijriOffset)
        holidays.forEach { hol ->
            val title = if (isArabic) hol.titleAr else hol.titleEn
            val desc = if (isArabic) hol.descriptionAr else hol.descriptionEn
            if (title.lowercase().contains(q) || desc.lowercase().contains(q)) {
                results.add(
                    SearchResultItem(
                        title = title,
                        subtitle = hol.hijriEquivalent.ifEmpty { hol.date.toString() },
                        date = hol.date,
                        type = "Holiday",
                        isTentative = hol.isTentative
                    )
                )
            }
        }

        // 3. Personal Events
        personalEvents.forEach { pe ->
            if (pe.title.lowercase().contains(q) || pe.description.lowercase().contains(q)) {
                results.add(
                    SearchResultItem(
                        title = pe.title,
                        subtitle = "${pe.category} • ${pe.time.ifEmpty { "All day" }}",
                        date = try { LocalDate.parse(pe.gregorianDate) } catch (_: Exception) { LocalDate.now() },
                        type = "Personal",
                        isTentative = false
                    )
                )
            }
        }

        return results
    }

    // Actions
    fun setYear(year: Int) {
        _currentYearMonth.value = YearMonth.of(year, _currentYearMonth.value.monthValue)
    }

    fun setMonth(month: Int) {
        _currentYearMonth.value = YearMonth.of(_currentYearMonth.value.year, month.coerceIn(1, 12))
    }

    fun nextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        if (YearMonth.from(date) != _currentYearMonth.value) {
            _currentYearMonth.value = YearMonth.from(date)
        }
    }

    fun jumpToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _currentYearMonth.value = YearMonth.from(today)
    }

    fun jumpTo2027() {
        val launchDate = LocalDate.of(2027, 1, 1)
        _selectedDate.value = launchDate
        _currentYearMonth.value = YearMonth.of(2027, 1)
    }

    fun setCountry(country: ArabCountry) {
        _selectedCountry.value = country
        viewModelScope.launch {
            repository.updateCountry(country.code)
            HolidayReminderScheduler.syncAndSchedule(
                context = getApplication(),
                enabled = _holidayReminderEnabled.value,
                countryCode = country.code,
                hijriOffset = _hijriOffset.value,
                languageCode = _languageCode.value
            )
        }
    }

    fun setHijriOffset(offset: Int) {
        val clamped = offset.coerceIn(-2, 2)
        _hijriOffset.value = clamped
        viewModelScope.launch {
            repository.updateHijriOffset(clamped)
            HolidayReminderScheduler.syncAndSchedule(
                context = getApplication(),
                enabled = _holidayReminderEnabled.value,
                countryCode = _selectedCountry.value.code,
                hijriOffset = clamped,
                languageCode = _languageCode.value
            )
        }
    }

    fun setLanguage(lang: String) {
        _languageCode.value = lang
        viewModelScope.launch {
            repository.updateLanguage(lang)
            HolidayReminderScheduler.syncAndSchedule(
                context = getApplication(),
                enabled = _holidayReminderEnabled.value,
                countryCode = _selectedCountry.value.code,
                hijriOffset = _hijriOffset.value,
                languageCode = lang
            )
        }
    }

    fun setFirstDayOfWeek(day: DayOfWeek) {
        _firstDayOfWeek.value = day
        viewModelScope.launch {
            val code = when (day) {
                DayOfWeek.SUNDAY -> 7
                DayOfWeek.MONDAY -> 1
                else -> 6
            }
            repository.updateFirstDayOfWeek(code)
        }
    }

    fun setDarkMode(mode: String) {
        _darkMode.value = mode
        viewModelScope.launch {
            repository.updateDarkMode(mode)
        }
    }

    fun setHolidayReminderEnabled(enabled: Boolean) {
        _holidayReminderEnabled.value = enabled
        viewModelScope.launch {
            repository.updateHolidayReminderEnabled(enabled)
            HolidayReminderScheduler.syncAndSchedule(
                context = getApplication(),
                enabled = enabled,
                countryCode = _selectedCountry.value.code,
                hijriOffset = _hijriOffset.value,
                languageCode = _languageCode.value
            )
        }
    }

    fun setCalendarViewMode(mode: String) {
        _calendarViewMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addPersonalEvent(
        title: String,
        description: String,
        date: LocalDate,
        time: String,
        category: String,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            val hijri = HijriCalendarEngine.toHijri(date, _hijriOffset.value)
            repository.insertEvent(
                PersonalEventEntity(
                    title = title,
                    description = description,
                    gregorianDate = date.toString(),
                    hijriDate = hijri.formattedEn,
                    time = time,
                    category = category,
                    isFavorite = isFavorite
                )
            )
        }
    }

    fun updatePersonalEvent(event: PersonalEventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deletePersonalEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteEventById(id)
        }
    }

    fun toggleFavoriteEvent(event: PersonalEventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event.copy(isFavorite = !event.isFavorite))
        }
    }

    fun convertGregorianDate(date: LocalDate) {
        _conversionResult.value = repository.convertGregorianToHijri(
            date = date,
            countryCode = _selectedCountry.value.code,
            offset = _hijriOffset.value
        )
    }

    fun convertHijriDate(year: Int, month: Int, day: Int) {
        _conversionResult.value = repository.convertHijriToGregorian(
            hijriYear = year,
            hijriMonth = month,
            hijriDay = day,
            countryCode = _selectedCountry.value.code,
            offset = _hijriOffset.value
        )
    }
}
