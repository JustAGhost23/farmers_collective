package com.example.farmerscollective.data

// Data Class for Predictions stored on Firebase
data class Prediction(
    // Date
    val date: String,

    // Confidence of Prediction (From 0 to 1)
    val confidence: Float,

    // Predicted
    val predicted: Float,

    // Mean Price
    val output: Float,

    // Mean Gain
    var gain: Float,

    // Mean Loss
    var loss: Float,
)
