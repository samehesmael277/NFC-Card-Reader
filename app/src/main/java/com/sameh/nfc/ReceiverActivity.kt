package com.sameh.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.nio.charset.Charset
import java.util.Arrays
import kotlin.experimental.and

class ReceiverActivity : ComponentActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private var isNfcAvailable = false
    private val receivedMessages = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        isNfcAvailable = nfcAdapter != null

        // Process intent if already available
        intent?.let { processIntent(it) }

        setContent {
            ReceiverScreen(
                isNfcAvailable = isNfcAvailable,
                receivedMessages = receivedMessages
            )
        }
    }

    private fun enableNfcForegroundDispatch() {
        if (!isNfcAvailable) return

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )

        // Set up intent filters for NDEF messages
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Failed to add MIME type.", e)
            }
        }

        val intentFilters = arrayOf(ndef)
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        Log.d("TAGTAGTAG", "processIntent: $intent")
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.let { rawMessages ->
                val messages = rawMessages.map { it as NdefMessage }
                for (message in messages) {
                    for (record in message.records) {
                        if (record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                            Arrays.equals(record.type, NdefRecord.RTD_TEXT)
                        ) {

                            // Parse text from the record
                            val payload = record.payload
                            val textEncoding =
                                if ((payload[0] and 128.toByte()) == 0.toByte()) "UTF-8" else "UTF-16"
                            val languageCodeLength = payload[0] and 0x3f
                            val text = String(
                                payload,
                                languageCodeLength + 1,
                                payload.size - languageCodeLength - 1,
                                Charset.forName(textEncoding)
                            )

                            receivedMessages.add(text)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isNfcAvailable) {
            enableNfcForegroundDispatch()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isNfcAvailable) {
            nfcAdapter.disableForegroundDispatch(this)
        }
    }
}

@Composable
fun ReceiverScreen(
    isNfcAvailable: Boolean,
    receivedMessages: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isNfcAvailable) {
            Text(
                text = "NFC is not available on this device",
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = "Waiting for NFC data...",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(receivedMessages.size) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Message ${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(receivedMessages[index])
                        }
                    }
                }
            }
        }
    }
}