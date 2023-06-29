package com.example.farmerscollective.realtime

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.farmerscollective.data.IntPriceEntry
import com.example.farmerscollective.data.PriceDatabase
import com.example.farmerscollective.utils.Utils.Companion.internationalPricesCrops
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class IntPriceViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = PriceDatabase.getDatabase(application).intPriceDao()

    private val _crop = MutableLiveData(0)
    val crop: LiveData<Int>
        get() = _crop

    private val _prices = MutableLiveData<List<IntPriceEntry>>(arrayListOf())
    val prices: LiveData<List<IntPriceEntry>>
        get() = _prices

    private val _year = MutableLiveData<Int>()
    val year: LiveData<Int>
        get() = _year

    private val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1)))
        LocalDate.now().year - 1
    else LocalDate.now().year

    init {
        _year.value = current
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadList()
            }
        }
    }

    private fun loadList() {
        val list = dao.getPricesByCropId(_crop.value?.plus(1) ?: 1) as ArrayList<IntPriceEntry>
        val realList: ArrayList<IntPriceEntry> = arrayListOf()
        for (item in list) {
            val y = item.date.substring(0, 4)
            val m = item.date.substring(5, 7)
            if (_year.value!! == y.toInt()) {
                if (m.toInt() > 6) {
                    realList.add(item)
                }
            } else if (_year.value!!.plus(1) == y.toInt()) {
                if (m.toInt() < 7) {
                    realList.add(item)
                }
            }
        }
        _prices.postValue(realList)
    }

    fun changeCropId(index: Int) {
        _crop.value = index
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadList()
            }
        }
    }

    fun changeYear(year: Int) {
        _year.value = year
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadList()
            }
        }
    }

}