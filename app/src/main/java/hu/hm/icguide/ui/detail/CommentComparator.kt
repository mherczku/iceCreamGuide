package hu.hm.icguide.ui.detail

import androidx.recyclerview.widget.DiffUtil
import hu.hm.icguide.models.Comment

object CommentComparator : DiffUtil.ItemCallback<Comment>() {

    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}
