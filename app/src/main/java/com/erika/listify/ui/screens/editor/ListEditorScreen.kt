package com.erika.listify.ui.screens.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.foundation.combinedClickable

import com.erika.listify.data.repository.ListRepository

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListEditorScreen(
    listId: String,
    onExport: () -> Unit,
    onBack: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var version by remember { mutableIntStateOf(0) } // para refrescar simple
    var itemMenuExpanded by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }

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

            val knownSuggestions = remember(version) {
                ListRepository.getAllKnownItemTexts()
            }

            val filteredSuggestions = remember(input, knownSuggestions, existingTexts) {
                val q = input.trim()
                if (q.isEmpty()) emptyList()
                else knownSuggestions
                    .filter { it.contains(q, ignoreCase = true) }
                    .filter { it.lowercase().trim() !in existingTexts }
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
                    items(
                        items = current.items,
                        key = { it.id }
                    ) { item ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .combinedClickable(
                                        onClick = {
                                            ListRepository.toggleItem(listId, item.id)
                                            version++
                                        },
                                        onLongClick = {
                                            selectedItemId = item.id
                                            editText = item.text
                                            itemMenuExpanded = true
                                        }
                                    ),
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

                            DropdownMenu(
                                expanded = itemMenuExpanded && selectedItemId == item.id,
                                onDismissRequest = { itemMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    onClick = {
                                        itemMenuExpanded = false
                                        showEditDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar") },
                                    onClick = {
                                        itemMenuExpanded = false
                                        ListRepository.deleteItem(listId, item.id)
                                        version++
                                    }
                                )
                            }
                        }

                        Divider()
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Editar ítem") },
                    text = {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            singleLine = true,
                            label = { Text("Texto") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val id = selectedItemId
                                val newValue = editText.trim()

                                if (id != null && newValue.isNotBlank()) {
                                    val currentList = ListRepository.getList(listId)
                                    val existing = currentList?.items
                                        ?.filter { it.id != id }
                                        ?.map { it.text.lowercase().trim() }
                                        ?.toSet()
                                        .orEmpty()

                                    if (newValue.lowercase().trim() !in existing) {
                                        ListRepository.updateItemText(listId, id, newValue)
                                        version++
                                        showEditDialog = false
                                    }
                                }
                            }
                        ) { Text("Guardar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
                    }
                )
            }

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