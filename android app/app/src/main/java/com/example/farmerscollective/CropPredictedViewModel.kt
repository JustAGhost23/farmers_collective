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

class CropPredictedViewModel(application: Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    val context = application
    private val _data = MutableLiveData<ArrayList<Prediction>>(arrayListOf())

    val data: LiveData<ArrayList<Prediction>>
        get() = _data

    init {
        getData()
    }

    fun getData() {
        val temp = ArrayList<Prediction>()

        if(context.fileList().isNotEmpty()) {
            val file = File(context.filesDir, "predict.csv")

            csvReader().open(file) {
                readAllAsSequence().forEach {
                    temp.add(Prediction(it[0], it[1].toFloat(), it[2].toFloat()))
                }
            }

            _data.value = temp
        }
    }

}