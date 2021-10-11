package hu.hm.icguide.ui.add

/*
import android.graphics.Bitmap
import co.zsmb.rainbowcake.withIOContext
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.GeoPoint
import hu.hm.icguide.interactors.FirebaseInteractor
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class AddPresenter @Inject constructor(private val firebaseInteractor: FirebaseInteractor) {

    data class UploadShop(
        val name: String,
        val address: String,
        val geoPoint: GeoPoint,
        var photo: String,
        val rate: Float,
        val ratings: Int
    )

    fun uploadShop(newShop: UploadShop, onSuccessListener: OnSuccessListener<Any>, onFailureListener: OnFailureListener) {
        firebaseInteractor.uploadShop(newShop, onSuccessListener, onFailureListener)
    }

    fun uploadShopWithImage(newShop: UploadShop, bitmap: Bitmap, onSuccessListener: OnSuccessListener<Any>, onFailureListener: OnFailureListener) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        firebaseInteractor.uploadShopWithImage(imageInBytes, newShop, onFailureListener,onSuccessListener)
    }

}
*/