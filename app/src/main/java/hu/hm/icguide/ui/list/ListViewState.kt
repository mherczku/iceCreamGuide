package hu.hm.icguide.ui.list

import hu.hm.icguide.network.NetworkShop


data class ListViewState(val shops : MutableList<NetworkShop> = mutableListOf(), val isRefreshing: Boolean = false)
