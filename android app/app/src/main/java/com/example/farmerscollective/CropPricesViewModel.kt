package com.example.farmerscollective

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.farmerscollective.data.Prediction
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime


class CropPricesViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val _data = MutableLiveData<Map<Int, Map<String, Float>>>(mapOf())
    private val _selected = MutableLiveData(arrayListOf<Int>())
    private val _mandi = MutableLiveData("TELANGANA_ADILABAD_Price")

    val data: LiveData<Map<Int, Map<String, Float>>>
    get() = _data

//    val selected: LiveData<ArrayList<Int>>
//    get() = _selected

    init {
        val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1)))
            LocalDate.now().year - 1
        else LocalDate.now().year

        _selected.value = arrayListOf(current)

        getData()
    }

    fun getData() {

        val temp = mutableMapOf<Int, Map<String, Float>>()

        if(context.fileList().isNotEmpty()) {
            val array = _selected.value
            val mandi = _mandi.value

            for(year in array!!) {
                val file = File(context.filesDir, mandi!!)
                val prices = mutableMapOf<String, Float>()

                if(file.exists()) {
                    csvReader().open(file) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val start = LocalDate.of(year, 6, 30)
                            val end = LocalDate.of(year + 1, 7, 31)

                            if(date.isAfter(start) && date.isBefore(end)) prices[it[0]] = it[1].toFloat()
                        }
                    }
                }

                temp[year] = prices

            }

            _data.value = temp
        }

    }

    fun makeSelection(year: Int, check: Boolean) {
        val newList = _selected.value!!

        if(check) newList.add(year)
        else newList.remove(year)

        _selected.value = newList

        getData()
    }

    fun changeMandi(mandi: String) {
        _mandi.value = mandi
        getData()
    }

}