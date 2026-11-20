package com.example

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.notifications.HolidayReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hijri Holiday Calendar 2027", appName)
  }

  @Test
  fun `schedule and cancel national holiday reminders`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val shadowAlarm = Shadows.shadowOf(alarmManager)

    HolidayReminderScheduler.syncAndSchedule(
      context = context,
      enabled = true,
      countryCode = "SA",
      hijriOffset = 0,
      languageCode = "en"
    )
    assertTrue("Expected scheduled alarms when reminders enabled", shadowAlarm.scheduledAlarms.isNotEmpty())

    HolidayReminderScheduler.syncAndSchedule(
      context = context,
      enabled = false,
      countryCode = "SA",
      hijriOffset = 0,
      languageCode = "en"
    )
    assertTrue("Expected alarms cleared when reminders disabled", shadowAlarm.scheduledAlarms.isEmpty())
  }
}
