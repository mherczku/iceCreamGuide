package hu.hm.icguide.ui.detail

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.ui.detail.DetailPresenter
import hu.hm.icguide.ui.detail.DetailViewState
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val detailPresenter: DetailPresenter
) : RainbowCakeViewModel<DetailViewState>(DetailViewState()) {

    fun load() = execute {
        viewState = DetailViewState(detailPresenter.getData())
    }

}
