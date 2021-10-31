package hu.hm.icguide.ui.add

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.DialogAddBinding
import hu.hm.icguide.extensions.toast
import hu.hm.icguide.extensions.validateNonEmpty
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.UploadShop
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class AddDialog(private val position: LatLng) : DialogFragment(), OnFailureListener {

    @Inject
    lateinit var firebaseInteractor: FirebaseInteractor
    private lateinit var binding: DialogAddBinding
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private var imageSet = false
    private lateinit var startForResultGallery: ActivityResultLauncher<Intent>
    private var isPermissionGranted = false
    private lateinit var permReqLauncher: ActivityResultLauncher<String>


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogAddBinding.inflate(LayoutInflater.from(context))
        permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                toast(getString(R.string.permission_granted))
                isPermissionGranted = true
                pickImage()
            } else toast(getString(R.string.permission_denied))
        }
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap
                    imageSet = true
                    binding.imgAttach.setImageBitmap(imageBitmap)
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
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                    }
                    imageBitmap ?: return@registerForActivityResult
                    binding.imgAttach.setImageBitmap(
                        Bitmap.createScaledBitmap(
                            imageBitmap,
                            binding.imgAttach.width,
                            binding.imgAttach.height,
                            false
                        )
                    )
                    imageSet = true
                }
            }

        setupViews()
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupViews() {
        binding.addAddressTextField.editText?.setText(geocode(position))
        binding.imgAttach.setOnClickListener {
            AlertDialog.Builder(context)
                .setCancelable(true)
                .setMessage(R.string.gallery_or_create)
                .setPositiveButton(getString(R.string.take_photo)) { _, _ ->
                    attachImage()
                }
                .setNeutralButton(getString(R.string.upload_photo)) { _, _ ->
                    handleStoragePermission()
                }
                .create().show()
        }
        binding.btnAdd.setOnClickListener { addClick() }
    }

    private fun attachImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startForResult.launch(Intent(takePictureIntent))
    }

    private fun addClick() {
        if (!validateForm()) {
            return
        }

        val newShop = UploadShop(
            name = binding.addNameTextField.editText?.text.toString()
                .replaceFirstChar { it.uppercase() },
            address = binding.addAddressTextField.editText?.text.toString(),
            geoPoint = GeoPoint(position.latitude, position.longitude),
            rate = 0F,
            ratings = 0,
            photo = ""
        )

        if (!imageSet) {
            firebaseInteractor.uploadShop(newShop, this::feedBack)
            binding.btnAdd.isEnabled = false
        } else {
            try {
                val bitmap: Bitmap = (binding.imgAttach.drawable as BitmapDrawable).bitmap
                uploadShopWithImage(newShop, bitmap, this, this::feedBack)
                binding.btnAdd.isEnabled = false
            } catch (e: Exception) {
                toast(e.message)
            }
        }
    }

    private fun feedBack(message: String?) {
        val m = message ?: getString(R.string.upload_successful)
        toast(m)
        this.dismiss()
    }

    private fun validateForm(): Boolean {
        return if (binding.addNameTextField.validateNonEmpty() && binding.addAddressTextField.validateNonEmpty()) {
            true
        } else {
            toast(getString(R.string.name_address_empty))
            false
        }
    }

    private fun uploadShopWithImage(
        newShop: UploadShop,
        bitmap: Bitmap,
        onFailureListener: OnFailureListener,
        feedBack: (String?) -> Unit
    ) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        firebaseInteractor.uploadShopWithImage(
            imageInBytes,
            newShop,
            onFailureListener,
            feedBack
        )
    }

    override fun onFailure(p0: Exception) {
        toast(getString(R.string.add_unsuccessful))
        toast(p0.localizedMessage)
        this.dismiss()
    }

    private fun pickImage() {
        val getPicIntent = Intent(Intent.ACTION_PICK)
        getPicIntent.type = "image/*"
        startForResultGallery.launch(Intent(getPicIntent))
    }

    private fun geocode(latLng: LatLng): String {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        val address = addresses[0]
        return address.getAddressLine(0)
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