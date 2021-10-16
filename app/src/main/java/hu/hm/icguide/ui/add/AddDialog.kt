package hu.hm.icguide.ui.add

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.DialogAddBinding
import hu.hm.icguide.extensions.validateNonEmpty
import hu.hm.icguide.interactors.FirebaseInteractor
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class AddDialog(private val position: LatLng) : DialogFragment(),
    OnSuccessListener<Any>, OnFailureListener {

    data class UploadShop(
        val name: String,
        val address: String,
        val geoPoint: GeoPoint,
        var photo: String,
        val rate: Float,
        val ratings: Int
    )

    @Inject
    lateinit var firebaseInteractor: FirebaseInteractor
    private lateinit var binding: DialogAddBinding
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private var imageSet = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogAddBinding.inflate(LayoutInflater.from(context))
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap
                    imageSet = true
                    binding.imgAttach.setImageBitmap(imageBitmap)
                }
            }

        setupViews()

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupViews() {
        binding.imgAttach.setOnClickListener {
            attachImage()
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
            name = binding.addNameTextField.editText?.text.toString(),
            address = binding.addAddressTextField.editText?.text.toString(),
            geoPoint = GeoPoint(position.latitude, position.longitude),
            rate = binding.ratingBar.rating,
            ratings = 1,
            photo = ""
        )

        if (!imageSet) {
            uploadShop(newShop, this, this)
        } else {
            try {
                val bitmap: Bitmap = (binding.imgAttach.drawable as BitmapDrawable).bitmap
                uploadShopWithImage(newShop, bitmap, this, this)
            } catch (e: Exception) {
                e.printStackTrace()
                toast(e.message)
            }
        }
    }

    private fun toast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateForm(): Boolean {
        return if (binding.addNameTextField.validateNonEmpty() && binding.addAddressTextField.validateNonEmpty()) {
            true
        } else {
            toast(getString(R.string.name_address_empty))
            false
        }
    }

    private fun uploadShop(
        newShop: UploadShop,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        firebaseInteractor.uploadShop(newShop, onSuccessListener, onFailureListener)
    }

    private fun uploadShopWithImage(
        newShop: UploadShop,
        bitmap: Bitmap,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        firebaseInteractor.uploadShopWithImage(
            imageInBytes,
            newShop,
            onFailureListener,
            onSuccessListener
        )
    }

    override fun onSuccess(p0: Any?) {
        toast(getString(R.string.shop_added))
        this.dismiss()
    }

    override fun onFailure(p0: Exception) {
        toast(getString(R.string.add_unsuccessful))
        toast(p0.localizedMessage)
        this.dismiss()
    }

}