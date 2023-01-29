package com.example.farmerscollective.data

import java.time.LocalDate

data class OdkSubmission(
    val localTraderId: Int?,
    val mandalId: String?,
    val marketId: Int?,
    val personFillingId: String?,
    var price: Long,
    var date: LocalDate?,
)
