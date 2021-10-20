package hu.hm.icguide.extensions

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
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

class EditTextDialog(private val text: String, private val myCallback: KFunction1<String, Unit>) : DialogFragment() {

    private lateinit var binding: DialogEditTextBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogEditTextBinding.inflate(LayoutInflater.from(context))
        binding.addNameTextField.editText?.setText(text)
        binding.btnEditTextDialog.setOnClickListener {
            if (binding.addNameTextField.validateNonEmpty()) {
                myCallback(binding.addNameTextField.editText?.text.toString())
                this.dismiss()
            }
        }
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }
}