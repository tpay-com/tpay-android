package com.tpay.sdk.internal

import com.tpay.sdk.extensions.Observable
import java.util.*
import javax.inject.Singleton


@Singleton
internal class LanguageSwitcher {
    internal val localeObservable = Observable(Locale.getDefault())
    internal var currentLanguage = Language.DEFAULT

    fun setLanguage(language: Language){
        currentLanguage = language
        localeObservable.value = if(language == Language.DEFAULT){
            Locale.getDefault()
        } else Locale.forLanguageTag(language.languageTag)
    }
}

internal enum class Language(val languageTag: String){
    POLISH("pl"),
    ENGLISH("en"),
    DEFAULT("");

    companion object {
        var fromConfiguration = emptyList<Language>()

        internal fun fromConfiguration(list: List<com.tpay.sdk.api.models.Language>){
            fromConfiguration = list.map {
                when(it){
                    com.tpay.sdk.api.models.Language.PL -> POLISH
                    com.tpay.sdk.api.models.Language.EN -> ENGLISH
                }
            }
        }

        internal fun from(lang: com.tpay.sdk.api.models.Language): Language {
            return when(lang){
                com.tpay.sdk.api.models.Language.PL -> POLISH
                com.tpay.sdk.api.models.Language.EN -> ENGLISH
            }
        }

        internal fun Language.asApi(): com.tpay.sdk.api.models.Language {
            return when(this){
                POLISH -> com.tpay.sdk.api.models.Language.PL
                else -> com.tpay.sdk.api.models.Language.EN
            }
        }
    }
}