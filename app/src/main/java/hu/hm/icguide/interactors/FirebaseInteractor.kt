package hu.hm.icguide.interactors

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import hu.hm.icguide.ui.add.AddPresenter
import hu.hm.icguide.ui.list.ListPresenter
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject


class FirebaseInteractor @Inject constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDb: DatabaseReference = Firebase.database.reference
    private val firestoreDb: FirebaseFirestore = Firebase.firestore
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private var firebaseUser: FirebaseUser? = null
        get() = firebaseAuth.currentUser

    interface DataChangedListener {
        fun dataChanged(dc: QueryDocumentSnapshot, type: String)
    }

    interface OnToastListener{
        fun toast(message: String?)
    }
    interface OnRegisterSuccessListener{
        fun onRegisterSuccess()
    }


    fun loginFirebase(email: String, password: String, onSuccessListener: OnSuccessListener<Any>, onFailureListener: OnFailureListener) {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener)
    }

    fun registerFirebase(email: String, password: String, onRegisterSuccessListener: OnRegisterSuccessListener, onFailureListener: OnFailureListener) {

        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener{
                val firebaseUser = it.user
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(firebaseUser?.email?.substringBefore('@'))
                    .build()
                firebaseUser?.updateProfile(profileChangeRequest)

                val uid = firebaseUser.uid

                firebaseDb.child("users").child(uid).setValue("USER")
                onRegisterSuccessListener.onRegisterSuccess()
            }
            .addOnFailureListener(onFailureListener)
    }

    fun initShopsListener(listener: DataChangedListener, toastListenerListener: OnToastListener) {
        firestoreDb.collection("shops")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    toastListenerListener.toast(e.localizedMessage)
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