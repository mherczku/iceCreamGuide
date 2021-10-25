package hu.hm.icguide.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.hm.icguide.R
import hu.hm.icguide.databinding.RowShopBinding
import hu.hm.icguide.models.Shop

class ShopAdapter(private val listener: ShopAdapterListener) :
    ListAdapter<Shop, ShopAdapter.ViewHolder>(ShopComparator), Filterable {

    private var filterMainList = mutableListOf<Shop>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(RowShopBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = getItem(position)
        holder.item = shop
        holder.nameText.text = shop.name
        holder.addressText.text = shop.address
        holder.rateText.text = shop.rate.toString()
        if (shop.photo.isNotBlank()) {
            Glide.with(holder.shopImage)
                .load(shop.photo)
                .placeholder(R.drawable.placeholder)
                .into(holder.shopImage)
        }
    }

    interface ShopAdapterListener {
        fun onItemSelected(shop: Shop)
    }

    override fun submitList(list: MutableList<Shop>?) {
        super.submitList(list)
        filterMainList = list?.toMutableList() ?: mutableListOf()
    }

    private fun submitFilterList(list: MutableList<Shop>?) {
        super.submitList(list)
    }

    inner class ViewHolder(binding: RowShopBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameText: TextView = binding.nameText
        val addressText: TextView = binding.addressText
        val rateText: TextView = binding.rateText
        val shopImage: ImageView = binding.rivShop

        var item: Shop? = null

        init {
            itemView.setOnClickListener {
                item ?: return@setOnClickListener
                item.let { item -> listener.onItemSelected(item!!) }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filteredList: MutableList<Shop> = if (p0.toString().isEmpty()) {
                    filterMainList
                } else {
                    val tempList: MutableList<Shop> = mutableListOf()
                    for (shop: Shop in filterMainList) {
                        if (shop.name.lowercase().contains(p0.toString().lowercase())) {
                            tempList.add(shop)
                        }
                    }
                    tempList
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                val filteredList = p1?.values as MutableList<Shop>
                submitFilterList(filteredList)
            }

        }
    }

}