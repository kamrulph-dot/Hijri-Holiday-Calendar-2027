package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Holiday
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.GoldAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class CountdownColorTone {
    GREEN,
    WHITE,
    RED
}

data class CountdownItem(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val days: Long,
    val dateSubtitle: String,
    val icon: ImageVector,
    val isHoliday: Boolean = false
)

@Composable
fun CountdownCarousel(
    daysUntilRamadan: Long,
    daysUntilEidFitr: Long,
    daysUntilEidAdha: Long,
    ramadanDate: LocalDate,
    eidFitrDate: LocalDate,
    eidAdhaDate: LocalDate,
    nextHoliday: Holiday?,
    daysUntilNextHoliday: Long,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val items = mutableListOf(
        CountdownItem(
            id = "ramadan",
            titleEn = "Ramadan",
            titleAr = "رمضان المبارك",
            days = daysUntilRamadan,
            dateSubtitle = "${ramadanDate.format(dateFmt)} • 1 Ramadan",
            icon = Icons.Default.NightsStay
        ),
        CountdownItem(
            id = "eid_fitr",
            titleEn = "Eid al-Fitr",
            titleAr = "عيد الفطر السعيد",
            days = daysUntilEidFitr,
            dateSubtitle = "${eidFitrDate.format(dateFmt)} • 1 Shawwal",
            icon = Icons.Default.Celebration
        ),
        CountdownItem(
            id = "eid_adha",
            titleEn = "Eid al-Adha",
            titleAr = "عيد الأضحى المبارك",
            days = daysUntilEidAdha,
            dateSubtitle = "${eidAdhaDate.format(dateFmt)} • 10 Dhu al-Hijjah",
            icon = Icons.Default.Star
        )
    )

    if (nextHoliday != null) {
        items.add(
            CountdownItem(
                id = "next_holiday",
                titleEn = nextHoliday.titleEn,
                titleAr = nextHoliday.titleAr,
                days = daysUntilNextHoliday,
                dateSubtitle = "${nextHoliday.date} • ${if (nextHoliday.isTentative) "Estimated" else "Official"}",
                icon = Icons.Default.Celebration,
                isHoliday = true
            )
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "العد التنازلي للمناسبات الإسلامية" else "Islamic & National Countdowns",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isArabic) "تواريخ فلكية تقريبية" else "Astronomical estimate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag("countdown_carousel")
        ) {
            itemsIndexed(items) { index, item ->
                val tone = when (index % 3) {
                    0 -> CountdownColorTone.GREEN
                    1 -> CountdownColorTone.WHITE
                    else -> CountdownColorTone.RED
                }
                CountdownCard(item = item, tone = tone, isArabic = isArabic)
            }
        }
    }
}

private data class CountdownCardStyle(
    val gradient: Brush,
    val titleColor: Color,
    val subtitleColor: Color,
    val badgeBg: Color,
    val badgeText: Color,
    val iconBg: Color,
    val iconTint: Color,
    val borderStroke: BorderStroke?
)

@Composable
private fun CountdownCard(
    item: CountdownItem,
    tone: CountdownColorTone,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val style = when (tone) {
        CountdownColorTone.GREEN -> CountdownCardStyle(
            gradient = Brush.linearGradient(listOf(Emerald900, Emerald800)),
            titleColor = Color.White,
            subtitleColor = Color(0xFFC4DCD3),
            badgeBg = Color(0xFF14533C),
            badgeText = GoldAccent,
            iconBg = Color.White.copy(alpha = 0.18f),
            iconTint = GoldAccent,
            borderStroke = null
        )
        CountdownColorTone.WHITE -> CountdownCardStyle(
            gradient = Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC))),
            titleColor = Color(0xFF0F172A),
            subtitleColor = Color(0xFF475569),
            badgeBg = Color(0xFFFEE2E2),
            badgeText = Color(0xFF991B1B),
            iconBg = Emerald800.copy(alpha = 0.12f),
            iconTint = Emerald800,
            borderStroke = BorderStroke(1.dp, Color(0xFFE2E8F0))
        )
        CountdownColorTone.RED -> CountdownCardStyle(
            gradient = Brush.linearGradient(listOf(Color(0xFF8B1E24), Color(0xFFB3262E))),
            titleColor = Color.White,
            subtitleColor = Color(0xFFFEE2E2),
            badgeBg = Color(0xFF7F1D1D),
            badgeText = Color.White,
            iconBg = Color.White.copy(alpha = 0.18f),
            iconTint = Color.White,
            borderStroke = null
        )
    }

    Card(
        modifier = modifier
            .width(220.dp)
            .testTag("countdown_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = style.borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .background(style.gradient)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(style.iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = style.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(style.badgeBg)
                            .border(
                                1.dp,
                                style.badgeText.copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (item.days >= 0) {
                                if (isArabic) "${item.days} يوم متبقي" else "${item.days} days"
                            } else {
                                if (isArabic) "مضى" else "Passed"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = style.badgeText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isArabic) item.titleAr else item.titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = style.titleColor,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.dateSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = style.subtitleColor,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
