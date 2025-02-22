package com.sameh.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class HostCardEmulatorService : HostApduService() {

    companion object {
        val TAG = "Host Card Emulator"
        val STATUS_SUCCESS = "9000"
        val STATUS_FAILED = "6F00"
        val AID = "A0000002471001"
        val SELECT_INS = "A4"
        val DEFAULT_CLA = "00"
        val MIN_APDU_LENGTH = 12
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (NFCMessageHandler.isSendingEnabled().not()) return Utils.hexStringToByteArray(
            STATUS_FAILED
        )

        if (commandApdu == null) return Utils.hexStringToByteArray(STATUS_FAILED)

        val hexCommandApdu = Utils.toHex(commandApdu)
        Log.d(TAG, "Received APDU: $hexCommandApdu")

        // التعامل مع أمر SELECT
        if (hexCommandApdu.length >= 10 && hexCommandApdu.substring(0, 2) == DEFAULT_CLA &&
            hexCommandApdu.substring(2, 4) == SELECT_INS
        ) {

            if (hexCommandApdu.length < MIN_APDU_LENGTH) {
                return Utils.hexStringToByteArray(STATUS_FAILED)
            }

            // استخراج AID من الأمر
            val lengthIndex = 8
            if (hexCommandApdu.length < lengthIndex + 2) {
                return Utils.hexStringToByteArray(STATUS_FAILED)
            }

            val aidLength = hexCommandApdu.substring(lengthIndex, lengthIndex + 2).toInt(16)

            if (hexCommandApdu.length < lengthIndex + 2 + (aidLength * 2)) {
                return Utils.hexStringToByteArray(STATUS_FAILED)
            }

            val receivedAID =
                hexCommandApdu.substring(lengthIndex + 2, lengthIndex + 2 + (aidLength * 2))
            Log.d(TAG, "Extracted AID: $receivedAID, Expected AID: $AID")

            return if (receivedAID == AID) {
                Log.d(TAG, "AID Matched! Sending success response.")
                Utils.hexStringToByteArray(STATUS_SUCCESS)
            } else {
                Log.d(TAG, "AID Mismatch! Sending 6A82.")
                Utils.hexStringToByteArray("6A82")
            }
        }

        // التعامل مع أمر القراءة 00B0000000
        if (hexCommandApdu == "00B0000000") {
            Log.d(TAG, "Received READ command")
            val ncfMessage = NFCMessageHandler.getNCFSharedMessage()
            val messageInHex = Utils.stringToHex(ncfMessage)
            return Utils.hexStringToByteArray(messageInHex + STATUS_SUCCESS)
        }

        Log.d(TAG, "Command not recognized: $hexCommandApdu")
        return Utils.hexStringToByteArray(STATUS_FAILED)
    }
}