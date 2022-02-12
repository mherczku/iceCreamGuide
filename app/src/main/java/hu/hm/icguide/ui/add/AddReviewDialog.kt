package hu.hm.icguide.ui.add

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.DialogReviewBinding
import hu.hm.icguide.extensions.toast
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Review
import hu.hm.icguide.models.Shop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class AddReviewDialog(private val shop: Shop, private val refreshDetailFragment: (String) -> Unit) :
    DialogFragment() {

    @Inject
    lateinit var firebaseInteractor: FirebaseInteractor
    private lateinit var binding: DialogReviewBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogReviewBinding.inflate(LayoutInflater.from(context))
        setup()
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setup() {
        lifecycleScope.launch(Dispatchers.IO) {
            val qs = firebaseInteractor.getReviews(shop.id)
            val reviews = mutableListOf<Review>()
            if (qs != null) {
                for (d in qs.documents) {
                    val o = d.toObject<Review>()
                    if (o != null) {
                        val r = Review(
                            id = d.id,
                            userId = o.userId,
                            rate = o.rate
                        )
                        reviews.add(r)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                setupReviewButton(reviews)
            }
        }
    }

    private fun addClick() {
        firebaseInteractor.firebaseUser ?: return
        val review = Review(
            userId = firebaseInteractor.firebaseUser!!.uid,
            rate = binding.ratingBar.rating
        )
        firebaseInteractor.postReview(shop, review) {
            val m = it ?: getString(R.string.review_success)
            toast(m)
            refreshDetailFragment(shop.id)
            this.dismiss()
        }
    }

    private fun setupReviewButton(reviews: MutableList<Review>) {
        val review = reviews.find { it.userId == firebaseInteractor.firebaseUser?.uid }
        if (review != null) {
            binding.ratingBar.rating = review.rate
            toast(getString(R.string.already_reviewed))
            binding.btnAdd.text = getString(R.string.edit)
            binding.btnAdd.setOnClickListener {
                if (binding.ratingBar.rating != review.rate) {
                    firebaseInteractor.updateReview(shop, review, binding.ratingBar.rating) {
                        val m = it ?: getString(R.string.review_success)
                        toast(m)
                        refreshDetailFragment(shop.id)
                        this.dismiss()
                    }
                    refreshDetailFragment(shop.id)
                    this.dismiss()
                }
            }
        } else binding.btnAdd.setOnClickListener { addClick() }
    }
}