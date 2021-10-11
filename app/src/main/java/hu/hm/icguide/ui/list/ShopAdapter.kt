package hu.hm.icguide.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.icguide.R
import com.example.icguide.databinding.RowShopBinding
import hu.hm.icguide.network.NetworkShop

class ShopAdapter(private val listener : ShopAdapterListener) : ListAdapter<NetworkShop, ShopAdapter.ViewHolder>(ShopComparator){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(RowShopBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = getItem(position)
        holder.item = shop
        holder.nameText.text = shop.name
        holder.addressText.text = shop.address
        holder.rateText.text = shop.rate.toString()
        if(shop.photo.isNotBlank()) {
            Glide.with(holder.shopImage)
                .load(shop.photo)
                .placeholder(R.drawable.placeholder)
                .into(holder.shopImage)
        }
    }

    interface ShopAdapterListener{fun onItemSelected(shop: NetworkShop)}

    inner class ViewHolder(binding: RowShopBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameText: TextView = binding.nameText
        val addressText: TextView = binding.addressText
        val rateText: TextView = binding.rateText
        val shopImage: ImageView = binding.shopImage

        var item: NetworkShop? = null

        init {
            itemView.setOnClickListener {
                item ?: return@setOnClickListener
                item.let { item -> listener.onItemSelected(item!!)}
            }
        }
    }

}