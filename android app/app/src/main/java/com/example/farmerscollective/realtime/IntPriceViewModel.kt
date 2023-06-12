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

class IntPriceViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = PriceDatabase.getDatabase(application).intPriceDao()
    private var priceList: List<IntPriceEntry> = arrayListOf()

    private val _crop = MutableLiveData(0)
    val crop: LiveData<Int>
    get() = _crop

    private val _prices = MutableLiveData<List<IntPriceEntry>>(arrayListOf())
    val prices: LiveData<List<IntPriceEntry>>
    get() = _prices

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _prices.postValue(dao.getPricesByCropId(_crop.value?.plus(1) ?: 1))
            }
        }
    }

    fun changeCropId(index: Int) {
        _crop.value = index
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _prices.postValue(dao.getPricesByCropId(index + 1))
            }
        }
    }

}