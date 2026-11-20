package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobBanner
import com.example.ads.AdMobManager
import com.example.data.model.ArabCountry
import com.example.ui.components.CountryPickerBottomSheet
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ArabCalendarTheme
import com.example.ui.theme.Emerald800
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.mixedQuadColorBackground
import com.example.viewmodel.CalendarViewModel
import java.time.LocalDate

enum class AppScreen(
    val route: String,
    val titleEn: String,
    val titleAr: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    HOME("home", "Home", "الرئيسية", Icons.Default.Home, Icons.Outlined.Home),
    CALENDAR("calendar", "Calendar", "التقويم", Icons.Default.CalendarMonth, Icons.Outlined.CalendarMonth),
    EVENTS("events", "Events", "المناسبات", Icons.Default.Celebration, Icons.Outlined.Celebration),
    CONVERTER("converter", "Converter", "المحول", Icons.Default.SwapHoriz, Icons.Outlined.SwapHoriz),
    SETTINGS("settings", "Settings", "الإعدادات", Icons.Default.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdMobManager.initialize(this)

        setContent {
            val viewModel: CalendarViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val personalEvents by viewModel.personalEvents.collectAsStateWithLifecycle()

            val isDark = when (uiState.darkMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            ArabCalendarTheme(darkTheme = isDark) {
                val layoutDirection = if (uiState.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    ArabCalendarApp(
                        viewModel = viewModel,
                        uiState = uiState,
                        personalEvents = personalEvents,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
fun ArabCalendarApp(
    viewModel: CalendarViewModel,
    uiState: com.example.viewmodel.CalendarUiState,
    personalEvents: List<com.example.data.local.entity.PersonalEventEntity>,
    isDark: Boolean
) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var showCountryPickerSheet by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    val startupPrefs = remember(context) {
        context.getSharedPreferences("app_startup_prefs", Context.MODE_PRIVATE)
    }

    val startupNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        startupPrefs.edit().putBoolean("has_prompted_holiday_notifications", true).apply()
        if (granted) {
            viewModel.setHolidayReminderEnabled(true)
        }
    }

    LaunchedEffect(Unit) {
        val alreadyPrompted = startupPrefs.getBoolean("has_prompted_holiday_notifications", false)
        if (!alreadyPrompted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    startupPrefs.edit().putBoolean("has_prompted_holiday_notifications", true).apply()
                    viewModel.setHolidayReminderEnabled(true)
                } else {
                    startupNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                startupPrefs.edit().putBoolean("has_prompted_holiday_notifications", true).apply()
                viewModel.setHolidayReminderEnabled(true)
            }
        }
    }

    fun navigateTo(target: AppScreen) {
        if (target == currentScreen) return
        if (target == AppScreen.CONVERTER || target == AppScreen.EVENTS) {
            AdMobManager.showInterstitialIfReady(activity) {
                currentScreen = target
            }
        } else {
            currentScreen = target
        }
    }

    // Android back button handling: return to home screen if on a secondary screen, otherwise ask to exit
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        currentScreen = AppScreen.HOME
    }

    BackHandler(enabled = currentScreen == AppScreen.HOME) {
        showExitDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mixedQuadColorBackground(isDark = isDark)
            .testTag("mixed_background_root")
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AdMobBanner(adUnitId = AdMobManager.BANNER_AD_UNIT_ID)
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        AppScreen.entries.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { navigateTo(screen) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                        contentDescription = if (uiState.isArabic) screen.titleAr else screen.titleEn
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (uiState.isArabic) screen.titleAr else screen.titleEn,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Emerald800,
                                    selectedTextColor = Emerald800,
                                    indicatorColor = GoldPrimary.copy(alpha = 0.2f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onCountryClick = { showCountryPickerSheet = true },
                        onCountrySelected = { country -> viewModel.setCountry(country) },
                        onJumpToToday = { viewModel.jumpToToday() },
                        onJumpTo2027 = { viewModel.jumpTo2027() },
                        onDateSelected = { date ->
                            viewModel.selectDate(date)
                            currentScreen = AppScreen.CALENDAR
                        },
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onNavigateToCalendar = { navigateTo(AppScreen.CALENDAR) },
                        onNavigateToConverter = { navigateTo(AppScreen.CONVERTER) },
                        onNavigateToEvents = { navigateTo(AppScreen.EVENTS) }
                    )
                }

                AppScreen.CALENDAR -> {
                    CalendarScreen(
                        uiState = uiState,
                        onDateSelected = { date -> viewModel.selectDate(date) },
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onJumpToToday = { viewModel.jumpToToday() },
                        onJumpTo2027 = { viewModel.jumpTo2027() },
                        onSetYear = { yr -> viewModel.setYear(yr) },
                        onSetMonth = { m -> viewModel.setMonth(m) },
                        onToggleViewMode = { mode -> viewModel.setCalendarViewMode(mode) },
                        onAddPersonalEvent = { title, desc, date, time, cat, fav ->
                            viewModel.addPersonalEvent(title, desc, date, time, cat, fav)
                        },
                        onDeletePersonalEvent = { id -> viewModel.deletePersonalEvent(id) },
                        onToggleFavoriteEvent = { ev -> viewModel.toggleFavoriteEvent(ev) },
                        onNavigateToConverterWithDate = { date ->
                            viewModel.convertGregorianDate(date)
                            currentScreen = AppScreen.CONVERTER
                        }
                    )
                }

                AppScreen.EVENTS -> {
                    EventsScreen(
                        uiState = uiState,
                        personalEvents = personalEvents,
                        onCountrySelected = { country -> viewModel.setCountry(country) },
                        onSearchQueryChange = { q -> viewModel.setSearchQuery(q) },
                        onDeletePersonalEvent = { id -> viewModel.deletePersonalEvent(id) },
                        onToggleFavoriteEvent = { ev -> viewModel.toggleFavoriteEvent(ev) },
                        onDateSelected = { date ->
                            viewModel.selectDate(date)
                            currentScreen = AppScreen.CALENDAR
                        }
                    )
                }

                AppScreen.CONVERTER -> {
                    ConverterScreen(
                        uiState = uiState,
                        onConvertGregorian = { date -> viewModel.convertGregorianDate(date) },
                        onConvertHijri = { y, m, d -> viewModel.convertHijriDate(y, m, d) },
                        onNavigateToCalendarDate = { date ->
                            viewModel.selectDate(date)
                            currentScreen = AppScreen.CALENDAR
                        }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState,
                        onCountrySelected = { country -> viewModel.setCountry(country) },
                        onHijriOffsetChange = { offset -> viewModel.setHijriOffset(offset) },
                        onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                        onFirstDayOfWeekChange = { day -> viewModel.setFirstDayOfWeek(day) },
                        onDarkModeChange = { mode -> viewModel.setDarkMode(mode) },
                        onHolidayReminderChange = { enabled -> viewModel.setHolidayReminderEnabled(enabled) }
                    )
                }
            }

            if (showCountryPickerSheet) {
                CountryPickerBottomSheet(
                    selectedCountry = uiState.selectedCountry,
                    isArabic = uiState.isArabic,
                    onCountrySelected = { country ->
                        viewModel.setCountry(country)
                        showCountryPickerSheet = false
                    },
                    onDismiss = { showCountryPickerSheet = false }
                )
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = {
                        Text(
                            text = if (uiState.isArabic) "الخروج من التطبيق" else "Exit App"
                        )
                    },
                    text = {
                        Text(
                            text = if (uiState.isArabic) "هل تريد الخروج؟" else "Do you want to exit?"
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExitDialog = false
                                activity?.finish()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                            modifier = Modifier.testTag("exit_confirm_button")
                        ) {
                            Text(if (uiState.isArabic) "نعم، خروج" else "Exit")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showExitDialog = false },
                            modifier = Modifier.testTag("exit_cancel_button")
                        ) {
                            Text(if (uiState.isArabic) "إلغاء" else "Cancel")
                        }
                    },
                    modifier = Modifier.testTag("exit_confirmation_dialog")
                )
            }
        }
    }
    }
}
