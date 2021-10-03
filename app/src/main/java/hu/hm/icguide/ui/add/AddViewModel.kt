package hu.hm.icguide.ui.add

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.ui.add.AddPresenter
import hu.hm.icguide.ui.add.AddViewState
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val addPresenter: AddPresenter
) : RainbowCakeViewModel<AddViewState>(AddViewState()) {

    fun load() = execute {
        viewState = AddViewState(addPresenter.getData())
    }

}
