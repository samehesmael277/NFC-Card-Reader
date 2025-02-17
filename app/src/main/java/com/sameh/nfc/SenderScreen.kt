package com.sameh.nfc

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SendScreen(navController: NavController, receivedId: MutableState<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Send ID: ${receivedId.value}", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            // إرسال البيانات عبر NFC (مثال: إرسال ID)
            //sendNfcData("ID: 12345")
            navController.navigate("receiveScreen")
        }) {
            Text("Send New ID")
        }
    }
}

fun sendNfcData(context: Context, data: String, tag: Tag) {
    val nfcAdapter: NfcAdapter = NfcAdapter.getDefaultAdapter(context)
        ?: return // NFC غير مدعوم

    if (!nfcAdapter.isEnabled) {
        // NFC غير مفعل
        return
    }

    try {
        val ndef = Ndef.get(tag)
        ndef.connect()

        // إعداد الرسالة
        val ndefMessage = NdefMessage(
            NdefRecord.createTextRecord("en", data)
        )

        // إرسال البيانات عبر NFC
        ndef.writeNdefMessage(ndefMessage)

        ndef.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
