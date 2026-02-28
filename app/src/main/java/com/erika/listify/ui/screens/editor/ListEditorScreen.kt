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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem

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
            val existingTexts = current?.items?.map { it.text.lowercase().trim() }?.toSet().orEmpty()

            var expanded by remember { mutableStateOf(false) }

            val knownSuggestions = remember(version){
                ListRepository.getAllKnownItemTexts()
            }

            val filteredSuggestions = remember(input, knownSuggestions, existingTexts) {
                val q = input.trim()
                if (q.isEmpty()) emptyList()
                else knownSuggestions
                    .filter { it.contains(q, ignoreCase = true) }
                    .filter { it.lowercase().trim() !in existingTexts } // que no esté ya
                    .take(6)
            }

            ExposedDropdownMenuBox(
                expanded = expanded && filteredSuggestions.isNotEmpty(),
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        expanded = true
                    },
                    label = { Text("Añadir ítem") },
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .weight(1f, fill = false)
                )

                ExposedDropdownMenu(
                    expanded = expanded && filteredSuggestions.isNotEmpty(),
                    onDismissRequest = { expanded = false }
                ) {
                    filteredSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                ListRepository.addItem(listId, suggestion)
                                input = ""
                                expanded = false
                                version++
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val txt = input.trim()
                        if (txt.isNotEmpty()) {
                            // Evitar duplicados en la lista actual (por si acaso)
                            if (txt.lowercase().trim() !in existingTexts) {
                                ListRepository.addItem(listId, txt)
                                input = ""
                                expanded = false
                                version++
                            }
                        }
                    }
                ) { Text("Añadir") }
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