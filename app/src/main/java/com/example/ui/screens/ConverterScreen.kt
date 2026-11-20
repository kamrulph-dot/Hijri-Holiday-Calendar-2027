package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.HijriCalendarEngine
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.EventGold
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.HolidayRed
import com.example.ui.theme.JummahGreen
import com.example.viewmodel.CalendarUiState
import java.time.LocalDate

@Composable
fun ConverterScreen(
    uiState: CalendarUiState,
    onConvertGregorian: (LocalDate) -> Unit,
    onConvertHijri: (year: Int, month: Int, day: Int) -> Unit,
    onNavigateToCalendarDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = uiState.isArabic
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = G to H, 1 = H to G

    // Gregorian inputs (default 2027-01-01)
    var gYear by remember { mutableIntStateOf(2027) }
    var gMonth by remember { mutableIntStateOf(1) }
    var gDay by remember { mutableIntStateOf(1) }

    // Hijri inputs (default 1448-09-01 Ramadan)
    var hYear by remember { mutableIntStateOf(1448) }
    var hMonth by remember { mutableIntStateOf(9) }
    var hDay by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("converter_screen")
    ) {
        // Screen Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = GoldDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isArabic) "محول التاريخ الهجري والميلادي" else "Smart Date Converter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isArabic) "حسابات فلكية معتمدة وفق تقويم أم القرى" else "Umm al-Qura astronomical standard calculations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Conversion Mode Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isArabic) "ميلادي ← هجري" else "Gregorian → Hijri") },
                modifier = Modifier.testTag("tab_gregorian_to_hijri")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isArabic) "هجري ← ميلادي" else "Hijri → Gregorian") },
                modifier = Modifier.testTag("tab_hijri_to_gregorian")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // Gregorian to Hijri
            Text(
                text = if (isArabic) "أدخل التاريخ الميلادي:" else "Enter Gregorian Date:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Year selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isArabic) "السنة:" else "Year:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf(2025, 2026, 2027, 2028, 2029, 2030)) { yr ->
                        val isSelected = gYear == yr
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    gYear = yr
                                    val safeDay = gDay.coerceAtMost(LocalDate.of(gYear, gMonth, 1).lengthOfMonth())
                                    onConvertGregorian(LocalDate.of(gYear, gMonth, safeDay))
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$yr",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isArabic) "الشهر:" else "Month:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..12).toList()) { m ->
                        val isSelected = gMonth == m
                        val mName = if (isArabic) HijriCalendarEngine.GREGORIAN_MONTHS_AR[m - 1] else HijriCalendarEngine.GREGORIAN_MONTHS_EN[m - 1]
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    gMonth = m
                                    val safeDay = gDay.coerceAtMost(LocalDate.of(gYear, gMonth, 1).lengthOfMonth())
                                    onConvertGregorian(LocalDate.of(gYear, gMonth, safeDay))
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mName,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day selector
            val daysInMonth = try { LocalDate.of(gYear, gMonth, 1).lengthOfMonth() } catch (_: Exception) { 31 }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isArabic) "اليوم:" else "Day:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..daysInMonth).toList()) { d ->
                        val isSelected = gDay == d
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    gDay = d
                                    onConvertGregorian(LocalDate.of(gYear, gMonth, gDay))
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$d",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick shortcuts
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .clickable {
                            val now = LocalDate.now()
                            gYear = now.year
                            gMonth = now.monthValue
                            gDay = now.dayOfMonth
                            onConvertGregorian(now)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = if (isArabic) "اليوم الحالي" else "Today", color = GoldDark, style = MaterialTheme.typography.labelSmall)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .clickable {
                            gYear = 2027
                            gMonth = 2
                            gDay = 8
                            onConvertGregorian(LocalDate.of(2027, 2, 8))
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = if (isArabic) "رمضان 2027" else "Ramadan 2027", color = GoldDark, style = MaterialTheme.typography.labelSmall)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .clickable {
                            gYear = 2027
                            gMonth = 3
                            gDay = 10
                            onConvertGregorian(LocalDate.of(2027, 3, 10))
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = if (isArabic) "عيد الفطر 2027" else "Eid al-Fitr 2027", color = GoldDark, style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            // Hijri to Gregorian
            Text(
                text = if (isArabic) "أدخل التاريخ الهجري:" else "Enter Hijri Date:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Hijri Year selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isArabic) "السنة الهجرية:" else "Year AH:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf(1446, 1447, 1448, 1449, 1450, 1451)) { yr ->
                        val isSelected = hYear == yr
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    hYear = yr
                                    onConvertHijri(hYear, hMonth, hDay)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$yr AH",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hijri Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isArabic) "الشهر الهجري:" else "Month:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..12).toList()) { m ->
                        val isSelected = hMonth == m
                        val mName = if (isArabic) HijriCalendarEngine.HIJRI_MONTHS_AR[m - 1] else HijriCalendarEngine.HIJRI_MONTHS_EN[m - 1]
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    hMonth = m
                                    onConvertHijri(hYear, hMonth, hDay)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mName,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hijri Day selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isArabic) "اليوم الهجري:" else "Day:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..30).toList()) { d ->
                        val isSelected = hDay == d
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    hDay = d
                                    onConvertHijri(hYear, hMonth, hDay)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$d",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Conversion Result Display Card
        val result = uiState.conversionResult
        if (result != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("conversion_result_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald900),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "نتيجة التحويل الفوري" else "Conversion Result",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (isArabic) result.dayOfWeekAr else result.dayOfWeekEn,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gregorian Output Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "التاريخ الميلادي" else "Gregorian Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC4DCD3)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${result.gregorianDate.dayOfMonth} ${if (isArabic) HijriCalendarEngine.GREGORIAN_MONTHS_AR[result.gregorianDate.monthValue - 1] else HijriCalendarEngine.GREGORIAN_MONTHS_EN[result.gregorianDate.monthValue - 1]} ${result.gregorianDate.year} CE",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hijri Output Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "التاريخ الهجري (تقويم أم القرى)" else "Hijri Date (Umm al-Qura)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC4DCD3)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) result.hijriDate.formattedAr else result.hijriDate.formattedEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }

                    // Occurrences on this day
                    if (result.islamicEvents.isNotEmpty() || result.holidays.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        result.islamicEvents.forEach { ev ->
                            Text(
                                text = "⭐ ${if (isArabic) ev.titleAr else ev.titleEn}",
                                color = GoldAccent,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        result.holidays.forEach { hol ->
                            Text(
                                text = "🎉 ${if (isArabic) hol.titleAr else hol.titleEn}",
                                color = Color(0xFFFFB4AB),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onNavigateToCalendarDate(result.gregorianDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isArabic) "فتح هذا اليوم في التقويم" else "View Date in Calendar",
                            color = Emerald900,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Official Moon-Sighting Disclaimer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isArabic)
                        "تنبيه الرؤية الشرعية: التواريخ الهجرية المحسوبة تعتمد على الحسابات الفلكية المسبقة لتقويم أم القرى. قد تختلف بداية الأشهر الهجرية الفعلية يوماً واحداً وفقاً لإعلان المحكمة العليا أو دار الإفتاء الرسمية بثبوت رؤية الهلال."
                    else
                        "Moon Sighting Notice: Calculated Hijri dates are based on astronomical criteria (Umm al-Qura calendar). Actual religious observance may differ by +/- 1 day according to official religious announcements.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
