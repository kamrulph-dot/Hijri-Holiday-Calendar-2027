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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.ArabHolidaysProvider
import com.example.calendar.HijriCalendarEngine
import com.example.calendar.IslamicEventsProvider
import com.example.data.model.ArabCountry
import com.example.ui.components.ArabNationSelector
import com.example.ui.components.CountdownCarousel
import com.example.ui.components.IslamicHeader
import com.example.ui.components.MonthCalendarView
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.EventGold
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.HolidayRed
import com.example.viewmodel.CalendarUiState
import java.time.LocalDate

@Composable
fun HomeScreen(
    uiState: CalendarUiState,
    onCountryClick: () -> Unit,
    onCountrySelected: (ArabCountry) -> Unit,
    onJumpToToday: () -> Unit,
    onJumpTo2027: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToConverter: () -> Unit,
    onNavigateToEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = uiState.isArabic

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // 1. Header with Country, Dual Date for Today, and Jump to 2027
        item {
            IslamicHeader(
                selectedCountry = uiState.selectedCountry,
                onCountryClick = onCountryClick,
                onJumpToToday = onJumpToToday,
                onJumpTo2027 = onJumpTo2027,
                hijriOffset = uiState.hijriOffset,
                isArabic = isArabic
            )
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. Interactive 22 Arab League Nation Selector Component
        item {
            ArabNationSelector(
                selectedCountry = uiState.selectedCountry,
                selectedYear = uiState.currentYearMonth.year,
                hijriOffset = uiState.hijriOffset,
                isArabic = isArabic,
                onCountrySelected = onCountrySelected,
                onOpenFullPicker = onCountryClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Countdowns Carousel (Ramadan, Eid al-Fitr, Eid al-Adha, Next Holiday)
        item {
            CountdownCarousel(
                daysUntilRamadan = uiState.daysUntilRamadan,
                daysUntilEidFitr = uiState.daysUntilEidFitr,
                daysUntilEidAdha = uiState.daysUntilEidAdha,
                ramadanDate = uiState.ramadanDate,
                eidFitrDate = uiState.eidFitrDate,
                eidAdhaDate = uiState.eidAdhaDate,
                nextHoliday = uiState.nextHoliday,
                daysUntilNextHoliday = uiState.daysUntilNextHoliday,
                isArabic = isArabic
            )
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Quick Date Converter Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onNavigateToConverter)
                    .testTag("quick_converter_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.18f)),
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
                                text = if (isArabic) "محول التاريخ الذكي (هجري ⇄ ميلادي)" else "Smart Date Converter (Hijri ⇄ Gregorian)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) "تحويل فوري مع اسم اليوم والمناسبات الدينية" else "Instant conversion with weekdays & Islamic events",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Converter",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Monthly Calendar Snapshot for Launch Year 2027
        item {
            MonthCalendarView(
                yearMonth = uiState.currentYearMonth,
                days = uiState.daysMatrix,
                selectedDate = uiState.selectedDate,
                firstDayOfWeek = uiState.firstDayOfWeek,
                hijriOffset = uiState.hijriOffset,
                isArabic = isArabic,
                onDateSelected = onDateSelected,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onTodayClick = onJumpToToday,
                onToggleViewMode = onNavigateToCalendar
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 5. Upcoming Public Holidays for Selected Country
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "عطل ومناسبات ${uiState.selectedCountry.nameAr}" else "Holidays in ${uiState.selectedCountry.nameEn}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (isArabic) "عرض الكل" else "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onNavigateToEvents)
                )
            }
        }

        val targetYear = uiState.currentYearMonth.year
        val upcomingList = ArabHolidaysProvider.getHolidays(uiState.selectedCountry.code, targetYear, uiState.hijriOffset).take(4)

        items(upcomingList) { holiday ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(HolidayRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = HolidayRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isArabic) holiday.titleAr else holiday.titleEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${holiday.date} • ${holiday.hijriEquivalent.ifEmpty { if (isArabic) "عطلة رسمية" else "Official Holiday" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (holiday.isTentative) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isArabic) "تقريبي" else "Estimated",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldDark,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
