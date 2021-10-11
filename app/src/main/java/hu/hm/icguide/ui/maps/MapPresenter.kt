package hu.hm.icguide.ui.maps

import co.zsmb.rainbowcake.withIOContext
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.hm.icguide.network.NetworkShop
import javax.inject.Inject

class MapPresenter @Inject constructor() {

    suspend fun getData(): MutableList<Mark> = withIOContext {
        val shops : MutableList<Mark> = mutableListOf()
        Firebase.firestore.collection("shops").get().addOnSuccessListener {
            for (d in it.documents){
                val s = d.toObject<NetworkShop>()
                if(s == null || s.name.isEmpty()) continue
                shops.add(Mark(
                    name = s.name,
                    geoPoint = s.geoPoint
                ))
            }

        }
        shops
    }

    data class Mark(
        val name: String,
        val geoPoint: GeoPoint
    )
}
