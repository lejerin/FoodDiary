package lej.happy.fooddiary.ui.taste

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_taste.*
import lej.happy.fooddiary.Adapter.PhotoGridAdapter
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.databinding.FragmentTasteBinding
import lej.happy.fooddiary.databinding.FragmentTimeBinding
import lej.happy.fooddiary.ui.base.BaseFragment
import lej.happy.fooddiary.ui.custom.CustomImageButton
import lej.happy.fooddiary.ui.time.TimeViewModel
import lej.happy.fooddiary.ui.time.TimeViewModelFactory

class TasteFragment :  BaseFragment<FragmentTasteBinding>() {

    override val layoutResourceId: Int
        get() = R.layout.fragment_taste

    private lateinit var factory: TasteViewModelFactory
    private lateinit var viewModel: TasteViewModel

    private val TAG = "TasteFragment"

    override fun initStartView() {
        val repository = Repository(context!!)
        factory = TasteViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(TasteViewModel::class.java)
        viewDataBinding.viewModel = viewModel
    }

    override fun initDataBinding() {

        viewModel.context = context!!
        ts_review.adapter = viewModel.tasteAdapter
        ts_review.layoutManager = GridLayoutManager(context, 3)
    }

    override fun initAfterBinding() {

        viewModel.tasteList.observe(this, Observer {

            println("dddd" + it.size)

            ts_review.adapter?.notifyDataSetChanged()
            if(it.size > 0){
                taste_no_data_in_recyclerview.visibility = View.GONE
            }else{
                taste_no_data_in_recyclerview.visibility = View.VISIBLE
            }
        })

        viewModel.selectedBtn(best_taste_btn)
    }


}