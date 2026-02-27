package com.erika.listify.ui.screens.export

import androidx.compose.material3.ExperimentalMaterial3Api
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.erika.listify.data.repository.ListRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    listId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val list = ListRepository.getList(listId)

    val resultText = remember(list) {
        list?.items
            ?.filter { it.checked }
            ?.joinToString(", ") { it.text }
            ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exportar") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Texto generado:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = resultText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { copyToClipboard(context, resultText) },
                modifier = Modifier.fillMaxWidth(),
                enabled = resultText.isNotBlank()
            ) {
                Text("Copiar")
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("listify_export", text))
}