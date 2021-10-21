package hu.hm.icguide.ui.detail

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Comment
import hu.hm.icguide.models.Shop
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val detailPresenter: DetailPresenter
) : RainbowCakeViewModel<DetailViewState>(DetailViewState()) {

    fun load() = execute {
        viewState = DetailViewState()
    }

    fun isNetAvailable(): Boolean = detailPresenter.isNetAvailable()
    fun postComment(
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
    }

    fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        val newList = detailPresenter.dataChanged(dc, type, viewState.comments)
        newList.sortByDescending { it.date }
        viewState = DetailViewState(comments = newList, shop = viewState.shop)
    }

    fun getShop(id: String) {
        detailPresenter.getShop(id, ::updateShop)
    }

    private fun updateShop(shop : Shop){
        viewState = DetailViewState(comments = viewState.comments, shop = shop)
    }

    fun updateComments(shopId: String) {
        detailPresenter.updateComments(shopId, ::updateComments2)
    }

    private fun updateComments2(comments: MutableList<Comment>){
        comments.sortByDescending { it.date }
        viewState = DetailViewState(comments = comments, shop = viewState.shop)
    }

}
