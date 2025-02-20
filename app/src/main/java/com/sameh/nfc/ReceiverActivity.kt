package com.sameh.nfc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sameh.nfc.ui.theme.NFCTheme

class ReceiverActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private val nfcData = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NFC Reader Mode",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = nfcData.value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
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
        nfcAdapter?.enableReaderMode(
            this, this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        try {
            val isoDep = IsoDep.get(tag) ?: return
            isoDep.connect()

            // إرسال أمر SELECT مع AID الصحيح
            val selectApdu = "00A4040007A0000002471001" // AID طوله 7 بايت
            val selectResponse = isoDep.transceive(Utils.hexStringToByteArray(selectApdu))
            val selectHex = Utils.toHex(selectResponse)
            Log.d("NFC", "SELECT Response: $selectHex")

            if (selectHex != "9000") {
                // إذا فشلت عملية SELECT
                nfcData.value = "Error: Failed to select application. Response: $selectHex"
                isoDep.close()
                return
            }

            // إرسال أمر قراءة البيانات
            val readCommand = "00B0000000" // أمر القراءة
            val response = isoDep.transceive(Utils.hexStringToByteArray(readCommand))
            val responseHex = Utils.toHex(response)
            Log.d("NFC", "Read Response: $responseHex")

            // استخراج الرسالة من الاستجابة (بإزالة رمز الحالة 9000 أو التعامل مع الخطأ)
            if (responseHex.endsWith("9000")) {
                val messageHex = responseHex.substring(0, responseHex.length - 4)
                val message = Utils.hexToString(messageHex)
                nfcData.value = "Message: $message"
            } else {
                nfcData.value = "Error: Invalid response format. Response: $responseHex"
            }
        } catch (e: Exception) {
            Log.e("NFC", "Error in NFC communication", e)
            nfcData.value = "Error: ${e.message}"
        } finally {
            try {
                IsoDep.get(tag)?.close()
            } catch (e: Exception) {
                Log.e("NFC", "Error closing IsoDep", e)
            }
        }
    }
}