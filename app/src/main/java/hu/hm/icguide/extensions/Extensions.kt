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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
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