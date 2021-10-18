package hu.hm.icguide.models

import com.google.firebase.firestore.GeoPoint


data class Shop(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val geoPoint: GeoPoint = GeoPoint(0.0, 0.0),
    val rate: Float = 0F,
    val ratings: Int = 0,
    val photo: String = ""
)