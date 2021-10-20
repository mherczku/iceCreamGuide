package hu.hm.icguide.ui.settings

import android.os.Bundle
import android.view.View
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentSettingsBinding
import hu.hm.icguide.extensions.EditTextDialog
import hu.hm.icguide.extensions.SettingsPreference
import hu.hm.icguide.interactors.FirebaseInteractor
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : RainbowCakeFragment<SettingsViewState, SettingsViewModel>() {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_settings

    @Inject
    lateinit var firebaseInteractor: FirebaseInteractor
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var user : FirebaseUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        user = firebaseInteractor.firebaseUser!!
        setupView()
    }

    private fun setupView() {
        binding.toolbar.setNavigationOnClickListener { navigator?.pop() }
        binding.tvName.text = user.displayName
        Glide.with(binding.ivUser)
            .load(user.photoUrl)
            .placeholder(R.drawable.placeholder)
            .into(binding.ivUser)
        binding.tvName.setOnClickListener {
            EditTextDialog(binding.tvName.text.toString(), ::editName).show(childFragmentManager, null)
        }
    }

    private fun editName(name: String){
        binding.tvName.text = name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.beginTransaction().add(R.id.container, SettingsPreference()).commit()

    }

    override fun onStart() {
        super.onStart()
        viewModel.load()
    }

    override fun render(viewState: SettingsViewState) {
        // TODO Render state
    }


}
