package com.sameh.nfc

class Utils {

    companion object {
        private val HEX_CHARS = "0123456789ABCDEF"

        // Convert a hex string to a byte array
        fun hexStringToByteArray(data: String): ByteArray {
            val result = ByteArray(data.length / 2)
            for (i in data.indices step 2) {
                val firstIndex = HEX_CHARS.indexOf(data[i])
                val secondIndex = HEX_CHARS.indexOf(data[i + 1])
                val octet = firstIndex.shl(4).or(secondIndex)
                result[i.shr(1)] = octet.toByte()
            }
            return result
        }

        // Convert a byte array to a hex string
        fun toHex(byteArray: ByteArray): String {
            val result = StringBuffer()
            byteArray.forEach {
                val octet = it.toInt()
                val firstIndex = (octet and 0xF0).ushr(4)
                val secondIndex = octet and 0x0F
                result.append(HEX_CHARS[firstIndex])
                result.append(HEX_CHARS[secondIndex])
            }
            return result.toString()
        }

        // Convert a string to its hexadecimal representation
        fun stringToHex(input: String): String {
            val result = StringBuilder()
            for (char in input) {
                val hex = char.code.toString(16).uppercase()
                result.append(hex)
            }
            return result.toString()
        }

        fun hexToString(hex: String): String {
            val result = StringBuilder()
            for (i in hex.indices step 2) {
                val hexByte = hex.substring(i, i + 2)
                val charCode = hexByte.toInt(16)
                result.append(charCode.toChar())
            }
            return result.toString()
        }
    }
}
