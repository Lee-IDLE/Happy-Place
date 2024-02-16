package com.example.happyplace.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplace.R
import com.example.happyplace.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplace.databinding.ActivityHappyPlaceDetailBinding

class HappyPlaceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHappyPlaceDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // toolbar 초기화
        setSupportActionBar(binding.toolbarHappyPlaceDetail)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            binding.toolbarHappyPlaceDetail.title = "HAPPY PLACE DETAIL"
        }

        binding.toolbarHappyPlaceDetail.setOnClickListener{
            finish()
        }
    }
}