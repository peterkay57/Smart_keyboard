package com.example.data.dictionary

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.UserDictionary
import android.view.textservice.SentenceSuggestionsInfo
import android.view.textservice.SpellCheckerSession
import android.view.textservice.SuggestionsInfo
import android.view.textservice.TextInfo
import android.view.textservice.TextServicesManager
import java.util.Locale

class WordSuggestionEngine(private val context: Context) : SpellCheckerSession.SpellCheckerSessionListener {

    private var spellCheckerSession: SpellCheckerSession? = null
    private var lastSpellSuggestions: List<String> = emptyList()

    init {
        try {
            val tsm = context.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE) as? TextServicesManager
            if (tsm != null) {
                val session = tsm.newSpellCheckerSession(null, Locale.getDefault(), this, true)
                spellCheckerSession = session
            }
        } catch (_: Exception) {
            // TextServicesManager might be unavailable on some test runners or restricted environments
        }
    }

    // Android SpellChecker callbacks
    override fun onGetSuggestions(results: Array<out SuggestionsInfo>?) {
        if (results == null) return
        val list = mutableListOf<String>()
        for (info in results) {
            val count = info.suggestionsCount
            for (i in 0 until count) {
                val suggestion = info.getSuggestionAt(i)
                if (!suggestion.isNullOrBlank() && !list.contains(suggestion)) {
                    list.add(suggestion)
                }
            }
        }
        if (list.isNotEmpty()) {
            lastSpellSuggestions = list
        }
    }

    override fun onGetSentenceSuggestions(results: Array<out SentenceSuggestionsInfo>?) {
        if (results == null) return
        val list = mutableListOf<String>()
        for (sentenceInfo in results) {
            val count = sentenceInfo.suggestionsCount
            for (i in 0 until count) {
                val info = sentenceInfo.getSuggestionsInfoAt(i)
                val sCount = info.suggestionsCount
                for (j in 0 until sCount) {
                    val sug = info.getSuggestionAt(j)
                    if (!sug.isNullOrBlank() && !list.contains(sug)) {
                        list.add(sug)
                    }
                }
            }
        }
        if (list.isNotEmpty()) {
            lastSpellSuggestions = list
        }
    }

    fun requestSpellCheck(word: String) {
        if (word.length < 2) return
        try {
            spellCheckerSession?.getSuggestions(TextInfo(word), 5)
        } catch (_: Exception) {}
    }

    /**
     * Given the currently typed prefix/word, returns 3 to 5 candidate suggestions.
     */
    fun getSuggestions(prefix: String, maxCount: Int = 5): List<String> {
        if (prefix.isBlank()) {
            return getDefaultPredictions()
        }

        val cleanPrefix = prefix.trim()
        val isAllUpper = cleanPrefix.length > 1 && cleanPrefix.all { it.isUpperCase() }
        val isFirstUpper = cleanPrefix.first().isUpperCase()
        val lowerPrefix = cleanPrefix.lowercase(Locale.ROOT)

        val results = mutableListOf<String>()

        // 1. Check user dictionary (if available)
        queryUserDictionary(lowerPrefix, results, maxCount)

        // 2. Query built-in frequency dictionary
        for (word in COMMON_ENGLISH_WORDS) {
            if (results.size >= maxCount) break
            if (word.startsWith(lowerPrefix) && !results.contains(word)) {
                results.add(word)
            }
        }

        // 3. Fallback to Android spell checker results if prefix is not a complete prefix match
        if (results.size < maxCount) {
            for (sug in lastSpellSuggestions) {
                val sLower = sug.lowercase(Locale.ROOT)
                if (sLower.startsWith(lowerPrefix) && !results.contains(sLower)) {
                    results.add(sLower)
                }
            }
        }

        // 4. If still have room, check substring or fuzzy matches
        if (results.size < maxCount && cleanPrefix.length >= 3) {
            for (word in COMMON_ENGLISH_WORDS) {
                if (results.size >= maxCount) break
                if (word.contains(lowerPrefix) && !results.contains(word)) {
                    results.add(word)
                }
            }
        }

        // If no match found, at least provide the typed word itself
        if (results.isEmpty()) {
            results.add(lowerPrefix)
        }

        // Apply original casing
        return results.take(maxCount).map { word ->
            when {
                isAllUpper -> word.uppercase(Locale.ROOT)
                isFirstUpper -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                else -> word
            }
        }
    }

    /**
     * Default next-word suggestions when cursor is after a space or at start.
     */
    fun getDefaultPredictions(): List<String> {
        return listOf("hello", "hi", "how", "have", "the")
    }

    /**
     * Next-word suggestions based on the previous word.
     */
    fun getNextWordPredictions(previousWord: String): List<String> {
        val lower = previousWord.trim().lowercase(Locale.ROOT)
        return NEXT_WORD_PREDICTIONS[lower] ?: getDefaultPredictions()
    }

    private fun queryUserDictionary(prefix: String, targetList: MutableList<String>, maxCount: Int) {
        try {
            val cursor: Cursor? = context.contentResolver.query(
                UserDictionary.Words.CONTENT_URI,
                arrayOf(UserDictionary.Words.WORD),
                "${UserDictionary.Words.WORD} LIKE ?",
                arrayOf("$prefix%"),
                "${UserDictionary.Words.FREQUENCY} DESC"
            )
            cursor?.use {
                val wordIndex = it.getColumnIndex(UserDictionary.Words.WORD)
                while (it.moveToNext() && targetList.size < maxCount) {
                    if (wordIndex != -1) {
                        val word = it.getString(wordIndex).lowercase(Locale.ROOT)
                        if (!targetList.contains(word)) {
                            targetList.add(word)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Permission or provider not present
        }
    }

    fun close() {
        try {
            spellCheckerSession?.cancel()
            spellCheckerSession?.close()
        } catch (_: Exception) {}
    }

    companion object {
        private val NEXT_WORD_PREDICTIONS = mapOf(
            "i" to listOf("am", "have", "will", "think", "would"),
            "you" to listOf("are", "can", "have", "will", "know"),
            "it" to listOf("is", "was", "will", "would", "has"),
            "we" to listOf("are", "can", "will", "have", "need"),
            "they" to listOf("are", "were", "have", "will", "can"),
            "he" to listOf("is", "was", "has", "will", "said"),
            "she" to listOf("is", "was", "has", "will", "said"),
            "the" to listOf("first", "best", "same", "next", "other"),
            "how" to listOf("are", "is", "to", "do", "can"),
            "what" to listOf("is", "are", "do", "time", "about"),
            "thank" to listOf("you", "you very much", "god", "everyone", "all"),
            "good" to listOf("morning", "afternoon", "evening", "night", "job"),
            "please" to listOf("let", "find", "check", "help", "send"),
            "let" to listOf("me", "us", "it", "them", "him"),
            "going" to listOf("to", "home", "back", "out", "on")
        )

        private val COMMON_ENGLISH_WORDS = listOf(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "I",
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
            "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
            "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
            "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
            "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
            "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
            "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
            "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
            "is", "are", "was", "were", "been", "has", "had", "done", "am", "being",
            "hello", "help", "held", "here", "hear", "heard", "heart", "heavy", "head", "health",
            "thanks", "thank", "thankyou", "today", "tomorrow", "tonight", "together", "told",
            "please", "phone", "photo", "place", "play", "player", "plan", "point", "power", "problem",
            "keyboard", "key", "keep", "kept", "kind", "king", "know", "knowledge",
            "bluetooth", "device", "connect", "connected", "connection", "computer", "screen",
            "laptop", "system", "android", "application", "app", "message", "meeting", "mail",
            "quick", "question", "quite", "quiet", "quote", "quality", "query",
            "world", "work", "worker", "write", "writing", "written", "without", "water",
            "friend", "family", "from", "find", "found", "first", "feel", "feeling",
            "great", "group", "grow", "green", "give", "given", "general", "game",
            "simple", "start", "state", "status", "still", "stop", "such", "sure",
            "text", "type", "typing", "touch", "turn", "talk", "tell", "test",
            "number", "name", "need", "next", "never", "night", "new", "near",
            "online", "offline", "open", "order", "only", "once", "office", "often",
            "value", "view", "very", "voice", "version", "video", "visit",
            "yes", "yet", "yesterday", "young", "yourself",
            "zero", "zone", "zoom"
        )
    }
}
