package com.sameh.nfc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: NfcViewModel
    private lateinit var nfcAdapter: NfcAdapter
    private var isNfcAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[NfcViewModel::class.java]

        startService(Intent(this, HostCardEmulatorService::class.java))

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        isNfcAvailable = nfcAdapter != null

        val isHceAvailable = isHCESupported(this)

        // عرض نتيجة التحقق للمستخدم
        if (!isHceAvailable) {
            Toast.makeText(this, "جهازك لا يدعم HCE", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "جهازك يدعم HCE", Toast.LENGTH_LONG).show()
        }

        setContent {
            val message by viewModel.messageToSend.collectAsState()
            val isSendingEnabled by viewModel.isSendingEnabled.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = message,
                    onValueChange = {
                        viewModel.updateMessage(it)
                    },
                    label = { Text("Enter Message") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Switch(
                    checked = isSendingEnabled,
                    onCheckedChange = { viewModel.toggleSending(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Sending Enabled: $isSendingEnabled")
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

    @Composable
    fun SenderScreen(
        isNfcAvailable: Boolean
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
                    text = "NFC Host Card Emulation Mode",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Host Card Emulation Active",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Green
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your device is acting as a contactless smart card. Bring another NFC reader close to transfer the message.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}