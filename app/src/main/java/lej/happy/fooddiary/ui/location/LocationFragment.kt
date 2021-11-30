package lej.happy.fooddiary.ui.location

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
import lej.happy.fooddiary.data.model.HomeData
import lej.happy.fooddiary.data.model.ReviewRank
import lej.happy.fooddiary.databinding.FragmentDateBinding
import lej.happy.fooddiary.databinding.FragmentLocationBinding
import lej.happy.fooddiary.ui.adapter.DateAdapter
import lej.happy.fooddiary.ui.adapter.LocationAdapter
import lej.happy.fooddiary.ui.base.BaseFragment
import lej.happy.fooddiary.ui.base.BaseRVFragment
import lej.happy.fooddiary.ui.date.DateFragment
import lej.happy.fooddiary.ui.date.DateViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class LocationFragment : BaseRVFragment<FragmentLocationBinding, ReviewRank>() {
    private val TAG = LocationFragment::class.java.simpleName

    override val layoutResourceId: Int
        get() = R.layout.fragment_location

    override val mAdapter by lazy { LocationAdapter(requireContext()) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
        transaction.add(R.id.fcv_main, it)
        transaction.addToBackStack("review_detail")
        transaction.commit()
    }}
    override val mRecyclerView by lazy { binding.rvDate }
    override val mEmptyView by lazy { binding.noDataInRecyclerview }

    private val mLocationViewModel: LocationViewModel by viewModel()

    val clicksListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mLocationViewModel.mutex.isLocked || mLocationViewModel.isEnd) {
                return
            }

            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)?.findLastCompletelyVisibleItemPosition()
            val itemTotalCount = recyclerView.adapter?.itemCount

            if (lastVisibleItemPosition != null && itemTotalCount != null) {
                if (lastVisibleItemPosition >= itemTotalCount - (BaseValue.limit / 2)) { // 스크롤이 끝에 도달했는지 확인
                    mLocationViewModel.getDateData(BaseValue.DATA_TYPE.ADD)
                }
            }
        }
    }

    override fun initBinding() {
        super.initBinding()
        binding.fragment = this@LocationFragment
        initObserver()

        mLocationViewModel.getDateData(BaseValue.DATA_TYPE.INIT)
    }

    private fun initObserver() {
        mLocationViewModel.newDataList.observe(viewLifecycleOwner, {
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
        mDisposable.add(
            UpdateEvent.mOrderUpdateDataObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    initRecyclerView()
                    mLocationViewModel.isDESC = (it == BaseValue.ORDER_NEWEST)
                    mLocationViewModel.getDateData(BaseValue.DATA_TYPE.INIT)
                })
        mDisposable.add(
            UpdateEvent.mRefreshUpdateDataObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.i(TAG, "REFRESH initRecyclerView")
                    initRecyclerView()
                    mLocationViewModel.getDateData(BaseValue.DATA_TYPE.INIT)
                }
        )
    }

    private fun initRecyclerView() {
        Log.i(TAG, "initRecyclerView")
        mLocationViewModel.page = 0
        mLocationViewModel.isEnd = false
    }
}