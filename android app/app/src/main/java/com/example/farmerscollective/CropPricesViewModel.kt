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
    private val _data = MutableLiveData<Map<String, Map<String, Float>>>(mapOf())
    private val _selected = MutableLiveData(arrayListOf("TELANGANA_ADILABAD_Price"))

    val data: LiveData<Map<String, Map<String, Float>>>
    get() = _data

    val selected: LiveData<ArrayList<String>>
    get() = _selected

    init {
        getData()
    }

    private fun getData() {

        val temp = mutableMapOf<String, Map<String, Float>>()

        if(context.fileList().isNotEmpty()) {
            val array = _selected.value

            for(mandi in array!!) {
                val file = File(context.filesDir, "$mandi.csv")
                val prices = mutableMapOf<String, Float>()

                if(file.exists()) {

                    csvReader().open(file) {
                        readAllAsSequence().forEach {
                            if(it[0] != "DATE") prices[it[0]] = it[1].toFloat()
                        }
                    }
                }

                temp[mandi] = prices


            }

            _data.value = temp
        }

    }

    fun makeSelection(mandi: String, check: Boolean) {
        val newList = _selected.value!!

        if(check) newList.add(mandi)
        else newList.remove(mandi)

        Log.d("debugging $mandi", newList.toString())
        _selected.value = newList

        getData()
    }

}