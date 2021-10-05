package hu.hm.icguide.ui.add

import android.graphics.Bitmap
import co.zsmb.rainbowcake.withIOContext
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.hm.icguide.network.NetworkShop
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject

class AddPresenter @Inject constructor() {

    suspend fun getData(): String = withIOContext {
        ""
    }

    fun uploadShop(newShop: NetworkShop, onSuccessListener: OnSuccessListener<Any>, onFailureListener: OnFailureListener) {
        val db = Firebase.firestore

        db.collection("shops")
            .add(newShop)
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener)
    }

    fun uploadShopWithImage(newShop: NetworkShop, bitmap: Bitmap, onSuccessListener: OnSuccessListener<Any>, onFailureListener: OnFailureListener) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("images/$newImageName")

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener(onFailureListener)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }

                newImageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                newShop.photo = downloadUri.toString()
                uploadShop(newShop, onSuccessListener, onFailureListener)
            }
    }

}
