package com.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplace.activities.AddHappyPlaceActivity
import com.example.happyplace.activities.HappyPlaceDetailActivity
import com.example.happyplace.activities.MainActivity
import com.example.happyplace.databinding.ItemHappyPlaceBinding
import com.example.happyplace.models.HappyPlaceModel
//import kotlinx.android.synthetic.main.item_happy_place.view.*

// TODO (Step 6: Creating an adapter class for binding it to the recyclerview in the new package which is adapters.)
// START
open class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>,
    private val happyPlaceDetail: (HappyPlaceModel) -> Unit
) : RecyclerView.Adapter<HappyPlacesAdapter.MyViewHolder>() {
    private var onClickListener: OnClickListener? = null
    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context)))
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemBinding.ivPlaceImage.setImageURI(Uri.parse(model.image))
            holder.itemBinding.tvTitle.text = model.title
            holder.itemBinding.tvDescription.text = model.description

            holder.itemView.setOnClickListener{
                if(onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    inner class MyViewHolder(val itemBinding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }

    fun notifyEditItem(position: Int){
        happyPlaceDetail(list[position])
        /*
        val intent = Intent(context, HappyPlaceDetailActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivity(intent)
         */
        notifyItemChanged(position)
    }
}
// END