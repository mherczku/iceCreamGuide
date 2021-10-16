package hu.hm.icguide.ui.add

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.DialogReviewBinding
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.models.Review
import javax.inject.Inject


@AndroidEntryPoint
class AddReviewDialog(private val shopId: String) : DialogFragment(),
    OnSuccessListener<Any>, OnFailureListener {

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
        firebaseInteractor.getReviews(shopId, this)
    }

    private fun addClick() {
        firebaseInteractor.firebaseUser ?: return
        val review = Review(
            userId = firebaseInteractor.firebaseUser!!.uid,
            rate = binding.ratingBar.rating
        )
        //TODO update shop's rate and ratings with new rate, maybe in interactor
        firebaseInteractor.postReview(shopId, review, this, this)

    }

    private fun toast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSuccess(p0: Any?) {
        if(p0 is QuerySnapshot){
            val reviews = mutableListOf<Review>()
            for (d in p0.documents) {
                val s = d.toObject<Review>()
                if (s != null) {
                    reviews.add(s)
                }
            }
            setupReviewButton(reviews)
        }
        else {
            toast(getString(R.string.review_success))
            this.dismiss()
        }
    }

    private fun setupReviewButton(reviews: MutableList<Review>) {
        val review = reviews.find { it.userId == firebaseInteractor.firebaseUser?.uid }
        if( review != null) {
            binding.ratingBar.rating = review.rate
            toast(getString(R.string.already_reviewed)) //TODO lehet módosítás is
            binding.btnAdd.isEnabled = false
        }
        binding.btnAdd.setOnClickListener { addClick() }
    }

    override fun onFailure(p0: Exception) {
        toast(p0.localizedMessage)
        this.dismiss()
    }

}