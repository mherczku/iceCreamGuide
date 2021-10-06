package hu.hm.icguide.ui.list

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.network.NetworkShop
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val listPresenter: ListPresenter
) : RainbowCakeViewModel<ListViewState>(ListViewState()) {


    fun load() = execute {
        viewState = ListViewState()
    }

    fun initShopsListener(adapter: ShopAdapter) {
        listPresenter.initShopsListener(adapter)
    }

}
