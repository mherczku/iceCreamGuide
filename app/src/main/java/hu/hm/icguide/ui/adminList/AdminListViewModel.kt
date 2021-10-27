package hu.hm.icguide.ui.adminList

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.interactors.FirebaseInteractor
import javax.inject.Inject

@HiltViewModel
class AdminListViewModel @Inject constructor(
    private val adminListPresenter: AdminListPresenter
) : RainbowCakeViewModel<AdminListViewState>(AdminListViewState()) {


    fun load() = execute {
        viewState = AdminListViewState()
    }

    fun refreshList() {
        viewState = viewState.copy(isRefreshing = false)
    }

    fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        val newList = adminListPresenter.dataChanged(dc, type, viewState.shops)
        newList.sortBy { it.name }
        viewState = AdminListViewState(shops = newList)
    }

    fun initShopListeners(
        listener: FirebaseInteractor.DataChangedListener,
        toastListener: FirebaseInteractor.OnToastListener
    ) {
        adminListPresenter.initShopListeners(listener, toastListener)
    }

    fun getNewShop(){
        adminListPresenter.getNewShops {
            viewState = AdminListViewState(shops = it, isRefreshing = false)
        }
    }

}
