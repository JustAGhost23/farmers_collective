package com.example.farmerscollective.prediction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.farmerscollective.data.Prediction
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.time.LocalDate

class CropPredictedViewModel(application: Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    val context = application
    private val _data = MutableLiveData<List<Prediction>>(arrayListOf())
    private val _graph = MutableLiveData<Map<String, Float>>(mapOf())
    private val _today = MutableLiveData<Float>()

    val data: LiveData<List<Prediction>>
        get() = _data

    val graph: LiveData<Map<String, Float>>
        get() = _graph

    val today: LiveData<Float>
        get() = _today

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
                        temp.add(Prediction(it[0], it[1].toFloat(), it[2].toFloat(), it[3].toFloat(), it[4].toFloat(), it[5].toFloat()))
                        map[it[0]] = it[2].toFloat()
                    }
                }
            }

            file = File(context.filesDir, "MAHARASHTRA_NAGPUR_Price_${LocalDate.now().year}")

            if(file.exists()) {
                csvReader().open(file) {
                    readAllAsSequence().forEach {
                        val dt = LocalDate.parse(it[0])
                        if(dt.isBefore(LocalDate.parse(date))) map[it[0]] = it[1].toFloat()
                    }
                }
            }

            if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1))) {
                file = File(context.filesDir, "MAHARASHTRA_NAGPUR_Price_${LocalDate.now().year - 1}")

                if(file.exists()) {
                    csvReader().open(file) {
                        readAllAsSequence().forEach {
                            val dt = LocalDate.parse(it[0])
                            if(dt.isBefore(LocalDate.parse(date))) map[it[0]] = it[1].toFloat()
                        }
                    }
                }

            }


            temp.sortBy { value -> value.output }
            _data.value = temp.reversed().subList(1, 4)
            _graph.value = map

            _today.value = map[LocalDate.parse(date).minusDays(1).toString()] ?: 0.0f
        }
    }

}