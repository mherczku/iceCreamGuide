package hu.hm.icguide.models

import com.google.firebase.firestore.GeoPoint


data class Shop(
    var id: String = "",
    var name: String = "",
    var address: String = "",
    var geoPoint: GeoPoint = GeoPoint(0.0, 0.0),
    var rate: Float = 5F,
    var ratings: Int = 1,
    var photo: String = ""
)