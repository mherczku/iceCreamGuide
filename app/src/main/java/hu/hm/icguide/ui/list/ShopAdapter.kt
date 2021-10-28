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

class ShopAdapter(private val admin: Boolean = false) :
    ListAdapter<Shop, ShopAdapter.ViewHolder>(ShopComparator), Filterable {

    private var filterMainList = mutableListOf<Shop>()
    private var itemSelectedListener: ((Shop) -> Unit)? = null
    private var adminOptionsListener: ((Shop) -> Unit)? = null

    fun setItemSelectedListener(listener: (Shop) -> Unit) {
        itemSelectedListener = listener
    }
    fun setAdminOptionsListener(listener: (Shop) -> Unit) {
        adminOptionsListener = listener
    }

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

    override fun submitList(list: MutableList<Shop>?) {
        super.submitList(list)
        filterMainList = list?.toMutableList() ?: mutableListOf()
    }

    fun removeShop(shop: Shop) {
        val list = currentList.toMutableList()
        list.remove(shop)
        submitList(list)
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
                item?.let { item -> itemSelectedListener?.invoke(item) }
            }
            if (admin) {
                itemView.setOnLongClickListener {
                    item?.let { item -> adminOptionsListener?.invoke(item) }
                    true
                }
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

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                val filteredList = p1?.values as MutableList<Shop>
                submitFilterList(filteredList)
            }

        }
    }

}