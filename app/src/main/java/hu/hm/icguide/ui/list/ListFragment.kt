package hu.hm.icguide.ui.list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.example.icguide.R
import com.example.icguide.databinding.FragmentListBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.network.NetworkShop
import hu.hm.icguide.ui.add.AddFragment
import hu.hm.icguide.ui.detail.DetailFragment
import hu.hm.icguide.ui.login.LoginFragment
import hu.hm.icguide.ui.maps.MapFragment

@AndroidEntryPoint
class ListFragment : RainbowCakeFragment<ListViewState, ListViewModel>(),
    NavigationView.OnNavigationItemSelectedListener, FirebaseInteractor.DataChangedListener,
    FirebaseInteractor.OnToastListener, ShopAdapter.ShopAdapterListener {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_list

    private lateinit var binding: FragmentListBinding
    private lateinit var adapter: ShopAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListBinding.bind(view)
        setupToolbar()
        binding.navigationView.setNavigationItemSelectedListener(this)
        setupRecyclerView()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            navigator?.replace(LoginFragment())
        } else Toast.makeText(
            context,
            user.displayName!! + getString(R.string.logged_in),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onStart() {
        super.onStart()
        viewModel.load()
        viewModel.initShopListeners(this, this)
    }

    override fun render(viewState: ListViewState) {
        adapter.submitList(viewState.shops)
        binding.swipeRefreshLayout.isRefreshing = viewState.isRefreshing
        // TODO Render state

    }

    private fun setupRecyclerView() {
        adapter = ShopAdapter(this)
        binding.shopList.adapter = adapter
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.inflateMenu(R.menu.menu_list)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshList()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_drawer_one -> {
                navigator?.add(AddFragment(LatLng(12.1, 12.1)))
            }
            R.id.action_drawer_two -> {
                navigator?.add(MapFragment())
            }
            R.id.action_drawer_three -> {
                //TODO navigator?.add(SettingsFragment())
            }
            R.id.action_drawer_four -> {
                FirebaseAuth.getInstance().signOut()
                navigator?.replace(LoginFragment())
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        viewModel.dataChanged(dc, type)
    }

    override fun toast(message: String?) {
        message ?: return
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onItemSelected(shop: NetworkShop) {
        navigator?.add(DetailFragment(shop))
    }

}


