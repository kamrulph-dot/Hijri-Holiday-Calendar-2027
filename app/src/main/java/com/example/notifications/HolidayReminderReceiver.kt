package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HolidayReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_HOLIDAY_REMINDER -> {
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)
                val countryCode = intent.getStringExtra(EXTRA_COUNTRY_CODE) ?: "SA"
                val titleEn = intent.getStringExtra(EXTRA_TITLE_EN) ?: "National Holiday"
                val titleAr = intent.getStringExtra(EXTRA_TITLE_AR) ?: "عطلة وطنية"
                val holidayDate = intent.getStringExtra(EXTRA_HOLIDAY_DATE) ?: ""
                val isArabic = intent.getBooleanExtra(EXTRA_IS_ARABIC, false)

                HolidayReminderScheduler.showHolidayNotification(
                    context = context,
                    notificationId = notificationId,
                    countryCode = countryCode,
                    titleEn = titleEn,
                    titleAr = titleAr,
                    holidayDate = holidayDate,
                    isArabic = isArabic
                )
                HolidayReminderScheduler.rescheduleFromPreferences(context)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                HolidayReminderScheduler.rescheduleFromPreferences(context)
            }
        }
    }

    companion object {
        const val ACTION_HOLIDAY_REMINDER = "com.example.ACTION_HOLIDAY_REMINDER"
        const val EXTRA_HOLIDAY_ID = "extra_holiday_id"
        const val EXTRA_COUNTRY_CODE = "extra_country_code"
        const val EXTRA_TITLE_EN = "extra_title_en"
        const val EXTRA_TITLE_AR = "extra_title_ar"
        const val EXTRA_HOLIDAY_DATE = "extra_holiday_date"
        const val EXTRA_IS_ARABIC = "extra_is_arabic"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
