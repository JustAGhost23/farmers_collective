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

// ViewModel for Crop Predicted Fragment
class CropPredictedViewModel(application: Application) : AndroidViewModel(application) {
    // Private variables
    val context = application
    private val _data = MutableLiveData<List<Prediction>>(arrayListOf())
    private val _graph = MutableLiveData<Map<String, Float>>(mapOf())
    private val _today = MutableLiveData<Float>()
    private val _dailyOrWeekly = MutableLiveData<String>("Daily")

    // LiveData variables, getting data from above private variables
    val data: LiveData<List<Prediction>>
        get() = _data

    val graph: LiveData<Map<String, Float>>
        get() = _graph

    val today: LiveData<Float>
        get() = _today

    val dailyOrWeekly: LiveData<String>
        get() = _dailyOrWeekly

    // Code run when viewModel is initialized
    init {
        getData()
    }

    // Function to get Recommendations
    private fun getData() {
        // Initial variables
        var temp = ArrayList<Prediction>()
        val map = mutableMapOf<String, Float>()
        var date = ""
        var weekDate = ""

        if (context.fileList().isNotEmpty()) {
            var file: File
            // Obtain daily or weekly prediction file according to dailyOrWeekly.value
            file = if (dailyOrWeekly.value == "Daily") {
                File(context.filesDir, "dailyPredict.csv")
            } else {
                File(context.filesDir, "weeklyPredict.csv")
            }


            if (file.exists()) {
                // Read csv file and data to a temp list
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
                // Obtain last weekDate and date accordingly
                csvReader().open(File(context.filesDir, "weeklyPredict.csv")) {
                    weekDate = readAllAsSequence().toList()[0][0]
                }
                csvReader().open(File(context.filesDir, "dailyPredict.csv")) {
                    date = readAllAsSequence().toList()[0][0]
                }
            }

            // Add price data for current year from Nagpur
            file = File(context.filesDir, "MAHARASHTRA_NAGPUR_Price_${LocalDate.now().year}")

            if (file.exists()) {
                // Add data to map for daily/weekly accordingly
                csvReader().open(file) {
                    if (dailyOrWeekly.value == "Daily") {
                        readAllAsSequence().forEach {
                            val dt = LocalDate.parse(it[0])
                            if (dt.isBefore(LocalDate.parse(date))) map[it[0]] =
                                it[1].toFloat()
                        }
                    } else {
                        var count = 7
                        var sum = 0.0
                        readAllAsSequence().toMutableList().asReversed().forEach {
                            val dt = LocalDate.parse(it[0])
                            if (dt.isBefore(LocalDate.parse(weekDate))) {
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

            // Obtain data from previous year csv file if needed if current date is in the first 6 months of the year
            if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1))) {
                file = File(
                    context.filesDir,
                    "MAHARASHTRA_NAGPUR_Price_${LocalDate.now().year - 1}"
                )

                if (file.exists()) {
                    // Add data to map for daily/weekly accordingly
                    csvReader().open(file) {
                        if (dailyOrWeekly.value == "Daily") {
                            readAllAsSequence().forEach {
                                val dt = LocalDate.parse(it[0])
                                if (dt.isBefore(LocalDate.parse(date))) map[dt.toString()] =
                                    it[1].toFloat()
                            }
                        } else {
                            var count = 7
                            var sum = 0.0
                            readAllAsSequence().toMutableList().asReversed().forEach {
                                val dt = LocalDate.parse(it[0]).minusDays(1)
                                if (dt.isBefore(LocalDate.parse(weekDate))) {
                                    sum += it[1].toFloat()
                                    count -= 1
                                }
                                if (count == 0) {
                                    if (dt.isBefore(LocalDate.parse(weekDate))) map[dt.toString()] =
                                        it[1].toFloat()
                                    count = 7
                                    sum = 0.0
                                }
                            }
                        }
                    }
                }

            }

            // Set value for today map and sort temp as needed
            if (dailyOrWeekly.value == "Daily") {
                _today.value = map[LocalDate.parse(date).minusDays(1).toString()] ?: 0.0f
                temp.sortBy { value -> value.output }
                temp = ArrayList(temp.map {
                    it.gain -= map[date]!!
                    it.loss -= map[date]!!
                    it
                })
            } else if (dailyOrWeekly.value == "Weekly") {
                _today.value = map[LocalDate.parse(weekDate).minusDays(7).toString()] ?: 0.0f
                temp.sortBy { value -> value.output }
                temp = ArrayList(temp.map {
                    it.gain -= map[weekDate]!!
                    it.loss -= map[weekDate]!!
                    it
                })
            }
            // Assign value for data and graph
            Log.d("TAG", temp.reversed().subList(1, 4).toString())
            _data.value = temp.reversed().subList(1, 4)

            _graph.value = map

        }
    }

    // Function to change daily/weekly selection and refresh Recommendations
    fun changeSelection(dailyOrWeekly: String) {
        _dailyOrWeekly.value = dailyOrWeekly
        getData()
    }

}