package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.dictionary.WordSuggestionEngine
import com.example.data.hid.HidConstants
import com.example.ui.viewmodel.BluetoothKeyboardViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WordSuggestionTest {

    private lateinit var app: Application
    private lateinit var engine: WordSuggestionEngine

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        engine = WordSuggestionEngine(app)
    }

    @Test
    fun testWordSuggestionEngine_prefixMatching() {
        val suggestions = engine.getSuggestions("hel", 5)
        assertTrue("Suggestions should contain words starting with 'hel'", suggestions.isNotEmpty())
        assertTrue("Should suggest 'hello' or 'help'", suggestions.contains("hello") || suggestions.contains("help"))
    }

    @Test
    fun testWordSuggestionEngine_capitalizationPreserved() {
        val capitalized = engine.getSuggestions("Hel", 5)
        assertTrue("Capitalized prefix should return capitalized suggestions", capitalized.first().first().isUpperCase())

        val upper = engine.getSuggestions("HEL", 5)
        assertTrue("All-caps prefix should return all-caps suggestions", upper.first().all { it.isUpperCase() })
    }

    @Test
    fun testWordSuggestionEngine_nextWordPredictions() {
        val nextWords = engine.getNextWordPredictions("thank")
        assertTrue("After 'thank', suggestions should contain 'you'", nextWords.contains("you"))
    }

    @Test
    fun testViewModel_typingUpdatesSuggestions() {
        val viewModel = BluetoothKeyboardViewModel(app)
        
        viewModel.typeCharacter('h')
        viewModel.typeCharacter('e')
        viewModel.typeCharacter('l')

        assertEquals("hel", viewModel.composingWord.value)
        val currentSuggestions = viewModel.suggestions.value
        assertTrue("Suggestions should have candidate words for 'hel'", currentSuggestions.isNotEmpty())
        assertTrue("Suggestions should contain 'hello' or 'help'", currentSuggestions.contains("hello") || currentSuggestions.contains("help"))

        viewModel.selectSuggestion("hello")
        assertEquals("hello ", viewModel.textBuffer.value)
        assertEquals("", viewModel.composingWord.value)
    }

    @Test
    fun testViewModel_spaceAutoInsertsFirstSuggestion() {
        val viewModel = BluetoothKeyboardViewModel(app)
        
        viewModel.typeCharacter('t')
        viewModel.typeCharacter('h')
        viewModel.typeCharacter('a')
        viewModel.typeCharacter('n')
        viewModel.typeCharacter('k')

        val topSuggestion = viewModel.suggestions.value.first()
        viewModel.typeSpecialKey(HidConstants.KEY_SPACE, "Space")

        assertEquals("$topSuggestion ", viewModel.textBuffer.value)
    }
}
