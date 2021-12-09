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

    fun addShop(shop: Shop) {
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

    fun deleteNewShop(id: String) {
        firebaseInteractor.deleteNewShop(id)
    }

}
