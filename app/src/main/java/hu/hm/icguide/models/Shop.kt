package hu.hm.icguide.models

import com.google.firebase.firestore.GeoPoint

data class Shop(
    val id: Long,
    var name: String,
    var address: String,
    var geoPoint: GeoPoint,
    var rate: Float,
    var ratings: Int,
    var photo: String?
)