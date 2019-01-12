package com.example.rowaxl.voicerecognizersample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import org.greenrobot.eventbus.EventBus

class SpeechToText {
    private var mRecognizer: SpeechRecognizer? = null

    private lateinit var mContext: Context

    private lateinit var mRecognitionListener: RecognitionListener

    private var isRecording: Boolean = false

    fun init(context: Context) {
        this.mContext = context.applicationContext
    }

    fun startSpeech() {
        if (mRecognizer != null) {
            mRecognizer!!.destroy()
        }

        buildRecognizerListener()

        val mainHandler = Handler(mContext.mainLooper)

        val runnable = Thread(Runnable {
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext)

            mRecognizer!!.setRecognitionListener(mRecognitionListener)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            val lang = mContext.getString(R.string.lang_jp)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang)
            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, lang)
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            mRecognizer!!.startListening(intent)
            isRecording = true
        })

        // recognizer must be run on mainloop
        mainHandler.post(runnable)
    }

    fun stopSpeech() {
        if (isRecording) {
            mRecognizer!!.stopListening()
            mRecognizer!!.cancel()
            mRecognizer!!.destroy()
            isRecording = false
        }
    }

    private fun buildRecognizerListener() {
        mRecognitionListener = object : RecognitionListener {
            override fun onError(error: Int) {
                // this called when recognize canceled or timeout.
                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    EventBus.getDefault().post(Events.RecognizerOnStop())
                    if (!isRecording) {
                        return
                    }
                    startSpeech()
                }
            }

            override fun onResults(results: Bundle) {
                isRecording = false

                val values: ArrayList<String> = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!!

                val recognizeResult: String = values[0]
                EventBus.getDefault().post(Events.RecognizerOnResult(recognizeResult))
            }

            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {
                EventBus.getDefault().post(Events.RecognizerOnStop())
            }

            override fun onEvent(arg0: Int, arg1: Bundle) {}
            override fun onPartialResults(arg0: Bundle) {}

            override fun onReadyForSpeech(arg0: Bundle) {
                EventBus.getDefault().post(Events.RecognizerOnStart())
            }

            override fun onRmsChanged(arg0: Float) {}
        }
    }
}