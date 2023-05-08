package hu.bme.aut.android.portfoliotracker

import androidx.room.*
import hu.bme.aut.android.portfoliotracker.database.StockPortfolioDatabase
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockHistory
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockOwned
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockSearchItem
import java.lang.Exception
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


object User {

    private var balance: Double = 0.0
    var favorites = mutableListOf<StockSearchItem>()
    var stocksOwned = mutableListOf<StockOwned>()
    var transactionHistory = TreeMap<LocalDate,ArrayList<StockHistory>>()

    @Ignore
    lateinit var database: StockPortfolioDatabase

    fun addToBalance(amount: Double){
        balance+=amount

    }
    fun deductFromBalance(amount: Double){
        if( amount >  balance )
            throw Exception("Balance exceeded")
        balance-=amount
    }

    fun getBalance(): Double{
        return balance
    }

}