package hu.hm.icguide.ui.maps

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Shop
import javax.inject.Inject

class MapPresenter @Inject constructor(private val firebaseInteractor: FirebaseInteractor) {

    fun getData(onSuccessListener: OnSuccessListener<QuerySnapshot>) {
        firebaseInteractor.getShops(onSuccessListener)
    }

    fun getMarkers(qs: QuerySnapshot): MutableList<Marker> {
        val shops: MutableList<Marker> = mutableListOf()
        for (d in qs.documents){
            val s = d.toObject<Shop>()
            if(s == null || s.name.isEmpty()) continue
            shops.add(Marker(
                name = s.name,
                geoPoint = s.geoPoint
            ))
        }
        return shops
    }

    data class Marker(
        val name: String,
        val geoPoint: GeoPoint
    )
}
