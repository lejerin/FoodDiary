package lej.happy.fooddiary.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddiary.Network.RetrofitClient
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_map_search.*
import lej.happy.fooddiary.Adapter.MapListAdapter
import lej.happy.fooddiary.data.Model.Document
import lej.happy.fooddiary.data.Model.LocalMapData
import lej.happy.fooddiary.R
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapSearchActivity : AppCompatActivity() , View.OnClickListener{

    val mapList = mutableListOf<Document>()
    lateinit var mapView: MapView
    var selectLocation: Document? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_search)

        mapView = MapView(this)
        val mapViewContainer = findViewById<ViewGroup>(R.id.map_view)
        mapView.setZoomLevel(0,false)
        mapViewContainer.addView(mapView)

        map_search_back_btn.setOnClickListener(this)
        remove_btn_map_search.setOnClickListener(this)

        sliding_bar.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        map_search_btn.setOnClickListener {

            hideKeyboard(this)
            getMapSearchKeword()
        }

        keword_search_text.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    hideKeyboard(this)
                    getMapSearchKeword()
                }
                else -> return@OnEditorActionListener false
            }
            true
        })

        val adapter = MapListAdapter(mapList)
        adapter.setItemClickListener(object : MapListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val item = mapList[position]
                selectLocation = item
                select_title_text.text = item.placeName
                select_location_btn.visibility = View.VISIBLE
                mapViewChange(item.x.toDouble(),item.y.toDouble(),item.placeName)

            }
        })

        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(this)
        listView.addItemDecoration(DividerItemDecoration(this@MapSearchActivity, LinearLayoutManager.VERTICAL))


        select_location_btn.setOnClickListener {
            if(selectLocation != null){
                var newIntent = Intent()
                newIntent.putExtra("name", selectLocation!!.roadAddressName)
                newIntent.putExtra("x", selectLocation!!.x)
                newIntent.putExtra("y", selectLocation!!.y)
                newIntent.putExtra("roadAddress", selectLocation!!.placeName)
              //  newIntent.putExtra("address", selectLocation!!.addressName)
                setResult(RESULT_OK, newIntent);
                finish()
            }else{
                Toast.makeText(this,"장소를 검색하여 입력해주세요", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.map_search_back_btn -> {
                finish()
            }
            R.id.remove_btn_map_search -> {
                keword_search_text.setText("")
            }
        }
    }

    private fun mapViewChange(x: Double, y: Double, name: String){
        mapView.removeAllPOIItems()
        sliding_bar.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED

        val mapPoint = MapPoint.mapPointWithGeoCoord(y,x)

        mapView.setMapCenterPoint(mapPoint,true)
        val marker = MapPOIItem()
        marker.itemName = name
        marker.tag = 0
        marker.mapPoint = mapPoint
        marker.markerType = MapPOIItem.MarkerType.RedPin // 기본으로 제공하는 BluePin 마커 모양.

        marker.selectedMarkerType =
            MapPOIItem.MarkerType.RedPin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        mapView.addPOIItem(marker)


    }

    private fun hideKeyboard(activity: Activity) {
        val inputManager: InputMethodManager = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // check if no view has focus:
        val currentFocusedView: View? = activity.currentFocus
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(
                currentFocusedView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    private fun getMapSearchKeword(){


        RetrofitClient.getService().getKeywordMap(keword_search_text.text.toString(),
            "KakaoAK 7114008c849d19203b186846030bd6ad").enqueue(object :
            Callback<LocalMapData> {
            override fun onFailure(call: Call<LocalMapData>, t: Throwable) {
          //      System.out.println("서버 오류입니다 "+ t.message)
            }

            override fun onResponse(call: Call<LocalMapData>, response: Response<LocalMapData>) {
                if (response.isSuccessful) {
                    val data = response.body()!!.documents

                    //목록 받아오기
                    if (data.size > 0) {
                      //  sliding_bar.isTouchEnabled = true
                        mapList.clear()
                        mapList.addAll(data)
                        listView.adapter!!.notifyDataSetChanged()
                        sliding_bar.panelState = SlidingUpPanelLayout.PanelState.EXPANDED


                    }
                }else{
                 //   System.out.println("서버 오류입니다 카카오 맵")
                }

           }

        })
    }

    override fun onBackPressed() {
        if(sliding_bar.panelState == SlidingUpPanelLayout.PanelState.EXPANDED){
            sliding_bar.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }else{
            super.onBackPressed()
        }
    }
}