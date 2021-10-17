package hu.hm.icguide.ui.login

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.hm.icguide.interactors.FirebaseInteractor
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginPresenter: LoginPresenter
) : RainbowCakeViewModel<LoginViewState>(LoginViewState()) {

    fun load() = execute {
        viewState = LoginViewState()
    }

    fun register(
        email: String,
        password: String,
        onRegisterSuccessListener: FirebaseInteractor.OnRegisterSuccessListener,
        onFailureListener: OnFailureListener
    ) {
        loginPresenter.register(email, password, onRegisterSuccessListener, onFailureListener)
    }

    fun login(
        email: String,
        password: String,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        loginPresenter.login(email, password, onSuccessListener, onFailureListener)
    }

}
