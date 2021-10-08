package hu.hm.icguide.ui.add

import android.graphics.Bitmap
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val addPresenter: AddPresenter
) : RainbowCakeViewModel<AddViewState>(AddViewState()) {

    fun load() = execute {
        viewState = AddViewState()
    }

    fun uploadShop(newShop: AddPresenter.UploadShop, onSuccessListener: OnSuccessListener<Any>, onFailureListener: OnFailureListener) {
        addPresenter.uploadShop(newShop, onSuccessListener, onFailureListener)
    }

    fun uploadShopWithImage(newShop: AddPresenter.UploadShop, bitmap: Bitmap, onSuccessListener: OnSuccessListener<Any>, onFailureListener: OnFailureListener) {
        addPresenter.uploadShopWithImage(newShop, bitmap, onSuccessListener, onFailureListener)
    }

}
