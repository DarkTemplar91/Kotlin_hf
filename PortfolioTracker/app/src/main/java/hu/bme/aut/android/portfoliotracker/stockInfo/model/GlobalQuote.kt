package hu.bme.aut.android.portfoliotracker.stockInfo.model

import com.google.gson.annotations.SerializedName

data class GlobalQuote (
    @SerializedName("Global Quote")
    val stockCurrentState: StockCurrentState,
    val Note: String?
)