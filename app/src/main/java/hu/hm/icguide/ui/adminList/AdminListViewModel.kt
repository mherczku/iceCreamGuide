package hu.hm.icguide.ui.adminList

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.models.Shop
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AdminListViewModel @Inject constructor(
    private val adminListPresenter: AdminListPresenter
) : RainbowCakeViewModel<AdminListViewState>(AdminListViewState()) {

    fun load() = execute {
        Timber.d("Loading new shops for admin list fragment")
        viewState = AdminListViewState(shops = adminListPresenter.getNewShops())
    }

    fun refreshList() = execute {
        viewState = viewState.copy(isRefreshing = true)
        Timber.d("Manually refreshing admin list")
        viewState = AdminListViewState(shops = adminListPresenter.getNewShops(), isRefreshing = false)
    }

    fun addShop(shop: Shop) = execute {
        Timber.d("Adding new shop ${shop.id} to shops")
        adminListPresenter.addShop(shop)
    }

    fun deleteShop(id: String) = execute {
        Timber.d("Deleting new shop $id")
        adminListPresenter.deleteNewShop(id)
    }

    /*fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        val newList = adminListPresenter.dataChanged(dc, type, viewState.shops)
        newList.sortBy { it.name }
        viewState = AdminListViewState(shops = newList)
    }*/

    /*fun initShopListeners(
        listener: FirebaseInteractor.DataChangedListener,
        toastListener: FirebaseInteractor.OnToastListener
    ) {
        adminListPresenter.initShopListeners(listener, toastListener)
    }*/

    /*fun getNewShops(){
        adminListPresenter.getNewShops {
            viewState = AdminListViewState(shops = it, isRefreshing = false)
        }
    }*/

}
