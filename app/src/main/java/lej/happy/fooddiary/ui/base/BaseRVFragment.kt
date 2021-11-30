package lej.happy.fooddiary.ui.base

import android.util.Log
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.ui.custom.LinearLayoutManagerWrapper
import lej.happy.fooddiary.ui.main.MainViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

abstract class BaseRVFragment<T : ViewDataBinding, A: Any> : BaseFragment<T>() {

    abstract val mRecyclerView: RecyclerView
    abstract val mAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    abstract val mEmptyView: View?

    lateinit var mLayoutManager: LinearLayoutManagerWrapper

    val mMainViewModel: MainViewModel by sharedViewModel()

    override fun initBinding() {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        mLayoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
        mRecyclerView.apply {
            layoutManager = mLayoutManager
            adapter = mAdapter
        }
    }

    fun checkEmptyDataUi() {
        mEmptyView?.visibility = if (mAdapter.itemCount > 0) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}