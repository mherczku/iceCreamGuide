package hu.hm.icguide.ui.maps

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapPresenter: MapPresenter
) : RainbowCakeViewModel<MapViewState>(MapViewState()) {

    fun load() = execute {
        viewState = MapViewState(mapPresenter.getData())
    }

}
