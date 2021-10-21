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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import co.zsmb.rainbowcake.navigation.navigator
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentSettingsBinding
import hu.hm.icguide.extensions.EditTextDialog
import hu.hm.icguide.extensions.SettingsPreference
import hu.hm.icguide.extensions.toast
import hu.hm.icguide.interactors.FirebaseInteractor
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var firebaseInteractor: FirebaseInteractor
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var user: FirebaseUser
    private lateinit var startForResultGalery: ActivityResultLauncher<Intent>
    private lateinit var startForResultCamera: ActivityResultLauncher<Intent>
    private var isPermissionGranted = false
    private lateinit var permReqLauncher: ActivityResultLauncher<String>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = firebaseInteractor.firebaseUser!!
        setupView()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.beginTransaction().add(R.id.container, SettingsPreference()).commit()
        permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Toast.makeText(context, getString(R.string.permission_granted), Toast.LENGTH_SHORT)
                    .show()
                isPermissionGranted = true
                pickImage()

            } else {
                Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        startForResultCamera =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap
                    firebaseInteractor.uploadImage(imageBitmap, ::updatePic)
                }
            }
        startForResultGalery =
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
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        imageBitmap,
                        binding.ivUser.width,
                        binding.ivUser.height,
                        false
                    )
                    firebaseInteractor.uploadImage(scaledBitmap, ::updatePic)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupView() {
        binding.toolbar.setNavigationOnClickListener { navigator?.pop() }
        binding.tvName.text = user.displayName

        Glide.with(binding.ivUser)
            .load(user.photoUrl)
            .placeholder(R.drawable.placeholder)
            .into(binding.ivUser)

        binding.ivUser.setOnClickListener {
            AlertDialog.Builder(context)
                .setCancelable(true)
                .setMessage(R.string.galery_or_create)
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

        binding.tvName.setOnClickListener {
            EditTextDialog(binding.tvName.text.toString(), ::editName).show(
                childFragmentManager,
                null
            )
        }
    }

    private fun editName(name: String) {
        firebaseInteractor.updateProfile(name, myCallback =  ::updateView)
    }
    private fun updatePic(message : String? = getString(R.string.pic_upload_successful), photo: Uri? = null){
        toast(message)
        if(photo != null) firebaseInteractor.updateProfile(photo = photo, myCallback = ::updateView)
    }

    private fun updateView(message: String? = null) {
        if (message == null) toast(getString(R.string.profile_updated))
        else toast(message)
        user = firebaseInteractor.firebaseUser!!
        binding.tvName.text = user.displayName
        Glide.with(binding.ivUser)
            .load(user.photoUrl)
            .placeholder(R.drawable.placeholder)
            .into(binding.ivUser)
    }

    private fun pickImage() {
        val getPicIntent = Intent(Intent.ACTION_PICK)
        getPicIntent.type = "image/*"
        startForResultGalery.launch(Intent(getPicIntent))
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