package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ArabCountry
import com.example.notifications.HolidayReminderScheduler
import com.example.ui.components.CountryPickerBottomSheet
import com.example.ui.theme.Emerald800
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.viewmodel.CalendarUiState
import java.time.DayOfWeek

@Composable
fun SettingsScreen(
    uiState: CalendarUiState,
    onCountrySelected: (ArabCountry) -> Unit,
    onHijriOffsetChange: (Int) -> Unit,
    onLanguageChange: (String) -> Unit,
    onFirstDayOfWeekChange: (DayOfWeek) -> Unit,
    onDarkModeChange: (String) -> Unit,
    onHolidayReminderChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = uiState.isArabic
    val context = LocalContext.current
    var showCountryPicker by remember { mutableStateOf(false) }
    var notificationPermissionDenied by remember { mutableStateOf(false) }
    var pendingTestNotification by remember { mutableStateOf(false) }
    var testNotificationStatus by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            notificationPermissionDenied = false
            onHolidayReminderChange(true)
            if (pendingTestNotification) {
                pendingTestNotification = false
                val sent = HolidayReminderScheduler.sendTestHolidayNotification(
                    context = context,
                    countryCode = uiState.selectedCountry.code,
                    hijriOffset = uiState.hijriOffset,
                    isArabic = isArabic
                )
                testNotificationStatus = if (sent) {
                    if (isArabic) "تم إرسال إشعار تجريبي." else "Test holiday notification sent."
                } else null
            }
        } else {
            pendingTestNotification = false
            notificationPermissionDenied = true
            onHolidayReminderChange(false)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = if (isArabic) "الإعدادات والتخصيص" else "Settings & Preferences",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Country Selection Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCountryPicker = true }
                .testTag("settings_country_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Emerald800.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = uiState.selectedCountry.flag, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isArabic) "الدولة المختارة" else "Selected Country",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) uiState.selectedCountry.nameAr else uiState.selectedCountry.nameEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isArabic) "انقر للتغيير بين الدول العربية الـ 22" else "Tap to change between 22 Arab countries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1b. National Holiday Reminders (1 Day Before)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_holiday_reminder_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Emerald800
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "تذكيرات العطلات الوطنية" else "National Holiday Reminders",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic)
                                    "تنبيه قبل يوم واحد من العطلات الوطنية في ${uiState.selectedCountry.nameAr}"
                                else
                                    "Remind 1 day before holidays in ${uiState.selectedCountry.nameEn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = uiState.holidayReminderEnabled,
                        onCheckedChange = { enable ->
                            if (!enable) {
                                notificationPermissionDenied = false
                                onHolidayReminderChange(false)
                            } else if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                notificationPermissionDenied = false
                                onHolidayReminderChange(true)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Emerald800
                        ),
                        modifier = Modifier.testTag("holiday_reminder_switch")
                    )
                }

                uiState.nextHolidayReminder?.let { target ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald800.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isArabic)
                                "التذكير القادم: ${target.holiday.titleAr} • التنبيه في ${target.reminderDate} (09:00 ص) قبل عطلة ${target.holiday.date}"
                            else
                                "Next reminder: ${target.holiday.titleEn} • Alert on ${target.reminderDate} at 09:00 AM (Holiday: ${target.holiday.date})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald800,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (notificationPermissionDenied) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic)
                            "يلزم منح إذن الإشعارات لتفعيل تذكيرات العطلات الوطنية."
                        else
                            "Notification permission is required to enable holiday reminders. Please allow notifications in system settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            pendingTestNotification = true
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val sent = HolidayReminderScheduler.sendTestHolidayNotification(
                                context = context,
                                countryCode = uiState.selectedCountry.code,
                                hijriOffset = uiState.hijriOffset,
                                isArabic = isArabic
                            )
                            testNotificationStatus = if (sent) {
                                if (isArabic) "تم إرسال إشعار تجريبي." else "Test holiday notification sent."
                            } else {
                                if (isArabic) "تعذر إرسال الإشعار." else "Unable to send notification."
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_holiday_notification_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Emerald800
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "إرسال إشعار تجريبي" else "Send Test Holiday Notification",
                        color = Emerald800,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                testNotificationStatus?.let { status ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald800,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Hijri Manual Date Adjustment (-2 to +2 days)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_hijri_adjustment_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = GoldDark
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isArabic) "تعديل التاريخ الهجري يدوياً" else "Hijri Date Manual Adjustment",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Stepper: - and +
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onHijriOffsetChange(uiState.hijriOffset - 1) },
                            enabled = uiState.hijriOffset > -2
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease offset")
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${if (uiState.hijriOffset > 0) "+" else ""}${uiState.hijriOffset} ${if (isArabic) "يوم" else "d"}",
                                fontWeight = FontWeight.Bold,
                                color = GoldDark
                            )
                        }

                        IconButton(
                            onClick = { onHijriOffsetChange(uiState.hijriOffset + 1) },
                            enabled = uiState.hijriOffset < 2
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase offset")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isArabic)
                        "يتيح لك تقديم أو تأخير التاريخ الهجري من -2 إلى +2 يوم لمطابقة إعلان رؤية الهلال الرسمي في بلدك بدقة."
                    else
                        "Adjust Hijri dates between -2 and +2 days to match official local moon-sighting announcements.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Language Selection (Arabic / English)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "لغة التطبيق" else "App Language",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (uiState.languageCode == "en") Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onLanguageChange("en") }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "English",
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.languageCode == "en") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (uiState.languageCode == "ar") Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onLanguageChange("ar") }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "العربية (RTL)",
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.languageCode == "ar") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. First Day of Week
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) "بداية الأسبوع في التقويم" else "First Day of the Week",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        DayOfWeek.SATURDAY to (if (isArabic) "السبت (عربي)" else "Saturday"),
                        DayOfWeek.SUNDAY to (if (isArabic) "الأحد" else "Sunday"),
                        DayOfWeek.MONDAY to (if (isArabic) "الإثنين" else "Monday")
                    ).forEach { (day, label) ->
                        val isSelected = uiState.firstDayOfWeek == day
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onFirstDayOfWeekChange(day) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Theme Mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "المظهر (الوضع الليلي)" else "Display Theme",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "light" to (if (isArabic) "فاتح" else "Light"),
                        "dark" to (if (isArabic) "داكن" else "Dark"),
                        "system" to (if (isArabic) "تلقائي" else "System")
                    ).forEach { (mode, label) ->
                        val isSelected = uiState.darkMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onDarkModeChange(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Unofficial App Disclaimer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_disclaimer_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "إخلاء مسؤولية — تطبيق غير رسمي" else "Disclaimer — Unofficial App",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isArabic)
                        "هذا تطبيق مستقل وغير رسمي، ولا يتبع أو يمثل أو يحظى برعاية أو اعتماد أي حكومة أو وزارة أو سفارة أو هيئة دينية أو مرجعية رسمية لاستطلاع الأهلة في أي من الدول العربية الـ 22.\n\nجميع التواريخ الهجرية والعطلات الإسلامية المعروضة هي تقديرات فلكية تقريبية (غير مؤكدة) وتخضع دائماً للإعلان الرسمي لثبوت رؤية الهلال والقرارات الحكومية المحلية في كل دولة."
                    else
                        "This is an independent, unofficial calendar application. It is NOT affiliated with, endorsed by, or representing any government, ministry, embassy, or official religious or moon-sighting authority in any of the 22 Arab League nations.\n\nAll Hijri dates and Islamic public holidays are astronomical estimates (Tentative) and remain subject to official local moon-sighting announcements (Ru'yah) and government decrees.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Holiday & Calendar Data Sources
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_sources_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = GoldDark
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "مصادر بيانات العطلات والتقويم" else "Holiday & Calendar Data Sources",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isArabic)
                        "• الجداول الرسمية المنشورة للعطلات الوطنية والقرارات الحكومية في الدول العربية الـ 22.\n• قواعد بيانات العطلات العالمية الموثوقة: timeanddate.com و officeholidays.com.\n• الحسابات الفلكية لتقويم أم القرى (HijrahChronology) للمناسبات الإسلامية والهجري.\n• الخوارزميات الفلكية القياسية (Meeus Julian / Gregorian) لحساب الأعياد القبطية والمسيحية المتحركة (مثل شم النسيم وعيد القيامة)."
                    else
                        "• Published national holiday schedules and public decrees of the 22 Arab League nations.\n• Public holiday reference portals: timeanddate.com and officeholidays.com.\n• Umm al-Qura astronomical Hijri calculations (java.time.chrono.HijrahChronology).\n• Standard Julian & Gregorian Easter algorithms for movable holidays (e.g., Sham El-Nessim & Easter).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 8. About App
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "حول تقويم العطلات الهجرية 2027" else "About Hijri Holiday Calendar 2027",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isArabic)
                        "تقويم العطلات الهجرية 2027 هو تطبيق تقويم إسلامي وميلادي مخصص لجميع الدول العربية الـ 22. يتضمن العطلات الوطنية والدينية، وتذكيرات محلية قبل العطلات الوطنية بيوم واحد، والمناسبات الإسلامية، ومحول تواريخ دقيق يعمل بدون إنترنت."
                    else
                        "Hijri Holiday Calendar 2027 is a modern, lightweight Islamic & Gregorian calendar application built for all 22 Arab League nations. Includes national & religious holidays, optional 1-day-before holiday reminders, Islamic events, date conversion, and local event persistence offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Version 1.0 (2027 Launch Edition) • Unofficial & Offline First",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    if (showCountryPicker) {
        CountryPickerBottomSheet(
            selectedCountry = uiState.selectedCountry,
            isArabic = isArabic,
            onCountrySelected = {
                onCountrySelected(it)
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }
}
