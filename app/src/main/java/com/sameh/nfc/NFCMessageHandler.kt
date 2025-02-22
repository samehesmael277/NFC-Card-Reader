package com.sameh.nfc

object NFCMessageHandler {
    private var nfcMessage = "Hello NFC World!"
    private var isSendingEnabled = true

    fun setNCFSharedMessage(message: String) {
        nfcMessage = message
    }

    fun getNCFSharedMessage(): String {
        return nfcMessage
    }

    fun enableSending() {
        isSendingEnabled = true
    }

    fun disableSending() {
        isSendingEnabled = false
    }

    fun isSendingEnabled(): Boolean {
        return isSendingEnabled
    }
}