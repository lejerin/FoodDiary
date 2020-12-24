package lej.happy.fooddiary.ui.review

import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_review_detail.*
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.databinding.FragmentReviewDetailBinding
import lej.happy.fooddiary.ui.MainActivity
import lej.happy.fooddiary.ui.base.BaseFragment

class ReviewDetailFragment :  BaseFragment<FragmentReviewDetailBinding>() {

    override val layoutResourceId: Int
        get() = R.layout.fragment_review_detail

    private lateinit var factory: ReviewDetailViewModelFactory
    private lateinit var viewModel: ReviewDetailViewModel

    private val TAG = "ReviewDetailFragment"

    override fun initStartView() {


        val repository = Repository(context!!)
        factory = ReviewDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(ReviewDetailViewModel::class.java)
        viewDataBinding.viewModel = viewModel

        val a = activity as MainActivity
        a.setActionBarTitle(arguments!!.getString("name").toString())
        viewModel.address = arguments!!.getString("address").toString()

    }

    override fun initDataBinding() {
        viewModel.context = context!!

        rv_review_detail.adapter = viewModel.musicAdapter
        rv_review_detail.layoutManager = LinearLayoutManager(context)


    }

    override fun initAfterBinding() {
        viewModel.timeList.observe(this, Observer {

            rv_review_detail.adapter?.notifyDataSetChanged()
            if(it.size > 0){
                no_data_in_recyclerview.visibility = View.GONE
            }else{
                no_data_in_recyclerview.visibility = View.VISIBLE
            }
        })
        viewModel.getReviewDetailData()
    }

    fun setOrder(boolean: Boolean){
        viewModel.setOrder(boolean)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.getReviewDetailData()

    }

}