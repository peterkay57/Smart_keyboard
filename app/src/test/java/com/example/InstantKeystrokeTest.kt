package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.hid.HidConstants
import com.example.ui.viewmodel.BluetoothKeyboardViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InstantKeystrokeTest {

    private lateinit var app: Application
    private lateinit var viewModel: BluetoothKeyboardViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = BluetoothKeyboardViewModel(app)
    }

    @Test
    fun testInstantKeyPress_updatesLastTypedKeyInstantly() {
        viewModel.typeCharacter('h')
        assertEquals("h", viewModel.lastTypedKey.value)

        viewModel.toggleShift()
        viewModel.typeCharacter('e')
        assertEquals("E", viewModel.lastTypedKey.value)

        viewModel.typeCharacter('l')
        assertEquals("l", viewModel.lastTypedKey.value)

        viewModel.toggleCapsLock()
        viewModel.typeCharacter('o')
        assertEquals("O", viewModel.lastTypedKey.value)
    }

    @Test
    fun testSpecialKey_updatesLastTypedKeyInstantly() {
        viewModel.typeSpecialKey(HidConstants.KEY_ENTER, "Enter")
        assertEquals("Enter", viewModel.lastTypedKey.value)

        viewModel.typeSpecialKey(HidConstants.KEY_BACKSPACE, "Backspace")
        assertEquals("Backspace", viewModel.lastTypedKey.value)
    }

    @Test
    fun testCharToHidMappings_completeCoverage() {
        val testChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}|;':,.<>?/`~ \n\t"
        for (c in testChars) {
            val hid = HidConstants.charToHid(c)
            assertNotNull("Character '$c' should have a valid HID mapping", hid)
        }
    }
}
