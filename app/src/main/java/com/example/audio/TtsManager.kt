package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.model.Language
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: String? = null
    private var pendingLocale: Locale? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {
            tts = null
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            isInitialized = true
            pendingSpeech?.let { text ->
                val locale = pendingLocale ?: Locale("mr", "IN")
                speak(text, locale)
                pendingSpeech = null
                pendingLocale = null
            }
        }
    }

    fun speak(text: String, language: Language) {
        val targetLocale = when (language) {
            Language.MR -> Locale("mr", "IN")
            Language.HI -> Locale("hi", "IN")
            Language.EN -> Locale("en", "IN")
        }
        speak(text, targetLocale)
    }

    private fun speak(text: String, locale: Locale) {
        if (!isInitialized || tts == null) {
            pendingSpeech = text
            pendingLocale = locale
            return
        }

        try {
            // Prioritized fallback: specified locale -> Marathi -> Hindi -> English -> silent
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val mrResult = tts?.setLanguage(Locale("mr", "IN"))
                if (mrResult == TextToSpeech.LANG_MISSING_DATA || mrResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val hiResult = tts?.setLanguage(Locale("hi", "IN"))
                    if (hiResult == TextToSpeech.LANG_MISSING_DATA || hiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.ENGLISH)
                    }
                }
            }
            tts?.setSpeechRate(0.92f)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "khetkhata_tts_${System.currentTimeMillis()}")
        } catch (_: Exception) {
            // Silent fallback on any TTS exception
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
