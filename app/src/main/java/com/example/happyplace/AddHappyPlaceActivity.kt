package com.example.happyplace

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplace.databinding.ActivityAddHappyPlaceBinding

class AddHappyPlaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddHappyPlaceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAddPlace)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            binding.toolbarAddPlace.title = "ADD HAPPY PLACE"
        }

        binding.toolbarAddPlace.setNavigationOnClickListener{
            finish()
        }
    }
}