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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.local.entity.PersonalEventEntity
import com.example.ui.components.AddEditEventDialog
import com.example.ui.components.DayDetailSheet
import com.example.ui.components.MonthCalendarView
import com.example.ui.components.YearCalendarView
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.viewmodel.CalendarUiState
import java.time.LocalDate

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    onJumpTo2027: () -> Unit,
    onSetYear: (Int) -> Unit,
    onSetMonth: (Int) -> Unit,
    onToggleViewMode: (String) -> Unit,
    onAddPersonalEvent: (title: String, description: String, date: LocalDate, time: String, category: String, isFavorite: Boolean) -> Unit,
    onDeletePersonalEvent: (Long) -> Unit,
    onToggleFavoriteEvent: (PersonalEventEntity) -> Unit,
    onNavigateToConverterWithDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = uiState.isArabic
    var showDayDetailSheet by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    val years = listOf(2025, 2026, 2027, 2028, 2029, 2030)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
    ) {
        // Top Year Selector Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(years) { year ->
                    val isSelected = year == uiState.currentYearMonth.year
                    val isLaunch2027 = year == 2027

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) Emerald800
                                else if (isLaunch2027) GoldPrimary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isSelected) Emerald800
                                else if (isLaunch2027) GoldPrimary
                                else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSetYear(year) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("year_chip_$year")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$year",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected || isLaunch2027) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            if (isLaunch2027) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (isSelected) GoldAccent else GoldPrimary)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Jump to 2027 Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldPrimary)
                    .clickable(onClick = onJumpTo2027)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("quick_jump_2027_btn")
            ) {
                Text(
                    text = if (isArabic) "2027" else "2027",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Emerald900
                )
            }
        }

        // View Mode Switcher: Monthly Grid vs Yearly Overview
        TabRow(
            selectedTabIndex = if (uiState.calendarViewMode == "month") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = uiState.calendarViewMode == "month",
                onClick = { onToggleViewMode("month") },
                text = { Text(if (isArabic) "عرض شهري" else "Monthly Grid") },
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.testTag("tab_month_view")
            )
            Tab(
                selected = uiState.calendarViewMode == "year",
                onClick = { onToggleViewMode("year") },
                text = { Text(if (isArabic) "عرض سنوي شامل" else "Yearly Overview") },
                icon = { Icon(Icons.Default.GridView, contentDescription = null) },
                modifier = Modifier.testTag("tab_year_view")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Body based on selected view mode
        if (uiState.calendarViewMode == "month") {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                MonthCalendarView(
                    yearMonth = uiState.currentYearMonth,
                    days = uiState.daysMatrix,
                    selectedDate = uiState.selectedDate,
                    firstDayOfWeek = uiState.firstDayOfWeek,
                    hijriOffset = uiState.hijriOffset,
                    isArabic = isArabic,
                    onDateSelected = { date ->
                        onDateSelected(date)
                        showDayDetailSheet = true
                    },
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onTodayClick = onJumpToToday,
                    onToggleViewMode = { onToggleViewMode("year") }
                )

                // Quick Day Action Summary Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { showDayDetailSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "اليوم المحدد:" else "Selected Date:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${uiState.selectedDate} • ${HijriCalendarEngine.toHijri(uiState.selectedDate, uiState.hijriOffset).formattedEn}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = { showDayDetailSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isArabic) "عرض التفاصيل" else "View Details")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            YearCalendarView(
                year = uiState.currentYearMonth.year,
                selectedCountry = uiState.selectedCountry,
                isArabic = isArabic,
                onYearChange = onSetYear,
                onMonthSelected = onSetMonth,
                onCloseYearView = { onToggleViewMode("month") },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Day Detail Bottom Sheet
    if (showDayDetailSheet) {
        DayDetailSheet(
            date = uiState.selectedDate,
            hijriOffset = uiState.hijriOffset,
            holidays = uiState.holidaysForSelectedDate,
            islamicEvents = uiState.islamicEventsForSelectedDate,
            personalEvents = uiState.personalEventsForSelectedDate,
            isArabic = isArabic,
            onDismiss = { showDayDetailSheet = false },
            onAddEventClick = {
                showDayDetailSheet = false
                showAddEventDialog = true
            },
            onDeleteEvent = onDeletePersonalEvent,
            onToggleFavorite = onToggleFavoriteEvent,
            onOpenConverter = onNavigateToConverterWithDate
        )
    }

    // Add Personal Event Dialog
    if (showAddEventDialog) {
        AddEditEventDialog(
            date = uiState.selectedDate,
            isArabic = isArabic,
            onDismiss = { showAddEventDialog = false },
            onSave = { title, desc, time, cat, fav ->
                onAddPersonalEvent(title, desc, uiState.selectedDate, time, cat, fav)
                showAddEventDialog = false
            }
        )
    }
}
