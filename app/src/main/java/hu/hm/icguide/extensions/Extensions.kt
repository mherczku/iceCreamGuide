package hu.hm.icguide.extensions

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.textfield.TextInputLayout
import hu.hm.icguide.R
import hu.hm.icguide.databinding.DialogEditTextBinding
import java.util.*


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

class SettingsPreference : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == "darkTheme"){
            val darkMode = sharedPreferences?.getBoolean(key, false)
            if(darkMode == true) AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }
    }
}

class EditTextDialog: DialogFragment() {

    private lateinit var listener: (String) -> Unit
    var text: String? = null
    var editTextInputType: Int = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
    var hint: String = ""
    var toolbarTitle: String = ""
    var btnText: String = ""
    var toolbarNavigationIcon: Drawable? = null
    var textFieldEndIconMode: Int = TextInputLayout.END_ICON_CLEAR_TEXT

    fun setListener(listener: (String) -> Unit) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = DialogEditTextBinding.inflate(LayoutInflater.from(context))
        binding.textField.editText?.inputType = editTextInputType

        binding.toolbar.title = toolbarTitle
        binding.textField.editText?.setText(text)
        binding.toolbar.navigationIcon = toolbarNavigationIcon
        binding.textField.editText?.hint = hint
        binding.textField.endIconMode = textFieldEndIconMode
        binding.btnConfirm.text = btnText
        binding.btnConfirm.setOnClickListener {
            if (binding.textField.validateNonEmpty()) {
                listener(binding.textField.editText?.text.toString())
                this.dismiss()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }
}