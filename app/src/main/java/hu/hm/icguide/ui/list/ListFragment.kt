package hu.hm.icguide.ui.list

import android.os.Bundle
import android.view.View
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import com.example.icguide.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ListFragment : RainbowCakeFragment<ListViewState, ListViewModel>() {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO Setup views
    }

    override fun onStart() {
        super.onStart()

        viewModel.load()
    }

    override fun render(viewState: ListViewState) {
        // TODO Render state
    }

}
