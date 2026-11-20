package com.example

import com.example.calendar.ArabHolidaysProvider
import com.example.calendar.HijriCalendarEngine
import com.example.calendar.IslamicEventsProvider
import com.example.data.model.ArabCountry
import com.example.data.model.HolidayType
import com.example.notifications.HolidayReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class ExampleUnitTest {

    @Test
    fun testAll22ArabCountriesExist() {
        assertEquals(22, ArabCountry.entries.size)
        assertNotNull(ArabCountry.fromCode("SA"))
        assertNotNull(ArabCountry.fromCode("EG"))
        assertNotNull(ArabCountry.fromCode("AE"))
        assertNotNull(ArabCountry.fromCode("MA"))
    }

    @Test
    fun testHijriDateConversionAccuracy() {
        // 2027-02-08 is estimated 1st Ramadan 1448 AH
        val date2027 = LocalDate.of(2027, 2, 8)
        val hijri = HijriCalendarEngine.toHijri(date2027, 0)
        assertTrue(hijri.year in 1448..1449)
        assertTrue(hijri.month in 1..12)
        assertTrue(hijri.day in 1..30)
    }

    @Test
    fun testIslamicEventsFor2027() {
        val ramadan = IslamicEventsProvider.events.first { it.id == "ramadan_start" }
        val date = ramadan.getDateForYear(2027)
        assertEquals(LocalDate.of(2027, 2, 8), date)

        val eidFitr = IslamicEventsProvider.events.first { it.id == "eid_al_fitr" }
        assertEquals(LocalDate.of(2027, 3, 10), eidFitr.getDateForYear(2027))
    }

    @Test
    fun testAll22CountriesHolidayDataAcrossYears() {
        val years = listOf(2026, 2027, 2028, 2029, 2030)
        for (country in ArabCountry.entries) {
            for (year in years) {
                val holidays = ArabHolidaysProvider.getHolidays(country.code, year)
                assertTrue(
                    "Expected holidays for ${country.code} in $year",
                    holidays.size >= 5
                )
                assertTrue(
                    "All holiday dates should match requested year $year for ${country.code}",
                    holidays.all { it.date.year == year }
                )
                assertTrue(
                    "Every country should have Eid al-Fitr and Eid al-Adha in $year",
                    holidays.any { it.id.startsWith("is_eid_fitr") } &&
                        holidays.any { it.id.startsWith("is_eid_adha") }
                )
            }
        }
    }

    @Test
    fun testCountrySpecificHolidayAccuracy() {
        val saudiHolidays = ArabHolidaysProvider.getHolidays("SA", 2027)
        assertTrue(saudiHolidays.any { it.id == "sa_founding" && it.date == LocalDate.of(2027, 2, 22) })
        assertTrue(saudiHolidays.any { it.id == "sa_national" && it.date == LocalDate.of(2027, 9, 23) })
        assertTrue(saudiHolidays.none { it.date == LocalDate.of(2027, 1, 1) })
        assertTrue(saudiHolidays.any { it.id.startsWith("is_eid_fitr_4") })

        val qatar2026 = ArabHolidaysProvider.getHolidays("QA", 2026)
        val sportsDay2026 = qatar2026.first { it.id == "qa_sports" }
        assertEquals(DayOfWeek.TUESDAY, sportsDay2026.date.dayOfWeek)
        assertEquals(LocalDate.of(2026, 2, 10), sportsDay2026.date)

        val tunisia2027 = ArabHolidaysProvider.getHolidays("TN", 2027)
        assertTrue(tunisia2027.any { it.id == "tn_revolution" && it.date == LocalDate.of(2027, 12, 17) })

        val oman2027 = ArabHolidaysProvider.getHolidays("OM", 2027)
        assertTrue(oman2027.any { it.id == "om_accession" && it.date == LocalDate.of(2027, 1, 11) })
        assertTrue(oman2027.any { it.id == "om_national" && it.date == LocalDate.of(2027, 11, 18) })

        val egypt2027 = ArabHolidaysProvider.getHolidays("EG", 2027)
        assertTrue(egypt2027.any { it.id == "eg_coptic_easter" && it.date == LocalDate.of(2027, 5, 2) })
        assertTrue(egypt2027.any { it.id == "eg_sham" && it.date == LocalDate.of(2027, 5, 3) })

        val lebanon2027 = ArabHolidaysProvider.getHolidays("LB", 2027)
        assertTrue(lebanon2027.any { it.id == "lb_w_good_friday" && it.date == LocalDate.of(2027, 3, 26) })
        assertTrue(lebanon2027.any { it.id == "lb_w_easter" && it.date == LocalDate.of(2027, 3, 28) })
        assertTrue(lebanon2027.any { it.id == "lb_o_easter" && it.date == LocalDate.of(2027, 5, 2) })

        val iraq2027 = ArabHolidaysProvider.getHolidays("IQ", 2027)
        assertTrue(iraq2027.any { it.id.startsWith("is_ghadeer") })
        assertTrue(iraq2027.any { it.id == "iq_nowruz" && it.date == LocalDate.of(2027, 3, 21) })

        val kuwait2027 = ArabHolidaysProvider.getHolidays("KW", 2027)
        assertTrue(kuwait2027.any { it.id.startsWith("is_isra_miraj") })
    }

    @Test
    fun testOneDayBeforeNationalHolidayReminders() {
        val now = ZonedDateTime.of(2027, 2, 1, 8, 0, 0, 0, ZoneId.of("UTC"))
        val saTargets = HolidayReminderScheduler.getUpcomingReminderTargets("SA", 0, now)
        assertTrue(saTargets.isNotEmpty())
        assertTrue(saTargets.all { it.holiday.type == HolidayType.NATIONAL })
        assertTrue(saTargets.all { it.reminderDate == it.holiday.date.minusDays(1) })
        // First upcoming SA national holiday after Feb 1, 2027 is Saudi Founding Day on Feb 22 -> reminder Feb 21
        assertEquals(LocalDate.of(2027, 2, 22), saTargets.first().holiday.date)
        assertEquals(LocalDate.of(2027, 2, 21), saTargets.first().reminderDate)

        // Cross-year boundary check: Dec 31 afternoon for country with Jan 1 holiday (e.g., AE)
        val dec31 = ZonedDateTime.of(2027, 12, 31, 8, 0, 0, 0, ZoneId.of("UTC"))
        val aeTargets = HolidayReminderScheduler.getUpcomingReminderTargets("AE", 0, dec31)
        assertEquals(LocalDate.of(2028, 1, 1), aeTargets.first().holiday.date)
        assertEquals(LocalDate.of(2027, 12, 31), aeTargets.first().reminderDate)

        // Post-09:00 AM edge case: on Dec 31 at 16:00, tomorrow's Jan 1 holiday reminder is still scheduled
        val dec31Afternoon = ZonedDateTime.of(2027, 12, 31, 16, 0, 0, 0, ZoneId.of("UTC"))
        val afternoonTargets = HolidayReminderScheduler.getUpcomingReminderTargets("AE", 0, dec31Afternoon)
        assertEquals(LocalDate.of(2028, 1, 1), afternoonTargets.first().holiday.date)
        assertTrue(afternoonTargets.first().triggerAtMillis > dec31Afternoon.toInstant().toEpochMilli())
    }
}
