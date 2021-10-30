package hu.hm.icguide.ui.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentDetailBinding
import hu.hm.icguide.extensions.hideKeyboard
import hu.hm.icguide.extensions.toast
import hu.hm.icguide.extensions.validateNonEmpty
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Comment
import hu.hm.icguide.models.Shop
import hu.hm.icguide.ui.add.AddReviewDialog
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment(
    private val shopId: String
) : RainbowCakeFragment<DetailViewState, DetailViewModel>(), CommentAdapter.CommentAdapterListener {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_detail

    @Inject
    lateinit var firebaseInteractor: FirebaseInteractor
    private lateinit var binding: FragmentDetailBinding
    private lateinit var adapter: CommentAdapter
    private var shop: Shop = Shop()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailBinding.bind(view)
        setupToolbar()
        setupView()
    }

    override fun onStart() {
        super.onStart()
        viewModel.load(shopId)
    }

    override fun render(viewState: DetailViewState) {
        Timber.d("Received ${viewState.comments.size} comments and ${viewState.shop.id} shop to display")
        adapter.submitList(viewState.comments)
        shop = viewState.shop

        binding.tvName.text = shop.name
        binding.tvAddress.text = shop.address
        binding.ratingBar.rating = shop.rate
        Glide.with(binding.imgShop)
            .load(shop.photo)
            .placeholder(R.drawable.placeholder)
            .into(binding.imgShop)
    }

    private fun setupRecyclerView() {
        adapter = CommentAdapter(this)
        binding.rvComments.adapter = adapter
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navigator?.pop()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {

        binding.ratingBar.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                AddReviewDialog(shop, viewModel::refreshShop).show(childFragmentManager, null)
            }
            return@OnTouchListener true
        })

        if (!viewModel.isNetAvailable()) {
            binding.etComment.isEnabled = false
            binding.btnSend.isEnabled = false
            toast(getString(R.string.internet_unavailable))
        } else {
            setupRecyclerView()
            binding.btnSend.setOnClickListener {
                hideKeyboard()
                if (!binding.etComment.validateNonEmpty()) return@setOnClickListener
                val c = DetailPresenter.PostComment(
                    authorId = firebaseInteractor.firebaseUser?.uid.toString(),
                    content = binding.etComment.text.toString(),
                    date = Timestamp.now()
                )
                viewModel.postComment(shopId, c, this::feedback)
                binding.etComment.text?.clear()
            }
        }
    }

    override fun onItemSelected(comment: Comment) {
        toast(comment.content)
    }

    private fun feedback(message: String? = getString(R.string.successful_comment)) {
        viewModel.refreshShop(shopId)
        toast(message)
    }

}