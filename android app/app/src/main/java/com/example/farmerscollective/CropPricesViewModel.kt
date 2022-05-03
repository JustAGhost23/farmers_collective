package com.example.farmerscollective

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.farmerscollective.data.Prediction
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File


class CropPricesViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val _data = MutableLiveData<MutableMap<String, Float>>(mutableMapOf())

    val data: LiveData<MutableMap<String, Float>>
    get() = _data

    init {
        getData()
    }

    private fun getData() {

        val temp = mutableMapOf<String, Float>()

        if(context.fileList().isNotEmpty()) {
            val file = File(context.filesDir, "prices.csv")

            csvReader().open(file) {
                readAllAsSequence().forEach {
                    if(it[0] != "DATE") temp[it[0]] = it[1].toFloat()
                }
            }

            _data.value = temp
        }

    }

}