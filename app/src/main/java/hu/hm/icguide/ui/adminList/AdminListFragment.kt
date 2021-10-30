package hu.hm.icguide.ui.adminList

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentListBinding
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Shop
import hu.hm.icguide.ui.list.ShopAdapter
import hu.hm.icguide.ui.login.LoginFragment
import hu.hm.icguide.ui.maps.MapFragment
import hu.hm.icguide.ui.settings.SettingsFragment
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AdminListFragment : RainbowCakeFragment<AdminListViewState, AdminListViewModel>(),
    NavigationView.OnNavigationItemSelectedListener {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_list

    @Inject
    lateinit var firebaseInteractor: FirebaseInteractor
    private lateinit var binding: FragmentListBinding
    private lateinit var adapter: ShopAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListBinding.bind(view)
        setupToolbar()
        binding.navigationView.setNavigationItemSelectedListener(this)
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        viewModel.load()
    }

    override fun render(viewState: AdminListViewState) {
        Timber.d("Received ${viewState.shops.size} new shops to display in admin list")
        adapter.submitList(viewState.shops)
        binding.swipeRefreshLayout.isRefreshing = viewState.isRefreshing
    }

    private fun setupRecyclerView() {
        adapter = ShopAdapter( true)
        adapter.setAdminOptionsListener {
            val options = arrayOf(getString(R.string.approve), getString(R.string.delete))
            AlertDialog.Builder(requireContext())
                .setTitle(it.name)
                .setItems(options){ _, i: Int ->
                    when(i) {
                        0 -> addSelected(it)
                        1 -> deleteSelected(it)
                    }
                }
                .create().show()
        }
        binding.shopList.adapter = adapter
    }

    private fun setupToolbar() {
        binding.navigationView.inflateMenu(R.menu.drawer_list_admin)
        binding.toolbar.title = getString(R.string.waiting_for_approval)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.inflateMenu(R.menu.menu_list)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menuItemSearch -> {
                    val searchView = it.actionView as SearchView
                    searchView.queryHint = getString(R.string.search)
                    it.expandActionView()
                    searchView.setOnQueryTextListener(
                        object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return true
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                adapter.filter.filter(newText)
                                return true
                            }
                        }
                    )
                }
            }
            true
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshList()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_drawer_one -> {
                navigator?.pop()
            }
            R.id.action_drawer_two -> {
                navigator?.add(MapFragment())
            }
            R.id.action_drawer_three -> {
                navigator?.add(SettingsFragment())
            }
            R.id.action_drawer_four -> {
                firebaseInteractor.logout()
                navigator?.replace(LoginFragment())
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun deleteSelected(shop: Shop) {
        viewModel.deleteShop(shop.id)
        adapter.removeShop(shop)
    }

    private fun addSelected(shop: Shop) {
        viewModel.addShop(shop)
        adapter.removeShop(shop)
    }
}


