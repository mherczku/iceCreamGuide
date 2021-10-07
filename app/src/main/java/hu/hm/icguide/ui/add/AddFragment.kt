package hu.hm.icguide.ui.add

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.example.icguide.R
import com.example.icguide.databinding.FragmentAddBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.extensions.validateNonEmpty


@AndroidEntryPoint
class AddFragment(private val position: LatLng) : RainbowCakeFragment<AddViewState, AddViewModel>(),
    OnSuccessListener<Any>, OnFailureListener {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_add

    private lateinit var binding: FragmentAddBinding

    private var imageSet = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddBinding.bind(view)
        binding.fabAddShop.setOnClickListener {
            addClick()
        }
        binding.imgAttach.setOnClickListener {
            attachImage()
        }
        binding.imgAttach.setImageDrawable(resources.getDrawable(R.drawable.placeholder, resources.newTheme()))

        // TODO Setup views
    }

    private fun attachImage() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, 101)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == 101) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap ?: return
            imageSet = true
            binding.imgAttach.setImageBitmap(imageBitmap)
        }
    }


    override fun onStart() {
        super.onStart()

        viewModel.load()
    }

    override fun render(viewState: AddViewState) {
        // TODO Render state
    }

    private fun addClick() {
        if (!validateForm()) {
            return
        }

        val newShop = AddPresenter.UploadShop(
            name = binding.addNameTextField.editText?.text.toString(),
            address = binding.addAddressTextField.editText?.text.toString(),
            geoPoint = GeoPoint(position.latitude, position.longitude),
            rate = binding.ratingBar.rating,
            ratings = 1,
            photo = ""
        )

        if (!imageSet) {
            viewModel.uploadShop(newShop, this, this)
        }
        else
        {
            try {
                val bitmap: Bitmap = (binding.imgAttach.drawable as BitmapDrawable).bitmap
                viewModel.uploadShopWithImage(newShop, bitmap, this, this)
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
            toast("Név vagy cím üres")
            false
        }
    }

    override fun onSuccess(p0: Any?) {
        toast("Új fagylaltozó felvéve")
        navigator?.pop()
    }

    override fun onFailure(p0: java.lang.Exception) {
        toast("Felvétel sikertelen")
        navigator?.pop()
    }

}
