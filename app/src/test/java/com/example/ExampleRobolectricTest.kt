package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aula Virtual", appName)
  }

  @Test
  fun `course weighted average and attendance calculation`() {
    val course = com.example.data.model.Course(
      code = "IS-601",
      name = "Arquitectura de Software",
      gradeEc1 = 16.0,
      gradeEp = 18.0,
      gradeEc2 = 14.0,
      gradeEf = 16.0,
      attendancePresent = 9L,
      attendanceTotal = 10L
    )
    assertEquals(16.0, course.weightedAverage(), 0.01)
    assertEquals(90, course.attendancePercent())
  }
}
