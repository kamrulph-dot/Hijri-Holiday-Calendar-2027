package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.calendar.ArabHolidaysProvider
import com.example.calendar.HijriCalendarEngine
import com.example.calendar.IslamicEventsProvider
import com.example.data.local.entity.PersonalEventEntity
import com.example.data.model.ArabCountry
import com.example.data.model.HolidayType
import com.example.ui.components.ArabNationSelector
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.EventGold
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.HolidayRed
import com.example.ui.theme.PersonalBlue
import com.example.viewmodel.CalendarUiState
import java.time.LocalDate

@Composable
fun EventsScreen(
    uiState: CalendarUiState,
    personalEvents: List<PersonalEventEntity>,
    onCountrySelected: (ArabCountry) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDeletePersonalEvent: (Long) -> Unit,
    onToggleFavoriteEvent: (PersonalEventEntity) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = uiState.isArabic
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedYear by remember { mutableIntStateOf(uiState.currentYearMonth.year) }

    val tabs = listOf(
        if (isArabic) "المناسبات الإسلامية" else "Islamic Events",
        if (isArabic) "عطل ${uiState.selectedCountry.nameAr}" else "Holidays (${uiState.selectedCountry.code})",
        if (isArabic) "العالم العربي" else "All Arab Nations",
        if (isArabic) "ملاحظاتي الخاصة" else "My Events"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("events_screen")
    ) {
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(if (isArabic) "ابحث عن مناسبة، عطلة، أو ملاحظة..." else "Search events, holidays, or notes...")
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("events_search_field")
        )

        // Year Selector Filter (2026, 2027 featured, 2028)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "السنة:" else "Year:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            listOf(2026, 2027, 2028).forEach { yr ->
                val isSelected = yr == selectedYear
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedYear = yr }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("year_filter_$yr")
                ) {
                    Text(
                        text = if (yr == 2027) "$yr ⭐" else "$yr",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("event_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        if (uiState.searchQuery.isNotBlank()) {
            // Search Results List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = if (isArabic) "نتائج البحث (${uiState.searchResults.size})" else "Search Results (${uiState.searchResults.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(uiState.searchResults) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDateSelected(item.date) },
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${item.subtitle} • ${item.date}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (item.type) {
                                            "Islamic" -> EventGold.copy(alpha = 0.15f)
                                            "Holiday" -> HolidayRed.copy(alpha = 0.15f)
                                            else -> PersonalBlue.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (item.type) {
                                        "Islamic" -> GoldDark
                                        "Holiday" -> HolidayRed
                                        else -> PersonalBlue
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            when (selectedTab) {
                0 -> {
                    // Islamic Events Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Emerald800.copy(alpha = 0.08f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("events_islamic_disclaimer")
                            ) {
                                Text(
                                    text = if (isArabic)
                                        "تنويه (تطبيق غير رسمي): التواريخ الإسلامية لعام $selectedYear هي تقديرات فلكية وفق تقويم أم القرى وتخضع للإعلان الرسمي لثبوت رؤية الهلال."
                                    else
                                        "Unofficial App Notice: Islamic dates for $selectedYear are astronomical estimates (Umm al-Qura) and depend on official local moon-sighting announcements.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald800,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        items(IslamicEventsProvider.events) { event ->
                            val eventDate = event.getDateForYear(selectedYear)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDateSelected(eventDate) }
                                    .testTag("islamic_event_${event.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(EventGold.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NightsStay,
                                            contentDescription = null,
                                            tint = EventGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isArabic) event.titleAr else event.titleEn,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${HijriCalendarEngine.HIJRI_MONTHS_EN[event.hijriMonth - 1]} ${event.hijriDay} • Approx: $eventDate",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GoldDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (isArabic) event.descriptionAr else event.descriptionEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }

                1 -> {
                    // Country Public Holidays Tab with Region Selector
                    val holidays = ArabHolidaysProvider.getHolidays(
                        countryCode = uiState.selectedCountry.code,
                        year = selectedYear,
                        hijriOffset = uiState.hijriOffset
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            ArabNationSelector(
                                selectedCountry = uiState.selectedCountry,
                                selectedYear = selectedYear,
                                hijriOffset = uiState.hijriOffset,
                                isArabic = isArabic,
                                onCountrySelected = onCountrySelected
                            )
                        }

                        item {
                            HolidayDisclaimerBanner(isArabic = isArabic)
                        }

                        items(holidays) { hol ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDateSelected(hol.date) }
                                    .testTag("holiday_item_${hol.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(HolidayRed.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Celebration,
                                            contentDescription = null,
                                            tint = HolidayRed,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isArabic) hol.titleAr else hol.titleEn,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${hol.date} • ${hol.hijriEquivalent.ifEmpty { hol.type.name }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = HolidayRed,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (hol.descriptionEn.isNotBlank() || hol.descriptionAr.isNotBlank()) {
                                            Text(
                                                text = if (isArabic) hol.descriptionAr else hol.descriptionEn,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }

                2 -> {
                    // All Arab Nations Explorer Tab
                    var exploredCountry by remember(uiState.selectedCountry) {
                        mutableStateOf(uiState.selectedCountry)
                    }

                    val expHolidays = ArabHolidaysProvider.getHolidays(
                        countryCode = exploredCountry.code,
                        year = selectedYear,
                        hijriOffset = uiState.hijriOffset
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            ArabNationSelector(
                                selectedCountry = exploredCountry,
                                selectedYear = selectedYear,
                                hijriOffset = uiState.hijriOffset,
                                isArabic = isArabic,
                                onCountrySelected = { country ->
                                    exploredCountry = country
                                    onCountrySelected(country)
                                }
                            )
                        }

                        item {
                            HolidayDisclaimerBanner(isArabic = isArabic)
                        }

                        items(expHolidays) { hol ->
                            val badgeColor = when (hol.type) {
                                HolidayType.NATIONAL -> HolidayRed
                                HolidayType.ISLAMIC -> GoldDark
                                HolidayType.OBSERVANCE -> PersonalBlue
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDateSelected(hol.date) },
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
                                        Text(
                                            text = hol.date.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (isArabic) hol.titleAr else hol.titleEn,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = hol.hijriEquivalent.ifEmpty {
                                                    if (isArabic) hol.descriptionAr.ifEmpty { hol.type.name }
                                                    else hol.descriptionEn.ifEmpty { hol.type.name }
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = hol.type.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeColor,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }

                3 -> {
                    // Personal Events Tab
                    if (personalEvents.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isArabic) "لا توجد ملاحظات خاصة مسجلة بعد" else "No personal events or notes yet",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isArabic) "انقر على أي يوم في التقويم لإضافة ملاحظة أو موعد" else "Tap any calendar day to add a reminder or note",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(personalEvents) { pe ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = pe.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(PersonalBlue.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = pe.category,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = PersonalBlue,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${pe.gregorianDate} ${if (pe.time.isNotBlank()) "• ${pe.time}" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (pe.description.isNotBlank()) {
                                                Text(
                                                    text = pe.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { onToggleFavoriteEvent(pe) }) {
                                                Icon(
                                                    imageVector = if (pe.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = "Favorite",
                                                    tint = if (pe.isFavorite) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(onClick = { onDeletePersonalEvent(pe.id) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HolidayDisclaimerBanner(isArabic: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Emerald800.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("events_disclaimer_banner")
    ) {
        Text(
            text = if (isArabic)
                "تطبيق غير رسمي — غير تابع لأي جهة حكومية. العطلات القمرية تقديرية وتخضع لرؤية الهلال والقرارات الرسمية.\nالمصادر: الجداول الوطنية المعتمدة، timeanddate.com، officeholidays.com، وتقويم أم القرى."
            else
                "Unofficial App — Not affiliated with any government. Lunar dates are tentative pending official moon sighting & decrees.\nSources: National holiday schedules, timeanddate.com, officeholidays.com, and Umm al-Qura.",
            style = MaterialTheme.typography.bodySmall,
            color = Emerald800,
            modifier = Modifier.padding(10.dp)
        )
    }
}
