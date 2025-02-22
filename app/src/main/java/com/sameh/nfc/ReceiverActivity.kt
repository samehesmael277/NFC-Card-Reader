package com.sameh.nfc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.sameh.nfc.ui.theme.NFCTheme

class ReceiverActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private val nfcData = mutableStateOf("")

    private lateinit var viewModel: NfcViewModel
    private var isReceivingEnabled = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[NfcViewModel::class.java]

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val intent = Intent(this, HostCardEmulatorService::class.java)
        startService(intent)

        val isHceAvailable = isHCESupported(this)

        // عرض نتيجة التحقق للمستخدم
        if (!isHceAvailable) {
            Toast.makeText(this, "جهازك لا يدعم HCE", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "جهازك يدعم HCE", Toast.LENGTH_LONG).show()
        }

        setContent {
            NFCTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NFC Reader Mode")
                    Spacer(modifier = Modifier.height(16.dp))
                    Switch(
                        checked = isReceivingEnabled.value,
                        onCheckedChange = {
                            isReceivingEnabled.value = it
                            viewModel.toggleReceiving(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Receiving Enabled: ${isReceivingEnabled.value}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Received Data: ${nfcData.value}")
                }
            }
        }
    }

    private fun isHCESupported(context: Context): Boolean {
        val packageManager = context.packageManager

        // التحقق من وجود NFC أولاً
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            return false
        }

        // التحقق من وجود خدمة HCE
        return packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)
    }

    override fun onResume() {
        super.onResume()
        if (isReceivingEnabled.value) {
            nfcAdapter?.enableReaderMode(
                this, this,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null
            )
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (!isReceivingEnabled.value) return

        try {
            val isoDep = IsoDep.get(tag) ?: return
            isoDep.connect()

            val selectApdu = "00A4040007A0000002471001"
            val selectResponse = isoDep.transceive(Utils.hexStringToByteArray(selectApdu))

            if (Utils.toHex(selectResponse) != "9000") {
                nfcData.value = "Error: Failed to select application"
                isoDep.close()
                return
            }

            val readCommand = "00B0000000"
            val response = isoDep.transceive(Utils.hexStringToByteArray(readCommand))
            val responseHex = Utils.toHex(response)

            if (responseHex.endsWith("9000")) {
                val messageHex = responseHex.substring(0, responseHex.length - 4)
                nfcData.value = Utils.hexToString(messageHex)
            }

            isoDep.close()
        } catch (e: Exception) {
            nfcData.value = "Error reading NFC data"
        }
    }
}