package lej.happy.fooddiary.ui.review

import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_review.*
import kotlinx.android.synthetic.main.fragment_taste.*
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.databinding.FragmentReviewBinding
import lej.happy.fooddiary.ui.base.BaseFragment
import lej.happy.fooddiary.ui.taste.TasteViewModel
import lej.happy.fooddiary.ui.taste.TasteViewModelFactory

class ReviewFragment :  BaseFragment<FragmentReviewBinding>() {

    override val layoutResourceId: Int
        get() = R.layout.fragment_review

    private lateinit var factory: ReviewViewModelFactory
    private lateinit var viewModel: ReviewViewModel

    private val TAG = "ReviewFragment"

    override fun initStartView() {
        val repository = Repository(context!!)
        factory = ReviewViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(ReviewViewModel::class.java)
        viewDataBinding.viewModel = viewModel
    }

    override fun initDataBinding() {
        viewModel.context = context!!
        rv_review.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        rv_review.adapter = viewModel.reviewAdapter
        rv_review.layoutManager = LinearLayoutManager(context)

    }

    override fun initAfterBinding() {
        viewModel.rankList.observe(this, Observer {

            rv_review.adapter?.notifyDataSetChanged()
            if(it.size > 0){
                review_no_data_in_recyclerview.visibility = View.GONE
            }else{
                review_no_data_in_recyclerview.visibility = View.VISIBLE
            }
        })

        viewModel.isDetailClick.observe(this, Observer {
            val transaction = fragmentManager!!.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left,
                R.anim.exit_to_right)
            transaction.add(R.id.frame_layout, it)
            transaction.addToBackStack("review_detail")
            transaction.commit()
        })

        viewModel.getReviewData()
    }

    fun setOrder(boolean: Boolean){
        viewModel.setOrder(boolean)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.getReviewData()

    }
}