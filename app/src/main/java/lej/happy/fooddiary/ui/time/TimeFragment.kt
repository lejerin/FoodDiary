package lej.happy.fooddiary.ui.time

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_time.*
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.ui.base.BaseFragment
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Observer
import lej.happy.fooddiary.databinding.FragmentTimeBinding

class TimeFragment :  BaseFragment<FragmentTimeBinding>() {

    override val layoutResourceId: Int
        get() = R.layout.fragment_time

    private lateinit var factory: TimeViewModelFactory
    private lateinit var viewModel: TimeViewModel

    private val TAG = "TimeFragment"



    override fun initStartView() {

        val repository = Repository(context!!)
        factory = TimeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(TimeViewModel::class.java)
        viewDataBinding.viewModel = viewModel

    }

    override fun initDataBinding() {

        viewModel.context = context!!
        viewModel.homeLayoutManager = LinearLayoutManager(context)
        rv_home.layoutManager =  viewModel.homeLayoutManager
        rv_home.adapter = viewModel.homePhotoAdapter
        rv_home.setHasFixedSize(true)
        rv_home.setItemViewCacheSize(20)

        //이번달 첫째날, 마지막날
        val nowYM = SimpleDateFormat("yyyy-M", Locale.KOREA).format(Date())
        viewModel.stringToDate(nowYM)



    }

    override fun initAfterBinding() {

        viewModel.timeList.observe(this, Observer {

            rv_home.adapter?.notifyDataSetChanged()
            if(it.size > 0){
                no_data_in_recyclerview.visibility = View.GONE
            }else{
                no_data_in_recyclerview.visibility = View.VISIBLE
            }
        })
        viewModel.getTimeData()

    }

    fun setOrder(boolean: Boolean){
        viewModel.setOrder(boolean)
    }

    fun setHomeDate(isMonth: Boolean, year: String, mon : String) {
        viewModel.setHomeDate(isMonth, year, mon)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.initPage()
        viewModel.getTimeData()
    }

}