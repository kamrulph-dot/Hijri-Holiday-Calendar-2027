package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.ArabHolidaysProvider
import com.example.calendar.HijriCalendarEngine
import com.example.data.model.ArabCountry
import com.example.ui.theme.Emerald800
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.HolidayRed
import java.time.YearMonth

@Composable
fun YearCalendarView(
    year: Int,
    selectedCountry: ArabCountry,
    isArabic: Boolean,
    onYearChange: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onCloseYearView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("year_calendar_view")
    ) {
        // Year Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onYearChange(year - 1) },
                modifier = Modifier.testTag("prev_year_button")
            ) {
                Icon(
                    imageVector = if (isArabic) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Year",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isArabic) "نظرة عامة على سنة $year" else "Year $year Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isArabic) "اختر أي شهر للانتقال إليه" else "Tap any month to expand",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { onYearChange(year + 1) },
                modifier = Modifier.testTag("next_year_button")
            ) {
                Icon(
                    imageVector = if (isArabic) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Year",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 12 Months Grid
        val allHolidays = ArabHolidaysProvider.getHolidays(selectedCountry.code, year)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items((1..12).toList()) { month ->
                val ym = YearMonth.of(year, month)
                val monthHolidays = allHolidays.filter { it.date.monthValue == month }
                val monthNameEn = HijriCalendarEngine.GREGORIAN_MONTHS_EN[month - 1]
                val monthNameAr = HijriCalendarEngine.GREGORIAN_MONTHS_AR[month - 1]

                MonthThumbnailCard(
                    month = month,
                    monthName = if (isArabic) monthNameAr else monthNameEn,
                    holidaysCount = monthHolidays.size,
                    daysCount = ym.lengthOfMonth(),
                    isRamadanMonth = (year == 2027 && (month == 2 || month == 3)),
                    onClick = {
                        onMonthSelected(month)
                        onCloseYearView()
                    }
                )
            }
        }
    }
}

@Composable
fun MonthThumbnailCard(
    month: Int,
    monthName: String,
    holidaysCount: Int,
    daysCount: Int,
    isRamadanMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("month_thumbnail_$month"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRamadanMonth) Color(0xFF14533C).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isRamadanMonth) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isRamadanMonth) Emerald800 else MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$daysCount days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (holidaysCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HolidayRed.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$holidaysCount holidays",
                        style = MaterialTheme.typography.labelSmall,
                        color = HolidayRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}
