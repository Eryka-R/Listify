package com.erika.listify.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erika.listify.data.repository.ListRepository

@Composable
fun HomeScreen(
    onOpenList: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    val lists = remember { mutableStateOf(ListRepository.getLists()) }
    fun refresh() { lists.value = ListRepository.getLists() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
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
                                .clickable { onOpenList(myList.id) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(myList.title, style = MaterialTheme.typography.titleMedium)
                                Text("${myList.items.size} ítems", style = MaterialTheme.typography.bodyMedium)
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
}