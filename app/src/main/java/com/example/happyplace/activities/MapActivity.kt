package com.example.happyplace.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.happyplace.R
import com.example.happyplace.databinding.ActivityMapBinding
import com.example.happyplace.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding

    private var mHappyPlaceDetail: HappyPlaceModel? = null
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            if(Build.VERSION.SDK_INT >= 33)
                mHappyPlaceDetail = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS, HappyPlaceModel::class.java)
            else
                mHappyPlaceDetail = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        if(mHappyPlaceDetail != null){
            // toolbar 초기화
            setSupportActionBar(binding.toolbarMap)
            if(supportActionBar != null) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowTitleEnabled(true)
                binding.toolbarMap.title = mHappyPlaceDetail!!.title

                binding.toolbarMap.setNavigationOnClickListener{
                    finish()
                }
            }

            // map 프래그먼트에 class를 SupportMapFragment로 했음
            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // 위치값 설정 및 마커 설정
        val position = LatLng(mHappyPlaceDetail!!.latitude!!, mHappyPlaceDetail!!.longitude!!)
        googleMap.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetail!!.location))

        // 지정한 위치로 줌이 이동하는 애니메이션 효과 적용
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)
    }
}