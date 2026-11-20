package com.example.data.model

import java.time.DayOfWeek

enum class ArabCountry(
    val code: String,
    val nameEn: String,
    val nameAr: String,
    val flag: String,
    val defaultHijriOffset: Int = 0,
    val weekendDays: Set<DayOfWeek> = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
    val capitalEn: String,
    val capitalAr: String
) {
    SAUDI_ARABIA(
        code = "SA",
        nameEn = "Saudi Arabia",
        nameAr = "المملكة العربية السعودية",
        flag = "🇸🇦",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Riyadh",
        capitalAr = "الرياض"
    ),
    UAE(
        code = "AE",
        nameEn = "United Arab Emirates",
        nameAr = "الإمارات العربية المتحدة",
        flag = "🇦🇪",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), // UAE public sector shifted to Sat-Sun in 2022
        capitalEn = "Abu Dhabi",
        capitalAr = "أبوظبي"
    ),
    QATAR(
        code = "QA",
        nameEn = "Qatar",
        nameAr = "قطر",
        flag = "🇶🇦",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Doha",
        capitalAr = "الدوحة"
    ),
    KUWAIT(
        code = "KW",
        nameEn = "Kuwait",
        nameAr = "الكويت",
        flag = "🇰🇼",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Kuwait City",
        capitalAr = "مدينة الكويت"
    ),
    BAHRAIN(
        code = "BH",
        nameEn = "Bahrain",
        nameAr = "البحرين",
        flag = "🇧🇭",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Manama",
        capitalAr = "المنامة"
    ),
    OMAN(
        code = "OM",
        nameEn = "Oman",
        nameAr = "سلطنة عمان",
        flag = "🇴🇲",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Muscat",
        capitalAr = "مسقط"
    ),
    YEMEN(
        code = "YE",
        nameEn = "Yemen",
        nameAr = "اليمن",
        flag = "🇾🇪",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Sana'a",
        capitalAr = "صنعاء"
    ),
    IRAQ(
        code = "IQ",
        nameEn = "Iraq",
        nameAr = "العراق",
        flag = "🇮🇶",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Baghdad",
        capitalAr = "بغداد"
    ),
    JORDAN(
        code = "JO",
        nameEn = "Jordan",
        nameAr = "الأردن",
        flag = "🇯🇴",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Amman",
        capitalAr = "عمان"
    ),
    PALESTINE(
        code = "PS",
        nameEn = "Palestine",
        nameAr = "فلسطين",
        flag = "🇵🇸",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Jerusalem",
        capitalAr = "القدس"
    ),
    LEBANON(
        code = "LB",
        nameEn = "Lebanon",
        nameAr = "لبنان",
        flag = "🇱🇧",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
        capitalEn = "Beirut",
        capitalAr = "بيروت"
    ),
    SYRIA(
        code = "SY",
        nameEn = "Syria",
        nameAr = "سوريا",
        flag = "🇸🇾",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Damascus",
        capitalAr = "دمشق"
    ),
    EGYPT(
        code = "EG",
        nameEn = "Egypt",
        nameAr = "جمهورية مصر العربية",
        flag = "🇪🇬",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Cairo",
        capitalAr = "القاهرة"
    ),
    SUDAN(
        code = "SD",
        nameEn = "Sudan",
        nameAr = "السودان",
        flag = "🇸🇩",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Khartoum",
        capitalAr = "الخرطوم"
    ),
    LIBYA(
        code = "LY",
        nameEn = "Libya",
        nameAr = "ليبيا",
        flag = "🇱🇾",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Tripoli",
        capitalAr = "طرابلس"
    ),
    TUNISIA(
        code = "TN",
        nameEn = "Tunisia",
        nameAr = "تونس",
        flag = "🇹🇳",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
        capitalEn = "Tunis",
        capitalAr = "تونس"
    ),
    ALGERIA(
        code = "DZ",
        nameEn = "Algeria",
        nameAr = "الجزائر",
        flag = "🇩🇿",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Algiers",
        capitalAr = "الجزائر"
    ),
    MOROCCO(
        code = "MA",
        nameEn = "Morocco",
        nameAr = "المملكة المغربية",
        flag = "🇲🇦",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
        capitalEn = "Rabat",
        capitalAr = "الرباط"
    ),
    MAURITANIA(
        code = "MR",
        nameEn = "Mauritania",
        nameAr = "موريتانيا",
        flag = "🇲🇷",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Nouakchott",
        capitalAr = "نواكشوط"
    ),
    SOMALIA(
        code = "SO",
        nameEn = "Somalia",
        nameAr = "الصومال",
        flag = "🇸🇴",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Mogadishu",
        capitalAr = "مقديشو"
    ),
    DJIBOUTI(
        code = "DJ",
        nameEn = "Djibouti",
        nameAr = "جيبوتي",
        flag = "🇩🇯",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Djibouti City",
        capitalAr = "مدينة جيبوتي"
    ),
    COMOROS(
        code = "KM",
        nameEn = "Comoros",
        nameAr = "جزر القمر",
        flag = "🇰🇲",
        defaultHijriOffset = 0,
        weekendDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        capitalEn = "Moroni",
        capitalAr = "موروني"
    );

    fun isWeekend(dayOfWeek: DayOfWeek): Boolean = weekendDays.contains(dayOfWeek)

    companion object {
        fun fromCode(code: String): ArabCountry {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SAUDI_ARABIA
        }
    }
}
