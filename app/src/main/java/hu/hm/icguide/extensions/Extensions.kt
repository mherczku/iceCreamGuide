package hu.hm.icguide.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout


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