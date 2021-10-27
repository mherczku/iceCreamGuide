package hu.hm.icguide.ui.adminList

import hu.hm.icguide.models.Shop


data class AdminListViewState(
    val shops: MutableList<Shop> = mutableListOf(),
    val isRefreshing: Boolean = false
)
