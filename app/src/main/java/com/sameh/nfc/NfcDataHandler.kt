package com.sameh.nfc

object NfcDataHandler {
    private var userId: String = "00000"
    private var phone: String = "0000000000"

    fun setNfcData(id: String, phoneNumber: String) {
        userId = id
        phone = phoneNumber
    }

    fun getNfcData(): Pair<String, String> {
        return Pair(userId, phone)
    }
}