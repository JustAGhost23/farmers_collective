package com.example.farmerscollective.prediction

import android.app.Application
import android.util.Log
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
    private val _dailyOrWeekly = MutableLiveData<String>()

    val data: LiveData<List<Prediction>>
        get() = _data

    val graph: LiveData<Map<String, Float>>
        get() = _graph

    val today: LiveData<Float>
        get() = _today

    val dailyOrWeekly: LiveData<String>
        get() = _dailyOrWeekly

    init {
        getData()
    }

    fun getData() {
        var temp = ArrayList<Prediction>()
        val map = mutableMapOf<String, Float>()
        var date = ""
        var weekDate = ""

        if (context.fileList().isNotEmpty()) {
            var file: File
            file = if(dailyOrWeekly.value == "Daily") {
                File(context.filesDir, "dailyPredict.csv")
            } else {
                File(context.filesDir, "weeklyPredict.csv")
            }


            if (file.exists()) {
                csvReader().open(file) {
                    readAllAsSequence().forEachIndexed { i, it ->
                        temp.add(
                            Prediction(
                                it[0],
                                it[1].toFloat(),
                                it[2].toFloat(),
                                it[3].toFloat(),
                                it[4].toFloat(),
                                it[5].toFloat()
                            )
                        )
                        map[it[0]] = it[2].toFloat()
                    }
                }
                csvReader().open(File(context.filesDir, "weeklyPredict.csv")) {
                    weekDate = readAllAsSequence().toList()[0][0]
                }
                csvReader().open(File(context.filesDir, "dailyPredict.csv")) {
                    date = readAllAsSequence().toList()[0][0]
                }
            }

            file = File(context.filesDir, "MAHARASHTRA_NAGPUR_Price_${LocalDate.now().year}")

            if (file.exists()) {
                csvReader().open(file) {
                    if(dailyOrWeekly.value == "Daily") {
                        readAllAsSequence().forEach {
                            val dt = LocalDate.parse(it[0])
                            if (dt.isBefore(LocalDate.parse(date))) map[it[0]] =
                                it[1].toFloat()
                        }
                    }
                    else {
                        var count = 7
                        var sum = 0.0
                        readAllAsSequence().toMutableList().asReversed().forEach {
                            val dt = LocalDate.parse(it[0])
                            if(dt.isBefore(LocalDate.parse(weekDate))) {
                                sum += it[1].toFloat()
                                count -= 1
                            }
                            if (count == 0) {
                                if (dt.isBefore(LocalDate.parse(weekDate))) map[it[0]] =
                                    it[1].toFloat()
                                count = 7
                                sum = 0.0
                            }
                        }
                    }
                }
            }

            if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1))) {
                file = File(
                    context.filesDir,
                    "MAHARASHTRA_NAGPUR_Price_${LocalDate.now().year - 1}"
                )

                if (file.exists()) {
                    csvReader().open(file) {
                        if(dailyOrWeekly.value == "Daily") {
                            readAllAsSequence().forEach {
                                val dt = LocalDate.parse(it[0])
                                if (dt.isBefore(LocalDate.parse(date))) map[it[0]] =
                                    it[1].toFloat()
                            }
                        }
                        else {
                            var count = 7
                            var sum = 0.0
                            readAllAsSequence().toMutableList().asReversed().forEach {
                                val dt = LocalDate.parse(it[0])
                                if(dt.isBefore(LocalDate.parse(weekDate))) {
                                    sum += it[1].toFloat()
                                    count -= 1
                                }
                                if (count == 0) {
                                    if (dt.isBefore(LocalDate.parse(weekDate))) map[it[0]] =
                                        it[1].toFloat()
                                    count = 7
                                    sum = 0.0
                                }
                            }
                        }
                    }
                }

            }
            Log.e("TAG", map.toString())
            map.toSortedMap()

            _graph.value = map
            if(dailyOrWeekly.value == "Daily") {
                _today.value = map[LocalDate.parse(date).minusDays(1).toString()] ?: 0.0f
            }
            else if(dailyOrWeekly.value == "Weekly") {
                _today.value = map[LocalDate.parse(weekDate).minusDays(7).toString()] ?: 0.0f
            }
            Log.e("TAG", today.value.toString())

            temp.sortBy { value -> value.output }
            temp = ArrayList(temp.map {
                it.gain -= map[date]!!
                it.loss -= map[date]!!
                it
            })
            _data.value = temp.reversed().subList(1, 4)
        }
    }
    fun changeSelection(dailyOrWeekly: String) {
        _dailyOrWeekly.value = dailyOrWeekly
        getData()
    }

}