package hu.hm.icguide.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.hm.icguide.R
import hu.hm.icguide.databinding.RowCommentBinding
import hu.hm.icguide.models.Comment

class CommentAdapter(private val listener: CommentAdapterListener) :
    ListAdapter<Comment, CommentAdapter.ViewHolder>(CommentComparator) {

    interface CommentAdapterListener {
        fun onItemSelected(comment: Comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder =
        ViewHolder(RowCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = getItem(position)
        holder.item = comment
        holder.authorText.text = comment.authorName
        holder.contentText.text = comment.content
        if (comment.photo.isNotBlank()) {
            Glide.with(holder.authorImage)
                .load(comment.photo)
                .placeholder(R.drawable.outline_account_circle_24)
                .into(holder.authorImage)
        }
    }

    inner class ViewHolder(binding: RowCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        val authorText: TextView = binding.tvAuthor
        val contentText: TextView = binding.tvComment
        val authorImage: ImageView = binding.rivAuthor

        var item: Comment? = null

        init {
            itemView.setOnClickListener {
                item ?: return@setOnClickListener
                item.let { item -> listener.onItemSelected(item!!) }
            }
        }
    }

}