package com.example.calendar

import com.example.data.model.Holiday
import com.example.data.model.HolidayType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object ArabHolidaysProvider {

    /**
     * Returns all verified holidays applicable for a given country code and Gregorian year.
     * Includes multi-day Islamic holidays, country-specific national days, and movable holidays.
     */
    fun getHolidays(
        countryCode: String,
        year: Int,
        hijriOffset: Int = 0
    ): List<Holiday> {
        val code = countryCode.uppercase()
        val list = mutableListOf<Holiday>()

        // 1. Dynamic Umm al-Qura Islamic Holidays for the requested Gregorian year
        list.addAll(getIslamicHolidaysForYear(code, year, hijriOffset))

        // 2. Country-Specific Official National, Civil & Movable Holidays
        list.addAll(getCountryNationalHolidays(code, year))

        return list.sortedWith(compareBy({ it.date }, { it.type }))
    }

    private data class IslamicHolidaySpec(
        val idPrefix: String,
        val hijriMonth: Int,
        val hijriDay: Int,
        val titleEn: String,
        val titleAr: String,
        val type: HolidayType,
        val descEn: String,
        val descAr: String,
        val applicableCountries: Set<String>? = null // null = all 22 Arab countries
    )

    private val islamicHolidaySpecs = listOf(
        // Isra and Mi'raj (27 Rajab) - official holiday in KW, OM, JO, PS, SO, DJ, KM
        IslamicHolidaySpec(
            idPrefix = "is_isra_miraj",
            hijriMonth = 7,
            hijriDay = 27,
            titleEn = "Isra and Mi'raj",
            titleAr = "ذكرى الإسراء والمعراج",
            type = HolidayType.ISLAMIC,
            descEn = "Commemorating the Night Journey and Ascension (tentative lunar date).",
            descAr = "ذكرى الإسراء والمعراج (تاريخ فلكي تقريبي).",
            applicableCountries = setOf("KW", "OM", "JO", "PS", "SO", "DJ", "KM")
        ),
        // Beginning of Ramadan (1 Ramadan) - observance across all 22 countries
        IslamicHolidaySpec(
            idPrefix = "is_ramadan",
            hijriMonth = 9,
            hijriDay = 1,
            titleEn = "Beginning of Ramadan",
            titleAr = "غرة شهر رمضان المبارك",
            type = HolidayType.OBSERVANCE,
            descEn = "First day of the holy fasting month (subject to moon sighting).",
            descAr = "أول أيام شهر الصيام المبارك (يخضع لرؤية الهلال)."
        ),
        // Eid al-Fitr (1, 2, 3 Shawwal) - multi-day official holiday across all 22 nations
        IslamicHolidaySpec(
            idPrefix = "is_eid_fitr_1",
            hijriMonth = 10,
            hijriDay = 1,
            titleEn = "Eid al-Fitr (Day 1)",
            titleAr = "عيد الفطر المبارك (اليوم الأول)",
            type = HolidayType.ISLAMIC,
            descEn = "Official public holiday marking the end of Ramadan (tentative).",
            descAr = "أول أيام عيد الفطر السعيد (تاريخ تقريبي)."
        ),
        IslamicHolidaySpec(
            idPrefix = "is_eid_fitr_2",
            hijriMonth = 10,
            hijriDay = 2,
            titleEn = "Eid al-Fitr Holiday (Day 2)",
            titleAr = "إجازة عيد الفطر (اليوم الثاني)",
            type = HolidayType.ISLAMIC,
            descEn = "Second day of Eid al-Fitr public holiday (tentative).",
            descAr = "ثاني أيام إجازة عيد الفطر المبارك."
        ),
        IslamicHolidaySpec(
            idPrefix = "is_eid_fitr_3",
            hijriMonth = 10,
            hijriDay = 3,
            titleEn = "Eid al-Fitr Holiday (Day 3)",
            titleAr = "إجازة عيد الفطر (اليوم الثالث)",
            type = HolidayType.ISLAMIC,
            descEn = "Third day of Eid al-Fitr public holiday (tentative).",
            descAr = "ثالث أيام إجازة عيد الفطر المبارك.",
            applicableCountries = setOf(
                "SA", "AE", "QA", "KW", "BH", "OM", "YE", "IQ", "JO", "PS",
                "LB", "SY", "EG", "SD", "LY", "TN", "DZ", "MA", "DJ", "KM"
            )
        ),
        IslamicHolidaySpec(
            idPrefix = "is_eid_fitr_4",
            hijriMonth = 10,
            hijriDay = 4,
            titleEn = "Eid al-Fitr Holiday (Day 4)",
            titleAr = "إجازة عيد الفطر (اليوم الرابع)",
            type = HolidayType.ISLAMIC,
            descEn = "Extended Eid al-Fitr holiday in Gulf nations (tentative).",
            descAr = "رابع أيام عطلة عيد الفطر المبارك.",
            applicableCountries = setOf("SA", "AE", "QA", "OM", "JO")
        ),
        // Day of Arafah (9 Dhu al-Hijjah)
        IslamicHolidaySpec(
            idPrefix = "is_arafah",
            hijriMonth = 12,
            hijriDay = 9,
            titleEn = "Day of Arafah",
            titleAr = "وقفة عرفات (يوم عرفة)",
            type = HolidayType.ISLAMIC,
            descEn = "Day of Arafah preceding Eid al-Adha (tentative lunar date).",
            descAr = "وقفة حجاج بيت الله الحرام على صعيد عرفات الطاهر.",
            applicableCountries = setOf(
                "SA", "AE", "QA", "KW", "BH", "OM", "YE", "JO", "PS",
                "EG", "SD", "LY", "DJ"
            )
        ),
        // Eid al-Adha (10, 11, 12 Dhu al-Hijjah) - multi-day official holiday across all 22 nations
        IslamicHolidaySpec(
            idPrefix = "is_eid_adha_1",
            hijriMonth = 12,
            hijriDay = 10,
            titleEn = "Eid al-Adha (Day 1)",
            titleAr = "عيد الأضحى المبارك (اليوم الأول)",
            type = HolidayType.ISLAMIC,
            descEn = "Official public holiday celebrating the Feast of the Sacrifice (tentative).",
            descAr = "أول أيام عيد الأضحى المبارك ويوم النحر (تاريخ تقريبي)."
        ),
        IslamicHolidaySpec(
            idPrefix = "is_eid_adha_2",
            hijriMonth = 12,
            hijriDay = 11,
            titleEn = "Eid al-Adha Holiday (Day 2)",
            titleAr = "إجازة عيد الأضحى (اليوم الثاني)",
            type = HolidayType.ISLAMIC,
            descEn = "Second day of Eid al-Adha public holiday (tentative).",
            descAr = "ثاني أيام عيد الأضحى المبارك."
        ),
        IslamicHolidaySpec(
            idPrefix = "is_eid_adha_3",
            hijriMonth = 12,
            hijriDay = 12,
            titleEn = "Eid al-Adha Holiday (Day 3)",
            titleAr = "إجازة عيد الأضحى (اليوم الثالث)",
            type = HolidayType.ISLAMIC,
            descEn = "Third day of Eid al-Adha public holiday (tentative).",
            descAr = "ثالث أيام عيد الأضحى المبارك.",
            applicableCountries = setOf(
                "SA", "AE", "QA", "KW", "BH", "OM", "YE", "IQ", "JO", "PS",
                "LB", "SY", "EG", "SD", "LY", "TN", "MA", "DJ", "KM"
            )
        ),
        IslamicHolidaySpec(
            idPrefix = "is_eid_adha_4",
            hijriMonth = 12,
            hijriDay = 13,
            titleEn = "Eid al-Adha Holiday (Day 4)",
            titleAr = "إجازة عيد الأضحى (اليوم الرابع)",
            type = HolidayType.ISLAMIC,
            descEn = "Fourth day of Eid al-Adha public holiday (tentative).",
            descAr = "رابع أيام عطلة عيد الأضحى المبارك.",
            applicableCountries = setOf("SA", "QA", "KW", "JO", "EG", "YE")
        ),
        // Eid al-Ghadeer (18 Dhu al-Hijjah) - Iraq public holiday
        IslamicHolidaySpec(
            idPrefix = "is_ghadeer",
            hijriMonth = 12,
            hijriDay = 18,
            titleEn = "Eid al-Ghadeer",
            titleAr = "عيد الغدير",
            type = HolidayType.ISLAMIC,
            descEn = "Public holiday observed in Iraq (tentative lunar date).",
            descAr = "عطلة رسمية في جمهورية العراق (تاريخ تقريبي).",
            applicableCountries = setOf("IQ")
        ),
        // Islamic New Year (1 Muharram) - official holiday outside SA/QA
        IslamicHolidaySpec(
            idPrefix = "is_new_year",
            hijriMonth = 1,
            hijriDay = 1,
            titleEn = "Islamic New Year (1st Muharram)",
            titleAr = "رأس السنة الهجرية",
            type = HolidayType.ISLAMIC,
            descEn = "First day of the new Hijri calendar year (tentative).",
            descAr = "غرة محرم وبداية العام الهجري الجديد (تاريخ تقريبي).",
            applicableCountries = setOf(
                "AE", "KW", "BH", "OM", "YE", "IQ", "JO", "PS", "LB", "SY",
                "EG", "SD", "LY", "TN", "DZ", "MA", "MR", "SO", "DJ", "KM"
            )
        ),
        // Tasu'a / 9 Muharram in Bahrain
        IslamicHolidaySpec(
            idPrefix = "is_ashura_eve",
            hijriMonth = 1,
            hijriDay = 9,
            titleEn = "Ashura Holiday (9th Muharram)",
            titleAr = "التاسع من محرم (إجازة عاشوراء)",
            type = HolidayType.ISLAMIC,
            descEn = "First day of the two-day Ashura public holiday in Bahrain (tentative).",
            descAr = "اليوم التاسع من محرم وإجازة عاشوراء في البحرين.",
            applicableCountries = setOf("BH")
        ),
        // Day of Ashura (10 Muharram)
        IslamicHolidaySpec(
            idPrefix = "is_ashura",
            hijriMonth = 1,
            hijriDay = 10,
            titleEn = "Day of Ashura",
            titleAr = "يوم عاشوراء",
            type = HolidayType.ISLAMIC,
            descEn = "The tenth day of Muharram (official holiday in BH, DZ, MA, IQ, LB, SO).",
            descAr = "اليوم العاشر من محرم (عطلة رسمية في عدة دول عربية).",
            applicableCountries = setOf("BH", "DZ", "MA", "IQ", "LB", "SO", "KM")
        ),
        // Prophet's Birthday / Mawlid an-Nabi (12 Rabi' al-Awwal)
        IslamicHolidaySpec(
            idPrefix = "is_mawlid",
            hijriMonth = 3,
            hijriDay = 12,
            titleEn = "Prophet's Birthday (Mawlid an-Nabi)",
            titleAr = "المولد النبوي الشريف",
            type = HolidayType.ISLAMIC,
            descEn = "Commemoration of the birth of Prophet Muhammad (PBUH) (tentative).",
            descAr = "ذكرى مولد خاتم الأنبياء والمرسلين صلى الله عليه وسلم (تاريخ تقريبي).",
            applicableCountries = setOf(
                "AE", "KW", "BH", "OM", "YE", "IQ", "JO", "PS", "LB", "SY",
                "EG", "SD", "LY", "TN", "DZ", "MA", "MR", "SO", "DJ", "KM"
            )
        )
    )

    private fun getIslamicHolidaysForYear(
        countryCode: String,
        year: Int,
        hijriOffset: Int
    ): List<Holiday> {
        val startHijriYear = HijriCalendarEngine.toHijri(LocalDate.of(year, 1, 1), hijriOffset).year
        val endHijriYear = HijriCalendarEngine.toHijri(LocalDate.of(year, 12, 31), hijriOffset).year
        val results = mutableListOf<Holiday>()

        for (hYear in startHijriYear..endHijriYear) {
            for (spec in islamicHolidaySpecs) {
                if (spec.applicableCountries != null && !spec.applicableCountries.contains(countryCode)) {
                    continue
                }
                val date = HijriCalendarEngine.toGregorian(hYear, spec.hijriMonth, spec.hijriDay, hijriOffset)
                if (date.year == year) {
                    val monthEn = HijriCalendarEngine.HIJRI_MONTHS_EN[spec.hijriMonth - 1]
                    results.add(
                        Holiday(
                            id = "${spec.idPrefix}_${year}_$hYear",
                            countryCode = countryCode,
                            titleEn = spec.titleEn,
                            titleAr = spec.titleAr,
                            date = date,
                            hijriEquivalent = "${spec.hijriDay} $monthEn $hYear AH",
                            type = spec.type,
                            isTentative = true,
                            descriptionEn = spec.descEn,
                            descriptionAr = spec.descAr
                        )
                    )
                }
            }
        }
        return results
    }

    private fun getCountryNationalHolidays(countryCode: String, year: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()
        val orthodoxEaster = computeOrthodoxEasterGregorian(year)
        val westernEaster = computeWesternEasterGregorian(year)

        when (countryCode) {
            "SA" -> {
                holidays.add(Holiday("sa_founding", "SA", "Saudi Founding Day", "يوم التأسيس السعودي", LocalDate.of(year, 2, 22), type = HolidayType.NATIONAL, descriptionEn = "Commemorating the founding of the First Saudi State in 1727.", descriptionAr = "ذكرى تأسيس الدولة السعودية الأولى على يد الإمام محمد بن سعود عام 1727م."))
                holidays.add(Holiday("sa_flag", "SA", "Saudi Flag Day", "يوم العلم السعودي", LocalDate.of(year, 3, 11), type = HolidayType.OBSERVANCE, descriptionEn = "National observance honoring the Saudi flag.", descriptionAr = "مناسبة وطنية للاحتفاء بالعلم السعودي."))
                holidays.add(Holiday("sa_national", "SA", "Saudi National Day", "اليوم الوطني للمملكة", LocalDate.of(year, 9, 23), type = HolidayType.NATIONAL, descriptionEn = "Unification of the Kingdom of Saudi Arabia by King Abdulaziz.", descriptionAr = "ذكرى توحيد المملكة العربية السعودية على يد الملك عبد العزيز آل سعود."))
            }
            "AE" -> {
                holidays.add(Holiday("ae_newyear", "AE", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ae_commem", "AE", "Commemoration Day", "يوم الشهيد", LocalDate.of(year, 11, 30), type = HolidayType.OBSERVANCE, descriptionEn = "Honoring fallen Emirati heroes.", descriptionAr = "تكريم شهداء الوطن الذين ضحوا بأرواحهم في سبيل رفعة الإمارات."))
                holidays.add(Holiday("ae_national", "AE", "UAE National Day (Eid Al Etihad)", "اليوم الوطني (عيد الاتحاد)", LocalDate.of(year, 12, 2), type = HolidayType.NATIONAL, descriptionEn = "Anniversary of the federal unification of the Emirates in 1971.", descriptionAr = "ذكرى قيام اتحاد دولة الإمارات العربية المتحدة عام 1971م."))
                holidays.add(Holiday("ae_national2", "AE", "Eid Al Etihad Holiday", "عطلة عيد الاتحاد", LocalDate.of(year, 12, 3), type = HolidayType.NATIONAL))
            }
            "QA" -> {
                val sportsDay = LocalDate.of(year, 2, 1).with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.TUESDAY))
                holidays.add(Holiday("qa_sports", "QA", "National Sports Day", "اليوم الرياضي للدولة", sportsDay, type = HolidayType.NATIONAL, descriptionEn = "Celebrated every second Tuesday of February.", descriptionAr = "يوم وطني لتعزيز الرياضة والصحة العامة (الثلاثاء الثاني من فبراير)."))
                holidays.add(Holiday("qa_national", "QA", "Qatar National Day", "اليوم الوطني لقطر", LocalDate.of(year, 12, 18), type = HolidayType.NATIONAL, descriptionEn = "Commemorating national unity led by Sheikh Jassim bin Mohammed Al Thani.", descriptionAr = "ذكرى تولي الشيخ جاسم بن محمد آل ثاني الحكم وتأسيس الدولة الحديثة."))
            }
            "KW" -> {
                holidays.add(Holiday("kw_newyear", "KW", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("kw_national", "KW", "Kuwait National Day", "اليوم الوطني الكويتي", LocalDate.of(year, 2, 25), type = HolidayType.NATIONAL, descriptionEn = "Celebration of Kuwait's independence and heritage.", descriptionAr = "احتفال باستقلال الكويت وعزتها الوطنية."))
                holidays.add(Holiday("kw_liberation", "KW", "Liberation Day", "يوم التحرير", LocalDate.of(year, 2, 26), type = HolidayType.NATIONAL, descriptionEn = "Anniversary of the liberation of Kuwait in 1991.", descriptionAr = "ذكرى تحرير الكويت عام 1991م."))
            }
            "BH" -> {
                holidays.add(Holiday("bh_newyear", "BH", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("bh_labour", "BH", "Labour Day", "يوم العمال العالمي", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("bh_national", "BH", "Bahrain National Day", "اليوم الوطني البحريني", LocalDate.of(year, 12, 16), type = HolidayType.NATIONAL, descriptionEn = "National Day of the Kingdom of Bahrain.", descriptionAr = "ذكرى قيام الدولة البحرينية الحديثة."))
                holidays.add(Holiday("bh_national2", "BH", "Accession Day", "عيد الجلوس", LocalDate.of(year, 12, 17), type = HolidayType.NATIONAL))
            }
            "OM" -> {
                holidays.add(Holiday("om_newyear", "OM", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("om_accession", "OM", "Sultan's Accession Day", "ذكرى تولي السلطان مقاليد الحكم", LocalDate.of(year, 1, 11), type = HolidayType.NATIONAL, descriptionEn = "Commemorating the accession of Sultan Haitham bin Tariq.", descriptionAr = "ذكرى تولي حضرة صاحب الجلالة السلطان هيثم بن طارق مقاليد الحكم."))
                holidays.add(Holiday("om_national", "OM", "Oman National Day", "العيد الوطني العماني", LocalDate.of(year, 11, 18), type = HolidayType.NATIONAL, descriptionEn = "National Day of the Sultanate of Oman.", descriptionAr = "احتفالات سلطنة عمان بالعيد الوطني المجيد."))
                holidays.add(Holiday("om_national2", "OM", "National Day Holiday", "عطلة العيد الوطني", LocalDate.of(year, 11, 19), type = HolidayType.NATIONAL))
            }
            "YE" -> {
                holidays.add(Holiday("ye_newyear", "YE", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("ye_labour", "YE", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ye_unity", "YE", "National Unity Day", "عيد الوحدة اليمنية (22 مايو)", LocalDate.of(year, 5, 22), type = HolidayType.NATIONAL, descriptionEn = "Unification of North and South Yemen in 1990.", descriptionAr = "ذكرى إعادة تحقيق الوحدة اليمنية المباركة عام 1990م."))
                holidays.add(Holiday("ye_sep26", "YE", "September 26 Revolution Day", "ذكرى ثورة 26 سبتمبر", LocalDate.of(year, 9, 26), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ye_oct14", "YE", "October 14 Revolution Day", "ذكرى ثورة 14 أكتوبر", LocalDate.of(year, 10, 14), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ye_independence", "YE", "Independence Day", "عيد الاستقلال (30 نوفمبر)", LocalDate.of(year, 11, 30), type = HolidayType.NATIONAL, descriptionEn = "Independence and evacuation day in 1967.", descriptionAr = "ذكرى الاستقلال المجيد عام 1967م."))
            }
            "IQ" -> {
                holidays.add(Holiday("iq_newyear", "IQ", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("iq_army", "IQ", "Iraqi Army Day", "عيد الجيش العراقي", LocalDate.of(year, 1, 6), type = HolidayType.NATIONAL))
                holidays.add(Holiday("iq_halabja", "IQ", "Halabja Memorial Day", "ذكرى فاجعة حلبجة", LocalDate.of(year, 3, 16), type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("iq_nowruz", "IQ", "Nowruz (Spring Festival)", "عيد نوروز", LocalDate.of(year, 3, 21), type = HolidayType.NATIONAL))
                holidays.add(Holiday("iq_apr9", "IQ", "Baghdad Liberation Day (Regional)", "يوم 9 أبريل", LocalDate.of(year, 4, 9), type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("iq_labour", "IQ", "Labour Day", "عيد العمال العالمي", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("iq_republic", "IQ", "Republic Day", "ذكرى ثورة 14 تموز", LocalDate.of(year, 7, 14), type = HolidayType.NATIONAL))
                holidays.add(Holiday("iq_independence", "IQ", "Iraqi National Day", "اليوم الوطني العراقي", LocalDate.of(year, 10, 3), type = HolidayType.NATIONAL, descriptionEn = "Independence and entry into the League of Nations in 1932.", descriptionAr = "ذكرى استقلال العراق ودخوله عصبة الأمم عام 1932م."))
                holidays.add(Holiday("iq_victory", "IQ", "Victory Day", "يوم النصر", LocalDate.of(year, 12, 10), type = HolidayType.NATIONAL))
                holidays.add(Holiday("iq_xmas", "IQ", "Christmas Day", "عيد الميلاد المجيد", LocalDate.of(year, 12, 25), type = HolidayType.NATIONAL))
            }
            "JO" -> {
                holidays.add(Holiday("jo_newyear", "JO", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("jo_labour", "JO", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("jo_independence", "JO", "Jordan Independence Day", "عيد استقلال المملكة الأردنية الهاشمية", LocalDate.of(year, 5, 25), type = HolidayType.NATIONAL, descriptionEn = "Independence of the Hashemite Kingdom of Jordan in 1946.", descriptionAr = "ذكرى استقلال المملكة الأردنية الهاشمية عام 1946م."))
                holidays.add(Holiday("jo_xmas", "JO", "Christmas Day", "عيد الميلاد المجيد", LocalDate.of(year, 12, 25), type = HolidayType.NATIONAL))
            }
            "PS" -> {
                holidays.add(Holiday("ps_newyear", "PS", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ps_ortho_xmas", "PS", "Orthodox Christmas", "عيد الميلاد الشرقي", LocalDate.of(year, 1, 7), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ps_land", "PS", "Land Day", "يوم الأرض الخالد", LocalDate.of(year, 3, 30), type = HolidayType.OBSERVANCE, descriptionEn = "Commemorating Palestinian attachment to their land.", descriptionAr = "ذكرى يوم الأرض الخالد."))
                holidays.add(Holiday("ps_labour", "PS", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ps_ortho_easter", "PS", "Orthodox Easter Sunday", "عيد الفصح الشرقي", orthodoxEaster, type = HolidayType.NATIONAL))
                holidays.add(Holiday("ps_independence", "PS", "Palestinian Independence Day", "عيد الاستقلال الفلسطيني", LocalDate.of(year, 11, 15), type = HolidayType.NATIONAL, descriptionEn = "Proclamation of the Palestinian Declaration of Independence in 1988.", descriptionAr = "ذكرى إعلان وثيقة الاستقلال عام 1988م."))
                holidays.add(Holiday("ps_xmas", "PS", "Christmas Day", "عيد الميلاد الغربي", LocalDate.of(year, 12, 25), type = HolidayType.NATIONAL))
            }
            "LB" -> {
                holidays.add(Holiday("lb_newyear", "LB", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_armenian_xmas", "LB", "Armenian Orthodox Christmas", "عيد الميلاد لدى الأرمن الأرثوذكس", LocalDate.of(year, 1, 6), type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_maron", "LB", "Saint Maron Day", "عيد مار مارون", LocalDate.of(year, 2, 9), type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_hariri", "LB", "Rafik Hariri Memorial Day", "ذكرى استشهاد الرئيس رفيق الحريري", LocalDate.of(year, 2, 14), type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("lb_annunc", "LB", "Feast of the Annunciation", "عيد البشارة المشترك", LocalDate.of(year, 3, 25), type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_w_good_friday", "LB", "Good Friday (Western)", "الجمعة العظيمة (الغربية)", westernEaster.minusDays(2), type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_w_easter", "LB", "Easter Sunday (Western)", "عيد الفصح المجيد (الغربي)", westernEaster, type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_o_good_friday", "LB", "Good Friday (Orthodox)", "الجمعة العظيمة (الشرقية)", orthodoxEaster.minusDays(2), type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_o_easter", "LB", "Easter Sunday (Orthodox)", "عيد الفصح المجيد (الشرقي)", orthodoxEaster, type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_labour", "LB", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_martyrs", "LB", "Martyrs' Day", "عيد الشهداء", LocalDate.of(year, 5, 6), type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("lb_liberation", "LB", "Resistance and Liberation Day", "عيد المقاومة والتحرير", LocalDate.of(year, 5, 25), type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_assumption", "LB", "Assumption of Mary", "عيد انتقال السيدة العذراء", LocalDate.of(year, 8, 15), type = HolidayType.NATIONAL))
                holidays.add(Holiday("lb_independence", "LB", "Lebanon Independence Day", "عيد الاستقلال اللبناني", LocalDate.of(year, 11, 22), type = HolidayType.NATIONAL, descriptionEn = "Independence of Lebanon in 1943.", descriptionAr = "ذكرى استقلال لبنان عام 1943م."))
                holidays.add(Holiday("lb_xmas", "LB", "Christmas Day", "عيد الميلاد المجيد", LocalDate.of(year, 12, 25), type = HolidayType.NATIONAL))
            }
            "SY" -> {
                holidays.add(Holiday("sy_newyear", "SY", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("sy_rev", "SY", "Syrian Revolution Day", "ذكرى الثورة السورية", LocalDate.of(year, 3, 18), type = HolidayType.NATIONAL))
                holidays.add(Holiday("sy_mothers", "SY", "Mother's Day & Nowruz", "عيد الأم وعيد النيروز", LocalDate.of(year, 3, 21), type = HolidayType.NATIONAL))
                holidays.add(Holiday("sy_w_easter", "SY", "Easter Sunday (Western)", "عيد الفصح (الغربي)", westernEaster, type = HolidayType.NATIONAL))
                holidays.add(Holiday("sy_evacuation", "SY", "Evacuation Day (Independence)", "عيد الجلاء السوري", LocalDate.of(year, 4, 17), type = HolidayType.NATIONAL, descriptionEn = "Evacuation of foreign forces and Syrian independence in 1946.", descriptionAr = "ذكرى جلاء آخر جندي مستعمر عن أرض سوريا عام 1946م."))
                holidays.add(Holiday("sy_labour", "SY", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("sy_o_easter", "SY", "Easter Sunday (Orthodox)", "عيد الفصح (الشرقي)", orthodoxEaster, type = HolidayType.NATIONAL))
                holidays.add(Holiday("sy_martyrs", "SY", "Martyrs' Day", "عيد الشهداء", LocalDate.of(year, 5, 6), type = HolidayType.NATIONAL))
                holidays.add(Holiday("sy_liberation", "SY", "Liberation Day", "عيد التحرير", LocalDate.of(year, 12, 8), type = HolidayType.NATIONAL))
                holidays.add(Holiday("sy_xmas", "SY", "Christmas Day", "عيد الميلاد المجيد", LocalDate.of(year, 12, 25), type = HolidayType.NATIONAL))
            }
            "EG" -> {
                holidays.add(Holiday("eg_xmas", "EG", "Coptic Christmas Day", "عيد الميلاد المجيد", LocalDate.of(year, 1, 7), type = HolidayType.NATIONAL))
                holidays.add(Holiday("eg_police", "EG", "Revolution Day (25 Jan) / Police Day", "عيد الشرطة وثورة 25 يناير", LocalDate.of(year, 1, 25), type = HolidayType.NATIONAL))
                holidays.add(Holiday("eg_sinai", "EG", "Sinai Liberation Day", "عيد تحرير سيناء", LocalDate.of(year, 4, 25), type = HolidayType.NATIONAL, descriptionEn = "Liberation of the Sinai Peninsula in 1982.", descriptionAr = "ذكرى استرداد كامل أرض سيناء الحبيبة."))
                holidays.add(Holiday("eg_labour", "EG", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("eg_coptic_easter", "EG", "Coptic Easter Sunday", "عيد القيامة المجيد", orthodoxEaster, type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("eg_sham", "EG", "Sham El-Nessim (Spring Festival)", "عيد شم النسيم", orthodoxEaster.plusDays(1), type = HolidayType.NATIONAL, descriptionEn = "Traditional Egyptian spring holiday on Coptic Easter Monday.", descriptionAr = "عيد الربيع المصري (الإثنين التالي لعيد القيامة المجيد)."))
                holidays.add(Holiday("eg_june30", "EG", "June 30 Revolution", "ثورة 30 يونيو", LocalDate.of(year, 6, 30), type = HolidayType.NATIONAL))
                holidays.add(Holiday("eg_july23", "EG", "July 23 Revolution Day", "عيد ثورة 23 يوليو", LocalDate.of(year, 7, 23), type = HolidayType.NATIONAL))
                holidays.add(Holiday("eg_oct6", "EG", "Armed Forces Day (6th of October)", "عيد القوات المسلحة (نصر 6 أكتوبر)", LocalDate.of(year, 10, 6), type = HolidayType.NATIONAL, descriptionEn = "Anniversary of the October 1973 crossing.", descriptionAr = "ذكرى انتصارات حرب أكتوبر المجيدة."))
            }
            "SD" -> {
                holidays.add(Holiday("sd_independence", "SD", "Sudan Independence Day", "عيد الاستقلال السوداني", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL, descriptionEn = "Independence of Sudan in 1956.", descriptionAr = "ذكرى استقلال جمهورية السودان عام 1956م."))
                holidays.add(Holiday("sd_coptic", "SD", "Coptic Christmas", "عيد الميلاد المجيد", LocalDate.of(year, 1, 7), type = HolidayType.NATIONAL))
                holidays.add(Holiday("sd_labour", "SD", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("sd_revolution", "SD", "December Revolution Day", "ذكرى ثورة ديسمبر المجيدة", LocalDate.of(year, 12, 19), type = HolidayType.NATIONAL))
            }
            "LY" -> {
                holidays.add(Holiday("ly_rev17", "LY", "February 17 Revolution Day", "ذكرى ثورة 17 فبراير", LocalDate.of(year, 2, 17), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ly_mar19", "LY", "March 19 Commemoration Day", "ذكرى 19 مارس", LocalDate.of(year, 3, 19), type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("ly_labour", "LY", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ly_martyrs", "LY", "Martyrs' Day (Omar al-Mukhtar)", "يوم الشهيد (ذكرى عمر المختار)", LocalDate.of(year, 9, 16), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ly_liberation", "LY", "Liberation Day", "عيد التحرير", LocalDate.of(year, 10, 23), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ly_independence", "LY", "Libyan Independence Day", "عيد الاستقلال الليبي", LocalDate.of(year, 12, 24), type = HolidayType.NATIONAL, descriptionEn = "Independence of Libya in 1951.", descriptionAr = "ذكرى استقلال ليبيا عام 1951م."))
            }
            "TN" -> {
                holidays.add(Holiday("tn_newyear", "TN", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("tn_independence", "TN", "Tunisian Independence Day", "عيد الاستقلال التونسي", LocalDate.of(year, 3, 20), type = HolidayType.NATIONAL, descriptionEn = "Independence of Tunisia in 1956.", descriptionAr = "ذكرى استقلال تونس عام 1956م."))
                holidays.add(Holiday("tn_martyrs", "TN", "Martyrs' Day", "عيد الشهداء", LocalDate.of(year, 4, 9), type = HolidayType.NATIONAL))
                holidays.add(Holiday("tn_labour", "TN", "Labour Day", "عيد الشغل", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("tn_republic", "TN", "Republic Day", "عيد الجمهورية", LocalDate.of(year, 7, 25), type = HolidayType.NATIONAL))
                holidays.add(Holiday("tn_women", "TN", "National Women's Day", "عيد المرأة الوطني", LocalDate.of(year, 8, 13), type = HolidayType.NATIONAL))
                holidays.add(Holiday("tn_evacuation", "TN", "Evacuation Day", "عيد الجلاء", LocalDate.of(year, 10, 15), type = HolidayType.NATIONAL))
                holidays.add(Holiday("tn_revolution", "TN", "Revolution Day", "عيد الثورة", LocalDate.of(year, 12, 17), type = HolidayType.NATIONAL, descriptionEn = "Commemorating the start of the Tunisian Revolution in 2010.", descriptionAr = "ذكرى اندلاع الثورة التونسية عام 2010م."))
            }
            "DZ" -> {
                holidays.add(Holiday("dz_newyear", "DZ", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("dz_yennayer", "DZ", "Yennayer (Amazigh New Year)", "رأس السنة الأمازيغية (يناير)", LocalDate.of(year, 1, 12), type = HolidayType.NATIONAL))
                holidays.add(Holiday("dz_fraternity", "DZ", "Day of Fraternity and Cohesion", "اليوم الوطني للأخوة والتلاحم", LocalDate.of(year, 2, 22), type = HolidayType.OBSERVANCE))
                holidays.add(Holiday("dz_labour", "DZ", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("dz_independence", "DZ", "Algerian Independence Day", "عيد الاستقلال والشباب", LocalDate.of(year, 7, 5), type = HolidayType.NATIONAL, descriptionEn = "Independence of Algeria in 1962.", descriptionAr = "ذكرى استقلال الجزائر المجيدة عام 1962م."))
                holidays.add(Holiday("dz_revolution", "DZ", "Revolution Day (1st November)", "ذكرى اندلاع الثورة التحريرية", LocalDate.of(year, 11, 1), type = HolidayType.NATIONAL, descriptionEn = "Outbreak of the Liberation Revolution in 1954.", descriptionAr = "ذكرى اندلاع ثورة أول نوفمبر الخالدة عام 1954م."))
            }
            "MA" -> {
                holidays.add(Holiday("ma_newyear", "MA", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ma_indep_manif", "MA", "Proclamation of Independence Manifesto", "ذكرى تقديم وثيقة الاستقلال", LocalDate.of(year, 1, 11), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ma_amazigh", "MA", "Amazigh New Year", "رأس السنة الأمازيغية", LocalDate.of(year, 1, 14), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ma_labour", "MA", "Labour Day", "عيد الشغل", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ma_throne", "MA", "Throne Day", "عيد العرش المجيد", LocalDate.of(year, 7, 30), type = HolidayType.NATIONAL, descriptionEn = "Accession of King Mohammed VI to the throne.", descriptionAr = "ذكرى تولي جلالة الملك محمد السادس عرش أسلافه."))
                holidays.add(Holiday("ma_oued", "MA", "Oued Ed-Dahab Allegiance Day", "ذكرى استرجاع إقليم وادي الذهب", LocalDate.of(year, 8, 14), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ma_king_people", "MA", "Revolution of the King and the People", "ذكرى ثورة الملك والشعب", LocalDate.of(year, 8, 20), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ma_youth", "MA", "Youth Day", "عيد الشباب", LocalDate.of(year, 8, 21), type = HolidayType.NATIONAL))
                holidays.add(Holiday("ma_green_march", "MA", "Green March Day", "ذكرى المسيرة الخضراء المظفرة", LocalDate.of(year, 11, 6), type = HolidayType.NATIONAL, descriptionEn = "Anniversary of the peaceful Green March in 1975.", descriptionAr = "ذكرى انطلاق المسيرة الخضراء السلمية عام 1975م."))
                holidays.add(Holiday("ma_independence", "MA", "Moroccan Independence Day", "عيد الاستقلال المغربي", LocalDate.of(year, 11, 18), type = HolidayType.NATIONAL, descriptionEn = "Independence of the Kingdom of Morocco.", descriptionAr = "ذكرى إعلان استقلال المملكة المغربية الشريفة."))
            }
            "MR" -> {
                holidays.add(Holiday("mr_newyear", "MR", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("mr_labour", "MR", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("mr_africa", "MR", "Africa Day", "يوم إفريقيا", LocalDate.of(year, 5, 25), type = HolidayType.NATIONAL))
                holidays.add(Holiday("mr_independence", "MR", "Mauritania Independence Day", "عيد الاستقلال الوطني الموريتاني", LocalDate.of(year, 11, 28), type = HolidayType.NATIONAL, descriptionEn = "Independence of Mauritania in 1960.", descriptionAr = "ذكرى استقلال الجمهورية الإسلامية الموريتانية عام 1960م."))
            }
            "SO" -> {
                holidays.add(Holiday("so_newyear", "SO", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("so_labour", "SO", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("so_indep_north", "SO", "Independence Day (Northern Regions)", "عيد استقلال الأقاليم الشمالية", LocalDate.of(year, 6, 26), type = HolidayType.NATIONAL))
                holidays.add(Holiday("so_republic", "SO", "Somalia Republic Day", "عيد الجمهورية واستقلال الصومال", LocalDate.of(year, 7, 1), type = HolidayType.NATIONAL, descriptionEn = "Unification and Independence Day of the Somali Republic in 1960.", descriptionAr = "ذكرى توحيد واستقلال جمهورية الصومال عام 1960م."))
            }
            "DJ" -> {
                holidays.add(Holiday("dj_newyear", "DJ", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("dj_labour", "DJ", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("dj_independence", "DJ", "Djibouti Independence Day", "عيد استقلال جمهورية جيبوتي", LocalDate.of(year, 6, 27), type = HolidayType.NATIONAL, descriptionEn = "Independence of Djibouti in 1977.", descriptionAr = "ذكرى استقلال جمهورية جيبوتي عام 1977م."))
                holidays.add(Holiday("dj_independence2", "DJ", "Independence Day Holiday", "عطلة عيد الاستقلال", LocalDate.of(year, 6, 28), type = HolidayType.NATIONAL))
            }
            "KM" -> {
                holidays.add(Holiday("km_newyear", "KM", "New Year's Day", "رأس السنة الميلادية", LocalDate.of(year, 1, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("km_maarouf", "KM", "Cheikh Al-Maarouf Day", "يوم الشيخ المعروف", LocalDate.of(year, 3, 18), type = HolidayType.NATIONAL))
                holidays.add(Holiday("km_labour", "KM", "Labour Day", "عيد العمال", LocalDate.of(year, 5, 1), type = HolidayType.NATIONAL))
                holidays.add(Holiday("km_independence", "KM", "Comoros Independence Day", "عيد استقلال جزر القمر", LocalDate.of(year, 7, 6), type = HolidayType.NATIONAL, descriptionEn = "Independence of the Union of the Comoros in 1975.", descriptionAr = "ذكرى استقلال اتحاد جزر القمر عام 1975م."))
                holidays.add(Holiday("km_maore", "KM", "Maore Day", "يوم ماوري الوطني", LocalDate.of(year, 11, 12), type = HolidayType.OBSERVANCE))
            }
        }

        return holidays
    }

    /**
     * Computes Eastern/Coptic Orthodox Easter Sunday in the Gregorian calendar
     * using Meeus's Julian Easter algorithm (valid for 1900–2099).
     */
    internal fun computeOrthodoxEasterGregorian(year: Int): LocalDate {
        val a = year % 4
        val b = year % 7
        val c = year % 19
        val d = (19 * c + 15) % 30
        val e = (2 * a + 4 * b - d + 34) % 7
        val julianMonth = (d + e + 114) / 31 // 3 = March, 4 = April (Julian)
        val julianDay = ((d + e + 114) % 31) + 1
        // Julian-to-Gregorian offset is +13 days for 1900..2099
        return LocalDate.of(year, julianMonth, julianDay).plusDays(13)
    }

    /**
     * Computes Western Gregorian Easter Sunday using the Anonymous Gregorian algorithm.
     */
    internal fun computeWesternEasterGregorian(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }
}
