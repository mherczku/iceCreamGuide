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
import hu.hm.icguide.models.Shop
import javax.inject.Inject
import kotlin.reflect.KFunction1


@AndroidEntryPoint
class AddReviewDialog(private val shop: Shop, private val myCallback: KFunction1<String, Unit>) : DialogFragment(),
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
        firebaseInteractor.getReviews(shop.id, this)
    }

    private fun addClick() {
        firebaseInteractor.firebaseUser ?: return
        val review = Review(
            userId = firebaseInteractor.firebaseUser!!.uid,
            rate = binding.ratingBar.rating
        )
        firebaseInteractor.postReview(shop, review, this, this)
    }

    private fun toast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSuccess(p0: Any?) {
        if (p0 is QuerySnapshot) {
            val reviews = mutableListOf<Review>()
            for (d in p0.documents) {
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
            setupReviewButton(reviews)
        } else {
            toast(getString(R.string.review_success))
            myCallback(shop.id)
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
                if(binding.ratingBar.rating != review.rate){
                    firebaseInteractor.updateReview(shop, review, binding.ratingBar.rating)
                    //TODO átírni hogy shopot updatelje mint régen
                    myCallback(shop.id)
                    this.dismiss()
                }
            }
        }
        else binding.btnAdd.setOnClickListener { addClick() }
    }

    override fun onFailure(p0: Exception) {
        toast(p0.localizedMessage)
        myCallback(shop.id)
        this.dismiss()
    }

}