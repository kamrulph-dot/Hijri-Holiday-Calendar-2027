package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.calendar.ArabHolidaysProvider
import com.example.data.model.ArabCountry
import com.example.ui.theme.Emerald800
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import java.time.DayOfWeek

/**
 * Interactive region selector component allowing users to choose any of the
 * 22 Arab League nations via a quick dropdown or scrollable chip strip to
 * immediately load regional holiday data.
 */
@Composable
fun ArabNationSelector(
    selectedCountry: ArabCountry,
    selectedYear: Int,
    hijriOffset: Int,
    isArabic: Boolean,
    onCountrySelected: (ArabCountry) -> Unit,
    onOpenFullPicker: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val holidaysCount = ArabHolidaysProvider.getHolidays(
        countryCode = selectedCountry.code,
        year = selectedYear,
        hijriOffset = hijriOffset
    ).size

    val weekendLabel = if (selectedCountry.weekendDays.contains(DayOfWeek.FRIDAY)) {
        if (isArabic) "عطلة نهاية الأسبوع: الجمعة - السبت" else "Weekend: Fri - Sat"
    } else {
        if (isArabic) "عطلة نهاية الأسبوع: السبت - الأحد" else "Weekend: Sat - Sun"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("arab_nation_selector_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header row with title and "All 22 Nations" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Emerald800.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isArabic) "اختر الدولة العربية لتحميل العطلات" else "Select Arab League Nation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isArabic) "22 دولة عربية • $holidaysCount عطلة محملة لعام $selectedYear"
                            else "22 Arab nations • $holidaysCount holidays loaded for $selectedYear",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onOpenFullPicker != null) {
                    Text(
                        text = if (isArabic) "بحث" else "Browse All",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenFullPicker)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("browse_all_nations_btn")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dropdown Selector Box
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("nation_dropdown_trigger"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = selectedCountry.flag, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) selectedCountry.nameAr else selectedCountry.nameEn,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${if (isArabic) selectedCountry.capitalAr else selectedCountry.capitalEn} • $weekendLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldDark
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Choose Arab League Nation",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.84f)
                        .testTag("nation_dropdown_menu")
                ) {
                    ArabCountry.entries.forEach { country ->
                        val isSelected = country == selectedCountry
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = country.flag, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (isArabic) country.nameAr else country.nameEn,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isArabic) country.capitalAr else "${country.capitalEn} (${country.code})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GoldPrimary.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isArabic) "مفعّل" else "Active",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GoldDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                onCountrySelected(country)
                                dropdownExpanded = false
                            },
                            modifier = Modifier.testTag("dropdown_nation_${country.code}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Scrollable Strip for all 22 Arab League Nations
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nation_quick_chips_row")
            ) {
                items(ArabCountry.entries) { country ->
                    val isSelected = country == selectedCountry
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isSelected) GoldPrimary else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onCountrySelected(country) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("nation_chip_${country.code}")
                    ) {
                        Text(
                            text = "${country.flag} ${if (isArabic) country.nameAr else country.nameEn}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
