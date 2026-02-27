package com.erika.listify.ui.screens.editor

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erika.listify.data.repository.ListRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListEditorScreen(
    listId: String,
    onExport: () -> Unit,
    onBack: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var version by remember { mutableIntStateOf(0) } // para refrescar simple

    val list = ListRepository.getList(listId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(list?.title ?: "Lista") },
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
            val current = remember(version) { ListRepository.getList(listId) }

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Añadir ítem") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val txt = input.trim()
                    if (txt.isNotEmpty()) {
                        ListRepository.addItem(listId, txt)
                        input = ""
                        version++
                    }
                }) { Text("Añadir") }
            }

            Spacer(Modifier.height(16.dp))

            if (current == null || current.items.isEmpty()) {
                Text("Añade ítems arriba (ej: Aceite, Huevos...).")
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(current.items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    ListRepository.toggleItem(listId, item.id)
                                    version++
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.text)
                            Checkbox(
                                checked = item.checked,
                                onCheckedChange = {
                                    ListRepository.toggleItem(listId, item.id)
                                    version++
                                }
                            )
                        }
                        Divider()
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                enabled = current != null && current.items.any { it.checked }
            ) {
                Text("Aceptar y exportar")
            }
        }
    }
}