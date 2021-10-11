package hu.hm.icguide.ui.detail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.bumptech.glide.Glide
import com.example.icguide.R
import com.example.icguide.databinding.FragmentDetailBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.interactors.SystemInteractor
import hu.hm.icguide.network.NetworkShop
import hu.hm.icguide.ui.add.AddFragment
import hu.hm.icguide.ui.login.LoginFragment
import hu.hm.icguide.ui.maps.MapFragment

@AndroidEntryPoint
class DetailFragment(
    private val shop: NetworkShop
) : RainbowCakeFragment<DetailViewState, DetailViewModel>(){

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_detail

    private lateinit var binding: FragmentDetailBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailBinding.bind(view)
        setupToolbar()
        setupView()


        // TODO Setup views
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navigator?.pop()
        }
        binding.toolbar.inflateMenu(R.menu.menu_list)
    }

    private fun setupView() {
        binding.tvName.text = shop.name
        binding.tvAddress.text = shop.address
        binding.ratingBar.rating = shop.rate

        Glide.with(binding.imgShop)
            .load(shop.photo)
            .placeholder(R.drawable.placeholder)
            .into(binding.imgShop)

        if(!viewModel.isNetAvailable()){
            binding.etComment.isEnabled = false
            Toast.makeText(context, getString(R.string.internet_unavailable), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.load()
    }

    override fun render(viewState: DetailViewState) {
        // TODO Render state
    }

}
