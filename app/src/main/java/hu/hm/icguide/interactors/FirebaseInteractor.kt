package hu.hm.icguide.interactors

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import hu.hm.icguide.models.Comment
import hu.hm.icguide.models.Review
import hu.hm.icguide.models.Shop
import hu.hm.icguide.models.User
import hu.hm.icguide.ui.add.AddDialog
import hu.hm.icguide.ui.detail.DetailPresenter
import hu.hm.icguide.ui.list.ListPresenter
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject


class FirebaseInteractor @Inject constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb: FirebaseFirestore = Firebase.firestore
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private var users = mutableListOf<User>()
    var user: User? = null
    val firebaseUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    interface DataChangedListener {
        fun dataChanged(dc: QueryDocumentSnapshot, type: String)
    }

    interface OnToastListener {
        fun toast(message: String?)
    }

    interface OnRegisterSuccessListener {
        fun onRegisterSuccess()
    }

    init {
        if (firebaseAuth.currentUser != null) {
            getUsers()
            firestoreDb.document("users/${firebaseUser?.uid}").get()
                .addOnSuccessListener { it2 ->
                    val getUser: User? = it2.toObject()
                    if (getUser != null) {
                        user = getUser
                    }
                }
        }
    }

    fun loginFirebase(
        email: String,
        password: String,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    getUsers()
                    firestoreDb.document("users/${firebaseUser?.uid}").get()
                        .addOnSuccessListener { it2 ->
                            val getUser: User? = it2.toObject()
                            if (getUser == null) {
                                val newUser = User(
                                    uid = firebaseUser?.uid!!,
                                    role = "user",
                                    name = firebaseUser?.displayName.toString(),
                                    photo = firebaseUser?.photoUrl.toString()
                                )
                                firestoreDb.collection("users").document(newUser.uid).set(newUser)
                            } else {
                                user = getUser
                            }
                        }
                }
            }
    }

    fun registerFirebase(
        email: String,
        password: String,
        onRegisterSuccessListener: OnRegisterSuccessListener,
        onFailureListener: OnFailureListener
    ) {

        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val newFirebaseUser = it.user
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(newFirebaseUser?.email?.substringBefore('@'))
                    .build()
                newFirebaseUser?.updateProfile(profileChangeRequest)
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
        newShop: AddDialog.UploadShop,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        firestoreDb.collection("shops")
            .add(newShop)
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener)
    }

    fun uploadShopWithImage(
        imageInBytes: ByteArray,
        newShop: AddDialog.UploadShop,
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

    fun postComment(
        comment: DetailPresenter.PostComment, shopId: String,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        firestoreDb.collection("shops/${shopId}/comments")
            .add(comment)
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener)
    }

    private fun getUsers() {
        firestoreDb.collection("users").get().addOnSuccessListener {
            val list = mutableListOf<User>()
            for (dc in it.documents) {
                val o = dc.toObject<User>()
                o ?: continue
                val user = User(
                    uid = o.uid,
                    role = o.role,
                    name = o.name,
                    photo = o.photo
                )
                list.add(user)
            }
            users = list
        }
    }

    fun getComments(shopId: String, callBack: (MutableList<Comment>) -> Unit) {
        firestoreDb.collection("shops/${shopId}/comments").get().addOnSuccessListener {
            val list = mutableListOf<Comment>()
            for (dc in it.documents) {
                val o = dc.toObject<Comment>()
                o ?: continue
                val comment = Comment(
                    id = dc.id,
                    authorId = o.authorId,
                    authorName = users.find { it.uid == o.authorId }?.name ?: o.authorName,
                    content = o.content,
                    photo = users.find { it.uid == o.authorId }?.photo ?: o.photo,
                    date = o.date
                )
                list.add(comment)
            }
            callBack(list)
        }
    }

    fun initCommentsListeners(
        shopId: String,
        listener: DataChangedListener,
        onToastListener: OnToastListener
    ) {
        firestoreDb.collection("shops/${shopId}/comments")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onToastListener.toast(e.localizedMessage)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> listener.dataChanged(
                            dc.document,
                            DetailPresenter.NEW_COMMENT
                        )
                        DocumentChange.Type.MODIFIED -> listener.dataChanged(
                            dc.document,
                            DetailPresenter.EDIT_COMMENT
                        )
                        DocumentChange.Type.REMOVED -> listener.dataChanged(
                            dc.document, DetailPresenter.REMOVE_COMMENT
                        )
                    }
                }
            }
    }

    fun getReviews(shopId: String, onSuccessListener: OnSuccessListener<Any>) {
        firestoreDb.collection("shops/${shopId}/reviews").get()
            .addOnSuccessListener(onSuccessListener)
    }

    fun getShops(onSuccessListener: OnSuccessListener<QuerySnapshot>) {
        firestoreDb.collection("shops").get().addOnSuccessListener(onSuccessListener)
    }

    fun getShop(shopId: String, myCallback: (Shop) -> Unit) {
        firestoreDb.document("shops/${shopId}").get().addOnSuccessListener {
            it ?: return@addOnSuccessListener
            val o: Shop? = it.toObject()
            o ?: return@addOnSuccessListener
            val shop = Shop(
                id = it.id,
                name = o.name,
                address = o.address,
                geoPoint = o.geoPoint,
                rate = o.rate,
                ratings = o.ratings,
                photo = o.photo
            )
            myCallback(shop)
        }
    }

    fun postReview(
        shop: Shop,
        review: Review,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        firestoreDb.collection("shops/${shop.id}/reviews")
            .add(review)
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener)

        // Update shop's rate and ratings
        val newRate =
            ((shop.rate * shop.ratings) / (shop.ratings + 1)) + (review.rate / (shop.ratings + 1))
        firestoreDb.document("shops/${shop.id}").update("rate", newRate)
        firestoreDb.document("shops/${shop.id}").update("ratings", shop.ratings + 1)
    }

    fun updateReview(shop: Shop, review: Review, newRate: Float) {
        firestoreDb.document("shops/${shop.id}/reviews/${review.id}").update("rate", newRate)
        // Update the shop's rate
        val newShopRate = (shop.rate * shop.ratings - review.rate + newRate) / (shop.ratings)
        firestoreDb.document("shops/${shop.id}").update("rate", newShopRate)
    }

    fun updateUserRole(role: Int = 0) {
        when (role) {
            0 -> firestoreDb.document("users/${firebaseUser?.uid}").update("role", "user")
            1 -> firestoreDb.document("users/${firebaseUser?.uid}").update("role", "admin")
        }
    }

    fun updateProfile(
        name: String? = null,
        email: String? = null,
        photo: Uri? = null,
        myCallback: (String?) -> Unit
    ) {
        val profileUpdate = UserProfileChangeRequest.Builder()
        if (name != null) profileUpdate.displayName = name
        if (photo != null) profileUpdate.photoUri = photo

        if (name != null || photo != null) {
            firebaseUser?.updateProfile(profileUpdate.build())?.addOnSuccessListener {
                myCallback(null)
            }
            if (name != null) {
                firestoreDb.document("users/${firebaseUser?.uid}").update("name", name)
            }
            if (photo != null) {
                firestoreDb.document("users/${firebaseUser?.uid}").update("photo", photo.toString())
            }

        }
        if (email != null) {
            firebaseUser?.updateEmail(email)
                ?.addOnSuccessListener {
                    myCallback(null)
                }
                ?.addOnFailureListener {
                    myCallback(it.localizedMessage)
                }
        }
    }

    fun uploadImage(imageBitmap: Bitmap, myCallback: (String?, Uri?) -> Unit) {

        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("userImages/$newImageName")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener {
                myCallback(it.localizedMessage, null)
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                newImageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                myCallback(null, downloadUri)
            }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun authenticate(password: String) {
        firebaseUser ?: return
        firebaseAuth.signInWithEmailAndPassword(firebaseUser!!.email!!, password)
    }

    fun verifyEmail() {
        firebaseUser?.sendEmailVerification()
    }

    fun updatePassword(password: String, callBack: () -> Unit) {
        firebaseUser?.updatePassword(password)?.addOnSuccessListener {
            callBack()
        }
    }
}