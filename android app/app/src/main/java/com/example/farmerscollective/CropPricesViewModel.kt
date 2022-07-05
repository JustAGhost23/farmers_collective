package com.example.farmerscollective

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.time.LocalDate


class CropPricesViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val _dataByYear = MutableLiveData<Map<Int, Map<String, Float>>>(mapOf())
    private val _dataByMandi = MutableLiveData<Map<String, Map<String, Float>>>(mapOf())
    private val _selectedYears = MutableLiveData(arrayListOf<Int>())
    private val _mandi = MutableLiveData("TELANGANA_ADILABAD_Price")
    private val _selectedMandis = MutableLiveData(arrayListOf("TELANGANA_ADILABAD_Price"))

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

        getDataByYear()
        getDataByMandi()
    }

    private fun getDataByYear() {

        val mean = mutableMapOf<String, Float>()
        val number = mutableMapOf<String, Int>()
        val temp = mutableMapOf<Int, Map<String, Float>>()

        if(context.fileList().isNotEmpty()) {
            val mandi = _mandi.value

            val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1)))
                LocalDate.now().year - 1
            else LocalDate.now().year

            val range = 2014..current
            for(year in range) {
                val file = File(context.filesDir, mandi!!)
                val prices = mutableMapOf<String, Float>()

                if(file.exists()) {
                    csvReader().open(file) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val mmdd = it[0].substring(5)
                            val start = LocalDate.of(year, 6, 30)
                            val end = LocalDate.of(year + 1, 7, 1)

                            if(date.isAfter(start) && date.isBefore(end) && _selectedYears.value!!.contains(year)) prices[mmdd] = it[1].toFloat()

                            if(!mean.containsKey(mmdd)) {
                                mean[mmdd] = 0.0f
                                number[mmdd] = 0
                            }

                            mean[mmdd] = mean[mmdd]!! + it[1].toFloat()
                            number[mmdd] = number[mmdd]!! + 1

                        }
                    }
                }

                if(_selectedYears.value!!.contains(year)) temp[year] = prices

            }

            for(entry in mean) {
                mean[entry.key] = mean[entry.key]!! / number[entry.key]!!
            }

            if(_selectedYears.value!!.contains(-1)) temp[-1] = mean

            _dataByYear.value = temp

        }

    }

    private fun getDataByMandi() {

        val temp = mutableMapOf<String, Map<String, Float>>()

        if(context.fileList().isNotEmpty()) {
            val array = _selectedMandis.value

            val start = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1)))
                LocalDate.of(LocalDate.now().year - 2, 12, 31)
            else LocalDate.of(LocalDate.now().year - 1, 12, 31)

            val end = LocalDate.now()

            for(mandi in array!!) {
                val file = File(context.filesDir, mandi)

                val prices = mutableMapOf<String, Float>()

                if(file.exists()) {
                    csvReader().open(file) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])

                            if(date.isAfter(start) && date.isBefore(end)) prices[it[0]] = it[1].toFloat()

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

//    fun getMeanData(b: Boolean) {
//        val temp = _dataByYear.value!!
//
//        if(b) temp[-1] = _meanData.value!!
//        else temp.remove(-1)
//
//        _dataByYear.value = temp
//    }

    fun changeMandi(mandi: String) {
        _mandi.value = mandi
        getDataByYear()
    }

}