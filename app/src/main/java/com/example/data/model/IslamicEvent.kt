package com.example.data.model

import java.time.LocalDate

data class IslamicEvent(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val hijriMonth: Int, // 1 = Muharram .. 12 = Dhu al-Hijjah
    val hijriDay: Int,
    val approxDate2026: LocalDate,
    val approxDate2027: LocalDate,
    val approxDate2028: LocalDate,
    val isMajorCountdown: Boolean = false,
    val descriptionEn: String = "",
    val descriptionAr: String = ""
) {
    fun getDateForYear(gregorianYear: Int): LocalDate {
        return when (gregorianYear) {
            2026 -> approxDate2026
            2027 -> approxDate2027
            2028 -> approxDate2028
            else -> {
                // Approximate progression: Hijri year is ~11 days shorter than Gregorian
                val diff = gregorianYear - 2027
                approxDate2027.plusDays((diff * 354.367).toLong())
            }
        }
    }
}
