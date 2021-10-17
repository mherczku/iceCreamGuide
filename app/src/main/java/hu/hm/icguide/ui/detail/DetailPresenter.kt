package hu.hm.icguide.ui.detail

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.interactors.SystemInteractor
import hu.hm.icguide.models.Comment
import javax.inject.Inject

class DetailPresenter @Inject constructor(
    private val systemInteractor: SystemInteractor,
    private val firebaseInteractor: FirebaseInteractor
) {

    data class PostComment(
        val author: String,
        val content: String,
        val photo: String,
        val date: Timestamp
    )

    companion object {
        const val NEW_COMMENT = "NEW_DATA"
        const val EDIT_COMMENT = "EDIT_DATA"
        const val REMOVE_COMMENT = "REMOVE_DATA"
    }

    fun isNetAvailable(): Boolean = systemInteractor.isInternetAvailable()

    fun postComment(
        shopId: String,
        c: PostComment,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {
        firebaseInteractor.postComment(c, shopId, onSuccessListener, onFailureListener)
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
            author = objectComment.author,
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

}
