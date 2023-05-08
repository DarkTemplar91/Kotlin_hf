package hu.bme.aut.android.portfoliotracker.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import hu.bme.aut.android.portfoliotracker.R
import hu.bme.aut.android.portfoliotracker.User
import hu.bme.aut.android.portfoliotracker.database.StockPortfolioDatabase
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockHistory
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockOwned
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockSearchItem
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        User.database = StockPortfolioDatabase.getDatabase(applicationContext)

        lifecycleScope.launch(Dispatchers.Main){
            withContext(Dispatchers.IO){
                loadItemsInBackground()
                loadUserBalance()
            }

        }
    }

    override fun onStop() {
        val pref = getSharedPreferences("app_prefs",Activity.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putFloat("user_balance",User.getBalance().toFloat())
        editor?.apply()
        super.onStop()
    }

    private fun loadItemsInBackground() {
            //Load Stock transaction history of the user
            val history = User.database.stockHistoryDAO().getAll()
            createStockTransactionHistory(history)

            val favorites = User.database.stockFavoriteDAO().getAll()
            User.favorites = favorites as MutableList<StockSearchItem>

            val stocksOwned = User.database.stockOwnedDAO().getAll()
            User.stocksOwned = stocksOwned as MutableList<StockOwned>

            createStockHistoryFromTransactions(stocksOwned, history)
    }

    private fun createStockTransactionHistory(items: List<StockHistory>){
        for(item in items)
        {
            var listHistory = User.transactionHistory[item.date]
            if( listHistory == null)
            {
                listHistory = ArrayList()
                User.transactionHistory[item.date] = listHistory
            }
            listHistory.add(item)
        }
        User.transactionHistory
    }

    private fun createStockHistoryFromTransactions(stocksOwned: List<StockOwned>, history: List<StockHistory>){
        for (item in history){
            val stock= stocksOwned.find { s -> s.symbol == item.symbol }
                ?:
                continue
            stock.stockHistory.add(item)

        }
    }

    private fun loadUserBalance(){
        val sp = getSharedPreferences("app_prefs", Activity.MODE_PRIVATE)
        val value = sp.getFloat("user_balance",0.0f).toDouble()
        User.addToBalance(value)
    }
}