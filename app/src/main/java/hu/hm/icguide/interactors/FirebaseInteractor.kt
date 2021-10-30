package hu.hm.icguide.interactors

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import hu.hm.icguide.models.Review
import hu.hm.icguide.models.Shop
import hu.hm.icguide.models.UploadShop
import hu.hm.icguide.models.User
import hu.hm.icguide.ui.detail.DetailPresenter
import hu.hm.icguide.ui.list.ListPresenter
import hu.hm.icguide.ui.login.LoginFragment
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject


class FirebaseInteractor @Inject constructor() {

    //TODO fireAUTH legyen külön
    //TODO az admin jogos kérésekhez még egy ellenőrzés és visszajelzés ha már nincs joga
    //TODO change functions to handle errors and failures

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb: FirebaseFirestore = Firebase.firestore
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    val firebaseUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    interface DataChangedListener {
        fun dataChanged(dc: QueryDocumentSnapshot, type: String)
    }

    //TODO replace this with sth else
    interface OnToastListener {
        fun onToast(message: String?)
    }

    suspend fun getUserRole(): String? {
        val dc = firestoreDb.document("users/${firebaseUser?.uid}").get().await()
        dc ?: return null
        val user: User? = dc.toObject()
        user ?: return null
        return user.role
    }

    fun loginFirebase(
        email: String,
        password: String,
        feedback: (Int, String?) -> Unit
    ) {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
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
                        }
                        feedback(LoginFragment.LOGIN_SUCCESS, null)
                    }
            }
            .addOnFailureListener {
                feedback(LoginFragment.FAILURE, it.localizedMessage)
            }
    }

    fun registerFirebase(
        email: String,
        password: String,
        feedback: (Int, String?) -> Unit
    ) {
        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val newFirebaseUser = it.user
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(newFirebaseUser?.email?.substringBefore('@'))
                    .build()
                newFirebaseUser?.updateProfile(profileChangeRequest)
                feedback(LoginFragment.REGISTRATION_SUCCESS, null)
            }
            .addOnFailureListener {
                feedback(LoginFragment.FAILURE, it.localizedMessage)
            }
    }

    fun initShopsListener(
        listener: DataChangedListener,
        toastListenerListener: OnToastListener
    ) {
        firestoreDb.collection("shops")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    toastListenerListener.onToast(e.localizedMessage)
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

    fun uploadShop(newShop: UploadShop, done: () -> Unit) {
        Timber.d("Uploading shop into firestore new shops")
        firestoreDb.collection("newShops").add(newShop).addOnSuccessListener {
            done()
        }
        //TODO change done to feedback error handling
    }


    fun deleteNewShop(shopId: String) {
        Timber.d("Deleting new shop $shopId from firestore")
        firestoreDb.document("newShops/$shopId").delete()
    }

    fun addNewShopToShops(newShop: UploadShop, shopId: String) {
        Timber.d("Uploading new shop into shops in firestore")
        firestoreDb.collection("shops").add(newShop).addOnSuccessListener {
            deleteNewShop(shopId)
        }
    }

    fun uploadShopWithImage(
        imageInBytes: ByteArray,
        newShop: UploadShop,
        onFailureListener: OnFailureListener,
        done: () -> Unit
    ) {
        Timber.d("Uploading image into firestore")
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
                uploadShop(newShop, done)
            }
    }

    fun postComment(
        comment: DetailPresenter.PostComment, shopId: String,
        feedback: (String?) -> Unit
    ) {
        Timber.d("Posting comment into firestore for $shopId shop")
        firestoreDb.collection("shops/${shopId}/comments")
            .add(comment)
            .addOnSuccessListener {
                feedback(null)
            }
            .addOnFailureListener {
                feedback(it.localizedMessage)
            }
    }

    suspend fun getUsers(): QuerySnapshot? {
        Timber.d("Downloading firestore users")
        return firestoreDb.collection("users").get().await()
    }

    suspend fun getComments(shopId: String): QuerySnapshot? {
        Timber.d("Downloading firestore $shopId shops comments")
        return firestoreDb.collection("shops/${shopId}/comments").get().await()
    }


    fun getReviews(shopId: String, onSuccessListener: OnSuccessListener<Any>) {
        Timber.d("Downloading firestore $shopId shops reviews")
        firestoreDb.collection("shops/$shopId/reviews").get()
            .addOnSuccessListener(onSuccessListener)
    }

    suspend fun getShops(): QuerySnapshot? {
        Timber.d("Downloading firestore shops")
        return firestoreDb.collection("shops").get().await()
    }

    suspend fun getNewShops(): QuerySnapshot? {
        Timber.d("Downloading firestore new shops")
        return firestoreDb.collection("newShops").get().await()
    }

    suspend fun getShop(shopId: String): DocumentSnapshot? {
        Timber.d("Downloading firestore $shopId shop")
        return firestoreDb.document("shops/${shopId}").get().await()
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