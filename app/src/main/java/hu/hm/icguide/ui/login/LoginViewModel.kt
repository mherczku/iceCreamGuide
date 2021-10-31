package hu.hm.icguide.ui.login

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginPresenter: LoginPresenter
) : RainbowCakeViewModel<LoginViewState>(LoginViewState()) {

    fun load() = execute {
        Timber.d("Loading empty state for login fragment")
        viewState = LoginViewState()
    }

    fun register(
        email: String,
        password: String,
        feedback: (Int, String?) -> Unit
    ) {
        Timber.d("Registering $email user")
        loginPresenter.register(email, password, feedback)
    }

    fun login(
        email: String,
        password: String,
        feedback: (Int, String?) -> Unit
    ) {
        Timber.d("Logging in $email user")
        loginPresenter.login(email, password, feedback)
    }

}