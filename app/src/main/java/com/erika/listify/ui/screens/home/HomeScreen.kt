package com.erika.listify.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.combinedClickable
import com.erika.listify.data.repository.ListRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Icon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onOpenList: (String) -> Unit,
    onImport: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    var menuExpanded by remember { mutableStateOf(false) }
    var selectedListId by remember { mutableStateOf<String?>(null) }

    // Para renombrar
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    var fabMenuExpanded by remember { mutableStateOf(false) }

    val lists = remember { mutableStateOf(ListRepository.getLists()) }
    fun refresh() { lists.value = ListRepository.getLists() }

    Scaffold(
        floatingActionButton = {
            Box {
                LargeFloatingActionButton(onClick = { fabMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir",
                        modifier = Modifier.size(22.dp) // tamaño del "+"
                    )
                }

                DropdownMenu(
                    expanded = fabMenuExpanded,
                    onDismissRequest = { fabMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nueva lista") },
                        onClick = {
                            fabMenuExpanded = false
                            showDialog = true // tu diálogo actual
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Importar desde texto") },
                        onClick = {
                            fabMenuExpanded = false
                            onImport()
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Mis listas", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            if (lists.value.isEmpty()) {
                Text("Aún no tienes listas. Pulsa + para crear una.")
            } else {
                LazyColumn {
                    items(lists.value) { myList ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .combinedClickable(
                                    onClick = { onOpenList(myList.id) },
                                    onLongClick = {
                                        selectedListId = myList.id
                                        renameText = myList.title
                                        menuExpanded = true
                                    }
                                )
                        ) {
                            Box {
                                Column(Modifier.padding(16.dp)) {
                                    Text(myList.title, style = MaterialTheme.typography.titleMedium)
                                    Text("${myList.items.size} ítems", style = MaterialTheme.typography.bodyMedium)
                                }

                                // Menú contextual
                                DropdownMenu(
                                    expanded = menuExpanded && selectedListId == myList.id,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Cambiar nombre") },
                                        onClick = {
                                            menuExpanded = false
                                            showRenameDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Duplicar") },
                                        onClick = {
                                            menuExpanded = false
                                            ListRepository.duplicateList(myList.id)
                                            refresh()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Eliminar") },
                                        onClick = {
                                            menuExpanded = false
                                            ListRepository.deleteList(myList.id)
                                            refresh()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nueva lista") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Nombre (ej: Lista compras)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = newTitle.trim()
                        if (title.isNotEmpty()) {
                            val id = ListRepository.createList(title)
                            newTitle = ""
                            showDialog = false
                            refresh()
                            onOpenList(id)
                        }
                    }
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Cambiar nombre") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Nuevo nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = selectedListId
                        if (id != null) {
                            ListRepository.renameList(id, renameText)
                            refresh()
                        }
                        showRenameDialog = false
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
