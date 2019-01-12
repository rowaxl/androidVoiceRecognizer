package com.example.rowaxl.voicerecognizersample

class Events {
    class RecognizerOnResult(recognizeResult: String) {
        private val mRecognizeResult: String = recognizeResult

        fun getRecognizeResult(): String {
            return mRecognizeResult
        }
    }

    class RecognizerOnStart

    class RecognizerOnStop
}