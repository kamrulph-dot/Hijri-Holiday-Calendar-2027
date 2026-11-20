package com.example.calendar

import com.example.data.model.HijriDateModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import kotlin.math.floor

object HijriCalendarEngine {

    val HIJRI_MONTHS_EN = listOf(
        "Muharram",
        "Safar",
        "Rabi' al-Awwal",
        "Rabi' al-Thani",
        "Jumada al-Ula",
        "Jumada al-Akhirah",
        "Rajab",
        "Sha'ban",
        "Ramadan",
        "Shawwal",
        "Dhu al-Qi'dah",
        "Dhu al-Hijjah"
    )

    val HIJRI_MONTHS_AR = listOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الثاني",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة"
    )

    val GREGORIAN_MONTHS_EN = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val GREGORIAN_MONTHS_AR = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    fun getWeekdayName(dayOfWeek: DayOfWeek, isArabic: Boolean): String {
        return if (isArabic) {
            when (dayOfWeek) {
                DayOfWeek.SATURDAY -> "السبت"
                DayOfWeek.SUNDAY -> "الأحد"
                DayOfWeek.MONDAY -> "الإثنين"
                DayOfWeek.TUESDAY -> "الثلاثاء"
                DayOfWeek.WEDNESDAY -> "الأربعاء"
                DayOfWeek.THURSDAY -> "الخميس"
                DayOfWeek.FRIDAY -> "الجمعة"
            }
        } else {
            dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    fun getWeekdayShortName(dayOfWeek: DayOfWeek, isArabic: Boolean): String {
        return if (isArabic) {
            when (dayOfWeek) {
                DayOfWeek.SATURDAY -> "سبت"
                DayOfWeek.SUNDAY -> "أحد"
                DayOfWeek.MONDAY -> "إثنين"
                DayOfWeek.TUESDAY -> "ثلاثاء"
                DayOfWeek.WEDNESDAY -> "أربعاء"
                DayOfWeek.THURSDAY -> "خميس"
                DayOfWeek.FRIDAY -> "جمعة"
            }
        } else {
            when (dayOfWeek) {
                DayOfWeek.SATURDAY -> "Sat"
                DayOfWeek.SUNDAY -> "Sun"
                DayOfWeek.MONDAY -> "Mon"
                DayOfWeek.TUESDAY -> "Tue"
                DayOfWeek.WEDNESDAY -> "Wed"
                DayOfWeek.THURSDAY -> "Thu"
                DayOfWeek.FRIDAY -> "Fri"
            }
        }
    }

    /**
     * Converts a Gregorian LocalDate to HijriDateModel using Umm al-Qura chronology
     * with an applied manual offset (e.g. -2 to +2 days).
     */
    fun toHijri(gregorianDate: LocalDate, manualOffsetDays: Int = 0): HijriDateModel {
        val effectiveDate = if (manualOffsetDays != 0) {
            gregorianDate.plusDays(manualOffsetDays.toLong())
        } else {
            gregorianDate
        }

        return try {
            val hijrahDate = HijrahChronology.INSTANCE.date(effectiveDate)
            val hYear = hijrahDate.get(ChronoField.YEAR)
            val hMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val hDay = hijrahDate.get(ChronoField.DAY_OF_MONTH)

            val monthIndex = (hMonth - 1).coerceIn(0, 11)
            val monthNameEn = HIJRI_MONTHS_EN[monthIndex]
            val monthNameAr = HIJRI_MONTHS_AR[monthIndex]

            HijriDateModel(
                year = hYear,
                month = hMonth,
                day = hDay,
                monthNameEn = monthNameEn,
                monthNameAr = monthNameAr,
                formattedEn = "$hDay $monthNameEn $hYear AH",
                formattedAr = "$hDay $monthNameAr $hYear هـ"
            )
        } catch (_: Exception) {
            // Robust algorithmic fallback (Kuwaiti / astronomical tabular algorithm)
            fallbackKuwaitiAlgorithm(effectiveDate)
        }
    }

    /**
     * Converts a Hijri date back to Gregorian LocalDate.
     */
    fun toGregorian(hijriYear: Int, hijriMonth: Int, hijriDay: Int, manualOffsetDays: Int = 0): LocalDate {
        return try {
            val hijrahDate = HijrahChronology.INSTANCE.date(hijriYear, hijriMonth.coerceIn(1, 12), hijriDay.coerceIn(1, 30))
            val baseGregorian = LocalDate.from(hijrahDate)
            if (manualOffsetDays != 0) {
                baseGregorian.minusDays(manualOffsetDays.toLong())
            } else {
                baseGregorian
            }
        } catch (_: Exception) {
            // Algorithmic reverse conversion
            val jd = hijriToJulianDay(hijriYear, hijriMonth, hijriDay)
            val converted = julianDayToLocalDate(jd)
            if (manualOffsetDays != 0) converted.minusDays(manualOffsetDays.toLong()) else converted
        }
    }

    /**
     * High precision astronomical fallback calculation for Hijri dates
     */
    private fun fallbackKuwaitiAlgorithm(date: LocalDate): HijriDateModel {
        val jd = localDateToJulianDay(date)
        val l = jd.toLong() - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        val l2 = l - 10631 * n + 354
        val j = (((10985 - l2) / 5316).toInt()) * (((50 * l2) / 17719).toInt()) +
                ((l2 / 5670).toInt()) * (((43 * l2) / 15238).toInt())
        val l3 = l2 - (((30 - j) / 15).toInt()) * (((17719 * j) / 50).toInt()) -
                ((j / 16).toInt()) * (((15238 * j) / 43).toInt()) + 29
        val m = ((24 * l3) / 709).toInt()
        val d = (l3 - ((709 * m) / 24)).toInt().coerceIn(1, 30)
        val y = (30 * n + j - 30).coerceAtLeast(1)

        val monthIndex = (m - 1).coerceIn(0, 11)
        val monthNameEn = HIJRI_MONTHS_EN[monthIndex]
        val monthNameAr = HIJRI_MONTHS_AR[monthIndex]

        return HijriDateModel(
            year = y,
            month = m,
            day = d,
            monthNameEn = monthNameEn,
            monthNameAr = monthNameAr,
            formattedEn = "$d $monthNameEn $y AH",
            formattedAr = "$d $monthNameAr $y هـ"
        )
    }

    private fun localDateToJulianDay(date: LocalDate): Double {
        var year = date.year
        var month = date.monthValue
        val day = date.dayOfMonth

        if (month <= 2) {
            year -= 1
            month += 12
        }

        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)

        return floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + b - 1524.5
    }

    private fun hijriToJulianDay(year: Int, month: Int, day: Int): Double {
        return floor((11.0 * year + 3.0) / 30.0) + 354.0 * year + 30.0 * month -
                floor((month - 1.0) / 2.0) + day + 1948440.0 - 385.0
    }

    private fun julianDayToLocalDate(jd: Double): LocalDate {
        val z = floor(jd + 0.5).toLong()
        val a = if (z < 2299161) {
            z
        } else {
            val alpha = floor((z - 1867216.25) / 36524.25).toLong()
            z + 1 + alpha - floor(alpha / 4.0).toLong()
        }
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toLong()
        val d = floor(365.25 * c).toLong()
        val e = floor((b - d) / 30.6001).toLong()

        val day = (b - d - floor(30.6001 * e).toLong()).toInt()
        val month = (if (e < 14) e - 1 else e - 13).toInt()
        val year = (if (month > 2) c - 4716 else c - 4715).toInt()

        return LocalDate.of(year, month, day)
    }
}
