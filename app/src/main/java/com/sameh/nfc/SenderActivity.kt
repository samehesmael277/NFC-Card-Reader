package com.sameh.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class SenderActivity : ComponentActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private var isNfcAvailable = false
    private var messageToSend = "Hello NFC World!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        isNfcAvailable = nfcAdapter != null

        setContent {
            SenderScreen(
                isNfcAvailable = isNfcAvailable,
                message = messageToSend,
                onMessageChange = { messageToSend = it },
                onSendClick = { enableNfcForegroundDispatch() }
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

        // Create intent filters for the actions you want to handle
        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val filters = arrayOf(tagDetected)

        // Define tech lists if needed (optional)
        val techLists = arrayOf(arrayOf(Ndef::class.java.name))

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techLists)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d("NFC_TAG", "New intent received: ${intent.action}")

        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action
        ) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                try {
                    val ndef = Ndef.get(tag)
                    if (ndef != null) {
                        sendNdefMessage(ndef, messageToSend)
                    } else {
                        Log.e("NFC_TAG", "Tag doesn't support NDEF")
                        Toast.makeText(this, "This tag doesn't support NDEF format", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("NFC_TAG", "Error handling tag: ${e.message}")
                    Toast.makeText(this, "Error handling tag: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("NFC_TAG", "No tag in intent")
            }
        }
    }

    private fun sendNdefMessage(ndef: Ndef, message: String) {
        try {
            ndef.connect()

            val ndefRecord = NdefRecord.createTextRecord("en", message)
            val ndefMessage = NdefMessage(arrayOf(ndefRecord))

            ndef.writeNdefMessage(ndefMessage)
            Toast.makeText(this, "Message sent successfully!", Toast.LENGTH_SHORT).show()

            ndef.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
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
fun SenderScreen(
    isNfcAvailable: Boolean,
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit
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
            TextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Message to send") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSendClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tap to Send via NFC")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bring devices close together to transfer data",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}