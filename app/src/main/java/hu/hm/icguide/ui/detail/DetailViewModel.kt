package hu.hm.icguide.ui.detail

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.interactors.FirebaseInteractor
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
        //TODO lehet toMutableList felesleges
        val newList = detailPresenter.dataChanged(dc, type, viewState.comments.toMutableList())
        viewState = DetailViewState(comments = newList)
    }

}
