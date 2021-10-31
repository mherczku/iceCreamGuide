package hu.hm.icguide.ui.login

import hu.hm.icguide.interactors.FirebaseInteractor
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val interactor: FirebaseInteractor) {

    fun register(
        email: String,
        password: String,
        feedback: (Int, String?) -> Unit
    ) {
        interactor.registerFirebase(email, password, feedback)
    }

    fun login(
        email: String,
        password: String,
        feedback: (Int, String?) -> Unit
    ) {
        interactor.loginFirebase(email, password, feedback)
    }

}