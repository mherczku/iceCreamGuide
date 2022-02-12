package hu.hm.icguide.ui.detail

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val detailPresenter: DetailPresenter
) : RainbowCakeViewModel<DetailViewState>(DetailViewState()) {

    fun load(id: String) = execute {
        Timber.d("Loading shop and comments for detail fragment")
        viewState = DetailViewState(detailPresenter.getShop(id), detailPresenter.getComments(id))
    }

    fun isNetAvailable(): Boolean = detailPresenter.isNetAvailable()

    fun refreshShop(shopId: String) = execute {
        Timber.d("Refreshing shop for detail fragment")
        viewState = viewState.copy(shop = detailPresenter.getShop(shopId))
    }

    fun refreshComments(shopId: String) = execute {
        Timber.d("Refreshing comments for detail fragment")
        viewState = viewState.copy(comments = detailPresenter.getComments(shopId))
    }

    fun postComment(shopId: String, c: DetailPresenter.PostComment, feedback: (String?) -> Unit) {
        Timber.d("Posting comment in detail fragment")
        detailPresenter.postComment(shopId, c, feedback)
    }

}