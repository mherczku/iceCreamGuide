package hu.hm.icguide.ui.list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentListBinding
import hu.hm.icguide.extensions.toast
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Shop
import hu.hm.icguide.ui.detail.DetailFragment
import hu.hm.icguide.ui.login.LoginFragment
import hu.hm.icguide.ui.maps.MapFragment
import hu.hm.icguide.ui.settings.SettingsFragment
import javax.inject.Inject

@AndroidEntryPoint
class ListFragment : RainbowCakeFragment<ListViewState, ListViewModel>(),
    NavigationView.OnNavigationItemSelectedListener, FirebaseInteractor.DataChangedListener,
    FirebaseInteractor.OnToastListener, ShopAdapter.ShopAdapterListener {

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
            //TODO vagy legyen értelme vagy ne legyen swipeRefresh
            viewModel.refreshList()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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

    override fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        viewModel.dataChanged(dc, type)
    }

    override fun onItemSelected(shop: Shop) {
        navigator?.add(DetailFragment(shop.id))
    }

    override fun onToast(message: String?) {
        toast(message)
    }

}


