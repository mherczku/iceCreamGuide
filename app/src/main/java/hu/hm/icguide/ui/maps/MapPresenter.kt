package hu.hm.icguide.ui.maps

import co.zsmb.rainbowcake.withIOContext
import javax.inject.Inject

class MapPresenter @Inject constructor() {

    suspend fun getData(): String = withIOContext {
        ""
    }

}
