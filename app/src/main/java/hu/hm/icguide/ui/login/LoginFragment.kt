package hu.hm.icguide.ui.login

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.isVisible
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentLoginBinding
import hu.hm.icguide.extensions.hideKeyboard
import hu.hm.icguide.extensions.toast
import hu.hm.icguide.extensions.validateNonEmpty
import hu.hm.icguide.ui.list.ListFragment
import hu.hm.icguide.ui.settings.EditTextDialog

@AndroidEntryPoint
class LoginFragment : RainbowCakeFragment<LoginViewState, LoginViewModel>() {

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
        binding.tvForgotPassword.setOnClickListener {
            val editTextDialog = EditTextDialog()
            editTextDialog.btnText = getString(R.string.send)
            editTextDialog.toolbarNavigationIcon
            editTextDialog.editTextInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            editTextDialog.toolbarTitle = getString(R.string.send_password_reset_email)
            editTextDialog.text = binding.etEmail.text.toString()
            editTextDialog.setListener { email ->
                viewModel.requestPasswordReset(email){
                    val m = it ?: getString(R.string.password_reset_email_sent)
                    toast(m)
                }
            }
            editTextDialog.show(childFragmentManager, null)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.load()
    }

    override fun render(viewState: LoginViewState) {
        /*Empty since the viewState is always empty, there are no different states for this view.*/
    }

    private fun showProgressDialog() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressDialog() {
        progressBar.visibility = View.GONE
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
        viewModel.register(binding.etEmail.text.toString(), binding.etPassword.text.toString()){
            hideProgressDialog()
            val m = it ?: getString(R.string.registration_successful)
            toast(m)
        }
    }

    private fun loginClick() {
        showProgressDialog()
        if (!validateForm()) {
            hideProgressDialog()
            return
        }
        hideKeyboard()
        viewModel.login(binding.etEmail.text.toString(), binding.etPassword.text.toString()) {
            hideProgressDialog()
            val m = it ?: getString(R.string.login_successful)
            toast(m)
            if(it == null) {
                navigator?.replace(ListFragment())
            }
            else binding.tvForgotPassword.isVisible = true
        }
    }

}