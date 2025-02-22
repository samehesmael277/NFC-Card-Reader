package com.sameh.nfc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NfcViewModel(application: Application) : AndroidViewModel(application) {

    private val _messageToSend = MutableStateFlow("Hello NFC World!")
    val messageToSend: StateFlow<String> = _messageToSend

    private val _isSendingEnabled = MutableStateFlow(true)
    val isSendingEnabled: StateFlow<Boolean> = _isSendingEnabled

    private val _isReceivingEnabled = MutableStateFlow(true)
    val isReceivingEnabled: StateFlow<Boolean> = _isReceivingEnabled

    fun updateMessage(newMessage: String) {
        _messageToSend.value = newMessage
        NFCMessageHandler.setNCFSharedMessage(newMessage)
    }

    fun toggleSending(enabled: Boolean) {
        _isSendingEnabled.value = enabled
        if (enabled)
            NFCMessageHandler.enableSending()
        else
            NFCMessageHandler.disableSending()
    }

    fun toggleReceiving(enabled: Boolean) {
        _isReceivingEnabled.value = enabled
    }
}
