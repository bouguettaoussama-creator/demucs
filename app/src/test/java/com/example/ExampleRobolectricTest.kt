package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DemucsPreset
import com.example.model.DemucsSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Demucs Audio", appName)
  }

  @Test
  fun `verify demucs default settings and presets`() {
    val defaultSettings = DemucsSettings.DEFAULT
    assertEquals(0.75f, defaultSettings.separationIntensity, 0.01f)
    assertEquals(DemucsPreset.BALANCED, defaultSettings.activePreset)

    val karaokeSettings = DemucsSettings.fromPreset(DemucsPreset.PURE_KARAOKE)
    assertEquals(0.95f, karaokeSettings.separationIntensity, 0.01f)
    assertEquals(DemucsPreset.PURE_KARAOKE, karaokeSettings.activePreset)
  }

  @Test
  fun `launch MainActivity`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    assertNotNull(controller.get())
  }
}
