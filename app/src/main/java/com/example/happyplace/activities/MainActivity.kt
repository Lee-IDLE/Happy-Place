package com.example.happyplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplace.database.DatabaseHandler
import com.example.happyplace.databinding.ActivityMainBinding
import com.example.happyplace.models.HappyPlaceModel
import com.happyplaces.adapters.HappyPlacesAdapter
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getHappyPlacesListFromLocalDB()

        binding.fabAddHappyPlace.setOnClickListener{
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
            addHappyPlaceListener.launch(intent)
        }
    }

    private fun setupHappyPlaceRecyclerView(
        happyPlaceList: ArrayList<HappyPlaceModel>){
        binding.rvHappyPlacesList.layoutManager = LinearLayoutManager(this)
        binding.rvHappyPlacesList.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlaceList, editHappyPlaceDetail)
        binding.rvHappyPlacesList.adapter = placesAdapter

        placesAdapter.setOnClickListener(object: HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                placesAdapter.notifyEditItem(viewHolder.adapterPosition)
            }
        }
    }

    // adapter로 부터 entity값 가져왔고 AddHappyPlaceActivity에 넘겨줄 일만 남았다.
    private val editHappyPlaceDetail: (HappyPlaceModel) -> Unit = {entity ->
        val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
        addHappyPlaceListener.launch(intent)
    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList : ArrayList<HappyPlaceModel> = dbHandler.getHappyPlaceList()

        if(getHappyPlaceList.size > 0){
            binding.rvHappyPlacesList.visibility = View.VISIBLE
            binding.tvNoRecordsAvailable.visibility = View.GONE
            setupHappyPlaceRecyclerView(getHappyPlaceList)
        }else{
            binding.rvHappyPlacesList.visibility = View.GONE
            binding.tvNoRecordsAvailable.visibility = View.VISIBLE
        }
    }

    val addHappyPlaceListener: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Activity", "Cancelled or back pressed")
            }
        }

    companion object {
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}