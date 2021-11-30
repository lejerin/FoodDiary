package lej.happy.fooddiary.ui.date

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.UpdateEvent
import lej.happy.fooddiary.data.model.BarDate
import lej.happy.fooddiary.data.model.HomeData
import lej.happy.fooddiary.databinding.FragmentDateBinding
import lej.happy.fooddiary.ui.adapter.DateAdapter
import lej.happy.fooddiary.ui.base.BaseRVFragment
import org.koin.android.viewmodel.ext.android.viewModel

class DateFragment : BaseRVFragment<FragmentDateBinding, HomeData>() {
    private val TAG = DateFragment::class.java.simpleName

    override val layoutResourceId: Int
        get() = R.layout.fragment_date

    override val mAdapter by lazy { DateAdapter(requireContext()) }
    override val mRecyclerView by lazy { binding.rvDate }
    override val mEmptyView by lazy { binding.noDataInRecyclerview }

    private val mDateViewModel: DateViewModel by viewModel()

    val clicksListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mDateViewModel.mutex.isLocked || mDateViewModel.isEnd) {
                return
            }

            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)?.findLastCompletelyVisibleItemPosition()
            val itemTotalCount = recyclerView.adapter?.itemCount

            if (lastVisibleItemPosition != null && itemTotalCount != null) {
                if (lastVisibleItemPosition >= itemTotalCount - (BaseValue.limit / 2)) { // 스크롤이 끝에 도달했는지 확인
                    mDateViewModel.getDateData(requireContext(), BaseValue.DATA_TYPE.ADD)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainViewModel.dateLiveEvent.value = BarDate()
    }

    override fun initBinding() {
        super.initBinding()
        binding.fragment = this@DateFragment
        initObserver()
        initSubscriber()
    }

    private fun initObserver() {
        mDateViewModel.newDataList.observe(viewLifecycleOwner, {
            it?.let { pair ->
                Log.i(TAG, "newDataList ${it.second?.size}")
                CoroutineScope(Dispatchers.Main).launch {
                    mAdapter.updateList(pair)
                    checkEmptyDataUi()
                    if (pair.first == BaseValue.DATA_TYPE.INIT) {
                        mRecyclerView.scrollToPosition(0)
                    }
                }
            }
        })
    }

    private fun initSubscriber() {
        mMainViewModel.dateLiveEvent.observe(viewLifecycleOwner, {
            initRecyclerView()
            mAdapter.isAllMode = it.isAll
            mDateViewModel.isAllMode = it.isAll
            mDateViewModel.setDate("${it.year}-${it.month}")
            mDateViewModel.getDateData(context, BaseValue.DATA_TYPE.INIT)
        })
        mDisposable.add(
        UpdateEvent.mOrderUpdateDataObs
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                initRecyclerView()
                mDateViewModel.isDESC = (it == BaseValue.ORDER_NEWEST)
                mDateViewModel.getDateData(context, BaseValue.DATA_TYPE.INIT)
            })
        mDisposable.add(
            UpdateEvent.mRefreshUpdateDataObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    initRecyclerView()
                    mDateViewModel.getDateData(context, BaseValue.DATA_TYPE.INIT)
                }
        )
    }

    private fun initRecyclerView() {
        Log.i("getData", "initRecyclerView")
        mDateViewModel.page = 0
        mDateViewModel.isEnd = false
    }
}