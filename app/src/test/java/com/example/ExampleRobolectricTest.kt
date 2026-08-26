package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.hid.HidConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bluetooth Keyboard", appName)
    }

    @Test
    fun `test char to hid key mapping`() {
        val aKey = HidConstants.charToHid('a')
        assertNotNull(aKey)
        assertEquals(HidConstants.KEY_A, aKey?.second)
        assertEquals(HidConstants.MOD_NONE, aKey?.first)

        val capitalAKey = HidConstants.charToHid('A')
        assertNotNull(capitalAKey)
        assertEquals(HidConstants.KEY_A, capitalAKey?.second)
        assertEquals(HidConstants.MOD_LSHIFT, capitalAKey?.first)

        val enterKey = HidConstants.charToHid('\n')
        assertNotNull(enterKey)
        assertEquals(HidConstants.KEY_ENTER, enterKey?.second)
    }
}
