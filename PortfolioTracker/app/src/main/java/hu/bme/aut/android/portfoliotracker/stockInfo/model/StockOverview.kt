package hu.bme.aut.android.portfoliotracker.stockInfo.model

import com.google.gson.annotations.SerializedName

data class StockOverview (
    val Symbol: String,
    val Name: String,
    val Description: String,
    val Exchange: String,
    val Currency: String,
    val Country: String,
    val Sector: String,
    val Industry: String,
    @SerializedName("52WeekHigh")
    val WeekHigh52: String,
    @SerializedName("52WeekLow")
    val WeekLow52: String,


    override val Note: String?
) : ApiContainerInterface