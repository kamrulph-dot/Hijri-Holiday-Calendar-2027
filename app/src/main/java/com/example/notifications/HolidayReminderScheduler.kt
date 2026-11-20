package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.calendar.ArabHolidaysProvider
import com.example.data.model.ArabCountry
import com.example.data.model.Holiday
import com.example.data.model.HolidayType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class HolidayReminderTarget(
    val holiday: Holiday,
    val reminderDate: LocalDate,
    val triggerAtMillis: Long,
    val requestCode: Int
)

object HolidayReminderScheduler {

    const val CHANNEL_ID = "national_holiday_reminders_v2"
    private const val PREFS_NAME = "holiday_reminder_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_COUNTRY = "country_code"
    private const val KEY_OFFSET = "hijri_offset"
    private const val KEY_LANG = "language_code"
    private const val KEY_SCHEDULED_CODES = "scheduled_request_codes"
    private const val MAX_SCHEDULED_ALARMS = 24
    private const val REMINDER_HOUR = 9

    fun ensureNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_holidays_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_holidays_desc)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * Computes upcoming national holiday reminders for the given country,
     * each scheduled 1 day before the holiday at 09:00 local time.
     */
    fun getUpcomingReminderTargets(
        countryCode: String,
        hijriOffset: Int,
        now: ZonedDateTime = ZonedDateTime.now(),
        zoneId: ZoneId = now.zone
    ): List<HolidayReminderTarget> {
        val currentYear = now.year
        val holidays = (ArabHolidaysProvider.getHolidays(countryCode, currentYear, hijriOffset) +
            ArabHolidaysProvider.getHolidays(countryCode, currentYear + 1, hijriOffset))
            .filter { it.type == HolidayType.NATIONAL }
            .distinctBy { "${it.id}_${it.date}" }
            .sortedBy { it.date }

        val targets = mutableListOf<HolidayReminderTarget>()
        val today = now.toLocalDate()
        for (holiday in holidays) {
            val reminderDate = holiday.date.minusDays(1)
            val triggerDateTime = ZonedDateTime.of(reminderDate, LocalTime.of(REMINDER_HOUR, 0), zoneId)
            val effectiveTriggerMillis = when {
                triggerDateTime.isAfter(now) -> triggerDateTime.toInstant().toEpochMilli()
                reminderDate == today -> now.plusSeconds(10).toInstant().toEpochMilli()
                else -> null
            }
            if (effectiveTriggerMillis != null) {
                val requestCode = ("${holiday.countryCode}_${holiday.id}_${holiday.date}").hashCode() and 0x7FFFFFFF
                targets.add(
                    HolidayReminderTarget(
                        holiday = holiday,
                        reminderDate = reminderDate,
                        triggerAtMillis = effectiveTriggerMillis,
                        requestCode = requestCode
                    )
                )
            }
            if (targets.size >= MAX_SCHEDULED_ALARMS) break
        }
        return targets
    }

    fun getNextUpcomingTarget(
        countryCode: String,
        hijriOffset: Int,
        now: ZonedDateTime = ZonedDateTime.now()
    ): HolidayReminderTarget? {
        return getUpcomingReminderTargets(countryCode, hijriOffset, now).firstOrNull()
    }

    fun syncAndSchedule(
        context: Context,
        enabled: Boolean,
        countryCode: String,
        hijriOffset: Int,
        languageCode: String
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putString(KEY_COUNTRY, countryCode)
            .putInt(KEY_OFFSET, hijriOffset)
            .putString(KEY_LANG, languageCode)
            .apply()

        cancelAllScheduled(context)

        if (!enabled) return

        ensureNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val targets = getUpcomingReminderTargets(countryCode, hijriOffset)
        val scheduledCodes = mutableSetOf<String>()

        for (target in targets) {
            val intent = Intent(context, HolidayReminderReceiver::class.java).apply {
                action = HolidayReminderReceiver.ACTION_HOLIDAY_REMINDER
                putExtra(HolidayReminderReceiver.EXTRA_HOLIDAY_ID, target.holiday.id)
                putExtra(HolidayReminderReceiver.EXTRA_COUNTRY_CODE, countryCode)
                putExtra(HolidayReminderReceiver.EXTRA_TITLE_EN, target.holiday.titleEn)
                putExtra(HolidayReminderReceiver.EXTRA_TITLE_AR, target.holiday.titleAr)
                putExtra(HolidayReminderReceiver.EXTRA_HOLIDAY_DATE, target.holiday.date.toString())
                putExtra(HolidayReminderReceiver.EXTRA_IS_ARABIC, languageCode == "ar")
                putExtra(HolidayReminderReceiver.EXTRA_NOTIFICATION_ID, target.requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                target.requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                target.triggerAtMillis,
                pendingIntent
            )
            scheduledCodes.add(target.requestCode.toString())
        }

        prefs.edit().putStringSet(KEY_SCHEDULED_CODES, scheduledCodes).apply()
    }

    fun rescheduleFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_ENABLED, false)
        if (!enabled) return

        val countryCode = prefs.getString(KEY_COUNTRY, "SA") ?: "SA"
        val offset = prefs.getInt(KEY_OFFSET, 0)
        val lang = prefs.getString(KEY_LANG, "en") ?: "en"
        syncAndSchedule(context, true, countryCode, offset, lang)
    }

    fun cancelAllScheduled(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingCodes = prefs.getStringSet(KEY_SCHEDULED_CODES, emptySet()).orEmpty()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (alarmManager != null) {
            for (codeStr in existingCodes) {
                val code = codeStr.toIntOrNull() ?: continue
                val intent = Intent(context, HolidayReminderReceiver::class.java).apply {
                    action = HolidayReminderReceiver.ACTION_HOLIDAY_REMINDER
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    code,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
        prefs.edit().remove(KEY_SCHEDULED_CODES).apply()
    }

    fun showHolidayNotification(
        context: Context,
        notificationId: Int,
        countryCode: String,
        titleEn: String,
        titleAr: String,
        holidayDate: String,
        isArabic: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureNotificationChannel(context)
        val country = ArabCountry.fromCode(countryCode)

        val notifTitle = if (isArabic) {
            "غداً: $titleAr"
        } else {
            "Tomorrow: $titleEn"
        }

        val notifBody = if (isArabic) {
            "عطلة وطنية في ${country.nameAr} بتاريخ $holidayDate."
        } else {
            "Upcoming ${country.nameEn} national holiday on $holidayDate."
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_calendar)
            .setContentTitle(notifTitle)
            .setContentText(notifBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notifBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun sendTestHolidayNotification(
        context: Context,
        countryCode: String,
        hijriOffset: Int,
        isArabic: Boolean
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }

        val nextTarget = getNextUpcomingTarget(countryCode, hijriOffset)
        val holidayTitleEn = nextTarget?.holiday?.titleEn ?: "National Holiday"
        val holidayTitleAr = nextTarget?.holiday?.titleAr ?: "عطلة وطنية"
        val holidayDate = nextTarget?.holiday?.date?.toString() ?: LocalDate.now().plusDays(1).toString()

        showHolidayNotification(
            context = context,
            notificationId = 992027,
            countryCode = countryCode,
            titleEn = holidayTitleEn,
            titleAr = holidayTitleAr,
            holidayDate = holidayDate,
            isArabic = isArabic
        )
        return true
    }
}
