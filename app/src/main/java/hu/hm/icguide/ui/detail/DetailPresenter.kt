package hu.hm.icguide.ui.detail

import co.zsmb.rainbowcake.withIOContext
import javax.inject.Inject

class DetailPresenter @Inject constructor() {

    suspend fun getData(): String = withIOContext {
        ""
    }

}
