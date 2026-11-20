package com.example.calendar

import com.example.data.model.IslamicEvent
import java.time.LocalDate

object IslamicEventsProvider {

    val events: List<IslamicEvent> = listOf(
        IslamicEvent(
            id = "ramadan_start",
            titleEn = "Beginning of Ramadan",
            titleAr = "بداية شهر رمضان المبارك",
            hijriMonth = 9,
            hijriDay = 1,
            approxDate2026 = LocalDate.of(2026, 2, 18),
            approxDate2027 = LocalDate.of(2027, 2, 8),
            approxDate2028 = LocalDate.of(2028, 1, 28),
            isMajorCountdown = true,
            descriptionEn = "The first day of fasting in the blessed holy month of Ramadan.",
            descriptionAr = "أول أيام الصيام في شهر رمضان المبارك، شهر الرحمة والمغفرة."
        ),
        IslamicEvent(
            id = "laylat_al_qadr",
            titleEn = "Laylat al-Qadr (Night of Power)",
            titleAr = "ليلة القدر المباركة",
            hijriMonth = 9,
            hijriDay = 27,
            approxDate2026 = LocalDate.of(2026, 3, 16),
            approxDate2027 = LocalDate.of(2027, 3, 6),
            approxDate2028 = LocalDate.of(2028, 2, 23),
            isMajorCountdown = false,
            descriptionEn = "The Night of Power, better than a thousand months.",
            descriptionAr = "ليلة مباركة خير من ألف شهر نزل فيها القرآن الكريم."
        ),
        IslamicEvent(
            id = "eid_al_fitr",
            titleEn = "Eid al-Fitr",
            titleAr = "عيد الفطر المبارك",
            hijriMonth = 10,
            hijriDay = 1,
            approxDate2026 = LocalDate.of(2026, 3, 20),
            approxDate2027 = LocalDate.of(2027, 3, 10),
            approxDate2028 = LocalDate.of(2028, 2, 27),
            isMajorCountdown = true,
            descriptionEn = "Islamic festival marking the culmination of the blessed fasting month of Ramadan.",
            descriptionAr = "عيد الفطر السعيد فرحة الصائمين بعد إتمام صيام شهر رمضان المبارك."
        ),
        IslamicEvent(
            id = "day_of_arafah",
            titleEn = "Day of Arafah",
            titleAr = "يوم عرفة (وقفة عرفات)",
            hijriMonth = 12,
            hijriDay = 9,
            approxDate2026 = LocalDate.of(2026, 5, 26),
            approxDate2027 = LocalDate.of(2027, 5, 16),
            approxDate2028 = LocalDate.of(2028, 5, 5),
            isMajorCountdown = false,
            descriptionEn = "Hajj pilgrims gather on the plains of Mount Arafah in supplication.",
            descriptionAr = "أعظم أيام الحج، حيث يقف حجاج بيت الله الحرام على صعيد عرفات الطاهر."
        ),
        IslamicEvent(
            id = "eid_al_adha",
            titleEn = "Eid al-Adha",
            titleAr = "عيد الأضحى المبارك",
            hijriMonth = 12,
            hijriDay = 10,
            approxDate2026 = LocalDate.of(2026, 5, 27),
            approxDate2027 = LocalDate.of(2027, 5, 17),
            approxDate2028 = LocalDate.of(2028, 5, 6),
            isMajorCountdown = true,
            descriptionEn = "Feast of the Sacrifice honoring the devotion of Prophet Ibrahim (peace be upon him).",
            descriptionAr = "عيد الأضحى المبارك (يوم النحر) وأيام التشريق المباركة."
        ),
        IslamicEvent(
            id = "islamic_new_year",
            titleEn = "Islamic New Year (1st Muharram)",
            titleAr = "رأس السنة الهجرية (1 محرم)",
            hijriMonth = 1,
            hijriDay = 1,
            approxDate2026 = LocalDate.of(2026, 6, 16),
            approxDate2027 = LocalDate.of(2027, 6, 6),
            approxDate2028 = LocalDate.of(2028, 5, 25),
            isMajorCountdown = false,
            descriptionEn = "Beginning of the new Hijri calendar year commemorating the Prophet's Migration.",
            descriptionAr = "غرة شهر محرم الحرام وبداية العام الهجري الجديد ذكرى الهجرة النبوية الشريفة."
        ),
        IslamicEvent(
            id = "ashura",
            titleEn = "Day of Ashura",
            titleAr = "يوم عاشوراء (10 محرم)",
            hijriMonth = 1,
            hijriDay = 10,
            approxDate2026 = LocalDate.of(2026, 6, 25),
            approxDate2027 = LocalDate.of(2027, 6, 15),
            approxDate2028 = LocalDate.of(2028, 6, 3),
            isMajorCountdown = false,
            descriptionEn = "The tenth day of Muharram observed with recommended fasting and reflection.",
            descriptionAr = "اليوم العاشر من محرم الحرام، يوم نجى الله فيه موسى عليه السلام."
        ),
        IslamicEvent(
            id = "mawlid_al_nabi",
            titleEn = "Mawlid an-Nabi (Prophet's Birthday)",
            titleAr = "المولد النبوي الشريف",
            hijriMonth = 3,
            hijriDay = 12,
            approxDate2026 = LocalDate.of(2026, 8, 25),
            approxDate2027 = LocalDate.of(2027, 8, 15),
            approxDate2028 = LocalDate.of(2028, 8, 4),
            isMajorCountdown = false,
            descriptionEn = "Commemoration of the birth of the Prophet Muhammad (peace and blessings be upon him).",
            descriptionAr = "ذكرى مولد خاتم الأنبياء والمرسلين نبينا محمد صلى الله عليه وسلم."
        ),
        IslamicEvent(
            id = "isra_and_miraj",
            titleEn = "Isra and Mi'raj",
            titleAr = "ذكرى الإسراء والمعراج",
            hijriMonth = 7,
            hijriDay = 27,
            approxDate2026 = LocalDate.of(2026, 1, 16),
            approxDate2027 = LocalDate.of(2027, 1, 5),
            approxDate2028 = LocalDate.of(2027, 12, 25),
            isMajorCountdown = false,
            descriptionEn = "Commemorating the miraculous Night Journey and Heavenly Ascension.",
            descriptionAr = "معجزة الإسراء والمعراج من المسجد الحرام إلى المسجد الأقصى وسدرة المنتهى."
        ),
        IslamicEvent(
            id = "mid_shaban",
            titleEn = "Mid-Sha'ban (Nisf Sha'ban)",
            titleAr = "ليلة النصف من شعبان",
            hijriMonth = 8,
            hijriDay = 15,
            approxDate2026 = LocalDate.of(2026, 2, 3),
            approxDate2027 = LocalDate.of(2027, 1, 23),
            approxDate2028 = LocalDate.of(2028, 1, 12),
            isMajorCountdown = false,
            descriptionEn = "The 15th night of Sha'ban preceding the holy month of Ramadan.",
            descriptionAr = "ليلة مباركة يستحب قيامها وصيام نهارها تمهيداً لرمضان المبارك."
        )
    )

    fun getEventsForDate(date: LocalDate, year: Int): List<IslamicEvent> {
        return events.filter { it.getDateForYear(year) == date }
    }
}
