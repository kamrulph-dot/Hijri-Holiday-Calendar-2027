# Hijri Holiday Calendar 2027
*Hijri & Gregorian Calendar for All 22 Arab Countries*

A premium, modern, lightweight Android application designed specifically for users across all 22 Arab League countries (`com.galaxy.hijriholidaycalender.kt`).

---

## 📱 Google Play Store Listing & App Description

**Play Store Title (27/30 chars):**
`Hijri Holiday Calendar 2027`

**Short Description (78/80 chars):**
`2027 Hijri & Gregorian calendar with public holidays for all 22 Arab nations.`

### Full Store Description

Stay organized throughout 2027 and beyond with **Hijri Holiday Calendar 2027** — a modern, offline-first dual calendar combining Gregorian and Hijri (Islamic) dates, multi-day national & religious public holidays, optional 1-day-before national holiday reminders, Islamic event countdowns, and personal notes across all 22 Arab League countries.

**Key Highlights:**
- **Dual Hijri & Gregorian Monthly & Yearly Calendar:** 2027 featured launch year with automatic support for 2026, 2028, and future/past years.
- **All 22 Arab League Countries Supported:** Saudi Arabia, UAE, Qatar, Kuwait, Bahrain, Oman, Yemen, Iraq, Jordan, Palestine, Lebanon, Syria, Egypt, Sudan, Libya, Tunisia, Algeria, Morocco, Mauritania, Somalia, Djibouti, and Comoros.
- **1-Day-Before National Holiday Reminders:** Optional local notification setting to remind you one day prior to upcoming national holidays in your selected country.
- **Full-Year Verified Holiday Schedules:** Includes multi-day Eid al-Fitr and Eid al-Adha breaks, Islamic New Year, Mawlid an-Nabi, Ashura, Isra & Mi'raj (where observed), National & Independence Days, and movable Coptic/Eastern & Western Christian holidays (such as Sham El-Nessim & Easter where applicable).
- **Umm al-Qura & Manual Hijri Offset (-2 to +2 days):** Adjust calculated Hijri dates to align with local moon-sighting announcements.
- **Countdown Cards & Smart Date Converter:** Live countdowns to Ramadan, Eid al-Fitr, Eid al-Adha, and the next holiday, plus bidirectional Gregorian ⇄ Hijri conversion.
- **Offline Personal Notes & Bilingual RTL/LTR UI:** Save personal events and favorite dates locally in English or Arabic (RTL) with Light, Dark, and System themes.

### Disclaimer (Unofficial App)
> **Notice:** This is an **independent, unofficial** calendar application. It is **NOT** affiliated with, endorsed by, sponsored by, or officially connected to any government, ministry, embassy, or official religious or moon-sighting authority in any of the 22 Arab League countries.
>
> All Hijri dates and Islamic public holidays are **astronomical estimates (Tentative)** and remain subject to official local moon-sighting confirmations (*Ru'yah*) and government announcements in each country.

### Holiday & Calendar Data Sources
All holiday schedules and calendar calculations included in this application are compiled from publicly available official and reference sources:
1. **National Holiday Schedules & Decrees:** Published public holiday laws and national calendars of respective Arab League nations (e.g., Saudi Founding & National Days, UAE Eid Al Etihad, Qatar National Sports Day, Oman Sultan's Accession & National Days, Tunisia Dec 17 Revolution Day, etc.).
2. **Public Holiday Reference Databases:** [timeanddate.com/holidays](https://www.timeanddate.com/holidays/) and [officeholidays.com](https://www.officeholidays.com/) for cross-checking 2026–2028 multi-day Eid holidays and regional observances.
3. **Umm al-Qura Astronomical Calendar:** `java.time.chrono.HijrahChronology` (`Hijrah-umalqura`) for Hijri ⇄ Gregorian conversions and Islamic event estimations.
4. **Astronomical Easter Algorithms:** Meeus Julian Easter and Anonymous Gregorian Easter algorithms for movable holidays such as Egypt's Sham El-Nessim and Coptic/Western Easter in Egypt, Lebanon, Syria, and Palestine.

---

## 🌟 Technical & Feature Overview

1. **Dual Calendar System (2027 Featured Launch Year + Any Year)**:
   - High-fidelity monthly calendar displaying large Gregorian dates and smaller Hijri dates.
   - Interactive Friday (Jummah) highlight, country-specific weekend styling, and event indicators.
   - Month-by-month navigation and a 12-month bird's-eye yearly overview.
   - Dedicated launch year 2027 buttons plus automatic support for 2026, 2028, and future/past years.

2. **All 22 Arab League Nations Supported**:
   - 🇸🇦 Saudi Arabia (KSA), 🇦🇪 UAE, 🇶🇦 Qatar, 🇰🇼 Kuwait, 🇧🇭 Bahrain, 🇴🇲 Oman, 🇾🇪 Yemen, 🇮🇶 Iraq, 🇯🇴 Jordan, 🇵🇸 Palestine, 🇱🇧 Lebanon, 🇸🇾 Syria, 🇪🇬 Egypt, 🇸🇩 Sudan, 🇱🇾 Libya, 🇹🇳 Tunisia, 🇩🇿 Algeria, 🇲🇦 Morocco, 🇲🇷 Mauritania, 🇸🇴 Somalia, 🇩🇯 Djibouti, 🇰🇲 Comoros.

3. **Umm al-Qura Hijri Calendar Accuracy**:
   - Integrated Umm al-Qura astronomical calculation engine.
   - Country-aware Hijri adjustments with manual offset slider/stepper (-2 to +2 days).
   - Clear disclosure in-app and in store metadata that calculated astronomical dates remain tentative until official local moon sighting (Ru'yah) announcements.

4. **Event & Holiday System**:
   - Major Islamic events: Ramadan, Laylat al-Qadr, multi-day Eid al-Fitr, Day of Arafah, multi-day Eid al-Adha, Islamic New Year, Ashura, Mawlid an-Nabi, Isra and Mi'raj.
   - Country-specific official national holidays and movable holidays.
   - Interactive Countdown Cards for Ramadan, Eid al-Fitr, Eid al-Adha, and the next upcoming national holiday.

5. **Smart Date Converter & Offline Personal Calendar**:
   - Bidirectional Gregorian ⇄ Hijri conversion with weekday & holiday lookup.
   - Powered by Room Database for zero-network, local-only persistence of personal events and date notes.

6. **Islamic-Inspired Design**:
   - Palette: Deep Emerald Green (`#0F382C`), warm gold accents (`#D4AF37`), and subtle quad-color Arab-inspired backdrop.
   - Full bilingual support with authentic Arabic RTL (Right-to-Left) and English LTR layouts.
   - Light, Dark, and System theme modes.

---

## 🛠️ Architecture & Tech Stack

- **Platform**: Android Native
- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Local Persistence**: Jetpack Room with KSP
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow`
- **Architecture**: MVVM with Repository Pattern
- **Date Calculation**: `java.time` + `HijrahChronology` (Umm al-Qura)

---

## 🚀 Building & Testing

### Compilation
To build the application APK:
```bash
gradle assembleDebug
```

### Running Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```
