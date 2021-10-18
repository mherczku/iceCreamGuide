package hu.hm.icguide.ui.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentDetailBinding
import hu.hm.icguide.extensions.validateNonEmpty
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Comment
import hu.hm.icguide.models.Shop
import hu.hm.icguide.ui.add.AddReviewDialog
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment(
    private val shopId: String
) : RainbowCakeFragment<DetailViewState, DetailViewModel>(), OnSuccessListener<Any>,
    OnFailureListener, CommentAdapter.CommentAdapterListener,
    FirebaseInteractor.DataChangedListener, FirebaseInteractor.OnToastListener {

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
        viewModel.getShop(shopId)
        setupToolbar()
        setupView()

        // TODO Setup views
    }

    override fun onStart() {
        super.onStart()
        viewModel.load()
        viewModel.initCommentsListeners(shopId, this, this)
    }

    override fun render(viewState: DetailViewState) {
        adapter.submitList(viewState.comments)
        shop = viewState.shop

        binding.tvName.text = shop.name
        binding.tvAddress.text = shop.address
        binding.ratingBar.rating = shop.rate
        Glide.with(binding.imgShop)
            .load(shop.photo)
            .placeholder(R.drawable.placeholder)
            .into(binding.imgShop)
        // TODO Render state
    }

    private fun setupRecyclerView() {
        adapter = CommentAdapter(this)
        binding.rvComments.adapter = adapter
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navigator?.pop()
        }
        binding.toolbar.inflateMenu(R.menu.menu_list)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {

        binding.ratingBar.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                AddReviewDialog(shop, viewModel::getShop).show(childFragmentManager, null)
            }
            return@OnTouchListener true
        })

        if (!viewModel.isNetAvailable()) {
            binding.etComment.isEnabled = false
            binding.btnSend.isEnabled = false
            Toast.makeText(context, getString(R.string.internet_unavailable), Toast.LENGTH_SHORT)
                .show()
        } else {
            setupRecyclerView()
            binding.btnSend.setOnClickListener {
                if (!binding.etComment.validateNonEmpty()) return@setOnClickListener

                val c = DetailPresenter.PostComment(
                    author = firebaseInteractor.firebaseUser?.displayName.toString(),
                    content = binding.etComment.text.toString(),
                    photo = (firebaseInteractor.firebaseUser?.photoUrl ?: "") as String,
                    date = Timestamp.now()
                )
                viewModel.postComment(shopId, c, this, this)
                binding.etComment.text?.clear()
            }
        }
    }

    override fun onSuccess(p0: Any?) {
        toast(getString(R.string.successful_comment))
    }

    override fun onFailure(p0: Exception) {
        toast(p0.localizedMessage)
    }

    override fun onItemSelected(comment: Comment) {
        toast(comment.content)
    }

    override fun dataChanged(dc: QueryDocumentSnapshot, type: String) {
        viewModel.dataChanged(dc, type)
    }

    override fun toast(message: String?) {
        message ?: return
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
