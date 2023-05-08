package hu.bme.aut.android.portfoliotracker.stockInfo.network

import hu.bme.aut.android.portfoliotracker.stockInfo.model.*
import hu.bme.aut.android.portfoliotracker.stockInfo.model.BestStockSearchMatches
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {

    @GET("query?function=SYMBOL_SEARCH")
    fun getStockSearchItem(
        @Query("keywords") keyword: String?,
        @Query("apikey") appId: String?
    ) : Call<BestStockSearchMatches?>

    @GET("query?function=OVERVIEW")
    fun getStockOverview(
        @Query("symbol") symbol: String?,
        @Query("apikey") appId: String?
    ) : Call<StockOverview?>

    @GET("query?function=GLOBAL_QUOTE")
    fun getStockCurrentState(
        @Query("symbol")symbol: String?,
        @Query("apikey")appId: String?
    ): Call<GlobalQuote?>

    @GET("query?function=TIME_SERIES_DAILY_ADJUSTED")
    fun getStockTimeSeries(
        @Query("symbol")symbol: String?,
        @Query("outputsize")outputSize: String?,
        @Query("apikey")appId: String?
    ): Call<TimeSeries?>

    @GET("query?function=TIME_SERIES_INTRADAY")
    fun getStockTimeSeriesIntraday(
        @Query("symbol")symbol: String?,
        @Query("interval")interval: String?,
        @Query("outputsize")outputSize: String?,
        @Query("apikey")appId: String?
    ): Call<TimeSeriesIntraday?>

}