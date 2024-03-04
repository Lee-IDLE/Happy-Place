package com.example.happyplace.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.happyplace.R
import com.example.happyplace.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplace.databinding.ActivityHappyPlaceDetailBinding
import com.example.happyplace.models.HappyPlaceModel
import java.lang.reflect.TypeVariable

class HappyPlaceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHappyPlaceDetailBinding
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var happyPlaceDetailModel: HappyPlaceModel? = null
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            when{
                SDK_INT >= 33 -> {
                    happyPlaceDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS, HappyPlaceModel::class.java)
                }
                SDK_INT < 33 -> {
                    happyPlaceDetailModel = intent.getParcelableExtra<HappyPlaceModel>(MainActivity.EXTRA_PLACE_DETAILS)
                }
            }
        }

        if(happyPlaceDetailModel != null){
            // toolbar 초기화
            setSupportActionBar(binding.toolbarHappyPlaceDetail)
            if(supportActionBar != null) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowTitleEnabled(true)
                binding.toolbarHappyPlaceDetail.title = happyPlaceDetailModel.title
            }

            binding.toolbarHappyPlaceDetail.setNavigationOnClickListener{
                finish()
            }

            binding.ivPlaceImage.setImageURI((Uri.parse(happyPlaceDetailModel.image)))
            binding.tvDescription.text = happyPlaceDetailModel.description
            binding.tvLocation.text = happyPlaceDetailModel.location

            binding.btnViewOnMap.setOnClickListener{
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
                startActivity(intent)
            }
        }
    }
}