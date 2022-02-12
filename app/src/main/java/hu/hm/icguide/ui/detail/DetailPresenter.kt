package hu.hm.icguide.ui.detail

import co.zsmb.rainbowcake.withIOContext
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.interactors.SystemInteractor
import hu.hm.icguide.models.Comment
import hu.hm.icguide.models.Shop
import hu.hm.icguide.models.User
import javax.inject.Inject

class DetailPresenter @Inject constructor(
    private val systemInteractor: SystemInteractor,
    private val firebaseInteractor: FirebaseInteractor
) {

    data class PostComment(
        val authorId: String,
        val content: String,
        val date: Timestamp
    )

    fun isNetAvailable(): Boolean = systemInteractor.isInternetAvailable()

    suspend fun getShop(id: String): Shop = withIOContext {
        val dc = firebaseInteractor.getShop(id)
        dc ?: return@withIOContext Shop()
        val o: Shop? = dc.toObject()
        o ?: return@withIOContext Shop()
        return@withIOContext Shop(
            id = dc.id,
            name = o.name,
            address = o.address,
            geoPoint = o.geoPoint,
            rate = o.rate,
            ratings = o.ratings,
            photo = o.photo
        )
    }

    private suspend fun getUsers(): MutableList<User> = withIOContext {
        val qs = firebaseInteractor.getUsers()
        val list = mutableListOf<User>()
        qs ?: return@withIOContext list
        for (dc in qs.documents) {
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
        return@withIOContext list
    }

    suspend fun getComments(shopId: String): MutableList<Comment> = withIOContext {
        val users = getUsers()
        val qs = firebaseInteractor.getComments(shopId)
        val list = mutableListOf<Comment>()
        qs ?: return@withIOContext list
        for (dc in qs.documents) {
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
        return@withIOContext list
    }

    fun postComment(
        shopId: String,
        c: PostComment,
        feedback: (String?) -> Unit
    ) {
        firebaseInteractor.postComment(c, shopId, feedback)
    }

}