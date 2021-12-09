package hu.hm.icguide.ui.maps

import co.zsmb.rainbowcake.withIOContext
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.toObject
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Shop
import javax.inject.Inject

class MapPresenter @Inject constructor(private val firebaseInteractor: FirebaseInteractor) {

    data class Marker(
        val id: String,
        val name: String,
        val geoPoint: GeoPoint
    )

    suspend fun getMarkers(): MutableList<Marker> = withIOContext{
        val qs = firebaseInteractor.getShops()
        val shops: MutableList<Marker> = mutableListOf()
        qs ?: return@withIOContext shops
        for (d in qs.documents){
            val s = d.toObject<Shop>()
            if(s == null || s.name.isEmpty()) continue
            shops.add(Marker(
                id = d.id,
                name = s.name,
                geoPoint = s.geoPoint
            ))
        }
        return@withIOContext shops
    }
}