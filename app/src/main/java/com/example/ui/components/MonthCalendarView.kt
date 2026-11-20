package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.HijriCalendarEngine
import com.example.data.model.CalendarDay
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.EventGold
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.HolidayRed
import com.example.ui.theme.JummahGreen
import com.example.ui.theme.PersonalBlue
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthCalendarView(
    yearMonth: YearMonth,
    days: List<CalendarDay>,
    selectedDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
    hijriOffset: Int = 0,
    isArabic: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onToggleViewMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("month_calendar_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Month Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(
                        imageVector = if (isArabic) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Month & Year Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val gMonthEn = HijriCalendarEngine.GREGORIAN_MONTHS_EN[yearMonth.monthValue - 1]
                    val gMonthAr = HijriCalendarEngine.GREGORIAN_MONTHS_AR[yearMonth.monthValue - 1]

                    Text(
                        text = if (isArabic) "$gMonthAr ${yearMonth.year}" else "$gMonthEn ${yearMonth.year}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Representative Hijri month estimation for the active Gregorian month
                    val midMonth = yearMonth.atDay(15)
                    val midHijri = HijriCalendarEngine.toHijri(midMonth, hijriOffset)
                    Text(
                        text = if (isArabic) midHijri.formattedAr else midHijri.formattedEn,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = GoldDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTodayClick,
                        modifier = Modifier.testTag("calendar_today_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Today",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onToggleViewMode,
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Toggle Yearly Overview",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = if (isArabic) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Headers
            val weekdays = getOrderedWeekdays(firstDayOfWeek)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekdays.forEach { dayOfWeek ->
                    val isFriday = dayOfWeek == DayOfWeek.FRIDAY
                    Text(
                        text = HijriCalendarEngine.getWeekdayShortName(dayOfWeek, isArabic),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isFriday) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isFriday) JummahGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Day Grid (Rows of 7 days)
            val chunkedDays = days.chunked(7)
            chunkedDays.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    week.forEach { day ->
                        DayCell(
                            day = day,
                            isArabic = isArabic,
                            onClick = { onDateSelected(day.date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = JummahGreen, label = if (isArabic) "الجمعة" else "Friday")
                LegendItem(color = HolidayRed, label = if (isArabic) "عطلة رسمية" else "Holiday")
                LegendItem(color = EventGold, label = if (isArabic) "مناسبة إسلامية" else "Islamic")
                LegendItem(color = PersonalBlue, label = if (isArabic) "ملاحظة خاصة" else "Event")
            }
        }
    }
}

@Composable
fun DayCell(
    day: CalendarDay,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = day.isSelected
    val isToday = day.isToday
    val isCurrentMonth = day.isCurrentMonth
    val isFriday = day.isFriday

    val backgroundColor = when {
        isSelected -> Emerald900
        isFriday && isCurrentMonth -> Color(0xFF14533C).copy(alpha = 0.08f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
        isFriday -> JummahGreen
        day.isWeekend -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val hijriColor = when {
        isSelected -> GoldAccent
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
        else -> GoldDark
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(0.88f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.5.dp, GoldPrimary, RoundedCornerShape(12.dp))
                } else if (isSelected) {
                    Modifier.border(1.5.dp, GoldAccent, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .testTag("day_cell_${day.date}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            // Gregorian Day Number (Large)
            Text(
                text = "${day.date.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected || isToday || isFriday) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                fontSize = 15.sp,
                lineHeight = 16.sp
            )

            // Hijri Day Number (Smaller)
            Text(
                text = "${day.hijriDate.day}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = hijriColor,
                fontSize = 10.sp,
                lineHeight = 11.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Indicators Dot Row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (day.hasHoliday) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(HolidayRed)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                if (day.hasIslamicEvent) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(EventGold)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                if (day.hasPersonalEvent) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(PersonalBlue)
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

private fun getOrderedWeekdays(firstDayOfWeek: DayOfWeek): List<DayOfWeek> {
    val days = mutableListOf<DayOfWeek>()
    var current = firstDayOfWeek
    for (i in 0..6) {
        days.add(current)
        current = current.plus(1)
    }
    return days
}
