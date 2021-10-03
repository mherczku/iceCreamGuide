package hu.hm.icguide.ui.detail

import android.os.Bundle
import android.view.View
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import com.example.icguide.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : RainbowCakeFragment<DetailViewState, DetailViewModel>() {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_detail

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO Setup views
    }

    override fun onStart() {
        super.onStart()

        viewModel.load()
    }

    override fun render(viewState: DetailViewState) {
        // TODO Render state
    }

}
