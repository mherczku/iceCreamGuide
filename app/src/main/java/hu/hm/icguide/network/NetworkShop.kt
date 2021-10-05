package hu.hm.icguide.network

import com.google.firebase.firestore.GeoPoint


data class NetworkShop(
    val name: String,
    val address: String,
    val geoPoint: GeoPoint,
    val rate: Float,
    val ratings: Int,
    var photo: String?
)