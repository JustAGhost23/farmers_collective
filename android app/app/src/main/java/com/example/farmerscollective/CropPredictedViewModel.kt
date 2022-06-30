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
import java.time.LocalDate

class CropPredictedViewModel(application: Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    val context = application
    private val _data = MutableLiveData<ArrayList<Prediction>>(arrayListOf())
    private val _graph = MutableLiveData<Map<String, Float>>(mapOf())

    val data: LiveData<ArrayList<Prediction>>
        get() = _data

    val graph: LiveData<Map<String, Float>>
        get() = _graph

    init {
        getData()
    }

    fun getData() {
        val temp = ArrayList<Prediction>()
        val map = mutableMapOf<String, Float>()
        var date = ""

        if(context.fileList().isNotEmpty()) {
            var file = File(context.filesDir, "predict.csv")

            if(file.exists()) {
                csvReader().open(file) {
                    readAllAsSequence().forEachIndexed { i, it ->

                        if(i == 0) date = it[0]
                        temp.add(Prediction(it[0], it[1].toFloat(), it[2].toFloat(), it[3].toFloat(), it[4].toFloat()))
                        map[it[0]] = it[3].toFloat()
                    }
                }
            }

            file = File(context.filesDir, "TELANGANA_ADILABAD_Price")

            if(file.exists()) {
                csvReader().open(file) {
                    readAllAsSequence().forEach {
                        if(LocalDate.parse(it[0])
                                .plusDays(31)
                                .isAfter(LocalDate.parse(date)) &&
                            LocalDate.parse(it[0]).isBefore(LocalDate.parse(date))) {
//                        if(it[0] != "DATE" && LocalDate.parse(it[0])
//                                .isBefore(LocalDate.of(2022, 5, 1)) &&
//                                LocalDate.parse(it[0]).isAfter(LocalDate.of(2022, 2, 28))) {

                            map[it[0]] = it[1].toFloat()
                        }
                    }
                }
            }

//           temp.forEach {
//               map[it.date] = it.gain
//           }

            temp.sortBy { value -> value.output }
            _data.value = temp
            _graph.value = map
        }
    }

}