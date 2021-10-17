package hu.hm.icguide.ui.login

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import hu.hm.icguide.interactors.FirebaseInteractor
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val interactor: FirebaseInteractor) {

    fun register(
        email: String,
        password: String,
        onRegisterSuccessListener: FirebaseInteractor.OnRegisterSuccessListener,
        onFailureListener: OnFailureListener
    ) {
        interactor.registerFirebase(email, password, onRegisterSuccessListener, onFailureListener)
    }

    fun login(
        email: String,
        password: String,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        interactor.loginFirebase(email, password, onSuccessListener, onFailureListener)
    }

}
