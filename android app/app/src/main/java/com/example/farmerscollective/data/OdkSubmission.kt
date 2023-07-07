package com.example.farmerscollective.data

import java.time.LocalDate

// Data Class for Odk Submissions stored on Firebase
data class OdkSubmission(
    // Crop Id (Refer to cropName array in strings.xml)
    val cropId: Int?,

    // Local Trader Id (Refer to traders array in Utils.kt)
    val localTraderId: Int?,

    // Mandal Id (Obtained from Firebase)
    val mandalId: String?,

    // Market Id
    val marketId: Int?,

    // Person Filling Id (Obtained from Firebase)
    val personFillingId: String?,

    // Price
    var price: Long,

    // Date
    var date: LocalDate?,
)
