package com.erika.listify.data.repository

import com.erika.listify.data.model.MyItem
import com.erika.listify.data.model.MyList
import java.util.UUID

object ListRepository {

    private val lists = mutableListOf<MyList>()

    fun getLists(): List<MyList> = lists.toList()

    fun createList(title: String): String {
        val id = UUID.randomUUID().toString()
        lists.add(MyList(id = id, title = title))
        return id
    }

    fun getList(id: String): MyList? = lists.find { it.id == id }

    fun addItem(listId: String, text: String) {
        val list = getList(listId) ?: return
        val newItem = MyItem(id = UUID.randomUUID().toString(), text = text)
        updateList(list.copy(items = list.items + newItem))
    }

    fun toggleItem(listId: String, itemId: String) {
        val list = getList(listId) ?: return
        val newItems = list.items.map {
            if (it.id == itemId) it.copy(checked = !it.checked) else it
        }
        updateList(list.copy(items = newItems))
    }

    private fun updateList(updated: MyList) {
        val index = lists.indexOfFirst { it.id == updated.id }
        if (index != -1) lists[index] = updated
    }
}