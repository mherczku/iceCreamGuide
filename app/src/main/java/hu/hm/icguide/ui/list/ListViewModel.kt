package hu.hm.icguide.ui.list

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.interactors.FirebaseInteractor
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val listPresenter: ListPresenter
) : RainbowCakeViewModel<ListViewState>(ListViewState()){


    fun load() = execute {
        viewState = ListViewState()
    }

    fun refreshList() {
        viewState = viewState.copy(isRefreshing = false)
    }

    fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        //TODO lehet toMutableList felesleges
        val newList = listPresenter.dataChanged(dc, type, viewState.shops.toMutableList())
        viewState = ListViewState(shops = newList)
    }

    fun initShopListeners(listener: FirebaseInteractor.DataChangedListener, toastListener: FirebaseInteractor.OnToastListener) {
        listPresenter.initShopListeners(listener, toastListener)
    }

}
