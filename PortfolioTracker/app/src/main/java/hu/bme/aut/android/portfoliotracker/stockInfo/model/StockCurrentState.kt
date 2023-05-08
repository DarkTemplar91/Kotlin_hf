package hu.bme.aut.android.portfoliotracker.stockInfo.model

import com.google.gson.annotations.SerializedName

data class StockCurrentState(
    @SerializedName("01. symbol")
    val symbol: String?,
    @SerializedName("05. price")
    val price: String?,
    @SerializedName("09. change")
    val change: String?,
    @SerializedName("10. change percent")
    val changePercent: String?,

    var Note: String?
)