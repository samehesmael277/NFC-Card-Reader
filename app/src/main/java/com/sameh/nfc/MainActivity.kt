package com.sameh.nfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sameh.nfc.HostCardEmulatorService.Companion.TAG
import com.sameh.nfc.ui.theme.NFCTheme

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null

    private val nfcData = mutableStateOf("Waiting for NFC...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            NFCTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nfcData.value
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(
            this, this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        try {
            val isoDep = IsoDep.get(tag)
            isoDep.connect()

            val command = "00A4040000" // أو جرّب "00A4040000"
            val response = isoDep.transceive(Utils.hexStringToByteArray(command))

            val hexResponse = Utils.toHex(response)
            Log.d(TAG, "Card Response: $hexResponse")

            runOnUiThread {
                nfcData.value = "Card Response: $hexResponse" // تحديث الواجهة
            }

            isoDep.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading NFC", e)
            runOnUiThread {
                nfcData.value = "Error reading NFC"
            }
        }
    }
}