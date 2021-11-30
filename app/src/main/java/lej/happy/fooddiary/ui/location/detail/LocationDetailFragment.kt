package lej.happy.fooddiary.ui.location.detail

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
import lej.happy.fooddiary.databinding.FragmentLocationDetailBinding
import lej.happy.fooddiary.ui.adapter.DateAdapter
import lej.happy.fooddiary.ui.base.BaseRVFragment
import lej.happy.fooddiary.ui.main.MainActivity
import org.koin.android.viewmodel.ext.android.viewModel

class LocationDetailFragment : BaseRVFragment<FragmentLocationDetailBinding, HomeData>() {
    private val TAG = LocationDetailFragment::class.java.simpleName

    override val layoutResourceId: Int
        get() = R.layout.fragment_location_detail

    override val mAdapter by lazy { DateAdapter(requireContext()) }
    override val mRecyclerView by lazy { binding.rvDate }
    override val mEmptyView by lazy { binding.noDataInRecyclerview }

    private val mLocationDetailViewModel: LocationDetailViewModel by viewModel()

    val clicksListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mLocationDetailViewModel.mutex.isLocked || mLocationDetailViewModel.isEnd) {
                return
            }

            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)?.findLastCompletelyVisibleItemPosition()
            val itemTotalCount = recyclerView.adapter?.itemCount

            if (lastVisibleItemPosition != null && itemTotalCount != null) {
                if (lastVisibleItemPosition >= itemTotalCount - (BaseValue.limit / 2)) { // 스크롤이 끝에 도달했는지 확인
                    mLocationDetailViewModel.getDateData(requireContext(), BaseValue.DATA_TYPE.ADD)
                }
            }
        }
    }

    override fun initBinding() {
        super.initBinding()
        binding.fragment = this@LocationDetailFragment
        initObserver()

        (activity as? MainActivity)?.also {
            it.setAppBarTitle(requireArguments().getString("name").toString())
        }
        mLocationDetailViewModel.address = requireArguments().getString("address").toString()

        mLocationDetailViewModel.getDateData(requireContext(), BaseValue.DATA_TYPE.INIT)
    }

    private fun initObserver() {
        mLocationDetailViewModel.newDataList.observe(viewLifecycleOwner, {
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
                    mLocationDetailViewModel.isDESC = (it == BaseValue.ORDER_NEWEST)
                    mLocationDetailViewModel.getDateData(requireContext(), BaseValue.DATA_TYPE.INIT)
                })
        mDisposable.add(
            UpdateEvent.mRefreshUpdateDataObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    initRecyclerView()
                    mLocationDetailViewModel.getDateData(requireContext(), BaseValue.DATA_TYPE.INIT)
                }
        )
    }

    private fun initRecyclerView() {
        Log.i("getData", "initRecyclerView")
        mLocationDetailViewModel.page = 0
        mLocationDetailViewModel.isEnd = false
    }
}