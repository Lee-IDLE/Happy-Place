package com.example.happyplace.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
data class HappyPlaceModel(
    val id: Int? = null,
    val title: String? = null,
    val image: String? = null,
    val description: String? = null,
    val date: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
): Parcelable