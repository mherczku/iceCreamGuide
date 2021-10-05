package hu.hm.icguide.ui.list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import co.zsmb.rainbowcake.navigation.popUntil
import com.example.icguide.R
import com.example.icguide.databinding.FragmentListBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.ui.add.AddFragment
import hu.hm.icguide.ui.login.LoginFragment
import hu.hm.icguide.ui.maps.MapFragment


@AndroidEntryPoint
class ListFragment : RainbowCakeFragment<ListViewState, ListViewModel>(), NavigationView.OnNavigationItemSelectedListener {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_list

    private lateinit var binding: FragmentListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListBinding.bind(view)
        setupToolbar()
        binding.navigationView.setNavigationItemSelectedListener(this)

        // TODO Setup views
    }



    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.inflateMenu(R.menu.menu_list)
    }

    override fun onStart() {
        super.onStart()

        viewModel.load()
    }

    override fun render(viewState: ListViewState) {
        // TODO Render state
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_drawer_one -> {
                navigator?.add(AddFragment(LatLng(12.1, 12.1)))
                //TODO binding.drawerLayout.closeDrawers()
            }
            R.id.action_drawer_two -> {
                navigator?.add(MapFragment())
            }
            R.id.action_drawer_two -> {
                //TODO navigator?.add(SettingsFragment())
                            }
            R.id.action_drawer_four -> {
                //TODO logout FirebaseAuth.getInstance()
                navigator?.replace(LoginFragment())
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}
