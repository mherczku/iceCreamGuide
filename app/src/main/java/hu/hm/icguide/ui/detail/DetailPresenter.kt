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

    /*companion object {
        const val NEW_COMMENT = "NEW_DATA"
        const val EDIT_COMMENT = "EDIT_DATA"
        const val REMOVE_COMMENT = "REMOVE_DATA"
    }

    fun initCommentsListeners(
        shopId: String,
        listener: FirebaseInteractor.DataChangedListener,
        toastListener: FirebaseInteractor.OnToastListener
    ) {
        firebaseInteractor.initCommentsListeners(shopId, listener, toastListener)
    }

    fun dataChanged(
        dc: QueryDocumentSnapshot,
        type: String,
        list: MutableList<Comment>
    ): MutableList<Comment> {
        val objectComment: Comment = dc.toObject()
        val comment = Comment(
            id = dc.id,
            authorId = objectComment.authorId,
            authorName = objectComment.authorName,
            content = objectComment.content,
            photo = objectComment.photo,
            date = objectComment.date
        )
        when (type) {
            NEW_COMMENT -> list.add(comment)
            EDIT_COMMENT -> {
                val old = list.find { it.id == comment.id }
                list.remove(old)
                list.add(comment)
            }
            REMOVE_COMMENT -> list.remove(comment)
        }
        return list
    }

    fun getShop(id: String, callback: KFunction1<Shop, Unit>) {
        firebaseInteractor.getShop(id, callback)
    }*/

}
