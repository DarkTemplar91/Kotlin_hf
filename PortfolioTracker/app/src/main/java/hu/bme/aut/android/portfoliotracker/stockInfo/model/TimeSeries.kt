package hu.bme.aut.android.portfoliotracker.stockInfo.model

import com.google.gson.annotations.SerializedName

class TimeSeries: ApiContainerInterface {

    @SerializedName("Time Series (Daily)")
    val timeStamps = HashMap<String?, TimeSeriesItem?>()

    override val Note: String?
        get() =  this.field

    @SerializedName("Note")
    val field: String? = null
    
    inner class TimeSeriesItem(price: String?) {
        @SerializedName("4. close")
        val price: String?

        init{
            this.price = price
        }
    }
}