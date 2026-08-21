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

    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid = _remoteUid.asStateFlow()

    init {
        // Observe active call from repository (both incoming and outgoing)
        repository.activeCall
            .onEach { call ->
                _callState.value = call
                if (call != null) {
                    observeCallStatus(call.callId)
                }
            }
            .launchIn(viewModelScope)

        // Also keep observing incoming calls
        repository.observeIncomingCalls()
            .catch { e -> Log.e("CallViewModel", "Error observing calls", e) }
            .launchIn(viewModelScope)
    }

    private fun observeCallStatus(callId: String) {
        repository.observeCallStatus(callId)
            .onEach { status ->
                val currentCall = _callState.value
                if (currentCall != null && currentCall.callId == callId) {
                    _callState.update { it?.copy(status = status) }
                    
                    when (status) {
                        CallStatus.ACCEPTED -> {
                            startAgoraCall(currentCall)
                        }
                        CallStatus.REJECTED, CallStatus.ENDED -> {
                            cleanupCall()
                        }
                        else -> {}
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAcceptCall() {
        val call = _callState.value ?: return
        viewModelScope.launch {
            val result = repository.updateCallStatus(call.callId, CallStatus.ACCEPTED)
            if (result.isSuccess) {
                startAgoraCall(call)
            }
        }
    }

    fun onRejectCall() {
        val call = _callState.value ?: return
        viewModelScope.launch {
            repository.updateCallStatus(call.callId, CallStatus.REJECTED)
            cleanupCall()
        }
    }

    fun onEndCall() {
        val call = _callState.value ?: return
        viewModelScope.launch {
            repository.updateCallStatus(call.callId, CallStatus.ENDED)
            cleanupCall()
        }
    }

    private fun cleanupCall() {
        agoraCallManager.removeHandler(agoraEventHandler)
        agoraCallManager.leaveChannel()
        repository.clearActiveCall()
        _callState.value = null
        _remoteUid.value = null
    }

    private val agoraEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.d(
                "AGORA_CALL",
                "Joined Agora channel successfully: $channel"
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
            _remoteUid.value = uid
        }

        override fun onUserOffline(
            uid: Int,
            reason: Int
        ) {
            Log.d(
                "AGORA_CALL",
                "Remote user left: uid=$uid reason=$reason"
            )
            _remoteUid.value = null
        }

        override fun onError(
            err: Int
        ) {
            Log.e(
                "AGORA_CALL",
                "Agora error: $err"
            )
        }
    }

    private fun startAgoraCall(call: CallInfo) {
        try {
            Log.d("AGORA_CALL", "Starting Agora call: channelId=${call.callId}")

            val initialized = agoraCallManager.initEngine(agoraEventHandler)
            if (!initialized) {
                Log.e("AGORA_CALL", "Failed to initialize Agora engine")
                return
            }

            val result = agoraCallManager.joinChannel(
                channelId = call.callId,
                token = call.token,
                uid = 0
            )

            if (result != 0) {
                Log.e("AGORA_CALL", "Failed to join Agora channel. Error=$result")
            }
        } catch (e: Exception) {
            Log.e("AGORA_CALL", "Exception while starting Agora call", e)
        }
    }
}
