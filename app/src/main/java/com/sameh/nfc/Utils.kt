package com.sameh.nfc

object Utils {
    fun stringToHex(input: String): String {
        return input.toByteArray().joinToString("") { "%02x".format(it) }.uppercase()
    }

    fun hexToString(hex: String): String {
        val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return String(bytes, Charsets.UTF_8)
    }

    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((s[i].digitToInt(16) shl 4) + s[i + 1].digitToInt(16)).toByte()
        }
        return data
    }

    fun toHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }.uppercase()
    }
}
