package hu.hm.icguide.ui.list

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.hm.icguide.network.NetworkShop
import javax.inject.Inject

class ListPresenter @Inject constructor() {

    companion object{
        const val NEW_SHOP = "NEW_DATA"
        const val EDIT_SHOP = "EDIT_DATA"
        const val REMOVE_SHOP = "REMOVE_SHOP"
    }

    private lateinit var shopAdapter: ShopAdapter

    fun initShopsListener(adapter: ShopAdapter) {
        shopAdapter = adapter
        val db = Firebase.firestore
        db.collection("shops")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    //TODO hibajelzés vissza
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> refreshList(dc.document.toObject(), NEW_SHOP)
                        DocumentChange.Type.MODIFIED -> refreshList(dc.document.toObject(), EDIT_SHOP)
                        DocumentChange.Type.REMOVED -> refreshList(dc.document.toObject(), REMOVE_SHOP)
                    }
                }
            }
    }

    private fun refreshList(shop: NetworkShop, function: String){
        val list = mutableListOf<NetworkShop>()
        list.addAll(shopAdapter.currentList)
        when (function) {
            NEW_SHOP -> list.add(shop)
            EDIT_SHOP -> {
                val old = list.find { it.id == shop.id }
                list.remove(old)
                list.add(shop)
            }
            REMOVE_SHOP -> list.remove(shop)
        }
        shopAdapter.submitList(list)
    }

}
