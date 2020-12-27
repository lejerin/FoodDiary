package lej.happy.fooddiary.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_map_detail.*
import lej.happy.fooddiary.R
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class MapDetailActivity : AppCompatActivity() {

    lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_detail)

        val x = intent.getDoubleExtra("x",0.0)
        val y = intent.getDoubleExtra("y",0.0)
        val name = intent.getStringExtra("name")
        val address = intent.getStringExtra("address")

        adress.text = address

        mapView = MapView(this)
        val mapViewContainer = findViewById<ViewGroup>(R.id.map_view)
        mapView.setZoomLevel(0,false)
        mapViewContainer.addView(mapView)

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


        map_detail_back_btn.setOnClickListener {
            finish()
        }

    }
}