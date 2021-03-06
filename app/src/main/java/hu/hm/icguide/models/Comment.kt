package hu.hm.icguide.models

import com.google.firebase.Timestamp
import java.util.*

data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val photo: String = "",
    val date: Timestamp = Timestamp(Date(10)  )
)