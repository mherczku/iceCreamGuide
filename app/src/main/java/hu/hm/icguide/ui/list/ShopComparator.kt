package hu.hm.icguide.ui.list

import androidx.recyclerview.widget.DiffUtil
import hu.hm.icguide.models.Shop

object ShopComparator : DiffUtil.ItemCallback<Shop>() {

    override fun areItemsTheSame(oldItem: Shop, newItem: Shop): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Shop, newItem: Shop): Boolean {
        return oldItem == newItem
    }
}