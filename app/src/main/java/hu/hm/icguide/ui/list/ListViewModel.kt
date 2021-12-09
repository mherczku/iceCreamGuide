package hu.hm.icguide.ui.list

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.interactors.FirebaseInteractor
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val listPresenter: ListPresenter
) : RainbowCakeViewModel<ListViewState>(ListViewState()) {

    fun load() = execute {
        Timber.d("Loading empty state for list fragment")
        viewState = ListViewState()
    }

    fun initShopListeners(
        listener: FirebaseInteractor.DataChangedListener,
        feedBack: (String?) -> Unit
    ) {
        Timber.d("Initializing listener in list fragment to listen for data changes")
        listPresenter.initShopListeners(listener, feedBack)
    }

    fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        Timber.d("Changing data in list fragment")
        val newList = listPresenter.dataChanged(dc, type, viewState.shops)
        newList.sortBy { it.name }
        viewState = ListViewState(shops = newList)
    }

}