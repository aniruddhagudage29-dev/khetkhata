package com.example.model

enum class Language(val code: String, val displayName: String, val ttsLocale: String) {
    MR("mr", "मराठी", "mr-IN"),
    HI("hi", "हिंदी", "hi-IN"),
    EN("en", "English", "en-IN");

    companion object {
        fun fromCode(code: String?): Language {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: MR
        }
    }
}
