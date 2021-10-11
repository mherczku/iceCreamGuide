package hu.hm.icguide.ui.detail

import co.zsmb.rainbowcake.withIOContext
import hu.hm.icguide.interactors.SystemInteractor
import javax.inject.Inject

class DetailPresenter @Inject constructor(private val systemInteractor: SystemInteractor) {

    suspend fun getData(): String = withIOContext {
        ""
    }

    fun isNetAvailable(): Boolean = systemInteractor.isInternetAvailable()

}
