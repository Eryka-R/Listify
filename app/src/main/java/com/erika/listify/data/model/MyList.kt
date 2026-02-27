package com.erika.listify.data.model

data class MyList(
    val id: String,
    val title: String,
    val items: List<MyItem> = emptyList()
)
