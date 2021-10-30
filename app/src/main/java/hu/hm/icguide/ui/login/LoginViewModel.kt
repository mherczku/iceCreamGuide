package hu.hm.icguide.ui.login

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
        feedback: (Int, String?) -> Unit
    ) {
        loginPresenter.register(email, password, feedback)
    }

    fun login(
        email: String,
        password: String,
        feedback: (Int, String?) -> Unit
    ) {
        loginPresenter.login(email, password, feedback)
    }

}
