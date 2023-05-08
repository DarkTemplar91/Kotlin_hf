package hu.bme.aut.android.portfoliotracker.database

import android.content.Context
import androidx.room.*
import hu.bme.aut.android.portfoliotracker.Converters
import hu.bme.aut.android.portfoliotracker.dao.FavoriteItemDAO
import hu.bme.aut.android.portfoliotracker.dao.HistoryItemDao
import hu.bme.aut.android.portfoliotracker.dao.StockOwnedDAO
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockHistory
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockOwned
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockSearchItem

@Database(entities = [StockHistory::class, StockOwned::class, StockSearchItem::class], version = 1)
@TypeConverters(Converters::class)
abstract class StockPortfolioDatabase :RoomDatabase() {
    abstract fun stockHistoryDAO() : HistoryItemDao
    abstract fun stockOwnedDAO() : StockOwnedDAO
    abstract fun stockFavoriteDAO() : FavoriteItemDAO

    companion object{
        fun getDatabase(applicationContext: Context): StockPortfolioDatabase{
            return Room.databaseBuilder(applicationContext,StockPortfolioDatabase::class.java,"stock-database").build()
        }
    }
}