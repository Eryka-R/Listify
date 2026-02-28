package com.erika.listify.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

import com.erika.listify.data.model.MyItem
import com.erika.listify.data.model.MyList


object ListRepository {

    private const val FILE_NAME = "listify_data.json"
    private lateinit var appContext: Context
    private val gson = Gson()

    private val lists = mutableListOf<MyList>()

    fun init(context: Context) {
        appContext = context.applicationContext
        loadFromDisk()
    }
    fun getLists(): List<MyList> = lists.toList()

    fun createList(title: String): String {
        val id = UUID.randomUUID().toString()
        lists.add(MyList(id = id, title = title))
        saveToDisk()
        return id
    }

    fun getList(id: String): MyList? = lists.find { it.id == id }

    fun addItem(listId: String, text: String) {
        val list = getList(listId) ?: return
        val normalized = text.trim()
        if (normalized.isBlank()) return

        val newItem = MyItem(id = UUID.randomUUID().toString(), text = normalized)
        updateList(list.copy(items = list.items + newItem))
    }

    fun toggleItem(listId: String, itemId: String) {
        val list = getList(listId) ?: return
        val newItems = list.items.map {
            if (it.id == itemId) it.copy(checked = !it.checked) else it
        }
        updateList(list.copy(items = newItems))
    }

    fun getAllKnownItemTexts(): List<String> {
        // Devuelve todos los textos distintos que existen en todas las listas
        return lists
            .flatMap { it.items }
            .map { it.text.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    fun renameList(listId: String, newTitle: String) {
        val list = getList(listId) ?: return
        val title = newTitle.trim()
        if (title.isBlank()) return
        updateList(list.copy(title = title))
    }

    fun deleteList(listId: String) {
        val index = lists.indexOfFirst { it.id == listId }
        if (index != -1) {
            lists.removeAt(index)
            saveToDisk()
        }
    }

    fun duplicateList(listId: String): String? {
        val original = getList(listId) ?: return null
        val newId = UUID.randomUUID().toString()

        // Duplicamos items con ids nuevos
        val newItems = original.items.map {
            it.copy(id = UUID.randomUUID().toString())
        }

        val copyTitle = "${original.title} (copia)"
        lists.add(MyList(id = newId, title = copyTitle, items = newItems))
        saveToDisk()
        return newId
    }

    private fun updateList(updated: MyList) {
        val index = lists.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            lists[index] = updated
            saveToDisk()
        }
    }

    private fun dataFile(): File = File(appContext.filesDir, FILE_NAME)

    private fun saveToDisk() {
        if (!::appContext.isInitialized) return

        runCatching {
            val json = gson.toJson(lists)
            dataFile().writeText(json)
        }
    }

    private fun loadFromDisk() {
        if (!::appContext.isInitialized) return

        runCatching {
            val file = dataFile()
            if (!file.exists()) return

            val json = file.readText()
            val type = object : TypeToken<List<MyList>>() {}.type
            val loaded: List<MyList> = gson.fromJson(json, type) ?: emptyList()

            lists.clear()
            lists.addAll(loaded)
        }
    }

}