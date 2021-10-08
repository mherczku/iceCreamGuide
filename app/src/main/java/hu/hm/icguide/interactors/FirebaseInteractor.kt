package hu.hm.icguide.interactors

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.hm.icguide.ui.add.AddPresenter
import hu.hm.icguide.ui.list.ListPresenter
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject


class FirebaseInteractor @Inject constructor(){

    interface DataChangedListener{
        fun dataChanged(dc: QueryDocumentSnapshot, type: String)
    }

    fun initShopsListener(listener: DataChangedListener ) {
        val db = Firebase.firestore
        db.collection("shops")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // TODO Toast.makeText(context, "FireStore hiba", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> listener.dataChanged(
                            dc.document,
                            ListPresenter.NEW_SHOP
                        )
                        DocumentChange.Type.MODIFIED -> listener.dataChanged(
                            dc.document,
                            ListPresenter.EDIT_SHOP
                        )
                        DocumentChange.Type.REMOVED -> listener.dataChanged(
                            dc.document, ListPresenter.REMOVE_SHOP
                        )
                    }
                }
            }
    }

    fun uploadShop(
        newShop: AddPresenter.UploadShop,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        val db = Firebase.firestore

        db.collection("shops")
            .add(newShop)
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener)
    }

    fun uploadShopWithImage(
        imageInBytes: ByteArray,
        newShop: AddPresenter.UploadShop,
        onFailureListener: OnFailureListener,
        onSuccessListener: OnSuccessListener<Any>
    ) {
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val storageReference = FirebaseStorage.getInstance().reference
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