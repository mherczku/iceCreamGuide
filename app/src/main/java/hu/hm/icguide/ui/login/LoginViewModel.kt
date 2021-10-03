package hu.hm.icguide.ui.login

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginPresenter: LoginPresenter
) : RainbowCakeViewModel<LoginViewState>(LoginViewState()) {

    fun load() = execute {
        viewState = LoginViewState(loginPresenter.getData())
    }

}
