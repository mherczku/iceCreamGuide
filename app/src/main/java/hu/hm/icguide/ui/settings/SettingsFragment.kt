package hu.hm.icguide.ui.settings

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import co.zsmb.rainbowcake.navigation.navigator
import co.zsmb.rainbowcake.navigation.popUntil
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentSettingsBinding
import hu.hm.icguide.extensions.toast
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.ui.list.ListFragment
import hu.hm.icguide.ui.login.LoginFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var firebaseInteractor: FirebaseInteractor
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var user: FirebaseUser
    private lateinit var startForResultGallery: ActivityResultLauncher<Intent>
    private lateinit var startForResultCamera: ActivityResultLauncher<Intent>
    private var isPermissionGranted = false
    private lateinit var permReqLauncher: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = firebaseInteractor.firebaseUser ?: return
        setupView()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.beginTransaction().add(R.id.container, SettingsPreference()).commit()
        permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                toast(getString(R.string.permission_granted))
                isPermissionGranted = true
                pickImage()

            } else {
                toast(getString(R.string.permission_denied))
            }
        }
        startForResultCamera =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap?
                    imageBitmap ?: return@registerForActivityResult
                    firebaseInteractor.uploadImage(imageBitmap, this::feedBack)
                }
            }
        startForResultGallery =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    uri ?: return@registerForActivityResult
                    val imageBitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source =
                            ImageDecoder.createSource(requireActivity().contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            uri
                        )
                    }
                    imageBitmap ?: return@registerForActivityResult
                    firebaseInteractor.uploadImage(imageBitmap, this::feedBack)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupView() = with(binding) {
        toolbar.setNavigationOnClickListener { navigator?.pop() }
        tvName.text = user.displayName
        tvEmail.text = user.email
        checkBoxEmailVerified.isChecked = user.isEmailVerified
        if (user.isEmailVerified) btnVerifyEmail.visibility = View.GONE
        val date = SimpleDateFormat(
            getString(R.string.date_pattern_ymd),
            Locale.ENGLISH
        ).format(Date(user.metadata.creationTimestamp))
        tvRegistered.text = date

        Glide.with(ivUser)
            .load(user.photoUrl)
            .placeholder(R.drawable.placeholder)
            .into(ivUser)

        ivUser.setOnClickListener {
            AlertDialog.Builder(context)
                .setCancelable(true)
                .setMessage(R.string.gallery_or_create)
                .setPositiveButton(
                    getString(R.string.take_photo)
                ) { _, _ ->
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startForResultCamera.launch(Intent(takePictureIntent))
                }
                .setNeutralButton(getString(R.string.upload_photo)) { _, _ ->
                    handleStoragePermission()
                }
                .create().show()
        }

        binding.btnEditName.setOnClickListener {
            summonEditText(tvName.text.toString(), InputType.TYPE_TEXT_FLAG_CAP_WORDS) {
                firebaseInteractor.updateProfile(name = it, feedBack = ::updateView)
            }
        }
        btnEditEmail.setOnClickListener {
            authenticateThenDo {
                summonEditText(
                    tvEmail.text.toString(),
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                ) {
                    firebaseInteractor.updateProfile(email = it, feedBack = ::updateView)
                }
            }
        }
        btnLogout.setOnClickListener {
            firebaseInteractor.logout()
            navigator?.popUntil<ListFragment>()
            navigator?.replace(LoginFragment())
        }
        btnVerifyEmail.setOnClickListener {
            firebaseInteractor.verifyEmail {
                val m = it ?: getString(R.string.verification_email_sent)
                toast(m)
            }
            btnVerifyEmail.isEnabled = false
        }
        btnEditPassword.setOnClickListener {
            authenticateThenDo {
                summonEditText(
                    null,
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    TextInputLayout.END_ICON_PASSWORD_TOGGLE
                ) {
                    firebaseInteractor.updatePassword(it) { it2 ->
                        val m = it2 ?: getString(R.string.password_updated)
                        toast(m)
                    }
                }
            }
        }
    }

    private fun summonEditText(
        text: String? = null,
        inputType: Int? = null,
        endIconMode: Int? = null,
        listener: ((String) -> Unit)? = null
    ) {
        val editTextDialog = EditTextDialog()
        editTextDialog.text = text
        editTextDialog.toolbarTitle = getString(R.string.edit)
        editTextDialog.btnText = getString(R.string.edit)
        editTextDialog.toolbarNavigationIcon =
            ResourcesCompat.getDrawable(resources, R.drawable.outline_create_24, null)
        if (inputType != null) editTextDialog.editTextInputType = inputType
        if (listener != null) editTextDialog.setListener(listener)
        if (endIconMode != null) editTextDialog.textFieldEndIconMode = endIconMode
        editTextDialog.show(childFragmentManager, null)
    }

    private fun authenticateThenDo(doIfSuccess: () -> Unit) {
        val editTextDialog = EditTextDialog()
        editTextDialog.btnText = getString(R.string.authenticate)
        editTextDialog.toolbarNavigationIcon =
            ResourcesCompat.getDrawable(resources, R.drawable.security_light, null)
        editTextDialog.hint = getString(R.string.password)
        editTextDialog.textFieldEndIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        editTextDialog.editTextInputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        editTextDialog.toolbarTitle = getString(R.string.authenticate)
        editTextDialog.setListener {
            firebaseInteractor.authenticate(it) { it2 ->
                if (!it2.isNullOrBlank()) toast(it2)
                else if (it2 == null) {
                    toast(getString(R.string.reauth_succes))
                    doIfSuccess()
                }
            }
        }
        editTextDialog.show(childFragmentManager, null)
    }

    private fun feedBack(
        message: String? = getString(R.string.upload_successful),
        photo: Uri? = null
    ) {
        toast(message)
        if (photo != null) firebaseInteractor.updateProfile(
            photo = photo,
            feedBack = ::updateView
        )
    }

    private fun updateView(message: String? = null) {
        if (message == null) toast(getString(R.string.profile_updated))
        else toast(message)
        user = firebaseInteractor.firebaseUser!!
        binding.tvName.text = user.displayName
        binding.tvEmail.text = user.email
        binding.checkBoxEmailVerified.isChecked = user.isEmailVerified
        if (user.isEmailVerified) binding.btnVerifyEmail.visibility = View.GONE
        Glide.with(binding.ivUser)
            .load(user.photoUrl)
            .placeholder(R.drawable.placeholder)
            .into(binding.ivUser)
    }

    private fun pickImage() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        photoPickerIntent.putExtra("crop", "true")
        photoPickerIntent.putExtra("outputX", 300)
        photoPickerIntent.putExtra("outputY", 300)
        photoPickerIntent.putExtra("aspectX", 1)
        photoPickerIntent.putExtra("aspectY", 1)
        photoPickerIntent.putExtra("scale", true)
        startForResultGallery.launch(Intent(photoPickerIntent))
    }

    private fun showRationaleDialog(
        @StringRes title: Int = R.string.rationale_dialog_title,
        @StringRes explanation: Int,
        onPositiveButton: () -> Unit,
        onNegativeButton: () -> Boolean? = { navigator?.pop() }
    ) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(explanation)
            .setCancelable(false)
            .setPositiveButton(R.string.proceed) { dialog, _ ->
                dialog.cancel()
                onPositiveButton()
            }
            .setNegativeButton(R.string.exit) { _, _ -> onNegativeButton() }
            .create()
        alertDialog.show()
    }

    private fun handleStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showRationaleDialog(
                    explanation = R.string.external_storage_explanation,
                    onPositiveButton = this::requestStoragePermission
                )
            } else {
                requestStoragePermission()
            }
        } else {
            isPermissionGranted = true
            pickImage()
        }
    }

    private fun requestStoragePermission() {
        permReqLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}