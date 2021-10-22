package hu.hm.icguide.ui.login

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentLoginBinding
import hu.hm.icguide.extensions.hideKeyboard
import hu.hm.icguide.extensions.validateNonEmpty
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.ui.list.ListFragment

@AndroidEntryPoint
class LoginFragment : RainbowCakeFragment<LoginViewState, LoginViewModel>(),
    FirebaseInteractor.OnToastListener, OnSuccessListener<Any>, OnFailureListener,
    FirebaseInteractor.OnRegisterSuccessListener {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_login


    private lateinit var binding: FragmentLoginBinding
    private lateinit var progressBar: ProgressBar


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        progressBar = binding.progressBar
        binding.btnRegister.setOnClickListener { registerClick() }
        binding.btnLogin.setOnClickListener { loginClick() }
    }

    override fun onStart() {
        super.onStart()
        viewModel.load()
    }

    override fun render(viewState: LoginViewState) {
        // TODO Render state
    }

    private fun showProgressDialog() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressDialog() {
        progressBar.visibility = View.GONE
    }

    override fun toast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateForm(): Boolean {
        return if (binding.etEmail.validateNonEmpty() && binding.etPassword.validateNonEmpty()) {
            true
        } else {
            toast(getString(R.string.email_password_empty))
            false
        }
    }

    private fun registerClick() {
        if (!validateForm()) {
            return
        }
        showProgressDialog()
        viewModel.register(
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString(),
            this,
            this
        )

    }

    override fun onRegisterSuccess() {
        toast(getString(R.string.registration_successful))
        hideProgressDialog()
    }

    override fun onFailure(p0: Exception) {
        toast(p0.localizedMessage)
        hideProgressDialog()
    }

    private fun loginClick() {
        showProgressDialog()
        if (!validateForm()) {
            hideProgressDialog()
            return
        }
        hideKeyboard()
        viewModel.login(
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString(),
            this,
            this
        )
    }

    override fun onSuccess(p0: Any?) {
        hideProgressDialog()
        toast(getString(R.string.login_successful))
        navigator?.replace(ListFragment())
    }

}

