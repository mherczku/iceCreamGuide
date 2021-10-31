package hu.hm.icguide.ui.list

import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Shop
import javax.inject.Inject

class ListPresenter @Inject constructor(private val firebaseInteractor: FirebaseInteractor) {

    companion object {
        const val NEW_SHOP = "NEW_DATA"
        const val EDIT_SHOP = "EDIT_DATA"
        const val REMOVE_SHOP = "REMOVE_DATA"
    }

    fun dataChanged(
        dc: QueryDocumentSnapshot,
        function: String,
        list: MutableList<Shop>
    ): MutableList<Shop> {
        val objectShop: Shop = dc.toObject()
        val shop = Shop(
            id = dc.id,
            name = objectShop.name,
            address = objectShop.address,
            geoPoint = objectShop.geoPoint,
            rate = objectShop.rate,
            ratings = objectShop.ratings,
            photo = objectShop.photo
        )
        when (function) {
            NEW_SHOP -> list.add(shop)
            EDIT_SHOP -> {
                val old = list.find { it.id == shop.id }
                list.remove(old)
                list.add(shop)
            }
            REMOVE_SHOP -> list.remove(shop)
        }
        return list
    }

    fun initShopListeners(
        listener: FirebaseInteractor.DataChangedListener,
        feedBack: (String?) -> Unit
    ) {
        firebaseInteractor.initShopsListener(listener, feedBack)
    }

}