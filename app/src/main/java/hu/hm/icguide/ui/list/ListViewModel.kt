package hu.hm.icguide.ui.list

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val listPresenter: ListPresenter
) : RainbowCakeViewModel<ListViewState>(ListViewState()) {

    fun load() = execute {
        viewState = ListViewState(listPresenter.getData())
    }

}
