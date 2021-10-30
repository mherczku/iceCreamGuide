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

    internal fun refreshShop(shopId: String) = execute {
        Timber.d("Refreshing shop for detail fragment")
        viewState = viewState.copy(shop = detailPresenter.getShop(shopId))
    }

    fun postComment(shopId: String, c: DetailPresenter.PostComment, feedback: (String?) -> Unit) {
        Timber.d("Posting comment in detail fragment")
        detailPresenter.postComment(shopId, c, feedback)
    }


    /*fun postComment(
        shopId: String,
        c: DetailPresenter.PostComment,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        detailPresenter.postComment(shopId, c, onSuccessListener, onFailureListener)
    }

    fun initCommentsListeners(
        shopId: String,
        listener: FirebaseInteractor.DataChangedListener,
        toastListener: FirebaseInteractor.OnToastListener
    ) {
        detailPresenter.initCommentsListeners(shopId, listener, toastListener)
    }*/

    /*fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        val newList = detailPresenter.dataChanged(dc, type, viewState.comments)
        newList.sortByDescending { it.date }
        viewState = DetailViewState(comments = newList, shop = viewState.shop)
    }*/
    /*private fun updateComments2(comments: MutableList<Comment>){
        comments.sortByDescending { it.date }
        viewState = DetailViewState(comments = comments, shop = viewState.shop)
    }*/
    /*fun updateComments(shopId: String) {
        detailPresenter.updateComments(shopId, ::updateComments2)
    }*/

}
