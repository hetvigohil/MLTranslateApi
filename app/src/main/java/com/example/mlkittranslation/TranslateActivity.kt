package com.example.mlkittranslation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.jakewharton.rxbinding4.widget.textChanges
import kotlinx.android.synthetic.main.translate_activity.*
import java.util.concurrent.TimeUnit

class TranslateActivity : AppCompatActivity() {

    private var translation: Translator? = null
    private var downloadedModel: Task<Void>? = null
    private lateinit var conditions: DownloadConditions
    private var languageIdentifier: LanguageIdentifier? = null
    private var onComplete: (() -> Unit)? = null
    private var languageCode = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.translate_activity)
        languageIdentifier = LanguageIdentification.getClient()
        conditions = DownloadConditions.Builder()
            .build()

        sourceText?.textChanges()?.debounce(2000, TimeUnit.MILLISECONDS)
            ?.subscribe {
                languageIdentifier?.identifyLanguage(sourceText.text.toString())
                    ?.addOnSuccessListener { languageCode ->
                        if (languageCode != "und") {
                            this.languageCode = languageCode
                            val options = TranslatorOptions.Builder()
                                .setSourceLanguage(languageCode)
                                .setTargetLanguage(TranslateLanguage.ENGLISH)
                                .build()
                            translation = Translation.getClient(options)
                            downloadedModel = translation?.downloadModelIfNeeded(conditions)
                            onComplete?.invoke()
                        }
                    }
                    ?.addOnFailureListener {

                    }
            }

        onComplete = {
            downloadedModel?.addOnSuccessListener {
                downloadedModel?.let {
                    if (it.isSuccessful) {
                        val output = getString(
                            R.string.downloaded_models_label, languageCode
                        )
                        downloadedModels.text = output
                        translation?.translate(sourceText.text.toString())
                            ?.addOnSuccessListener { translatedText ->
                                targetText.text = translatedText
                            }
                            ?.addOnFailureListener {
                            }
                    }
                }
            }
            downloadedModel?.addOnFailureListener {

            }
        }
    }
}