package hu.hm.icguide.ui.settings

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import hu.hm.icguide.databinding.DialogEditTextBinding
import hu.hm.icguide.extensions.validateNonEmpty

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