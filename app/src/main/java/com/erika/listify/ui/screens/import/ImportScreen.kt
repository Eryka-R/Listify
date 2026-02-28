package com.erika.listify.ui.screens.import

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erika.listify.data.repository.ListRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var rawText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar desde texto") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nombre de la lista") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                label = { Text("Texto con comas (ej: comino, leche, pan)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val listTitle = title.trim()
                    if (listTitle.isBlank()) return@Button

                    val id = ListRepository.createList(listTitle)

                    val items = parseCommaText(rawText)
                    items.forEach { ListRepository.addItem(id, it) }

                    onCreated(id)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.trim().isNotEmpty() && rawText.trim().isNotEmpty()
            ) {
                Text("Crear lista")
            }
        }
    }
}

private fun parseCommaText(input: String): List<String> {
    // Soporta comas y saltos de línea
    return input
        .replace("\n", ",")
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() } // evita duplicados “Leche” vs “leche”
}