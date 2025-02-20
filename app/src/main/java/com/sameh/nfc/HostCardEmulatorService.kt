package com.sameh.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/*
class HostCardEmulatorService : HostApduService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "HostCardEmulatorService Started")
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return Utils.hexStringToByteArray(STATUS_FAILED)

        val hexCommandApdu = Utils.toHex(commandApdu)
        Log.d(TAG, "Received APDU Command: $hexCommandApdu")

        if (hexCommandApdu.length < 12) return Utils.hexStringToByteArray(STATUS_FAILED)

        if (hexCommandApdu.substring(0, 2) != "00" || hexCommandApdu.substring(2, 4) != "A4") {
            Log.d(TAG, "Invalid Command: Not an AID selection request")
            return Utils.hexStringToByteArray(INS_NOT_SUPPORTED)
        }

        if (hexCommandApdu.substring(10, 24) == AID) {
            val (userId, phone) = NfcDataHandler.getNfcData()
            val responseData = Utils.stringToHex("$userId|$phone") + STATUS_SUCCESS
            Log.d(TAG, "Sending Data: $userId | $phone")
            return Utils.hexStringToByteArray(responseData)
        }

        Log.d(TAG, "AID Not Matched")
        return Utils.hexStringToByteArray(STATUS_FAILED)

    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "NFC Deactivated: $reason")
    }

    companion object {
        private const val TAG = "HostCardEmulator"
        private const val STATUS_SUCCESS = "9000"
        private const val STATUS_FAILED = "6F00"
        private const val INS_NOT_SUPPORTED = "6D00"
        private const val AID = "A0000002471001"
    }
}
 */