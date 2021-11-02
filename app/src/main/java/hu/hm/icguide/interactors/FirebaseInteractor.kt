package hu.hm.icguide.interactors

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
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
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject


class FirebaseInteractor @Inject constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb: FirebaseFirestore = Firebase.firestore
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    val firebaseUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    interface DataChangedListener {
        fun dataChanged(dc: QueryDocumentSnapshot, type: String)
    }

    suspend fun getUserRole(): String? {
        Timber.d("Downloading user role")
        val dc = firestoreDb.document("users/${firebaseUser?.uid}").get().await()
        dc ?: return null
        val user: User? = dc.toObject()
        user ?: return null
        return user.role
    }

    fun loginFirebase(
        email: String,
        password: String,
        feedback: (String?) -> Unit
    ) {
        Timber.d("Logging in")
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
                        feedback(null)
                    }
            }
            .addOnFailureListener {
                feedback(it.localizedMessage)
            }
    }

    fun registerFirebase(
        email: String,
        password: String,
        feedback: (String?) -> Unit
    ) {
        Timber.d("Registering user")
        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val newFirebaseUser = it.user
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(newFirebaseUser?.email?.substringBefore('@'))
                    .build()
                newFirebaseUser?.updateProfile(profileChangeRequest)
                feedback(null)
            }
            .addOnFailureListener {
                feedback(it.localizedMessage)
            }
    }

    fun initShopsListener(
        listener: DataChangedListener,
        feedBack: (String?) -> Unit
    ) {
        Timber.d("Initializing shops listeners")
        firestoreDb.collection("shops")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    feedBack(e.localizedMessage)
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

    fun uploadShop(newShop: UploadShop, feedBack: (String?) -> Unit) {
        Timber.d("Uploading shop into firestore new shops")
        firestoreDb.collection("newShops").add(newShop)
            .addOnSuccessListener { feedBack(null) }
            .addOnFailureListener { feedBack(it.localizedMessage) }
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
        feedBack: (String?) -> Unit
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
                uploadShop(newShop, feedBack)
            }
    }

    fun postComment(
        comment: DetailPresenter.PostComment, shopId: String,
        feedback: (String?) -> Unit
    ) {
        Timber.d("Posting comment into firestore for $shopId shop")
        firestoreDb.collection("shops/${shopId}/comments")
            .add(comment)
            .addOnSuccessListener { feedback(null) }
            .addOnFailureListener { feedback(it.localizedMessage) }
    }

    suspend fun getUsers(): QuerySnapshot? {
        Timber.d("Downloading firestore users")
        return firestoreDb.collection("users").get().await()
    }

    suspend fun getComments(shopId: String): QuerySnapshot? {
        Timber.d("Downloading firestore $shopId shops comments")
        return firestoreDb.collection("shops/${shopId}/comments").get().await()
    }

    suspend fun getReviews(shopId: String): QuerySnapshot? {
        Timber.d("Downloading firestore $shopId shops reviews")
        return firestoreDb.collection("shops/$shopId/reviews").get().await()
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
        feedBack: (String?) -> Unit
    ) {
        Timber.d("Uploading review for ${shop.id} shop to firestore")
        firestoreDb.collection("shops/${shop.id}/reviews")
            .add(review)
            .addOnSuccessListener {
                feedBack(null)
                Timber.d("Updating ${shop.id} shop's rate and ratings")
                val newRate =
                    ((shop.rate * shop.ratings) / (shop.ratings + 1)) + (review.rate / (shop.ratings + 1))
                firestoreDb.document("shops/${shop.id}").update("rate", newRate)
                firestoreDb.document("shops/${shop.id}").update("ratings", shop.ratings + 1)

            }
            .addOnFailureListener { feedBack(it.localizedMessage) }
    }

    fun updateReview(shop: Shop, review: Review, newRate: Float, feedBack: (String?) -> Unit) {
        Timber.d("Updating ${review.id} review for ${shop.id} shop")
        firestoreDb.document("shops/${shop.id}/reviews/${review.id}").update("rate", newRate)
            .addOnSuccessListener {
                feedBack(null)
                Timber.d("Updating ${shop.id} shop's rate and ratings")
                val newShopRate =
                    (shop.rate * shop.ratings - review.rate + newRate) / (shop.ratings)
                firestoreDb.document("shops/${shop.id}").update("rate", newShopRate)
            }
            .addOnFailureListener { feedBack(it.localizedMessage) }
    }

    fun updateProfile(
        name: String? = null,
        email: String? = null,
        photo: Uri? = null,
        feedBack: (String?) -> Unit
    ) {
        Timber.d("Updating firebase user profile")
        val profileUpdate = UserProfileChangeRequest.Builder()
        if (name != null) profileUpdate.displayName = name
        if (photo != null) profileUpdate.photoUri = photo

        if (name != null || photo != null) {
            firebaseUser?.updateProfile(profileUpdate.build())
                ?.addOnSuccessListener { feedBack(null) }
                ?.addOnFailureListener { feedBack(it.localizedMessage) }
            if (name != null) {
                firestoreDb.document("users/${firebaseUser?.uid}").update("name", name)
                    .addOnSuccessListener { feedBack(null) }
                    .addOnFailureListener { feedBack(it.localizedMessage) }
            }
            if (photo != null) {
                firestoreDb.document("users/${firebaseUser?.uid}").update("photo", photo.toString())
                    .addOnFailureListener { feedBack(it.localizedMessage) }
                    .addOnSuccessListener { feedBack(null) }
            }

        }
        if (email != null) {
            firebaseUser?.updateEmail(email)
                ?.addOnSuccessListener {
                    feedBack(null)
                }
                ?.addOnFailureListener {
                    feedBack(it.localizedMessage)
                }
        }
    }

    fun uploadImage(imageBitmap: Bitmap, feedBack: (String?, Uri?) -> Unit) {
        Timber.d("Uploading photo to firebase storage")

        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("userImages/$newImageName")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener {
                feedBack(it.localizedMessage, null)
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                newImageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                feedBack(null, downloadUri)
            }
    }

    fun logout() {
        Timber.d("Logging out")
        firebaseAuth.signOut()
    }

    fun authenticate(password: String, feedBack: (String?) -> Unit) {
        Timber.d("Re-authenticating")
        firebaseUser ?: return
        firebaseAuth.signInWithEmailAndPassword(firebaseUser!!.email!!, password)
            .addOnSuccessListener {
                feedBack(null)
            }
            .addOnFailureListener {
                feedBack(it.localizedMessage)
            }
    }

    fun verifyEmail(feedBack: (String?) -> Unit) {
        Timber.d("Sending verification email")
        firebaseUser?.sendEmailVerification()
            ?.addOnSuccessListener { feedBack(null) }
            ?.addOnFailureListener { feedBack(it.localizedMessage) }
    }

    fun updatePassword(password: String, feedBack: (String?) -> Unit) {
        Timber.d("Updating firebase user password")
        firebaseUser?.updatePassword(password)
            ?.addOnSuccessListener { feedBack(null) }
            ?.addOnFailureListener { feedBack(it.localizedMessage) }
    }

    fun requestPasswordReset(email: String, feedback: (String?) -> Unit) {
        Timber.d("Sending passsword reset email to $email")
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {feedback(null)}
            .addOnFailureListener { feedback(it.localizedMessage) }
    }
}