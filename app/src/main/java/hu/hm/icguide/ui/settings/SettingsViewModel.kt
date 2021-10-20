package hu.hm.icguide.ui.settings

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPresenter: SettingsPresenter
) : RainbowCakeViewModel<SettingsViewState>(SettingsViewState()) {

    fun load() = execute {
        viewState = SettingsViewState(settingsPresenter.getData())
    }

}
