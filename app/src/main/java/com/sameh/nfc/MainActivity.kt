package com.sameh.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private var nfcData by mutableStateOf("Sample NFC Data")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // UI for Sending Data
            SendNfcScreen()
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            // NFC غير مدعوم
            return
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED))

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tag: android.nfc.Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        tag?.let {
            // عندما يتم اكتشاف الـ Tag من الجهاز الآخر، يمكن إرسال البيانات
            sendNfcData(it, nfcData)
        }
    }

    // هذه الفنكشن لارسال البيانات عبر NFC
    private fun sendNfcData(tag: android.nfc.Tag, data: String) {
        try {
            val ndef = Ndef.get(tag)
            ndef.connect()
            val ndefMessage = NdefMessage(
                NdefRecord.createTextRecord("en", data)
            )
            ndef.writeNdefMessage(ndefMessage)
            ndef.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    fun SendNfcScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Send Data via NFC")
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                // عند الضغط على الزر سيتم إرسال البيانات عبر NFC
                nfcData = "New Data to send" // مثال لتغيير البيانات
                // هذه العملية يتم تنفيذها عندما يتفاعل الجهاز مع NFC
            }) {
                Text("Send Data")
            }
        }
    }
}