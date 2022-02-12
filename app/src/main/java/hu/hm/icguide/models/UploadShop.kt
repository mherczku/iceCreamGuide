package hu.hm.icguide.models

import com.google.firebase.firestore.GeoPoint

data class UploadShop(
    val name: String,
    val address: String,
    val geoPoint: GeoPoint,
    var photo: String,
    val rate: Float,
    val ratings: Int
)