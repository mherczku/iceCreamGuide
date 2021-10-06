package hu.hm.icguide.ui.list

import androidx.recyclerview.widget.DiffUtil
import hu.hm.icguide.network.NetworkShop

object ShopComparator : DiffUtil.ItemCallback<NetworkShop>(){

    override fun areItemsTheSame(oldItem: NetworkShop, newItem: NetworkShop): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NetworkShop, newItem: NetworkShop): Boolean {
        return oldItem == newItem
    }
}