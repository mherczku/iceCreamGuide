package hu.hm.icguide.ui.maps

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapPresenter: MapPresenter
) : RainbowCakeViewModel<MapViewState>(MapViewState()) {

    fun load() = execute {
        viewState = MapViewState()
    }

    fun getData(onSuccessListener: OnSuccessListener<QuerySnapshot>){
        mapPresenter.getData(onSuccessListener)
    }

    fun getMarkers(p0: QuerySnapshot) {
        val markers = mapPresenter.getMarkers(p0)
        viewState = MapViewState(markers)
    }

}
