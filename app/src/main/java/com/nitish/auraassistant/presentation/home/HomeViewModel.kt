package com.nitish.auraassistant.presentation.home

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.auraassistant.data.sync.SyncState
import com.nitish.auraassistant.data.sync.SyncStatus
import com.nitish.auraassistant.domain.model.ChatMessage
import com.nitish.auraassistant.domain.model.MessageMeta
import com.nitish.auraassistant.domain.repository.ChatRepository
import com.nitish.auraassistant.domain.repository.UserRepository
import com.nitish.auraassistant.presentation.home.screens.AuraCircleState
import com.nitish.auraassistant.statemachine.MessageState
import com.nitish.auraassistant.statemachine.MessageStateMachine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.sqrt

data class HomeUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    val isLoadingMore: Boolean = false,
    val inputText: String = "",
    val isKeyboardVisible: Boolean = false,
    val auraCircleState: AuraCircleState = AuraCircleState.Idle,
    val userName: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val messageState: StateFlow<MessageState>
    private val stateMachine = MessageStateMachine(viewModelScope)

    val syncStatus: StateFlow<SyncStatus> = SyncState.status

    private var audioRecord: AudioRecord? = null
    private var listeningJob: Job? = null

    private val totalCount: StateFlow<Int> = chatRepository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        messageState = stateMachine.state
        loadNextPage()
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { profile ->
                _uiState.update { it.copy(userName = profile.name) }
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            chatRepository.getMessagesPaged(state.currentPage).collect { newMessages ->
                _uiState.update { current ->
                    val merged = (current.messages + newMessages).distinctBy { it.id }
                    current.copy(
                        messages = merged,
                        currentPage = current.currentPage + 1,
                        hasMorePages = newMessages.size == 20,
                        isLoadingMore = false
                    )
                }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
        stateMachine.onTyping(text)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        _uiState.update { it.copy(inputText = "", isKeyboardVisible = false) }
        val userMsg = ChatMessage(sender = "user", message = text)
        viewModelScope.launch {
            val id = chatRepository.sendMessage(userMsg)
            _uiState.update { it.copy(messages = listOf(userMsg.copy(id = id)) + it.messages) }
            stateMachine.sendMessage(text)
        }
        observeStateMachineForResponse(text)
    }

    private fun observeStateMachineForResponse(userText: String) {
        viewModelScope.launch {
            stateMachine.state.collect { s ->
                if (s is MessageState.Responding) {
                    val botMsg = ChatMessage(
                        sender = "aura",
                        message = s.response,
                        meta = MessageMeta(modelVersion = "aura-v1")
                    )
                    val id = chatRepository.sendMessage(botMsg)
                    _uiState.update { it.copy(messages = listOf(botMsg.copy(id = id)) + it.messages) }
                    return@collect
                }
            }
        }
    }

    fun retryLastMessage() = stateMachine.retry()

    fun toggleKeyboard() = _uiState.update { it.copy(isKeyboardVisible = !it.isKeyboardVisible) }

    fun hideKeyboard() = _uiState.update { it.copy(isKeyboardVisible = false) }

    @SuppressLint("MissingPermission")
    fun startListening() {
        listeningJob?.cancel()
        listeningJob = viewModelScope.launch(Dispatchers.IO) {
            val sampleRate = 44100
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
            )
            audioRecord?.startRecording()
            val buffer = ShortArray(bufferSize)
            while (true) {
                val read = audioRecord?.read(buffer, 0, bufferSize) ?: break
                if (read > 0) {
                    val rms = sqrt(buffer.take(read).map { it.toLong() * it }.average()).toFloat()
                    val normalized = (rms / 32768f).coerceIn(0f, 1f)
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(auraCircleState = AuraCircleState.Listening(normalized)) }
                    }
                }
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        _uiState.update { it.copy(auraCircleState = AuraCircleState.Idle) }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}

