package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class VoiceAgentManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _audioAmplitudes = MutableStateFlow(List(16) { 0.1f })
    val audioAmplitudes: StateFlow<List<Float>> = _audioAmplitudes.asStateFlow()

    private var amplitudeJob: Job? = null

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onSpeakingCompleted: (() -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true
                }
                setupTtsListener()
            } else {
                Log.e("VoiceAgentManager", "TTS initialization failed")
            }
        }
    }

    private fun setupTtsListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                startWaveAnimation(isSpeaking = true)
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                stopWaveAnimation()
                coroutineScope.launch(Dispatchers.Main) {
                    onSpeakingCompleted?.invoke()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                stopWaveAnimation()
            }
        })
    }

    fun speak(text: String, pitch: Float = 1.0f, speed: Float = 1.0f) {
        if (!isTtsReady) {
            initTts()
        }
        stopListening()
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "bot_speech_${System.currentTimeMillis()}")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "bot_speech_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        stopWaveAnimation()
    }

    fun startListening() {
        stopSpeaking()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("VoiceAgentManager", "Speech recognition not available")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        startWaveAnimation(isSpeaking = false)
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {
                        if (_isListening.value) {
                            val normalized = ((rmsdB + 2f) / 10f).coerceIn(0.1f, 1.0f)
                            _audioAmplitudes.value = List(16) {
                                (normalized * Random.nextFloat()).coerceIn(0.1f, 1.0f)
                            }
                        }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        stopWaveAnimation()
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        stopWaveAnimation()
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        stopWaveAnimation()
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull().orEmpty()
                        if (spokenText.isNotBlank()) {
                            _recognizedText.value = spokenText
                            onSpeechRecognized?.invoke(spokenText)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            _recognizedText.value = it
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("VoiceAgentManager", "Failed to start speech recognition: ${e.message}", e)
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignored
        }
        _isListening.value = false
        stopWaveAnimation()
    }

    private fun startWaveAnimation(isSpeaking: Boolean) {
        amplitudeJob?.cancel()
        amplitudeJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive) {
                val base = if (isSpeaking) 0.6f else 0.4f
                val newAmplitudes = List(16) {
                    (base * (0.3f + 0.7f * Random.nextFloat())).coerceIn(0.1f, 1.0f)
                }
                _audioAmplitudes.value = newAmplitudes
                delay(80)
            }
        }
    }

    private fun stopWaveAnimation() {
        amplitudeJob?.cancel()
        _audioAmplitudes.value = List(16) { 0.1f }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        amplitudeJob?.cancel()
    }
}
