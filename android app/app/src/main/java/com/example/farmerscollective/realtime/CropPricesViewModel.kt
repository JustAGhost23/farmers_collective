package com.example.farmerscollective.realtime

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate


class CropPricesViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val _dataByYear = MutableLiveData<Map<Int, Map<String, Float>>>(mapOf())
    private val _dataByMandi = MutableLiveData<Map<String, Map<String, Float>>>(mapOf())
    private val _selectedYears = MutableLiveData(arrayListOf<Int>())
    private val _mandi = MutableLiveData("MAHARASHTRA_NAGPUR_Price")
    private val _selectedMandis = MutableLiveData(arrayListOf("MAHARASHTRA_NAGPUR_Price"))
    private val _year = MutableLiveData<Int>()

    val dataByYear: LiveData<Map<Int, Map<String, Float>>>
    get() = _dataByYear

    val dataByMandi: LiveData<Map<String, Map<String, Float>>>
    get() = _dataByMandi

//    val selected: LiveData<ArrayList<Int>>
//    get() = _selected

    init {
        val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1)))
            LocalDate.now().year - 1
        else LocalDate.now().year

        _selectedYears.value = arrayListOf(current)
        _year.value = current

        getDataByYear()
        getDataByMandi()
    }

    private fun getDataByYear() {

        val temp = mutableMapOf<Int, Map<String, Float>>()

        if(context.fileList().isNotEmpty()) {
            val mandi = _mandi.value!!

            val range = _selectedYears.value!!

            for(year in range) {
                val file1 = File(context.filesDir, "${mandi}_${year}")
                val file2 = File(context.filesDir, "${mandi}_${year + 1}")
                val prices = mutableMapOf<String, Float>()

                if(file1.exists()) {
                    csvReader().open(file1) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            //12-25
                            val start = LocalDate.of(year, 6, 30)

                            if(date.isAfter(start)) prices[ddmm] = it[1].toFloat()

                        }
                    }
                }

                if(file2.exists()) {
                    csvReader().open(file2) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            val end = LocalDate.of(year + 1, 7, 1)

                            if(date.isBefore(end)) prices[ddmm] = it[1].toFloat()

                        }
                    }
                }

                temp[year] = prices

            }

            _dataByYear.value = temp

        }

    }

    private fun getDataByMandi() {

        val temp = mutableMapOf<String, Map<String, Float>>()

        if(context.fileList().isNotEmpty()) {
            val array = _selectedMandis.value
            val year = _year.value!!

            val start = LocalDate.of(year, 6, 30)
            val end = LocalDate.of(year + 1, 7, 1)

            for(mandi in array!!) {
                val file1 = File(context.filesDir, "${mandi}_${year}")
                val file2 = File(context.filesDir, "${mandi}_${year + 1}")

                val prices = mutableMapOf<String, Float>()

                if(file1.exists()) {
                    csvReader().open(file1) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            if(date.isAfter(start)) prices[ddmm] = it[1].toFloat()

                        }
                    }
                }

                if(file2.exists()) {
                    csvReader().open(file2) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            if(date.isBefore(end)) prices[ddmm] = it[1].toFloat()

                        }
                    }
                }

                temp[mandi] = prices

            }

            _dataByMandi.value = temp
        }

    }

    fun selectYear(year: Int, check: Boolean) {
        val newList = _selectedYears.value!!

        if(check) newList.add(year)
        else newList.remove(year)

        _selectedYears.value = newList

        getDataByYear()
    }

    fun selectMandi(mandi: String, check: Boolean) {
        val newList = _selectedMandis.value!!

        if(check) newList.add(mandi)
        else newList.remove(mandi)

        Log.d("debugging $mandi", newList.toString())
        _selectedMandis.value = newList

        getDataByMandi()
    }

    fun changeMandi(mandi: String) {
        _mandi.value = mandi
        getDataByYear()
    }

    fun changeYear(year: Int) {
        _year.value = year
        getDataByMandi()
    }

}