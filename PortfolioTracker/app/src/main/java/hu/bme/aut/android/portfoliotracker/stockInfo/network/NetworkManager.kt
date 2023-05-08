package hu.bme.aut.android.portfoliotracker.stockInfo.network

import hu.bme.aut.android.portfoliotracker.stockInfo.model.*
import hu.bme.aut.android.portfoliotracker.stockInfo.model.BestStockSearchMatches
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkManager {
    private val retrofit: Retrofit
    private val stockApi: StockApi

    private const val SERVICE_URL = "https://www.alphavantage.co"
    private const val APP_ID = "8FITC0MSXN0Q7GVX"

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(SERVICE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        stockApi = retrofit.create(StockApi::class.java)
    }


    fun getStockSearchItem(symbol: String?) : Call<BestStockSearchMatches?>{
        return stockApi.getStockSearchItem(symbol, APP_ID)
    }

    fun getStockOverview(symbol: String?) : Call<StockOverview?>{
        return stockApi.getStockOverview(symbol, APP_ID)
    }

    fun getStockCurrentState(symbol: String?) : Call<GlobalQuote?>{
        return stockApi.getStockCurrentState(symbol, APP_ID)
    }

    fun getTimeSeries(symbol: String?, outputSize: String?) : Call<TimeSeries?>{
        return stockApi.getStockTimeSeries(symbol,outputSize ,APP_ID)
    }
    fun getTimeSeriesIntraday(symbol: String?, outputSize: String?) :Call<TimeSeriesIntraday?>{
        return stockApi.getStockTimeSeriesIntraday(symbol,"5min",outputSize ,APP_ID)
    }
}