package hu.hm.icguide.ui.list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
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
import hu.hm.icguide.interactors.SystemInteractor
import hu.hm.icguide.ui.adminList.AdminListFragment
import hu.hm.icguide.ui.detail.DetailFragment
import hu.hm.icguide.ui.login.LoginFragment
import hu.hm.icguide.ui.maps.MapFragment
import hu.hm.icguide.ui.settings.SettingsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ListFragment : RainbowCakeFragment<ListViewState, ListViewModel>(),
    NavigationView.OnNavigationItemSelectedListener, FirebaseInteractor.DataChangedListener{

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_list

    @Inject
    lateinit var firebaseInteractor: FirebaseInteractor
    @Inject
    lateinit var systemInteractor: SystemInteractor
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
        viewModel.initShopListeners(this){ toast(it)}
    }

    override fun render(viewState: ListViewState) {
        Timber.d("Received ${viewState.shops.size} shops to display in list")
        adapter.submitList(viewState.shops)
        binding.swipeRefreshLayout.isRefreshing = viewState.isRefreshing
    }

    private fun setupRecyclerView() {
        adapter = ShopAdapter()
        adapter.setItemSelectedListener {
            navigator?.add(DetailFragment(it.id))
        }
        binding.shopList.adapter = adapter
    }

    private fun setupDrawer(role: String?) {
        if ( role == "admin") binding.navigationView.inflateMenu(R.menu.drawer_list_admin)
        else binding.navigationView.inflateMenu(R.menu.drawer_list)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.inflateMenu(R.menu.menu_list)
        binding.swipeRefreshLayout.isEnabled = false
        lifecycleScope.launch(Dispatchers.IO) {
            val role = firebaseInteractor.getUserRole()
            withContext(Dispatchers.Main){
                setupDrawer(role)
            }
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menuItemSearch -> {
                    val searchView = it.actionView as SearchView
                    searchView.queryHint = getString(R.string.search)
                    it.expandActionView()
                    /*searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
                        if (!hasFocus)
                            it.collapseActionView()
                    }*/
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
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if(systemInteractor.isInternetAvailable()) {
            when (item.itemId) {
                R.id.action_drawer_admin -> {
                    Timber.d("Navigate to AdminListFragment")
                    navigator?.add(AdminListFragment())
                }
                R.id.action_drawer_two -> {
                    Timber.d("Navigate to MapFragment")
                    navigator?.add(MapFragment())
                }
                R.id.action_drawer_three -> {
                    Timber.d("Navigate to SettingsFragment")
                    navigator?.add(SettingsFragment())
                }
                R.id.action_drawer_four -> {
                    Timber.d("Logout, navigate to LoginFragment")
                    firebaseInteractor.logout()
                    navigator?.replace(LoginFragment())
                }
            }
        }
        else toast(getString(R.string.no_internet))
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        viewModel.dataChanged(dc, type)
    }

}


