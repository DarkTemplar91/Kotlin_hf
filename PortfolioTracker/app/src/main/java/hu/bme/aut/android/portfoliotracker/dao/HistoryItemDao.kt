package hu.bme.aut.android.portfoliotracker.dao

import androidx.room.*
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockHistory

@Dao
interface HistoryItemDao {
    @Query("SELECT * FROM stockHistory")
    fun getAll(): List<StockHistory>

    @Insert
    fun insert(stockHistory: StockHistory): Long

    @Update
    fun update(stockHistory: StockHistory)

    @Delete
    fun deleteItem(stockHistory: StockHistory)
}