package hu.hm.icguide.extensions

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hu.hm.icguide.R
import hu.hm.icguide.databinding.DialogEditTextBinding
import kotlin.reflect.KFunction1


fun EditText.validateNonEmpty(): Boolean {
    if (text.isEmpty()) {
        error = "Required"
        return false
    }
    return true
}

fun TextInputLayout.validateNonEmpty(): Boolean {
    if (editText?.text?.isEmpty() == true) {
        error = "Required"
        return false
    }
    return true
}

fun Fragment.toast(message: String?, length: Int = Toast.LENGTH_SHORT) {
    message ?: return
    Toast.makeText(context, message, length).show()
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

class SettingsPreference : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}

class EditTextDialog(
    private val text: String? = null,
    private val myCallback: (String?, String?, String?) -> (Unit),
    private val inputType: Int = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
    private val type: Int
) : DialogFragment() {

    companion object {
        const val EDIT_NAME = 1
        const val EDIT_EMAIL = 2
        const val EDIT_PHONE = 3
        const val AUTHENTICATE = 0
    }

    private lateinit var binding: DialogEditTextBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogEditTextBinding.inflate(LayoutInflater.from(context))
        binding.addNameTextField.editText?.inputType = inputType

        if (type == AUTHENTICATE) {
            binding.toolbar.title = getString(R.string.authenticate)
            binding.toolbar.navigationIcon = ResourcesCompat.getDrawable(resources, R.drawable.security_light, null)
            binding.addNameTextField.editText?.hint = getString(R.string.password)
            binding.btnEditTextDialog.text = getString(R.string.authenticate)
            binding.addNameTextField.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            binding.btnEditTextDialog.setOnClickListener {
                if (binding.addNameTextField.validateNonEmpty()) {
                    myCallback(
                        binding.addNameTextField.editText?.text.toString(),
                        null,
                        null
                    )
                }
            }
        } else {
            binding.addNameTextField.editText?.setText(text)
            binding.btnEditTextDialog.setOnClickListener {

                when (type) {
                    EDIT_NAME -> {
                        if (binding.addNameTextField.validateNonEmpty()) {
                            myCallback(
                                binding.addNameTextField.editText?.text.toString(),
                                null,
                                null
                            )
                            this.dismiss()
                        }
                    }
                    EDIT_EMAIL -> {
                        if (binding.addNameTextField.validateNonEmpty()) {
                            myCallback(
                                null,
                                binding.addNameTextField.editText?.text.toString(),
                                null
                            )
                            this.dismiss()
                        }
                    }
                    EDIT_PHONE -> {
                        if (binding.addNameTextField.validateNonEmpty()) {
                            myCallback(
                                null,
                                null,
                                binding.addNameTextField.editText?.text.toString()
                            )
                            this.dismiss()
                        }
                    }
                }
            }
        }
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }
}