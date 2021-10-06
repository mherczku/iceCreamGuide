package hu.hm.icguide.ui.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.icguide.R
import com.example.icguide.databinding.RowShopBinding
import hu.hm.icguide.models.Shop
import hu.hm.icguide.network.NetworkShop

class ShopAdapter : ListAdapter<NetworkShop, ShopAdapter.ViewHolder>(ShopComparator){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(RowShopBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        //TODO ViewHolder(RowShopBinding.bind(parent.rootView))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = getItem(position)
        holder.nameText.text = shop.name
        holder.addressText.text = shop.address
        holder.rateText.text = shop.rate.toString()
        if(!shop.photo.isNullOrBlank()) {
            Glide.with(holder.shopImage)
                .load(shop.photo)
                .placeholder(R.drawable.placeholder)
                .into(holder.shopImage)
        }

    }

    inner class ViewHolder(binding: RowShopBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameText: TextView = binding.nameText
        val addressText: TextView = binding.addressText
        val rateText: TextView = binding.rateText
        val shopImage: ImageView = binding.shopImage

        var item: NetworkShop? = null

        init {
            binding.root.setOnClickListener {
                item?.let {
                    //TODO navigate to deatilF item -> listener?.onItemSelected(item.id)
                 }
            }
        }
    }

    //TODO interface Listener{fun onItemSelected(id: Long)}


}