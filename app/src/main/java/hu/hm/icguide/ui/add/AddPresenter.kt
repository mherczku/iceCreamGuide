package hu.hm.icguide.ui.add

import co.zsmb.rainbowcake.withIOContext
import javax.inject.Inject

class AddPresenter @Inject constructor() {

    suspend fun getData(): String = withIOContext {
        ""
    }

}
