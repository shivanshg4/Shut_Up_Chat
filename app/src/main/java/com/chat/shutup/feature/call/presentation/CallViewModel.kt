package com.chat.shutup.feature.call.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.model.CallInfo
import com.chat.shutup.domain.model.CallStatus
import com.chat.shutup.domain.repository.ChatRepository
import com.chat.shutup.feature.call.manager.AgoraCallManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val agoraCallManager: AgoraCallManager
) : ViewModel() {

    private val _callState = MutableStateFlow<CallInfo?>(null)
    val callState = _callState.asStateFlow()

    init {
        repository.observeIncomingCalls()
            .catch { e -> android.util.Log.e("CallViewModel", "Error observing calls", e) }
            .onEach { _callState.value = it }
            .launchIn(viewModelScope)
    }

    fun onAcceptCall() {
        val call = _callState.value ?: return
        viewModelScope.launch {
            val result  = repository.updateCallStatus(call.callId, CallStatus.ACCEPTED)

            if (result.isSuccess) {
                // Start Agora call
                startAgoraCall(call)
            }
        }
    }

    fun onRejectCall() {
        val call = _callState.value ?: return
        viewModelScope.launch {
            repository.updateCallStatus(call.callId, CallStatus.REJECTED)
            _callState.value = null
        }
    }

    fun onEndCall() {
        val call = _callState.value ?: return
        viewModelScope.launch {
            repository.updateCallStatus(call.callId, CallStatus.ENDED)
            _callState.value = null
        }
    }

    private val agoraEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.d(
                "AGORA_CALL",
                "Joined Agora channel successfully: ${connection?.channelId}"
            )
        }

        override fun onUserJoined(
            uid: Int,
            elapsed: Int
        ) {
            Log.d(
                "AGORA_CALL",
                "Remote user joined: $uid"
            )

            // Store remote UID in StateFlow if your UI
            // needs to display the remote video.
        }

        override fun onUserOffline(
            uid: Int,
            reason: Int
        ) {
            Log.d(
                "AGORA_CALL",
                "Remote user left: uid=$uid reason=$reason"
            )
        }

        override fun onError(
            err: Int
        ) {
            Log.e(
                "AGORA_CALL",
                "Agora error: $err"
            )
        }

        override fun onConnectionStateChanged(
            state: Int,
            reason: Int
        ) {
            Log.d(
                "AGORA_CALL",
                "Connection state changed: state=$state reason=$reason"
            )
        }
    }

    private fun startAgoraCall(call: CallInfo) {

        try {

            Log.d(
                "AGORA_CALL",
                "Starting Agora call"
            )

            Log.d(
                "AGORA_CALL",
                "channelId=${call.channelId}"
            )

            Log.d(
                "AGORA_CALL",
                "tokenPresent=${!call.token.isNullOrEmpty()}"
            )

            // 1. Initialize Agora engine
            val initialized = agoraCallManager.initEngine(
                agoraEventHandler
            )

            if (!initialized) {
                Log.e(
                    "AGORA_CALL",
                    "Failed to initialize Agora engine"
                )
                return
            }

            // 2. Join Agora channel
            val result = agoraCallManager.joinChannel(
                channelId = call.channelId,
                token = call.token,
                uid = 0
            )

            Log.d(
                "AGORA_CALL",
                "joinChannel result=$result"
            )

            if (result != 0) {
                Log.e(
                    "AGORA_CALL",
                    "Failed to join Agora channel. Error=$result"
                )
            }

        } catch (e: Exception) {

            Log.e(
                "AGORA_CALL",
                "Exception while starting Agora call",
                e
            )
        }
    }
}
