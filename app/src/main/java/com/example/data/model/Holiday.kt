package com.example.data.model

import java.time.LocalDate

enum class HolidayType {
    NATIONAL,
    ISLAMIC,
    OBSERVANCE
}

data class Holiday(
    val id: String,
    val countryCode: String, // Country code or "ALL" for all Arab countries
    val titleEn: String,
    val titleAr: String,
    val date: LocalDate,
    val hijriEquivalent: String = "",
    val type: HolidayType = HolidayType.NATIONAL,
    val isTentative: Boolean = false,
    val descriptionEn: String = "",
    val descriptionAr: String = ""
)
