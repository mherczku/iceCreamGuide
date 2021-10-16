package hu.hm.icguide.ui.list

import hu.hm.icguide.models.Shop


data class ListViewState(
    val shops: MutableList<Shop> = mutableListOf(),
    val isRefreshing: Boolean = false
)
