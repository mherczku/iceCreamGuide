package hu.hm.icguide.ui.adminList

import co.zsmb.rainbowcake.withIOContext
import com.google.firebase.firestore.ktx.toObject
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Shop
import hu.hm.icguide.models.UploadShop
import javax.inject.Inject

class AdminListPresenter @Inject constructor(private val firebaseInteractor: FirebaseInteractor) {

    suspend fun getNewShops(): MutableList<Shop> = withIOContext{
        val qs = firebaseInteractor.getNewShops()
        val list = mutableListOf<Shop>()
        qs ?: return@withIOContext list
        for( dc in qs.documents){
            val id = dc.id
            val o : Shop? = dc.toObject()
            o ?: continue
            val s = Shop(
                id = id,
                name = o.name,
                address = o.address,
                geoPoint = o.geoPoint,
                photo = o.photo
            )
            list.add(s)
        }
        return@withIOContext list
    }

    suspend fun addShop(shop: Shop) {
        val newShop = UploadShop(
            name = shop.name,
            address = shop.address,
            geoPoint = shop.geoPoint,
            rate = 0F,
            ratings = 0,
            photo = shop.photo
        )
        firebaseInteractor.addNewShopToShops(newShop, shop.id)
    }

    suspend fun deleteNewShop(id: String) {
        firebaseInteractor.deleteNewShop(id)
    }

    /*companion object {
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
        toastListener: FirebaseInteractor.OnToastListener
    ) {
        firebaseInteractor.initShopsListener(listener, toastListener)
    }*/

    /*fun getNewShopsOld(callBack: (MutableList<Shop>) -> Unit){
        firebaseInteractor.getNewShops{
            val list = mutableListOf<Shop>()
            for( dc in it.documents){
                val id = dc.id
                val o : Shop? = dc.toObject()
                o ?: continue
                val s = Shop(
                    id = id,
                    name = o.name,
                    address = o.address,
                    geoPoint = o.geoPoint,
                    photo = o.photo
                )
                list.add(s)
            }
            callBack(list)
        }
    }*/
}
